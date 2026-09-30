package com.translator.service;

import com.optimaize.langdetect.LanguageDetector;
import com.optimaize.langdetect.LanguageDetectorBuilder;
import com.optimaize.langdetect.i18n.LdLocale;
import com.optimaize.langdetect.ngram.NgramExtractors;
import com.optimaize.langdetect.profiles.LanguageProfile;
import com.optimaize.langdetect.profiles.LanguageProfileReader;
import com.optimaize.langdetect.text.CommonTextObjectFactories;
import com.optimaize.langdetect.text.TextObject;
import com.optimaize.langdetect.text.TextObjectFactory;
import com.translator.dto.LanguageInfoDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class LanguageDetectionService {

    private static final Logger logger = LoggerFactory.getLogger(LanguageDetectionService.class);
    private LanguageDetector languageDetector;
    private TextObjectFactory textObjectFactory;

    private static final Map<String, String> LANGUAGE_NAMES = new HashMap<>();

    static {
        LANGUAGE_NAMES.put("en", "English");
        LANGUAGE_NAMES.put("es", "Spanish (Español)");
        LANGUAGE_NAMES.put("fr", "French (Français)");
        LANGUAGE_NAMES.put("de", "German (Deutsch)");
        LANGUAGE_NAMES.put("it", "Italian (Italiano)");
        LANGUAGE_NAMES.put("pt", "Portuguese (Português)");
        LANGUAGE_NAMES.put("hi", "Hindi (हिन्दी)");
        LANGUAGE_NAMES.put("ja", "Japanese (日本語)");
        LANGUAGE_NAMES.put("zh", "Chinese (中文)");
        LANGUAGE_NAMES.put("ru", "Russian (Русский)");
        LANGUAGE_NAMES.put("ar", "Arabic (العربية)");
        LANGUAGE_NAMES.put("ko", "Korean (한국어)");
    }

    public LanguageDetectionService() {
        initDetector();
    }

    private void initDetector() {
        try {
            List<LanguageProfile> languageProfiles = new LanguageProfileReader().readAllBuiltIn();
            this.languageDetector = LanguageDetectorBuilder.create(NgramExtractors.standard())
                    .withProfiles(languageProfiles)
                    .build();
            this.textObjectFactory = CommonTextObjectFactories.forDetectingOnLargeText();
            logger.info("LanguageDetector initialized with {} language profiles", languageProfiles.size());
        } catch (Exception e) {
            logger.warn("Optimaize LanguageDetector initialization notice: {}", e.getMessage());
        }
    }

    /**
     * Detects the language of the provided text with confidence and display name.
     */
    public LanguageInfoDto detectLanguage(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new LanguageInfoDto("en", "English", 1.0);
        }

        // 1. Script-based heuristic checks
        if (Pattern.compile("\\p{IsDevanagari}").matcher(text).find()) {
            return new LanguageInfoDto("hi", LANGUAGE_NAMES.get("hi"), 0.99);
        }
        if (Pattern.compile("[\\p{IsHiragana}\\p{IsKatakana}]").matcher(text).find()) {
            return new LanguageInfoDto("ja", LANGUAGE_NAMES.get("ja"), 0.99);
        }
        if (Pattern.compile("\\p{IsHan}").matcher(text).find()) {
            return new LanguageInfoDto("zh", LANGUAGE_NAMES.get("zh"), 0.98);
        }
        if (Pattern.compile("\\p{IsCyrillic}").matcher(text).find()) {
            return new LanguageInfoDto("ru", LANGUAGE_NAMES.get("ru"), 0.99);
        }
        if (Pattern.compile("\\p{IsArabic}").matcher(text).find()) {
            return new LanguageInfoDto("ar", LANGUAGE_NAMES.get("ar"), 0.99);
        }
        if (Pattern.compile("\\p{IsHangul}").matcher(text).find()) {
            return new LanguageInfoDto("ko", LANGUAGE_NAMES.get("ko"), 0.99);
        }

        // 2. Ticket-specific multilingual keyword heuristic
        String lower = text.toLowerCase();
        int esCount = countOccurrences(lower, "billete", "origen", "destino", "pasajero", "asiento", "fecha", "renfe", "salida", "llegada", "coche");
        int frCount = countOccurrences(lower, "billet", "départ", "arrivée", "passager", "siège", "voiture", "dossier", "voyageur", "sncf", "gare");
        int deCount = countOccurrences(lower, "fahrkarte", "abfahrt", "ankunft", "fahrgast", "sitzplatz", "wagen", "bahn", "datum", "klasse", "zug");
        int itCount = countOccurrences(lower, "biglietto", "partenza", "arrivo", "passeggero", "posto", "vettura", "treno", "data", "stazione");
        int ptCount = countOccurrences(lower, "bilhete", "passagem", "origem", "destino", "passageiro", "assento", "carro", "data", "partida");

        int maxKeywords = Math.max(esCount, Math.max(frCount, Math.max(deCount, Math.max(itCount, ptCount))));
        if (maxKeywords >= 2) {
            if (maxKeywords == esCount) return new LanguageInfoDto("es", LANGUAGE_NAMES.get("es"), 0.95);
            if (maxKeywords == frCount) return new LanguageInfoDto("fr", LANGUAGE_NAMES.get("fr"), 0.95);
            if (maxKeywords == deCount) return new LanguageInfoDto("de", LANGUAGE_NAMES.get("de"), 0.95);
            if (maxKeywords == itCount) return new LanguageInfoDto("it", LANGUAGE_NAMES.get("it"), 0.95);
            if (maxKeywords == ptCount) return new LanguageInfoDto("pt", LANGUAGE_NAMES.get("pt"), 0.95);
        }

        // 3. Optimaize n-gram Language Detector
        if (languageDetector != null && textObjectFactory != null) {
            try {
                TextObject textObject = textObjectFactory.forText(text);
                com.google.common.base.Optional<LdLocale> lang = languageDetector.detect(textObject);
                if (lang.isPresent()) {
                    String langCode = lang.get().getLanguage();
                    String langName = LANGUAGE_NAMES.getOrDefault(langCode, langCode.toUpperCase());
                    return new LanguageInfoDto(langCode, langName, 0.90);
                }
            } catch (Exception e) {
                logger.debug("Optimaize detection fallback: {}", e.getMessage());
            }
        }

        return new LanguageInfoDto("en", "English", 0.75);
    }

    public static String normalizeLanguageCode(String input) {
        if (input == null || input.trim().isEmpty()) return "en";
        String s = input.trim().toLowerCase();
        switch (s) {
            case "english": case "en": return "en";
            case "spanish": case "español": case "es": return "es";
            case "french": case "français": case "fr": return "fr";
            case "german": case "deutsch": case "de": return "de";
            case "italian": case "italiano": case "it": return "it";
            case "portuguese": case "português": case "pt": return "pt";
            case "hindi": case "हिन्दी": case "hi": return "hi";
            case "japanese": case "日本語": case "ja": return "ja";
            case "chinese": case "中文": case "zh": return "zh";
            case "russian": case "русский": case "ru": return "ru";
            case "arabic": case "العربية": case "ar": return "ar";
            case "korean": case "한국어": case "ko": return "ko";
            default: return s.length() == 2 ? s : "en";
        }
    }

    public static String getLanguageDisplayName(String codeOrName) {
        if (codeOrName == null) return "English";
        String code = normalizeLanguageCode(codeOrName);
        return LANGUAGE_NAMES.getOrDefault(code, codeOrName);
    }

    private int countOccurrences(String text, String... words) {
        int count = 0;
        for (String word : words) {
            if (text.contains(word)) {
                count++;
            }
        }
        return count;
    }
}
