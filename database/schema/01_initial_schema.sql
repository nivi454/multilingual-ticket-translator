-- ====================================================================
-- Multilingual Ticket Translator - Production Database Schema
-- Database: MySQL 8.x
-- Character Set: utf8mb4 (Required for full multilingual support)
-- ====================================================================

CREATE DATABASE IF NOT EXISTS ticket_translator_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE ticket_translator_db;

-- 1. Analyzed Tickets History Table
CREATE TABLE IF NOT EXISTS analyzed_tickets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(100),
    file_size VARCHAR(50),
    detected_language_code VARCHAR(10),
    detected_language_name VARCHAR(100),
    detected_confidence DOUBLE,
    target_language_code VARCHAR(10),
    target_language_name VARCHAR(100),
    passenger_name VARCHAR(255),
    departure_station VARCHAR(255),
    arrival_destination VARCHAR(255),
    travel_date VARCHAR(100),
    departure_time VARCHAR(50),
    arrival_time VARCHAR(50),
    carrier_number VARCHAR(100),
    transport_type VARCHAR(50),
    seat VARCHAR(50),
    coach VARCHAR(50),
    ticket_class VARCHAR(100),
    pnr_number VARCHAR(100),
    fare VARCHAR(100),
    translated_departure_station VARCHAR(255),
    translated_arrival_destination VARCHAR(255),
    translated_transport_type VARCHAR(50),
    translated_seat VARCHAR(50),
    translated_coach VARCHAR(50),
    translated_ticket_class VARCHAR(100),
    summary VARCHAR(2000),
    raw_extracted_text LONGTEXT,
    translated_full_text LONGTEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_ticket_pnr (pnr_number),
    INDEX idx_ticket_passenger (passenger_name),
    INDEX idx_ticket_carrier (carrier_number),
    INDEX idx_ticket_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Multilingual Travel Glossary Terms Table
CREATE TABLE IF NOT EXISTS glossary_terms (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    source_language VARCHAR(10) NOT NULL,
    target_language VARCHAR(10) NOT NULL,
    source_term VARCHAR(255) NOT NULL,
    translated_term VARCHAR(255) NOT NULL,
    category VARCHAR(100) DEFAULT 'General',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_glossary_lang_pair (source_language, target_language),
    INDEX idx_glossary_term (source_term)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Initial Reference Glossary Seed Data
INSERT IGNORE INTO glossary_terms (source_language, target_language, source_term, translated_term, category) VALUES
('es', 'en', 'tren de alta velocidad', 'High-Speed Train', 'Carrier'),
('es', 'en', 'ave', 'AVE (High-Speed Train)', 'Carrier'),
('es', 'en', 'origen', 'Origin / Departure Station', 'Station'),
('es', 'en', 'destino', 'Destination Station', 'Station'),
('es', 'en', 'puerta de atocha', 'Madrid Puerta de Atocha', 'Station'),
('es', 'en', 'barcelona sants', 'Barcelona Sants Station', 'Station'),
('es', 'en', 'coche', 'Coach / Carriage', 'General'),
('es', 'en', 'asiento', 'Seat', 'General'),
('es', 'en', 'turista', 'Economy Class', 'Class'),
('es', 'en', 'preferente', 'First / Premium Class', 'Class'),
('es', 'en', 'localizador', 'Booking Reference (PNR)', 'General'),
('fr', 'en', 'train à grande vitesse', 'High-Speed Train', 'Carrier'),
('fr', 'en', 'tgv', 'TGV (High-Speed Train)', 'Carrier'),
('fr', 'en', 'gare de départ', 'Departure Station', 'Station'),
('fr', 'en', 'gare d\'arrivée', 'Arrival Station', 'Station'),
('fr', 'en', 'gare de lyon', 'Paris Gare de Lyon', 'Station'),
('fr', 'en', 'voiture', 'Coach', 'General'),
('fr', 'en', 'siège', 'Seat', 'General'),
('fr', 'en', 'première classe', 'First Class', 'Class'),
('fr', 'en', 'seconde classe', 'Economy / Standard Class', 'Class'),
('fr', 'en', 'référence dossier', 'Booking Reference (PNR)', 'General'),
('de', 'en', 'hochgeschwindigkeitszug', 'High-Speed Train', 'Carrier'),
('de', 'en', 'ice', 'ICE (InterCity Express)', 'Carrier'),
('de', 'en', 'abfahrt', 'Departure', 'General'),
('de', 'en', 'ankunft', 'Arrival', 'General'),
('de', 'en', 'hauptbahnhof', 'Central Station (Hbf)', 'Station'),
('de', 'en', 'wagen', 'Coach', 'General'),
('de', 'en', 'sitzplatz', 'Seat', 'General'),
('de', 'en', '1. klasse', '1st Class', 'Class'),
('de', 'en', '2. klasse', 'Standard / 2nd Class', 'Class'),
('de', 'en', 'buchungscode', 'Booking Reference (PNR)', 'General');
