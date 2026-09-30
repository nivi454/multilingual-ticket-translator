package com.translator.dto;

import com.translator.model.TicketDetails;

public class TicketAnalysisResponse {
    private boolean success;
    private String message;
    private Long id;
    private java.time.LocalDateTime createdAt;
    private String fileName;
    private String fileType;
    private String fileSize;
    private LanguageInfoDto detectedLanguage;
    private LanguageInfoDto targetLanguage;
    private TicketDetails extractedDetails;
    private TicketDetails translatedDetails;
    private String rawExtractedText;
    private String translatedFullText;

    public TicketAnalysisResponse() {}

    public static TicketAnalysisResponse success(
            Long id,
            java.time.LocalDateTime createdAt,
            String fileName,
            String fileType,
            String fileSize,
            LanguageInfoDto detectedLanguage,
            LanguageInfoDto targetLanguage,
            TicketDetails extractedDetails,
            TicketDetails translatedDetails,
            String rawExtractedText,
            String translatedFullText
    ) {
        TicketAnalysisResponse response = new TicketAnalysisResponse();
        response.setSuccess(true);
        response.setMessage("Ticket analyzed and translated successfully");
        response.setId(id);
        response.setCreatedAt(createdAt);
        response.setFileName(fileName);
        response.setFileType(fileType);
        response.setFileSize(fileSize);
        response.setDetectedLanguage(detectedLanguage);
        response.setTargetLanguage(targetLanguage);
        response.setExtractedDetails(extractedDetails);
        response.setTranslatedDetails(translatedDetails);
        response.setRawExtractedText(rawExtractedText);
        response.setTranslatedFullText(translatedFullText);
        return response;
    }

    public static TicketAnalysisResponse error(String message) {
        TicketAnalysisResponse response = new TicketAnalysisResponse();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public java.time.LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(java.time.LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public LanguageInfoDto getDetectedLanguage() {
        return detectedLanguage;
    }

    public void setDetectedLanguage(LanguageInfoDto detectedLanguage) {
        this.detectedLanguage = detectedLanguage;
    }

    public LanguageInfoDto getTargetLanguage() {
        return targetLanguage;
    }

    public void setTargetLanguage(LanguageInfoDto targetLanguage) {
        this.targetLanguage = targetLanguage;
    }

    public TicketDetails getExtractedDetails() {
        return extractedDetails;
    }

    public void setExtractedDetails(TicketDetails extractedDetails) {
        this.extractedDetails = extractedDetails;
    }

    public TicketDetails getTranslatedDetails() {
        return translatedDetails;
    }

    public void setTranslatedDetails(TicketDetails translatedDetails) {
        this.translatedDetails = translatedDetails;
    }

    public String getRawExtractedText() {
        return rawExtractedText;
    }

    public void setRawExtractedText(String rawExtractedText) {
        this.rawExtractedText = rawExtractedText;
    }

    public String getTranslatedFullText() {
        return translatedFullText;
    }

    public void setTranslatedFullText(String translatedFullText) {
        this.translatedFullText = translatedFullText;
    }
}
