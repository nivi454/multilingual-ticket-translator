package com.translator.controller;

import com.translator.entity.GlossaryTermEntity;
import com.translator.service.GlossaryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/glossary")
public class GlossaryController {

    private final GlossaryService glossaryService;

    public GlossaryController(GlossaryService glossaryService) {
        this.glossaryService = glossaryService;
    }

    @GetMapping
    public ResponseEntity<List<GlossaryTermEntity>> getGlossaryTerms(
            @RequestParam(value = "source", required = false) String sourceLang,
            @RequestParam(value = "target", required = false) String targetLang
    ) {
        if (sourceLang != null && targetLang != null) {
            return ResponseEntity.ok(glossaryService.getTermsForPair(sourceLang, targetLang));
        }
        return ResponseEntity.ok(glossaryService.getAllTerms());
    }

    @PostMapping
    public ResponseEntity<GlossaryTermEntity> saveTerm(@RequestBody GlossaryTermEntity term) {
        if (term.getSourceTerm() == null || term.getSourceTerm().trim().isEmpty() ||
            term.getTranslatedTerm() == null || term.getTranslatedTerm().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(glossaryService.addOrUpdateTerm(term));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteTerm(@PathVariable("id") Long id) {
        boolean deleted = glossaryService.deleteTerm(id);
        if (deleted) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Glossary term deleted successfully."));
        }
        return ResponseEntity.notFound().build();
    }
}
