package com.translator;

import com.translator.dto.LanguageInfoDto;
import com.translator.dto.TicketAnalysisResponse;
import com.translator.model.TicketDetails;
import com.translator.service.LanguageDetectionService;
import com.translator.service.OcrService;
import com.translator.service.TicketEntityExtractorService;
import com.translator.service.TranslationService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TicketAnalysisPipelineTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OcrService ocrService;

    @Autowired
    private TicketEntityExtractorService extractorService;

    @Autowired
    private LanguageDetectionService languageDetectionService;

    @Autowired
    private TranslationService translationService;

    private byte[] samplePdfBytes;
    private byte[] sampleImageBytes;

    @BeforeEach
    void setUp() throws Exception {
        // 1. Create an in-memory PDF ticket for testing
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                cs.setLeading(14.5f);
                cs.newLineAtOffset(50, 700);

                cs.showText("RENFE - Billete de Tren de Alta Velocidad");
                cs.newLine();
                cs.showText("Tren: AVE 03083");
                cs.newLine();
                cs.showText("Pasajero: Carlos Mendoza");
                cs.newLine();
                cs.showText("Origen: Madrid Puerta de Atocha");
                cs.newLine();
                cs.showText("Destino: Barcelona Sants");
                cs.newLine();
                cs.showText("Fecha: 15/10/2026");
                cs.newLine();
                cs.showText("Salida: 08:30");
                cs.newLine();
                cs.showText("Llegada: 11:15");
                cs.newLine();
                cs.showText("Coche: 04");
                cs.newLine();
                cs.showText("Asiento: 12A");
                cs.newLine();
                cs.showText("Clase: Preferente");
                cs.newLine();
                cs.showText("Localizador: W8KJ2Z");
                cs.endText();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);
            samplePdfBytes = baos.toByteArray();
        }

        // 2. Create an in-memory PNG ticket image for OCR testing
        BufferedImage img = new BufferedImage(800, 400, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, 800, 400);
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 22));

        g2d.drawString("Deutsche Bahn Fahrkarte", 40, 50);
        g2d.drawString("Fahrgast: Hans Mueller", 40, 90);
        g2d.drawString("Zug: ICE 573", 40, 130);
        g2d.drawString("Von: Berlin Hbf", 40, 170);
        g2d.drawString("Nach: Muenchen Hbf", 40, 210);
        g2d.drawString("Datum: 12.05.2025", 40, 250);
        g2d.drawString("Abfahrt: 09:15", 40, 290);
        g2d.drawString("Ankunft: 13:45", 40, 330);
        g2d.drawString("Buchungscode: DB94821", 40, 370);
        g2d.dispose();

        ByteArrayOutputStream imgBaos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", imgBaos);
        sampleImageBytes = imgBaos.toByteArray();
    }

    @Test
    void testPdfTextExtraction() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "renfe_ticket.pdf",
                "application/pdf",
                samplePdfBytes
        );

        String extracted = ocrService.extractText(file);
        assertNotNull(extracted);
        assertTrue(extracted.contains("AVE 03083"));
        assertTrue(extracted.contains("Carlos Mendoza"));
        assertTrue(extracted.contains("Madrid Puerta de Atocha"));
        assertTrue(extracted.contains("Barcelona Sants"));
    }

    @Test
    void testImageTextExtraction() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "db_ticket.png",
                "image/png",
                sampleImageBytes
        );

        String extracted = ocrService.extractText(file);
        assertNotNull(extracted);
        // OCR text should contain extracted text
        assertTrue(extracted.length() > 0, "OCR should extract text from clean synthesized image");
    }

    @Test
    void testSpanishTicketEntityExtraction() {
        String spanishTicket = """
                RENFE - Billete AVE 03083
                Pasajero: Carlos Mendoza
                Origen: Madrid Puerta de Atocha
                Destino: Barcelona Sants
                Fecha: 15/10/2026
                Salida: 08:30
                Llegada: 11:15
                Coche: 04
                Asiento: 12A
                Clase: Preferente
                Localizador: W8KJ2Z
                """;

        TicketDetails details = extractorService.extractDetails(spanishTicket);

        assertEquals("Carlos Mendoza", details.getPassengerName());
        assertEquals("Madrid Puerta de Atocha", details.getDepartureStation());
        assertEquals("Barcelona Sants", details.getArrivalDestination());
        assertEquals("15/10/2026", details.getTravelDate());
        assertEquals("08:30", details.getDepartureTime());
        assertEquals("11:15", details.getArrivalTime());
        assertEquals("AVE 03083", details.getCarrierNumber());
        assertEquals("Train", details.getTransportType());
        assertEquals("12A", details.getSeat());
        assertEquals("04", details.getCoach());
        assertEquals("Preferente", details.getTicketClass());
        assertEquals("W8KJ2Z", details.getPnrNumber());
    }

    @Test
    void testGermanTicketEntityExtraction() {
        String germanTicket = """
                Deutsche Bahn Fahrkarte
                Fahrgast: Hans Mueller
                Zug: ICE 573
                Von: Berlin Hbf
                Nach: Muenchen Hbf
                Datum: 12.05.2025
                Abfahrt: 09:15
                Ankunft: 13:45
                Wagen: 07
                Sitzplatz: 45
                Klasse: 2. Klasse
                Buchungscode: DB94821
                """;

        TicketDetails details = extractorService.extractDetails(germanTicket);

        assertEquals("Hans Mueller", details.getPassengerName());
        assertEquals("Berlin Hbf", details.getDepartureStation());
        assertEquals("Muenchen Hbf", details.getArrivalDestination());
        assertEquals("12.05.2025", details.getTravelDate());
        assertEquals("09:15", details.getDepartureTime());
        assertEquals("13:45", details.getArrivalTime());
        assertEquals("ICE 573", details.getCarrierNumber());
        assertEquals("Train", details.getTransportType());
        assertEquals("45", details.getSeat());
        assertEquals("07", details.getCoach());
        assertEquals("DB94821", details.getPnrNumber());
    }

    @Test
    void testFrenchTgvTicketExtraction() {
        String frenchTicket = """
                SNCF Billet TGV 9210
                Passager: Jean Dupont
                Départ: Paris Gare de Lyon
                Arrivée: Marseille Saint-Charles
                Date: 2026-11-20
                Départ: 14:00
                Arrivée: 17:15
                Voiture: 02
                Siège: 34
                Classe: 1ère Classe
                Référence Dossier: TGV987
                """;

        TicketDetails details = extractorService.extractDetails(frenchTicket);

        assertEquals("Jean Dupont", details.getPassengerName());
        assertEquals("Paris Gare de Lyon", details.getDepartureStation());
        assertEquals("Marseille Saint-Charles", details.getArrivalDestination());
        assertEquals("2026-11-20", details.getTravelDate());
        assertEquals("14:00", details.getDepartureTime());
        assertEquals("17:15", details.getArrivalTime());
        assertEquals("TGV 9210", details.getCarrierNumber());
        assertEquals("Train", details.getTransportType());
        assertEquals("34", details.getSeat());
        assertEquals("02", details.getCoach());
        assertEquals("TGV987", details.getPnrNumber());
    }

    @Test
    void testFlightTicketExtraction() {
        String flightTicket = """
                Air France Flight AF 1234
                Passenger: Alice Smith
                From: Paris CDG
                To: New York JFK
                Date: 25-Dec-2026
                Time: 10:30
                Seat: 14A
                Class: Business Class
                PNR: AF98765
                """;

        TicketDetails details = extractorService.extractDetails(flightTicket);

        assertEquals("Alice Smith", details.getPassengerName());
        assertEquals("Paris CDG", details.getDepartureStation());
        assertEquals("New York JFK", details.getArrivalDestination());
        assertEquals("25-Dec-2026", details.getTravelDate());
        assertEquals("AF 1234", details.getCarrierNumber());
        assertEquals("Flight", details.getTransportType());
        assertEquals("14A", details.getSeat());
        assertEquals("Business Class", details.getTicketClass());
        assertEquals("AF98765", details.getPnrNumber());
    }

    @Test
    void testLanguageDetection() {
        String spanishText = "Billete de tren Renfe con salida de Madrid y llegada a Barcelona para el pasajero Carlos.";
        LanguageInfoDto spanishInfo = languageDetectionService.detectLanguage(spanishText);
        assertEquals("es", spanishInfo.getCode());

        String frenchText = "Billet de train SNCF avec départ de Paris et arrivée à Lyon pour le voyageur Jean.";
        LanguageInfoDto frenchInfo = languageDetectionService.detectLanguage(frenchText);
        assertEquals("fr", frenchInfo.getCode());

        String germanText = "Deutsche Bahn Fahrkarte mit Abfahrt in Berlin und Ankunft in München.";
        LanguageInfoDto germanInfo = languageDetectionService.detectLanguage(germanText);
        assertEquals("de", germanInfo.getCode());

        String hindiText = "नई दिल्ली से वाराणसी के लिए ट्रेन टिकट।";
        LanguageInfoDto hindiInfo = languageDetectionService.detectLanguage(hindiText);
        assertEquals("hi", hindiInfo.getCode());
    }

    @Test
    void testTranslationService() {
        TicketDetails source = new TicketDetails();
        source.setPassengerName("Carlos Mendoza");
        source.setDepartureStation("Madrid Puerta de Atocha");
        source.setArrivalDestination("Barcelona Sants");
        source.setTransportType("Tren de alta velocidad");
        source.setTicketClass("Preferente");
        source.setTravelDate("15/10/2026");
        source.setDepartureTime("08:30");
        source.setCarrierNumber("AVE 03083");
        source.setCoach("04");
        source.setSeat("12A");
        source.setPnrNumber("W8KJ2Z");

        TicketDetails translated = translationService.translateTicketDetails(source, "es", "en");

        assertNotNull(translated);
        assertEquals("Carlos Mendoza", translated.getPassengerName());
        assertEquals("High-Speed Train", translated.getTransportType());
        assertEquals("First / Premium Class", translated.getTicketClass());
        assertNotNull(translated.getSummary());
        assertTrue(translated.getSummary().contains("High-Speed Train"));
        assertTrue(translated.getSummary().contains("Carlos Mendoza") || translated.getSummary().contains("Madrid"));
    }

    @Test
    void testAnalyzeEndpointWithMockMvc() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "madrid_barcelona_ticket.pdf",
                "application/pdf",
                samplePdfBytes
        );

        mockMvc.perform(multipart("/api/tickets/analyze")
                        .file(file)
                        .param("targetLanguage", "en"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.fileName").value("madrid_barcelona_ticket.pdf"))
                .andExpect(jsonPath("$.fileType").value("PDF Document"))
                .andExpect(jsonPath("$.detectedLanguage.code").value("es"))
                .andExpect(jsonPath("$.targetLanguage.code").value("en"))
                .andExpect(jsonPath("$.extractedDetails.passengerName").value("Carlos Mendoza"))
                .andExpect(jsonPath("$.extractedDetails.departureStation").value("Madrid Puerta de Atocha"))
                .andExpect(jsonPath("$.extractedDetails.arrivalDestination").value("Barcelona Sants"))
                .andExpect(jsonPath("$.extractedDetails.carrierNumber").value("AVE 03083"))
                .andExpect(jsonPath("$.extractedDetails.pnrNumber").value("W8KJ2Z"))
                .andExpect(jsonPath("$.translatedDetails.summary").isNotEmpty());
    }

    @Test
    void testMultilineLayoutExtraction() {
        String multilineTicket = """
                PASSENGER
                Carlos Mendoza
                FROM
                London St Pancras
                TO
                Paris Gare du Nord
                DATE
                15 Oct 2026
                TIME
                08:30
                Eurostar 9012
                Seat 45
                Coach 08
                PNR: EUR9482
                """;

        TicketDetails details = extractorService.extractDetails(multilineTicket);

        assertEquals("Carlos Mendoza", details.getPassengerName());
        assertEquals("London St Pancras", details.getDepartureStation());
        assertEquals("Paris Gare du Nord", details.getArrivalDestination());
        assertEquals("15 Oct 2026", details.getTravelDate());
        assertEquals("08:30", details.getDepartureTime());
        assertEquals("Eurostar 9012", details.getCarrierNumber());
        assertEquals("Train", details.getTransportType());
        assertEquals("45", details.getSeat());
        assertEquals("08", details.getCoach());
        assertEquals("EUR9482", details.getPnrNumber());
    }

    @Test
    void testAnalyzeEndpointWithEmptyFile() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.pdf",
                "application/pdf",
                new byte[0]
        );

        mockMvc.perform(multipart("/api/tickets/analyze")
                        .file(emptyFile)
                        .param("targetLanguage", "en"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
