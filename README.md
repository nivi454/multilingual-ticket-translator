# Multilingual Ticket Translator

An enterprise-grade customer support platform designed to bridge language barriers between global customers and support agents. The system automatically detects incoming customer inquiry languages, translates tickets into English for agents, and translates agent responses back to the customer's native language while maintaining domain glossary terminology and context.

---

## 📌 Project Purpose

Global customer support teams often face communication friction when assisting customers across different languages. **Multilingual Ticket Translator** streamlines ticket handling by:
- Eliminating language barriers with real-time bidirectional translation.
- Maintaining customer satisfaction with natural, native-language support replies.
- Empowering support agents with intelligent ticket categorization, priority assessment, and key information extraction.
- Ensuring consistent enterprise terminology using customizable translation glossaries.

---

## 🛠️ Technology Stack

### Frontend
- **Framework:** React.js (v18+)
- **Build Tool:** Vite
- **Language:** JavaScript (ES6+)
- **Styling:** Modern, Responsive CSS Design System
- **API Communication:** REST API Client (Axios / Fetch)

### Backend
- **Language:** Java 17+
- **Framework:** Spring Boot 3.x
- **Build & Dependency Management:** Maven
- **Architecture:** RESTful API Architecture
- **Data Access:** Spring Data JPA (Hibernate)

### Database
- **Engine:** MySQL 8.x
- **Management:** SQL Schema Migration Scripts

### Developer Tools & Ecosystem
- **Version Control:** Git & GitHub
- **API Testing:** Postman
- **Runtime:** Node.js (v18+), OpenJDK (17+)

---

## 🚀 Planned Application Features

- [ ] **Authentication & Authorization:** Role-based access control (Admin, Agent, Supervisor).
- [ ] **Agent Dashboard:** Real-time ticket queue, active conversation tracking, and KPI statistics.
- [ ] **Ticket Lifecycle Management:** Full CRUD lifecycle for tickets (Create, Assign, Update Status, Close).
- [ ] **Automatic Language Detection:** Seamless detection of customer query languages.
- [ ] **Customer-to-Agent Translation:** Instant translation of incoming customer messages to English.
- [ ] **Agent-to-Customer Reply Translation:** Accurate translation of agent responses back into the customer's language.
- [ ] **Message History Storage:** Persistent storage of both original and translated text for audit and quality assurance.
- [ ] **Domain Glossary Management:** Custom term banks to prevent mistranslation of brand and industry terms.
- [ ] **AI-Powered Category & Priority Detection:** Automated urgency and department routing.
- [ ] **Key Information Extraction:** Automatic extraction of order IDs, product names, error codes, and sentiments.
- [ ] **Advanced Search & Filtering:** Filter tickets by language, urgency, status, agent, and tags.
- [ ] **Reports & Analytics:** Agent performance metrics, language distribution charts, and SLA tracking.
- [ ] **Responsive Professional UI:** Modern, accessible, and themeable interface.

---

## 📁 Project Structure

```text
multilingual-ticket-translator/
│
├── backend/                  # Spring Boot REST API application
│   ├── src/
│   │   ├── main/java/        # Application source code
│   │   └── main/resources/   # Application configuration & static assets
│   ├── pom.xml               # Maven configuration
│   ├── mvnw                  # Maven wrapper script (Unix)
│   └── mvnw.cmd              # Maven wrapper script (Windows)
│
├── frontend/                 # React + Vite web application
│   ├── src/                  # React components, styles, and logic
│   ├── public/               # Static assets
│   ├── index.html            # Application entry HTML
│   ├── vite.config.js        # Vite build configuration
│   └── package.json          # Node dependencies & scripts
│
├── database/                 # Database schema scripts & migrations
│   ├── schema/               # DDL & SQL initialization scripts
│   └── README.md             # Database setup guide
│
├── docs/                     # Project documentation & architecture notes
│   ├── architecture.md       # High-level architecture documentation
│   └── roadmap.md            # Development roadmap
│
├── .gitignore                # Master git ignore configuration
└── README.md                 # Project documentation
```

---

## ⚙️ Prerequisites

Ensure the following tools are installed on your system:
- **Java Development Kit (JDK):** Version 17 or higher
- **Node.js:** Version 18.x or higher (along with `npm`)
- **MySQL Server:** Version 8.0+
- **Git:** Version 2.x+
- **Maven:** Version 3.8+ (or use the included Maven Wrapper `./mvnw` / `mvnw.cmd`)

---

## 🏃 Getting Started

### 1. Clone the Repository
```bash
git clone <repository-url>
cd "Multilingual ticket transalator"
```

### 2. Backend Setup (Spring Boot)
```bash
cd backend

# On Windows (using Maven Wrapper):
mvnw.cmd spring-boot:run

# Or with global Maven installed:
mvn spring-boot:run
```
The backend API server will start on `http://localhost:8080`.

### 3. Frontend Setup (React + Vite)
```bash
cd ../frontend

# Install dependencies:
npm install

# Start the Vite development server:
npm run dev
```
The frontend UI will be available at `http://localhost:5173`.

---

## 📄 License
This project is proprietary and intended for enterprise multilingual ticket translation workflows.
