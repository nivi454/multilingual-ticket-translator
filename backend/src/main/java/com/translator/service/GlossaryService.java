package com.translator.service;

import com.translator.entity.GlossaryTermEntity;
import com.translator.repository.GlossaryTermRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GlossaryService {

    private static final Logger logger = LoggerFactory.getLogger(GlossaryService.class);
    private final GlossaryTermRepository glossaryRepository;

    public GlossaryService(GlossaryTermRepository glossaryRepository) {
        this.glossaryRepository = glossaryRepository;
    }

    @PostConstruct
    public void initDefaultGlossary() {
        if (glossaryRepository.count() == 0) {
            logger.info("Seeding initial travel glossary database terms...");

            // Spanish -> English
            saveIfNotExists("es", "en", "tren de alta velocidad", "High-Speed Train", "Carrier");
            saveIfNotExists("es", "en", "ave", "AVE (High-Speed Train)", "Carrier");
            saveIfNotExists("es", "en", "origen", "Origin / Departure Station", "Station");
            saveIfNotExists("es", "en", "destino", "Destination Station", "Station");
            saveIfNotExists("es", "en", "puerta de atocha", "Madrid Puerta de Atocha", "Station");
            saveIfNotExists("es", "en", "barcelona sants", "Barcelona Sants Station", "Station");
            saveIfNotExists("es", "en", "coche", "Coach / Carriage", "General");
            saveIfNotExists("es", "en", "asiento", "Seat", "General");
            saveIfNotExists("es", "en", "turista", "Economy Class", "Class");
            saveIfNotExists("es", "en", "preferente", "First / Premium Class", "Class");
            saveIfNotExists("es", "en", "localizador", "Booking Reference (PNR)", "General");

            // French -> English
            saveIfNotExists("fr", "en", "train à grande vitesse", "High-Speed Train", "Carrier");
            saveIfNotExists("fr", "en", "tgv", "TGV (High-Speed Train)", "Carrier");
            saveIfNotExists("fr", "en", "gare de départ", "Departure Station", "Station");
            saveIfNotExists("fr", "en", "gare d'arrivée", "Arrival Station", "Station");
            saveIfNotExists("fr", "en", "gare de lyon", "Paris Gare de Lyon", "Station");
            saveIfNotExists("fr", "en", "voiture", "Coach", "General");
            saveIfNotExists("fr", "en", "siège", "Seat", "General");
            saveIfNotExists("fr", "en", "première classe", "First Class", "Class");
            saveIfNotExists("fr", "en", "seconde classe", "Economy / Standard Class", "Class");
            saveIfNotExists("fr", "en", "référence dossier", "Booking Reference (PNR)", "General");

            // German -> English
            saveIfNotExists("de", "en", "hochgeschwindigkeitszug", "High-Speed Train", "Carrier");
            saveIfNotExists("de", "en", "ice", "ICE (InterCity Express)", "Carrier");
            saveIfNotExists("de", "en", "abfahrt", "Departure", "General");
            saveIfNotExists("de", "en", "ankunft", "Arrival", "General");
            saveIfNotExists("de", "en", "hauptbahnhof", "Central Station (Hbf)", "Station");
            saveIfNotExists("de", "en", "wagen", "Coach", "General");
            saveIfNotExists("de", "en", "sitzplatz", "Seat", "General");
            saveIfNotExists("de", "en", "1. klasse", "1st Class", "Class");
            saveIfNotExists("de", "en", "2. klasse", "Standard / 2nd Class", "Class");
            saveIfNotExists("de", "en", "buchungscode", "Booking Reference (PNR)", "General");

            // Italian -> English
            saveIfNotExists("it", "en", "frecciarossa", "Frecciarossa (High-Speed Train)", "Carrier");
            saveIfNotExists("it", "en", "stazione di partenza", "Departure Station", "Station");
            saveIfNotExists("it", "en", "stazione di arrivo", "Arrival Station", "Station");
            saveIfNotExists("it", "en", "posto", "Seat", "General");
            saveIfNotExists("it", "en", "vettura", "Coach", "General");
            saveIfNotExists("it", "en", "prima classe", "First Class", "Class");
            saveIfNotExists("it", "en", "codice prenotazione", "Booking Reference (PNR)", "General");
        }
    }

    private void saveIfNotExists(String src, String tgt, String term, String trans, String category) {
        Optional<GlossaryTermEntity> existing = glossaryRepository.findFirstBySourceLanguageAndTargetLanguageAndSourceTermIgnoreCase(src, tgt, term);
        if (existing.isEmpty()) {
            glossaryRepository.save(new GlossaryTermEntity(src, tgt, term, trans, category));
        }
    }

    public List<GlossaryTermEntity> getAllTerms() {
        return glossaryRepository.findAllByOrderBySourceLanguageAscSourceTermAsc();
    }

    public List<GlossaryTermEntity> getTermsForPair(String sourceLang, String targetLang) {
        return glossaryRepository.findBySourceLanguageAndTargetLanguage(
                sourceLang.toLowerCase().trim(),
                targetLang.toLowerCase().trim()
        );
    }

    public Optional<String> findTranslation(String sourceLang, String targetLang, String term) {
        if (term == null || term.trim().isEmpty()) return Optional.empty();
        return glossaryRepository.findFirstBySourceLanguageAndTargetLanguageAndSourceTermIgnoreCase(
                sourceLang.toLowerCase().trim(),
                targetLang.toLowerCase().trim(),
                term.trim()
        ).map(GlossaryTermEntity::getTranslatedTerm);
    }

    public GlossaryTermEntity addOrUpdateTerm(GlossaryTermEntity term) {
        Optional<GlossaryTermEntity> existing = glossaryRepository.findFirstBySourceLanguageAndTargetLanguageAndSourceTermIgnoreCase(
                term.getSourceLanguage(), term.getTargetLanguage(), term.getSourceTerm()
        );

        if (existing.isPresent()) {
            GlossaryTermEntity entity = existing.get();
            entity.setTranslatedTerm(term.getTranslatedTerm());
            entity.setCategory(term.getCategory());
            return glossaryRepository.save(entity);
        }

        return glossaryRepository.save(term);
    }

    public boolean deleteTerm(Long id) {
        if (glossaryRepository.existsById(id)) {
            glossaryRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
