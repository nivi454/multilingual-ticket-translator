# Multilingual Ticket Translator

A web-based application that helps users analyze transport tickets written in different languages.

## Features

- Upload transport tickets in JPG, PNG, WEBP, and PDF formats
- Extract ticket text using OCR
- Detect the language of the uploaded ticket
- Extract important ticket details such as:
  - Passenger name
  - Departure
  - Destination
  - Date and time
  - Train/Bus/Flight number
  - Seat/Coach/Class
  - PNR, when available
- Translate extracted ticket information into the selected language
- Display a simplified translated ticket summary
- Save analyzed tickets and view them through Ticket History
- Manage commonly used translation terms through Glossary Management
- Responsive React-based user interface

## Technology Stack

### Frontend
- React.js
- Vite
- HTML
- CSS
- JavaScript

### Backend
- Java
- Spring Boot
- Maven
- REST API

### Database
- H2 / MySQL configuration

### OCR & Translation
- Tesseract OCR
- Language Detection
- Translation Service

## Project Structure

```text
multilingual-ticket-translator/
├── backend/
├── frontend/
├── database/
├── docs/
├── tessdata/
└── README.md
