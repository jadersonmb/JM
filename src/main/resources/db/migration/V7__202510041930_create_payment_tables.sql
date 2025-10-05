CREATE TABLE IF NOT EXISTS payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    payment_id VARCHAR(100) UNIQUE,
    customer_id CHAR(36) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'BRL',
    payment_method ENUM('CREDIT_CARD', 'DEBIT_CARD', 'PIX', 'BANK_SLIP') NOT NULL,
    payment_status ENUM('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'REFUNDED') NOT NULL DEFAULT 'PENDING',
    description VARCHAR(255),
    metadata JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_payments_customer FOREIGN KEY (customer_id) REFERENCES user_entity (id)
);

CREATE INDEX idx_payments_customer_id ON payments (customer_id);
CREATE INDEX idx_payments_status ON payments (payment_status);
CREATE INDEX idx_payments_method ON payments (payment_method);

CREATE TABLE IF NOT EXISTS payment_cards (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_id CHAR(36) NOT NULL,
    card_token VARCHAR(255) NOT NULL,
    last_four VARCHAR(4) NOT NULL,
    brand VARCHAR(50),
    expiry_month INT NOT NULL,
    expiry_year INT NOT NULL,
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payment_cards_customer FOREIGN KEY (customer_id) REFERENCES user_entity (id)
);

CREATE INDEX idx_payment_cards_customer_id ON payment_cards (customer_id);
CREATE INDEX idx_payment_cards_default ON payment_cards (customer_id, is_default DESC);

CREATE TABLE IF NOT EXISTS recurring_payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_id CHAR(36) NOT NULL,
    plan_id VARCHAR(100) NOT NULL,
    gateway_subscription_id VARCHAR(100),
    payment_method_id BIGINT,
    status ENUM('ACTIVE', 'PAUSED', 'CANCELLED', 'EXPIRED') NOT NULL DEFAULT 'ACTIVE',
    amount DECIMAL(10,2) NOT NULL,
    collection_interval ENUM('DAILY', 'WEEKLY', 'MONTHLY', 'YEARLY') NOT NULL,
    next_billing_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_recurring_payments_customer FOREIGN KEY (customer_id) REFERENCES user_entity (id),
    CONSTRAINT fk_recurring_payment_method FOREIGN KEY (payment_method_id) REFERENCES payment_cards (id)
);

CREATE INDEX idx_recurring_payments_customer ON recurring_payments (customer_id);
CREATE INDEX idx_recurring_payments_status ON recurring_payments (status);

CREATE TABLE IF NOT EXISTS payment_webhooks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    event_type VARCHAR(100) NOT NULL,
    payload JSON NOT NULL,
    processed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_payment_webhooks_processed ON payment_webhooks (processed);


