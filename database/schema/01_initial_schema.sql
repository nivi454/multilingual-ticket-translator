-- ====================================================================
-- Multilingual Ticket Translator - Initial Schema Placeholder
-- Database: MySQL 8.x
-- Character Set: utf8mb4 (Required for full multilingual support)
-- ====================================================================

-- Create database if not exists
CREATE DATABASE IF NOT EXISTS ticket_translator_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE ticket_translator_db;

-- Table definitions will be implemented during the Database Setup stage.
-- Planned Tables:
-- 1. users (id, username, email, password_hash, role, preferred_language, created_at, updated_at)
-- 2. tickets (id, ticket_number, customer_name, customer_email, original_language, category, priority, status, assigned_agent_id, created_at, updated_at)
-- 3. ticket_messages (id, ticket_id, sender_type, sender_id, original_text, translated_text, source_language, target_language, created_at)
-- 4. glossaries (id, term, preferred_translation, source_language, target_language, category, created_at)
-- 5. audit_logs (id, ticket_id, action, details, performed_by, created_at)
