package com.translator.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "glossary_terms", indexes = {
        @Index(name = "idx_glossary_lang_pair", columnList = "sourceLanguage, targetLanguage"),
        @Index(name = "idx_glossary_term", columnList = "sourceTerm")
})
public class GlossaryTermEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String sourceLanguage;

    @Column(nullable = false, length = 10)
    private String targetLanguage;

    @Column(nullable = false, length = 255)
    private String sourceTerm;

    @Column(nullable = false, length = 255)
    private String translatedTerm;

    private String category; // e.g., "Station", "Carrier", "Class", "General"

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public GlossaryTermEntity() {}

    public GlossaryTermEntity(String sourceLanguage, String targetLanguage, String sourceTerm, String translatedTerm, String category) {
        this.sourceLanguage = sourceLanguage != null ? sourceLanguage.toLowerCase().trim() : "es";
        this.targetLanguage = targetLanguage != null ? targetLanguage.toLowerCase().trim() : "en";
        this.sourceTerm = sourceTerm != null ? sourceTerm.trim() : "";
        this.translatedTerm = translatedTerm != null ? translatedTerm.trim() : "";
        this.category = category != null ? category.trim() : "General";
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSourceLanguage() {
        return sourceLanguage;
    }

    public void setSourceLanguage(String sourceLanguage) {
        this.sourceLanguage = sourceLanguage != null ? sourceLanguage.toLowerCase().trim() : null;
    }

    public String getTargetLanguage() {
        return targetLanguage;
    }

    public void setTargetLanguage(String targetLanguage) {
        this.targetLanguage = targetLanguage != null ? targetLanguage.toLowerCase().trim() : null;
    }

    public String getSourceTerm() {
        return sourceTerm;
    }

    public void setSourceTerm(String sourceTerm) {
        this.sourceTerm = sourceTerm;
    }

    public String getTranslatedTerm() {
        return translatedTerm;
    }

    public void setTranslatedTerm(String translatedTerm) {
        this.translatedTerm = translatedTerm;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
