# Database Documentation

This directory contains SQL scripts, migrations, and schema definitions for the **Multilingual Ticket Translator** database.

## Database Engine
- **RDBMS:** MySQL 8.x
- **Default Database Name:** `ticket_translator_db`
- **Character Set:** `utf8mb4` (Full Unicode support for multilingual text, emojis, and special characters)
- **Collation:** `utf8mb4_unicode_ci`

## Directory Structure
- `schema/`: DDL scripts containing table definitions, indexes, and foreign keys.
- `seeds/`: Initial reference data (e.g., supported languages, default glossaries, test users).

## Planned Entities / Tables
1. **users**: Support agents, supervisors, and administrators.
2. **tickets**: Core support tickets including status, priority, category, detected language, and metadata.
3. **ticket_messages**: Customer and agent conversation messages with original and translated text.
4. **languages**: Supported source and target languages.
5. **glossaries**: Terminology and brand name translation dictionaries.
6. **audit_logs**: Operational history and translation analytics logs.
