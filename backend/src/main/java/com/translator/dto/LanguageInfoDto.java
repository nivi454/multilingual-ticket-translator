package com.translator.dto;

public class LanguageInfoDto {
    private String code;
    private String name;
    private Double confidence;

    public LanguageInfoDto() {}

    public LanguageInfoDto(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public LanguageInfoDto(String code, String name, Double confidence) {
        this.code = code;
        this.name = name;
        this.confidence = confidence;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }
}
