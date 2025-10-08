CREATE TABLE IF NOT EXISTS user_configuration (
    id CHAR(36) PRIMARY KEY,
    user_id CHAR(36) NOT NULL UNIQUE,
    dark_mode BOOLEAN NOT NULL DEFAULT FALSE,
    email_notifications BOOLEAN NOT NULL DEFAULT TRUE,
    whatsapp_notifications BOOLEAN NOT NULL DEFAULT FALSE,
    push_notifications BOOLEAN NOT NULL DEFAULT FALSE,
    security_alerts BOOLEAN NOT NULL DEFAULT TRUE,
    product_updates BOOLEAN NOT NULL DEFAULT TRUE,
    language VARCHAR(20) DEFAULT 'pt-BR',
    timezone VARCHAR(60) DEFAULT 'America/Sao_Paulo',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_configuration_user FOREIGN KEY (user_id) REFERENCES user_entity(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_user_configuration_user ON user_configuration(user_id);
