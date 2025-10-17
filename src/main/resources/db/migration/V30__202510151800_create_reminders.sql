CREATE TABLE IF NOT EXISTS reminder (
    id CHAR(36) PRIMARY KEY,
    title VARCHAR(120) NOT NULL,
    description VARCHAR(1000),
    scheduled_at TIMESTAMP NOT NULL,
    priority VARCHAR(20) NOT NULL,
    type VARCHAR(20) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    is_completed BOOLEAN DEFAULT FALSE,
    dispatched_at TIMESTAMP NULL,
    completed_at TIMESTAMP NULL,
    target_user_id CHAR(36) NOT NULL,
    created_by_user_id CHAR(36) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_reminder_target_user FOREIGN KEY (target_user_id) REFERENCES user_entity(id),
    CONSTRAINT fk_reminder_created_by FOREIGN KEY (created_by_user_id) REFERENCES user_entity(id)
);

CREATE INDEX IF NOT EXISTS idx_reminder_scheduled_at ON reminder(scheduled_at);
CREATE INDEX IF NOT EXISTS idx_reminder_active ON reminder(is_active);
CREATE INDEX IF NOT EXISTS idx_reminder_target_user ON reminder(target_user_id);
