package com.translator.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "analyzed_tickets", indexes = {
        @Index(name = "idx_ticket_pnr", columnList = "pnrNumber"),
        @Index(name = "idx_ticket_passenger", columnList = "passengerName"),
        @Index(name = "idx_ticket_carrier", columnList = "carrierNumber"),
        @Index(name = "idx_ticket_created", columnList = "createdAt")
})
public class AnalyzedTicketEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fileName;

    private String fileType;
    private String fileSize;

    private String detectedLanguageCode;
    private String detectedLanguageName;
    private Double detectedConfidence;

    private String targetLanguageCode;
    private String targetLanguageName;

    // Extracted Fields
    private String passengerName;
    private String departureStation;
    private String arrivalDestination;
    private String travelDate;
    private String departureTime;
    private String arrivalTime;
    private String carrierNumber;
    private String transportType;
    private String seat;
    private String coach;
    private String ticketClass;
    private String pnrNumber;
    private String fare;

    // Translated Fields
    private String translatedDepartureStation;
    private String translatedArrivalDestination;
    private String translatedTransportType;
    private String translatedSeat;
    private String translatedCoach;
    private String translatedTicketClass;

    @Column(length = 2000)
    private String summary;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String rawExtractedText;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String translatedFullText;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public AnalyzedTicketEntity() {}

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getDetectedLanguageCode() {
        return detectedLanguageCode;
    }

    public void setDetectedLanguageCode(String detectedLanguageCode) {
        this.detectedLanguageCode = detectedLanguageCode;
    }

    public String getDetectedLanguageName() {
        return detectedLanguageName;
    }

    public void setDetectedLanguageName(String detectedLanguageName) {
        this.detectedLanguageName = detectedLanguageName;
    }

    public Double getDetectedConfidence() {
        return detectedConfidence;
    }

    public void setDetectedConfidence(Double detectedConfidence) {
        this.detectedConfidence = detectedConfidence;
    }

    public String getTargetLanguageCode() {
        return targetLanguageCode;
    }

    public void setTargetLanguageCode(String targetLanguageCode) {
        this.targetLanguageCode = targetLanguageCode;
    }

    public String getTargetLanguageName() {
        return targetLanguageName;
    }

    public void setTargetLanguageName(String targetLanguageName) {
        this.targetLanguageName = targetLanguageName;
    }

    public String getPassengerName() {
        return passengerName;
    }

    public void setPassengerName(String passengerName) {
        this.passengerName = passengerName;
    }

    public String getDepartureStation() {
        return departureStation;
    }

    public void setDepartureStation(String departureStation) {
        this.departureStation = departureStation;
    }

    public String getArrivalDestination() {
        return arrivalDestination;
    }

    public void setArrivalDestination(String arrivalDestination) {
        this.arrivalDestination = arrivalDestination;
    }

    public String getTravelDate() {
        return travelDate;
    }

    public void setTravelDate(String travelDate) {
        this.travelDate = travelDate;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(String arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public String getCarrierNumber() {
        return carrierNumber;
    }

    public void setCarrierNumber(String carrierNumber) {
        this.carrierNumber = carrierNumber;
    }

    public String getTransportType() {
        return transportType;
    }

    public void setTransportType(String transportType) {
        this.transportType = transportType;
    }

    public String getSeat() {
        return seat;
    }

    public void setSeat(String seat) {
        this.seat = seat;
    }

    public String getCoach() {
        return coach;
    }

    public void setCoach(String coach) {
        this.coach = coach;
    }

    public String getTicketClass() {
        return ticketClass;
    }

    public void setTicketClass(String ticketClass) {
        this.ticketClass = ticketClass;
    }

    public String getPnrNumber() {
        return pnrNumber;
    }

    public void setPnrNumber(String pnrNumber) {
        this.pnrNumber = pnrNumber;
    }

    public String getFare() {
        return fare;
    }

    public void setFare(String fare) {
        this.fare = fare;
    }

    public String getTranslatedDepartureStation() {
        return translatedDepartureStation;
    }

    public void setTranslatedDepartureStation(String translatedDepartureStation) {
        this.translatedDepartureStation = translatedDepartureStation;
    }

    public String getTranslatedArrivalDestination() {
        return translatedArrivalDestination;
    }

    public void setTranslatedArrivalDestination(String translatedArrivalDestination) {
        this.translatedArrivalDestination = translatedArrivalDestination;
    }

    public String getTranslatedTransportType() {
        return translatedTransportType;
    }

    public void setTranslatedTransportType(String translatedTransportType) {
        this.translatedTransportType = translatedTransportType;
    }

    public String getTranslatedSeat() {
        return translatedSeat;
    }

    public void setTranslatedSeat(String translatedSeat) {
        this.translatedSeat = translatedSeat;
    }

    public String getTranslatedCoach() {
        return translatedCoach;
    }

    public void setTranslatedCoach(String translatedCoach) {
        this.translatedCoach = translatedCoach;
    }

    public String getTranslatedTicketClass() {
        return translatedTicketClass;
    }

    public void setTranslatedTicketClass(String translatedTicketClass) {
        this.translatedTicketClass = translatedTicketClass;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
