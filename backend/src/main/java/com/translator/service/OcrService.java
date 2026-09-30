package com.translator.service;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class OcrService {

    private static final Logger logger = LoggerFactory.getLogger(OcrService.class);
    private ITesseract tesseract;
    private boolean tesseractAvailable = false;

    public OcrService() {
        initTesseract();
    }

    private void initTesseract() {
        try {
            tesseract = new Tesseract();
            File tessDataDir = findTessDataDirectory();

            if (tessDataDir != null && tessDataDir.exists()) {
                tesseract.setDatapath(tessDataDir.getAbsolutePath());

                // Detect available language models in tessdata directory
                List<String> availableLangs = new ArrayList<>();
                for (String lang : Arrays.asList("eng", "spa", "fra", "deu", "ita")) {
                    if (new File(tessDataDir, lang + ".traineddata").exists()) {
                        availableLangs.add(lang);
                    }
                }
                String languages = availableLangs.isEmpty() ? "eng" : String.join("+", availableLangs);
                tesseract.setLanguage(languages);
                tesseractAvailable = true;
                logger.info("Tesseract OCR successfully initialized with datapath: {} (languages: {})",
                        tessDataDir.getAbsolutePath(), languages);
            } else {
                logger.warn("Tessdata directory not found in common paths. OCR will fall back gracefully.");
                tesseractAvailable = false;
            }
        } catch (Throwable t) {
            logger.warn("Tesseract OCR native libraries not fully available. Fallback parser active: {}", t.getMessage());
            tesseractAvailable = false;
        }
    }

    private File findTessDataDirectory() {
        String envPath = System.getenv("TESSDATA_PREFIX");
        if (envPath != null && !envPath.trim().isEmpty()) {
            File envDir = new File(envPath);
            if (hasEngTrainedData(envDir)) return envDir;
        }

        List<String> candidates = Arrays.asList(
            "tessdata",
            "backend/tessdata",
            "../backend/tessdata",
            "../tessdata",
            "./tessdata",
            "src/main/resources/tessdata"
        );

        String userDir = System.getProperty("user.dir");
        for (String candidate : candidates) {
            File dir = Paths.get(userDir, candidate).toFile();
            if (hasEngTrainedData(dir)) {
                return dir;
            }
            File relDir = new File(candidate);
            if (hasEngTrainedData(relDir)) {
                return relDir;
            }
        }

        return null;
    }

    private boolean hasEngTrainedData(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory()) {
            return false;
        }
        File engFile = new File(dir, "eng.traineddata");
        return engFile.exists() && engFile.length() > 0;
    }

    /**
     * Extracts text from an uploaded ticket file (PDF or image).
     */
    public String extractText(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty or not provided.");
        }

        String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
        String contentType = file.getContentType() != null ? file.getContentType().toLowerCase() : "";

        if (fileName.endsWith(".pdf") || contentType.contains("pdf")) {
            return extractTextFromPdf(file);
        } else {
            return extractTextFromImage(file);
        }
    }

    /**
     * Extracts text from a PDF document using Apache PDFBox.
     */
    public String extractTextFromPdf(MultipartFile file) throws Exception {
        byte[] bytes = file.getBytes();
        try (PDDocument document = Loader.loadPDF(new RandomAccessReadBuffer(bytes))) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            String pdfText = stripper.getText(document);

            if (pdfText != null && !pdfText.trim().isEmpty()) {
                logger.info("Successfully extracted {} characters of text from PDF using PDFBox", pdfText.length());
                return cleanExtractedText(pdfText);
            }

            // If text is empty, the PDF might be an image/scanned PDF - render pages and perform OCR
            logger.info("PDF contained no embedded text stream. Attempting OCR on rendered PDF pages...");
            PDFRenderer renderer = new PDFRenderer(document);
            StringBuilder ocrCombined = new StringBuilder();
            int maxPages = Math.min(document.getNumberOfPages(), 3);

            for (int i = 0; i < maxPages; i++) {
                BufferedImage image = renderer.renderImageWithDPI(i, 300);
                String pageText = performImageOcr(image);
                if (pageText != null && !pageText.trim().isEmpty()) {
                    ocrCombined.append(pageText).append("\n");
                }
            }

            String result = ocrCombined.toString().trim();
            return !result.isEmpty() ? cleanExtractedText(result) : "Scanned PDF ticket. No extractable text found.";
        }
    }

    /**
     * Extracts text from an image ticket (PNG, JPEG, WEBP) using OCR.
     */
    public String extractTextFromImage(MultipartFile file) throws Exception {
        try (InputStream is = file.getInputStream()) {
            BufferedImage image = ImageIO.read(is);
            if (image == null) {
                throw new IllegalArgumentException("Could not decode image file. Please provide a valid JPG, PNG, or WEBP.");
            }
            return cleanExtractedText(performImageOcr(image));
        }
    }

    /**
     * Runs OCR on a decoded BufferedImage with preprocessing.
     */
    public String performImageOcr(BufferedImage image) {
        if (!tesseractAvailable || tesseract == null) {
            logger.warn("Tesseract OCR not configured, returning empty OCR text.");
            return "";
        }

        try {
            BufferedImage preprocessed = preprocessImageForOcr(image);
            synchronized (this) {
                return tesseract.doOCR(preprocessed);
            }
        } catch (Throwable t) {
            logger.warn("OCR execution failed or native library issue: {}", t.getMessage());
            return "";
        }
    }

    /**
     * Preprocesses the image to RGB format and scales up small images to improve OCR accuracy.
     */
    private BufferedImage preprocessImageForOcr(BufferedImage src) {
        if (src == null) return null;

        int width = src.getWidth();
        int height = src.getHeight();

        // Scale up if resolution is too low for crisp character recognition
        double scale = 1.0;
        if (width < 1200 && height < 1200) {
            scale = Math.min(2.5, 1600.0 / Math.max(width, height));
        }

        int targetWidth = (int) (width * scale);
        int targetHeight = (int) (height * scale);

        BufferedImage rgbImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = rgbImage.createGraphics();

        // Fill white background for transparent images
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, targetWidth, targetHeight);

        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.drawImage(src, 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();

        return rgbImage;
    }

    private String cleanExtractedText(String text) {
        if (text == null) return "";
        return text.replaceAll("\r\n", "\n")
                   .replaceAll("\r", "\n")
                   .replaceAll("[ \t]+", " ")
                   .replaceAll("\n{3,}", "\n\n")
                   .trim();
    }
}
