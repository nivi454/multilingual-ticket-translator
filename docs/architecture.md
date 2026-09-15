# System Architecture Overview

The **Multilingual Ticket Translator** follows a modern, decoupled client-server architecture.

```
+-------------------------------------------------------------+
|                      React + Vite UI                        |
|   - Agent Dashboard                                         |
|   - Real-time Multilingual Chat / Ticket Viewer             |
|   - Glossary & Settings Management                          |
+------------------------------+------------------------------+
                               | (REST / JSON / HTTPS)
                               v
+-------------------------------------------------------------+
|                     Spring Boot Backend                     |
|   +-----------------------------------------------------+   |
|   | REST Controllers (Ticket, Message, Glossary, Auth)  |   |
|   +-----------------------------------------------------+   |
|   | Service Layer (Translation Service, AI Analytics)   |   |
|   +-----------------------------------------------------+   |
|   | Data Access Layer (Spring Data JPA / Hibernate)     |   |
|   +-----------------------------------------------------+   |
+------------------------------+------------------------------+
                               |
                               v
+-------------------------------------------------------------+
|                      MySQL 8.x Database                     |
|   - Tickets, Messages, Users, Glossaries, Audit Logs        |
+-------------------------------------------------------------+
```

## Component Breakdown

1. **Frontend (React.js + Vite)**
   - Single Page Application (SPA) providing an agent-centric workspace.
   - Clean state management for ticket filtering, live messaging, and language preferences.
   - Responsive layouts optimized for desktop support agents and mobile supervisors.

2. **Backend (Spring Boot 3.x + Java 17+)**
   - RESTful API controllers exposing endpoints for ticket operations, message processing, and configuration.
   - Decoupled translation service interface allowing seamless integration with translation providers and glossaries.
   - JPA entities with relational mapping and validation constraints.

3. **Database (MySQL 8.x)**
   - UTF-8 MB4 encoding to safely store scripts in Latin, Cyrillic, CJK, Arabic, Devanagari, and other global writing systems.
