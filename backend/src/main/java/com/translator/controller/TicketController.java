package com.translator.controller;

import com.translator.dto.LanguageInfoDto;
import com.translator.dto.TicketAnalysisResponse;
import com.translator.entity.AnalyzedTicketEntity;
import com.translator.model.TicketDetails;
import com.translator.repository.AnalyzedTicketRepository;
import com.translator.service.LanguageDetectionService;
import com.translator.service.OcrService;
import com.translator.service.TicketEntityExtractorService;
import com.translator.service.TranslationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private static final Logger logger = LoggerFactory.getLogger(TicketController.class);

    private final OcrService ocrService;
    private final TicketEntityExtractorService entityExtractorService;
    private final LanguageDetectionService languageDetectionService;
    private final TranslationService translationService;
    private final AnalyzedTicketRepository ticketRepository;

    public TicketController(
            OcrService ocrService,
            TicketEntityExtractorService entityExtractorService,
            LanguageDetectionService languageDetectionService,
            TranslationService translationService,
            AnalyzedTicketRepository ticketRepository
    ) {
        this.ocrService = ocrService;
        this.entityExtractorService = entityExtractorService;
        this.languageDetectionService = languageDetectionService;
        this.translationService = translationService;
        this.ticketRepository = ticketRepository;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "Multilingual Ticket Translator API",
                "version", "1.0.0"
        ));
    }

    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TicketAnalysisResponse> analyzeTicket(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "targetLanguage", defaultValue = "en") String targetLanguage
    ) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(TicketAnalysisResponse.error("Please provide a valid ticket file (PDF or Image)."));
        }

        try {
            String originalFileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "ticket";
            String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";
            String fileSize = formatFileSize(file.getSize());
            String fileType = getReadableFileType(originalFileName, contentType);

            logger.info("Analyzing ticket upload: '{}' ({}, {}), targetLanguage='{}'",
                    originalFileName, fileType, fileSize, targetLanguage);

            // 1. OCR / Text Extraction
            String rawText = ocrService.extractText(file);
            if (rawText == null || rawText.trim().isEmpty()) {
                rawText = "No clear text could be recognized from the uploaded ticket file. Please ensure high clarity.";
            }

            // 2. Language Detection
            LanguageInfoDto detectedLanguage = languageDetectionService.detectLanguage(rawText);

            // 3. Target Language Info
            String normalizedTargetLang = LanguageDetectionService.normalizeLanguageCode(targetLanguage);
            String targetLangDisplayName = LanguageDetectionService.getLanguageDisplayName(targetLanguage);
            LanguageInfoDto targetLangInfo = new LanguageInfoDto(
                    normalizedTargetLang,
                    targetLangDisplayName,
                    1.0
            );

            // 4. Entity Extraction
            TicketDetails extractedDetails = entityExtractorService.extractDetails(rawText);

            // 5. Translation
            TicketDetails translatedDetails = translationService.translateTicketDetails(
                    extractedDetails,
                    detectedLanguage.getCode(),
                    normalizedTargetLang
            );

            // 6. Translated Full Text Summary
            String translatedFullText = translationService.translateText(
                    rawText,
                    detectedLanguage.getCode(),
                    normalizedTargetLang
            );

            // 7. Persist to Database
            AnalyzedTicketEntity entity = new AnalyzedTicketEntity();
            entity.setFileName(originalFileName);
            entity.setFileType(fileType);
            entity.setFileSize(fileSize);
            entity.setDetectedLanguageCode(detectedLanguage.getCode());
            entity.setDetectedLanguageName(detectedLanguage.getName());
            entity.setDetectedConfidence(detectedLanguage.getConfidence());
            entity.setTargetLanguageCode(targetLangInfo.getCode());
            entity.setTargetLanguageName(targetLangInfo.getName());

            entity.setPassengerName(extractedDetails.getPassengerName());
            entity.setDepartureStation(extractedDetails.getDepartureStation());
            entity.setArrivalDestination(extractedDetails.getArrivalDestination());
            entity.setTravelDate(extractedDetails.getTravelDate());
            entity.setDepartureTime(extractedDetails.getDepartureTime());
            entity.setArrivalTime(extractedDetails.getArrivalTime());
            entity.setCarrierNumber(extractedDetails.getCarrierNumber());
            entity.setTransportType(extractedDetails.getTransportType());
            entity.setSeat(extractedDetails.getSeat());
            entity.setCoach(extractedDetails.getCoach());
            entity.setTicketClass(extractedDetails.getTicketClass());
            entity.setPnrNumber(extractedDetails.getPnrNumber());
            entity.setFare(extractedDetails.getFare());

            entity.setTranslatedDepartureStation(translatedDetails.getDepartureStation());
            entity.setTranslatedArrivalDestination(translatedDetails.getArrivalDestination());
            entity.setTranslatedTransportType(translatedDetails.getTransportType());
            entity.setTranslatedSeat(translatedDetails.getSeat());
            entity.setTranslatedCoach(translatedDetails.getCoach());
            entity.setTranslatedTicketClass(translatedDetails.getTicketClass());
            entity.setSummary(translatedDetails.getSummary() != null ? translatedDetails.getSummary() : extractedDetails.getSummary());

            entity.setRawExtractedText(rawText);
            entity.setTranslatedFullText(translatedFullText);

            AnalyzedTicketEntity savedEntity = ticketRepository.save(entity);

            TicketAnalysisResponse response = TicketAnalysisResponse.success(
                    savedEntity.getId(),
                    savedEntity.getCreatedAt(),
                    originalFileName,
                    fileType,
                    fileSize,
                    detectedLanguage,
                    targetLangInfo,
                    extractedDetails,
                    translatedDetails,
                    rawText,
                    translatedFullText
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error analyzing ticket: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(TicketAnalysisResponse.error("Failed to analyze ticket: " + e.getMessage()));
        }
    }

    @GetMapping("/history")
    public ResponseEntity<Map<String, Object>> getTicketHistory(
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        Page<AnalyzedTicketEntity> ticketPage = ticketRepository.searchTickets(
                query != null ? query.trim() : "",
                PageRequest.of(Math.max(0, page), Math.min(size, 100))
        );

        return ResponseEntity.ok(Map.of(
                "tickets", ticketPage.getContent(),
                "currentPage", ticketPage.getNumber(),
                "totalItems", ticketPage.getTotalElements(),
                "totalPages", ticketPage.getTotalPages()
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnalyzedTicketEntity> getTicketById(@PathVariable("id") Long id) {
        Optional<AnalyzedTicketEntity> ticket = ticketRepository.findById(id);
        return ticket.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteTicket(@PathVariable("id") Long id) {
        if (ticketRepository.existsById(id)) {
            ticketRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Ticket deleted from history."));
        }
        return ResponseEntity.notFound().build();
    }

    private String formatFileSize(long bytes) {
        if (bytes <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        return String.format("%.1f %s", bytes / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    private String getReadableFileType(String name, String mime) {
        String lowerName = name.toLowerCase();
        if (mime.contains("pdf") || lowerName.endsWith(".pdf")) return "PDF Document";
        if (mime.contains("png") || lowerName.endsWith(".png")) return "PNG Image";
        if (mime.contains("jpeg") || mime.contains("jpg") || lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg")) return "JPEG Image";
        if (mime.contains("webp") || lowerName.endsWith(".webp")) return "WEBP Image";
        return "Document";
    }
}
