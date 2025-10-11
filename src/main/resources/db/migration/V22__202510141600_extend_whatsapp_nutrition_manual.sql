ALTER TABLE whatsapp_messages
    ADD COLUMN IF NOT EXISTS owner_user_id CHAR(36),
    ADD COLUMN IF NOT EXISTS manual_entry BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX IF NOT EXISTS idx_whatsapp_messages_owner ON whatsapp_messages(owner_user_id);
CREATE INDEX IF NOT EXISTS idx_whatsapp_messages_manual_entry ON whatsapp_messages(manual_entry);

ALTER TABLE whatsapp_messages DROP CONSTRAINT fk_whatsapp_messages_owner;
ALTER TABLE whatsapp_messages
    ADD CONSTRAINT fk_whatsapp_messages_owner
    FOREIGN KEY (owner_user_id) REFERENCES user_entity(id);

ALTER TABLE nutrition_analysis
    ADD COLUMN IF NOT EXISTS calories_unit_id CHAR(36),
    ADD COLUMN IF NOT EXISTS protein_unit_id CHAR(36),
    ADD COLUMN IF NOT EXISTS carbs_unit_id CHAR(36),
    ADD COLUMN IF NOT EXISTS fat_unit_id CHAR(36);

ALTER TABLE nutrition_analysis DROP CONSTRAINT fk_nutrition_analysis_calories_unit;
ALTER TABLE nutrition_analysis DROP CONSTRAINT fk_nutrition_analysis_protein_unit;
ALTER TABLE nutrition_analysis DROP CONSTRAINT fk_nutrition_analysis_carbs_unit;
ALTER TABLE nutrition_analysis DROP CONSTRAINT fk_nutrition_analysis_fat_unit;

ALTER TABLE nutrition_analysis
    ADD CONSTRAINT fk_nutrition_analysis_calories_unit FOREIGN KEY (calories_unit_id) REFERENCES measurement_units(id);

ALTER TABLE nutrition_analysis
    ADD CONSTRAINT fk_nutrition_analysis_protein_unit FOREIGN KEY (protein_unit_id) REFERENCES measurement_units(id);

ALTER TABLE nutrition_analysis
    ADD CONSTRAINT fk_nutrition_analysis_carbs_unit FOREIGN KEY (carbs_unit_id) REFERENCES measurement_units(id);

ALTER TABLE nutrition_analysis
    ADD CONSTRAINT fk_nutrition_analysis_fat_unit FOREIGN KEY (fat_unit_id) REFERENCES measurement_units(id);

INSERT INTO measurement_units (id, code, description, symbol, unit_type)
SELECT UUID(), 'KCAL', 'Kilocalorie', 'kcal', 'ENERGY'
WHERE NOT EXISTS (SELECT 1 FROM measurement_units WHERE code = 'KCAL');

INSERT INTO measurement_units (id, code, description, symbol, unit_type)
SELECT UUID(), 'KJ', 'Kilojoule', 'kJ', 'ENERGY'
WHERE NOT EXISTS (SELECT 1 FROM measurement_units WHERE code = 'KJ');

UPDATE nutrition_analysis
SET calories_unit_id = (SELECT id FROM measurement_units WHERE code = 'KCAL' LIMIT 1)
WHERE calories IS NOT NULL AND calories_unit_id IS NULL;

UPDATE nutrition_analysis
SET protein_unit_id = (SELECT id FROM measurement_units WHERE code = 'G' LIMIT 1)
WHERE protein_g IS NOT NULL AND protein_unit_id IS NULL;

UPDATE nutrition_analysis
SET carbs_unit_id = (SELECT id FROM measurement_units WHERE code = 'G' LIMIT 1)
WHERE carbs_g IS NOT NULL AND carbs_unit_id IS NULL;

UPDATE nutrition_analysis
SET fat_unit_id = (SELECT id FROM measurement_units WHERE code = 'G' LIMIT 1)
WHERE fat_g IS NOT NULL AND fat_unit_id IS NULL;
