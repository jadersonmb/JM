ALTER TABLE user_entity ADD COLUMN first_access BOOLEAN DEFAULT TRUE;

UPDATE user_entity SET first_access = FALSE WHERE first_access IS NULL;
