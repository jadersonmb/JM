CREATE TABLE IF NOT EXISTS payment_plans (
    id CHAR(36) PRIMARY KEY,
    code VARCHAR(100) UNIQUE NOT NULL,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(255),
    amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'BRL',
    intervals ENUM('DAILY','WEEKLY','MONTHLY','QUARTERLY','SEMI_ANNUAL','YEARLY') NOT NULL,
    stripe_price_id VARCHAR(120),
    asaas_plan_id VARCHAR(120),
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT INTO payment_plans (id, code, name, description, amount, intervals)
VALUES
    (UUID(),'plan_monthly', 'Monthly Plan', 'Recurring monthly billing', 99.90, 'MONTHLY'),
    (UUID(), 'plan_quarterly', 'Quarterly Plan', 'Recurring billing every three months', 279.00, 'QUARTERLY'),
    (UUID(), 'plan_semi_annual', 'Semi-Annual Plan', 'Recurring billing every six months', 539.00, 'SEMI_ANNUAL'),
    (UUID(), 'plan_annual', 'Annual Plan', 'Recurring billing once a year', 999.00, 'YEARLY')
ON DUPLICATE KEY UPDATE id = VALUES(id), code = VALUES(code), name = VALUES(name), description = VALUES(description), amount = VALUES(amount), intervals = VALUES(intervals), active = TRUE;

ALTER TABLE payments_recurring
    MODIFY COLUMN collection_interval ENUM('DAILY','WEEKLY','MONTHLY','QUARTERLY','SEMI_ANNUAL','YEARLY') NOT NULL;

ALTER TABLE payments_recurring
    ADD COLUMN payment_plan_id CHAR(36) AFTER plan_id;

ALTER TABLE payments_recurring
    ADD CONSTRAINT fk_recurring_payment_plan FOREIGN KEY (payment_plan_id) REFERENCES payment_plans(id);
