-- ==========================================================
-- Flyway Migration: Create Table ollama
-- Description: Stores prompts, responses and status of Ollama API requests
-- ==========================================================

CREATE TABLE IF NOT EXISTS ollama (
    id CHAR(36) NOT NULL,
    from_of VARCHAR(150) NOT NULL,
    user_id CHAR(36) NOT NULL,
    model VARCHAR(150) NOT NULL,
    prompt TEXT,
    response LONGTEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    error_message TEXT,
    started_at TIMESTAMP NULL DEFAULT NULL,
    finished_at TIMESTAMP NULL DEFAULT NULL,
    elapsed_ms BIGINT,
    requested_by CHAR(36) NULL,
    images LONGTEXT,
    CONSTRAINT pk_ollama PRIMARY KEY (id)
);

-- ==========================================================
-- Optional: Enum constraint simulation (for safety)
-- ==========================================================
ALTER TABLE ollama DROP CONSTRAINT IF EXISTS chk_ollama_status;

ALTER TABLE ollama
    ADD CONSTRAINT chk_ollama_status CHECK (status IN ('PENDING', 'PROCESSING', 'DONE', 'ERROR'));
    
-- ==========================================================
-- Indexes
-- ==========================================================
CREATE INDEX IF NOT EXISTS idx_ollama_status ON ollama (status);
CREATE INDEX IF NOT EXISTS idx_ollama_model ON ollama (model);
CREATE INDEX IF NOT EXISTS idx_ollama_requested_by ON ollama (requested_by);
CREATE INDEX IF NOT EXISTS idx_ollama_started_at ON ollama (started_at);
