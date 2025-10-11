ALTER TABLE anamneses
    ADD COLUMN IF NOT EXISTS appetite VARCHAR(255) NULL AFTER study_schedule;