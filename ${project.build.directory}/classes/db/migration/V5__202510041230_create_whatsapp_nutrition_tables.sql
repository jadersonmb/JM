CREATE TABLE IF NOT EXISTS food_categories (
    id CHAR(36) PRIMARY KEY,
    name VARCHAR(120) NOT NULL UNIQUE,
    description VARCHAR(255),
    color VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS whatsapp_messages (
    id CHAR(36) PRIMARY KEY,
    whatsapp_message_id VARCHAR(120),
    from_phone VARCHAR(32),
    to_phone VARCHAR(32),
    message_type VARCHAR(32),
    text_content TEXT,
    media_id VARCHAR(120),
    media_url TEXT,
    mime_type VARCHAR(100),
    received_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS nutrition_analysis (
    id CHAR(36) PRIMARY KEY,
    message_id CHAR(36) NOT NULL,
    food_name VARCHAR(180),
    calories DECIMAL(10,2),
    protein_g DECIMAL(10,2),
    carbs_g DECIMAL(10,2),
    fat_g DECIMAL(10,2),
    summary TEXT,
    categories_json TEXT,
    confidence DECIMAL(5,2),
    primary_category_id CHAR(36),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_nutrition_message UNIQUE (message_id),
    CONSTRAINT fk_nutrition_message FOREIGN KEY (message_id) REFERENCES whatsapp_messages(id) ON DELETE CASCADE,
    CONSTRAINT fk_nutrition_category FOREIGN KEY (primary_category_id) REFERENCES food_categories(id)
);

CREATE INDEX IF NOT EXISTS idx_whatsapp_messages_received_at ON whatsapp_messages(received_at DESC);
CREATE INDEX IF NOT EXISTS idx_nutrition_analysis_created_at ON nutrition_analysis(created_at DESC);

