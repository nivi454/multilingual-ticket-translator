package com.translator.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.translator.model.TicketDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
public class TranslationService {

    private static final Logger logger = LoggerFactory.getLogger(TranslationService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final GlossaryService glossaryService;

    public TranslationService(GlossaryService glossaryService) {
        this.glossaryService = glossaryService;
    }

    // Default constructor for testing without spring context if needed
    public TranslationService() {
        this.glossaryService = null;
    }

    // Multilingual Travel Glossary Dictionary for reliable instant offline translation
    private static final Map<String, Map<String, String>> TRAVEL_DICTIONARY = new HashMap<>();

    static {
        // Spanish -> English mappings
        Map<String, String> esToEn = new HashMap<>();
        esToEn.put("tren", "Train");
        esToEn.put("tren de alta velocidad", "High-Speed Train");
        esToEn.put("ave", "AVE (High-Speed Train)");
        esToEn.put("vuelo", "Flight");
        esToEn.put("autobús", "Bus");
        esToEn.put("autobus", "Bus");
        esToEn.put("barco", "Ferry");
        esToEn.put("turista", "Economy Class");
        esToEn.put("turista plus", "Premium Economy / Tourist Plus");
        esToEn.put("preferente", "First / Premium Class");
        esToEn.put("primera clase", "First Class");
        esToEn.put("clase turista", "Economy Class");
        esToEn.put("coche", "Coach");
        esToEn.put("vagon", "Carriage");
        esToEn.put("coche / vagon", "Coach / Carriage");
        esToEn.put("asiento", "Seat");
        esToEn.put("plaza", "Seat");
        esToEn.put("pasajero", "Passenger");
        esToEn.put("nombre del pasajero", "Passenger Name");
        esToEn.put("salida", "Departure");
        esToEn.put("llegada", "Arrival");
        esToEn.put("estación", "Station");
        esToEn.put("estacion", "Station");
        esToEn.put("puerta de atocha", "Atocha Gate");
        esToEn.put("madrid puerta de atocha", "Madrid Puerta de Atocha");
        esToEn.put("barcelona sants estacion", "Barcelona Sants Station");
        esToEn.put("estación de atocha", "Atocha Station");
        esToEn.put("aeropuerto", "Airport");
        esToEn.put("localizador", "Booking Reference (PNR)");
        esToEn.put("billete", "Ticket");
        esToEn.put("billete de viaje", "Travel Ticket");
        esToEn.put("fecha de viaje", "Date of Travel");
        esToEn.put("hora de salida", "Departure Time");
        esToEn.put("tren numero", "Train Number");
        TRAVEL_DICTIONARY.put("es_en", esToEn);

        // French -> English mappings
        Map<String, String> frToEn = new HashMap<>();
        frToEn.put("train", "Train");
        frToEn.put("train à grande vitesse", "High-Speed Train");
        frToEn.put("tgv", "TGV (High-Speed Train)");
        frToEn.put("vol", "Flight");
        frToEn.put("bus", "Bus");
        frToEn.put("ferry", "Ferry");
        frToEn.put("première classe", "First Class");
        frToEn.put("1ère classe", "1st Class");
        frToEn.put("seconde classe", "2nd Class / Economy");
        frToEn.put("2ème classe", "2nd Class / Economy");
        frToEn.put("standard", "Standard Class");
        frToEn.put("voiture", "Coach");
        frToEn.put("siège", "Seat");
        frToEn.put("place", "Seat");
        frToEn.put("passager", "Passenger");
        frToEn.put("nom du passager", "Passenger Name");
        frToEn.put("départ", "Departure");
        frToEn.put("arrivée", "Arrival");
        frToEn.put("gare", "Station");
        frToEn.put("gare de lyon", "Paris Gare de Lyon");
        frToEn.put("paris gare de lyon", "Paris Gare de Lyon");
        frToEn.put("lyon part-dieu", "Lyon Part-Dieu");
        frToEn.put("aéroport", "Airport");
        frToEn.put("référence dossier", "Booking Reference");
        frToEn.put("dossier voyage", "Travel File / Booking");
        frToEn.put("billet de train", "Train Ticket");
        TRAVEL_DICTIONARY.put("fr_en", frToEn);

        // German -> English mappings
        Map<String, String> deToEn = new HashMap<>();
        deToEn.put("zug", "Train");
        deToEn.put("hochgeschwindigkeitszug", "High-Speed Train");
        deToEn.put("ice", "ICE (InterCity Express)");
        deToEn.put("flug", "Flight");
        deToEn.put("bus", "Bus");
        deToEn.put("1. klasse", "1st Class");
        deToEn.put("2. klasse", "2nd Class / Economy");
        deToEn.put("wagen", "Coach");
        deToEn.put("sitzplatz", "Seat");
        deToEn.put("platz", "Seat");
        deToEn.put("fahrgast", "Passenger");
        deToEn.put("abfahrt", "Departure");
        deToEn.put("ankunft", "Arrival");
        deToEn.put("hauptbahnhof", "Central Station");
        deToEn.put("berlin hbf", "Berlin Central Station (Hbf)");
        deToEn.put("münchen hbf", "Munich Central Station (Hbf)");
        deToEn.put("bahnhof", "Station");
        deToEn.put("flughafen", "Airport");
        deToEn.put("buchungscode", "Booking Reference");
        deToEn.put("fahrkarte", "Ticket");
        TRAVEL_DICTIONARY.put("de_en", deToEn);

        // Italian -> English mappings
        Map<String, String> itToEn = new HashMap<>();
        itToEn.put("treno", "Train");
        itToEn.put("alta velocità", "High-Speed Train");
        itToEn.put("frecciarossa", "Frecciarossa (High-Speed Train)");
        itToEn.put("volo", "Flight");
        itToEn.put("autobus", "Bus");
        itToEn.put("prima classe", "First Class");
        itToEn.put("seconda classe", "Standard / Economy Class");
        itToEn.put("vettura", "Coach");
        itToEn.put("posto", "Seat");
        itToEn.put("passeggero", "Passenger");
        itToEn.put("partenza", "Departure");
        itToEn.put("arrivo", "Arrival");
        itToEn.put("stazione", "Station");
        itToEn.put("roma termini", "Rome Termini Station");
        itToEn.put("milano centrale", "Milan Central Station");
        itToEn.put("aeroporto", "Airport");
        itToEn.put("codice prenotazione", "Booking Code / PNR");
        itToEn.put("biglietto", "Ticket");
        TRAVEL_DICTIONARY.put("it_en", itToEn);
    }

    /**
     * Translates a TicketDetails object into the target language.
     */
    public TicketDetails translateTicketDetails(TicketDetails source, String sourceLang, String targetLang) {
        if (source == null) return null;

        TicketDetails translated = new TicketDetails();
        translated.setPassengerName(source.getPassengerName()); // Keep personal names intact
        translated.setCarrierNumber(source.getCarrierNumber());
        translated.setPnrNumber(source.getPnrNumber());
        translated.setTravelDate(source.getTravelDate());
        translated.setDepartureTime(source.getDepartureTime());
        translated.setArrivalTime(source.getArrivalTime());

        if (sourceLang != null && targetLang != null && sourceLang.equalsIgnoreCase(targetLang)) {
            translated.setDepartureStation(source.getDepartureStation());
            translated.setArrivalDestination(source.getArrivalDestination());
            translated.setTransportType(source.getTransportType());
            translated.setSeat(source.getSeat());
            translated.setCoach(source.getCoach());
            translated.setTicketClass(source.getTicketClass());
            translated.setSummary(source.getSummary());
            return translated;
        }

        if (source.getDepartureStation() != null) {
            translated.setDepartureStation(translateText(source.getDepartureStation(), sourceLang, targetLang));
        }
        if (source.getArrivalDestination() != null) {
            translated.setArrivalDestination(translateText(source.getArrivalDestination(), sourceLang, targetLang));
        }
        if (source.getTransportType() != null) {
            translated.setTransportType(translateTerm(source.getTransportType(), sourceLang, targetLang));
        }
        if (source.getTicketClass() != null) {
            translated.setTicketClass(translateTerm(source.getTicketClass(), sourceLang, targetLang));
        }
        if (source.getSeat() != null) {
            translated.setSeat(translateTerm(source.getSeat(), sourceLang, targetLang));
        }
        if (source.getCoach() != null) {
            translated.setCoach(translateTerm(source.getCoach(), sourceLang, targetLang));
        }

        // Generate synthesized, fluent summary in target language
        translated.setSummary(generateTranslatedSummary(translated, targetLang));

        return translated;
    }

    /**
     * Translates arbitrary text from sourceLang to targetLang.
     */
    public String translateText(String text, String sourceLang, String targetLang) {
        if (text == null || text.trim().isEmpty()) return text;
        if (sourceLang != null && targetLang != null && sourceLang.equalsIgnoreCase(targetLang)) return text;

        if (text.contains("\n")) {
            String[] lines = text.split("\r?\n");
            StringBuilder sb = new StringBuilder();
            for (String line : lines) {
                if (line.trim().isEmpty()) {
                    sb.append("\n");
                } else {
                    sb.append(translateSingleLine(line, sourceLang, targetLang)).append("\n");
                }
            }
            return sb.toString().trim();
        }

        return translateSingleLine(text, sourceLang, targetLang);
    }

    private String translateSingleLine(String line, String sourceLang, String targetLang) {
        if (line == null || line.trim().isEmpty()) return line;

        // 1. Direct dictionary match
        String dictMatch = lookupDictionary(line, sourceLang, targetLang);
        if (dictMatch != null) {
            return dictMatch;
        }

        // 2. Structured key-value replacement
        if (line.contains(":")) {
            int colonIdx = line.indexOf(':');
            String key = line.substring(0, colonIdx).trim();
            String val = line.substring(colonIdx + 1).trim();

            String translatedKey = lookupDictionary(key, sourceLang, targetLang);
            if (translatedKey == null) {
                translatedKey = translateTerm(key, sourceLang, targetLang);
            }
            String translatedVal = lookupDictionary(val, sourceLang, targetLang);
            if (translatedVal == null) {
                translatedVal = val;
            }
            return (translatedKey != null ? translatedKey : key) + ": " + translatedVal;
        }

        // 3. Online public translation API attempt
        try {
            String onlineTranslation = callMyMemoryApi(line, sourceLang, targetLang);
            if (onlineTranslation != null && !onlineTranslation.trim().isEmpty() && !onlineTranslation.equalsIgnoreCase("null")) {
                return onlineTranslation;
            }
        } catch (Exception e) {
            logger.debug("Online translation API skipped: {}", e.getMessage());
        }

        // Fallback: return clean original line
        return line;
    }

    private String translateTerm(String term, String sourceLang, String targetLang) {
        if (term == null) return null;
        String dictMatch = lookupDictionary(term, sourceLang, targetLang);
        if (dictMatch != null) return dictMatch;

        try {
            String online = callMyMemoryApi(term, sourceLang, targetLang);
            if (online != null && !online.trim().isEmpty() && !online.equalsIgnoreCase("null")) {
                return online;
            }
        } catch (Exception ignored) {}

        return term;
    }

    private String lookupDictionary(String text, String sourceLang, String targetLang) {
        if (text == null || sourceLang == null || targetLang == null) return null;

        // 1. Dynamic Database Glossary Check
        if (glossaryService != null) {
            try {
                java.util.Optional<String> dynamicMatch = glossaryService.findTranslation(sourceLang, targetLang, text);
                if (dynamicMatch.isPresent()) {
                    return dynamicMatch.get();
                }
            } catch (Exception e) {
                logger.debug("Dynamic glossary lookup error: {}", e.getMessage());
            }
        }

        // 2. Static Built-in Travel Dictionary
        String key = (sourceLang + "_" + targetLang).toLowerCase();
        Map<String, String> dict = TRAVEL_DICTIONARY.get(key);
        if (dict != null) {
            String match = dict.get(text.toLowerCase().trim());
            if (match != null) return match;
        }
        return null;
    }

    private String callMyMemoryApi(String text, String sourceLang, String targetLang) throws Exception {
        if (text == null || text.trim().isEmpty()) return null;
        String langPair = sourceLang.toLowerCase() + "|" + targetLang.toLowerCase();
        String encodedQuery = URLEncoder.encode(text, StandardCharsets.UTF_8);
        String encodedLangPair = URLEncoder.encode(langPair, StandardCharsets.UTF_8);
        String endpoint = "https://api.mymemory.translated.net/get?q=" + encodedQuery + "&langpair=" + encodedLangPair;

        URI uri = URI.create(endpoint);
        HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(2500);
        conn.setReadTimeout(2500);
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Multilingual Ticket Translator API)");

        int status = conn.getResponseCode();
        if (status == 200) {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                JsonNode root = objectMapper.readTree(response.toString());
                JsonNode responseData = root.path("responseData");
                if (!responseData.isMissingNode()) {
                    String translatedText = responseData.path("translatedText").asText();
                    if (translatedText != null && !translatedText.isEmpty() && !translatedText.startsWith("MYMEMORY WARNING") && !translatedText.equalsIgnoreCase("null")) {
                        return translatedText;
                    }
                }
            }
        }
        return null;
    }

    private String generateTranslatedSummary(TicketDetails details, String targetLang) {
        String lang = targetLang != null ? targetLang.toLowerCase() : "en";
        String transport = details.getTransportType() != null ? details.getTransportType() : "Travel";
        String from = details.getDepartureStation() != null ? details.getDepartureStation() : "Origin";
        String to = details.getArrivalDestination() != null ? details.getArrivalDestination() : "Destination";
        String date = details.getTravelDate() != null ? details.getTravelDate() : "";
        String time = details.getDepartureTime() != null ? details.getDepartureTime() : "";
        String carrier = details.getCarrierNumber() != null ? details.getCarrierNumber() : "";
        String pnr = details.getPnrNumber() != null ? details.getPnrNumber() : "";

        if ("es".equals(lang)) {
            return String.format("Billete de %s de %s a %s para el %s a las %s (%s). Asiento: %s, Coche: %s. Localizador: %s.",
                    transport, from, to, date, time, carrier,
                    details.getSeat() != null ? details.getSeat() : "General",
                    details.getCoach() != null ? details.getCoach() : "1",
                    pnr);
        } else if ("fr".equals(lang)) {
            return String.format("Billet de %s de %s à %s le %s à %s (%s). Siège: %s, Voiture: %s. Référence: %s.",
                    transport, from, to, date, time, carrier,
                    details.getSeat() != null ? details.getSeat() : "Général",
                    details.getCoach() != null ? details.getCoach() : "1",
                    pnr);
        } else if ("de".equals(lang)) {
            return String.format("%s-Fahrkarte von %s nach %s am %s um %s Uhr (%s). Sitzplatz: %s, Wagen: %s. Buchungscode: %s.",
                    transport, from, to, date, time, carrier,
                    details.getSeat() != null ? details.getSeat() : "Freie Platzwahl",
                    details.getCoach() != null ? details.getCoach() : "1",
                    pnr);
        } else if ("hi".equals(lang)) {
            return String.format("%s से %s के लिए %s टिकट (%s), दिनांक %s, समय %s। सीट: %s, कोच: %s। PNR: %s।",
                    from, to, transport, carrier, date, time,
                    details.getSeat() != null ? details.getSeat() : "-",
                    details.getCoach() != null ? details.getCoach() : "-",
                    pnr);
        } else if ("ja".equals(lang)) {
            return String.format("%s発 %s行きの%sチケット (%s)。乗車日: %s %s。座席: %s号車 %s席。予約番号: %s。",
                    from, to, transport, carrier, date, time,
                    details.getCoach() != null ? details.getCoach() : "1",
                    details.getSeat() != null ? details.getSeat() : "自由席",
                    pnr);
        }

        // Default English
        StringBuilder sb = new StringBuilder();
        sb.append(transport).append(" ticket from ").append(from).append(" to ").append(to);
        if (!date.isEmpty()) sb.append(" on ").append(date);
        if (!time.isEmpty()) sb.append(" at ").append(time);
        if (!carrier.isEmpty()) sb.append(" (").append(carrier).append(")");
        if (details.getSeat() != null || details.getCoach() != null) {
            sb.append(". Seat details: ");
            if (details.getCoach() != null) sb.append("Coach ").append(details.getCoach()).append(", ");
            if (details.getSeat() != null) sb.append("Seat ").append(details.getSeat());
        }
        if (!pnr.isEmpty()) {
            sb.append(". Booking Reference: ").append(pnr);
        }
        sb.append(".");
        return sb.toString();
    }
}
