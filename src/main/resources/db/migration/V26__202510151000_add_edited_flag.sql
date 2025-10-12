ALTER TABLE whatsapp_messages
    ADD COLUMN IF NOT EXISTS edited_entry BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE whatsapp_messages
SET edited_entry = FALSE
WHERE edited_entry IS NULL;
