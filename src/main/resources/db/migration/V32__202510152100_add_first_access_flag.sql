ALTER TABLE user_entity ADD COLUMN IF NOT EXISTS first_access BOOLEAN DEFAULT TRUE;

UPDATE user_entity SET first_access = FALSE WHERE first_access IS NULL;

ALTER TABLE user_entity ADD COLUMN IF NOT EXISTS locale VARCHAR(10) DEFAULT 'en_US';

UPDATE user_entity SET locale = 'en_US' WHERE locale IS NULL;

ALTER TABLE user_entity ADD COLUMN IF NOT EXISTS password_recovery_token VARCHAR(200) DEFAULT NULL;
ALTER TABLE user_entity ADD COLUMN IF NOT EXISTS password_recovery_token_expires_at VARCHAR(200) DEFAULT NULL;
