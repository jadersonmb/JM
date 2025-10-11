-- Rename core anamnesis table and columns to English equivalents
ALTER TABLE anamnese RENAME TO anamneses;

ALTER TABLE anamneses
    CHANGE COLUMN membros limbs VARCHAR(255),
    CHANGE COLUMN cirurgias surgical_history VARCHAR(255),
    CHANGE COLUMN avaliacao_observacoes clinical_notes VARCHAR(1000),
    CHANGE COLUMN peso weight_kg DECIMAL(10,2),
    CHANGE COLUMN estatura height_cm DECIMAL(10,2),
    CHANGE COLUMN imc body_mass_index DECIMAL(10,2),
    CHANGE COLUMN gordura_percent body_fat_percentage DECIMAL(5,2),
    CHANGE COLUMN musculo_percent muscle_mass_percentage DECIMAL(5,2),
    CHANGE COLUMN tmb basal_metabolic_rate DECIMAL(10,2),
    CHANGE COLUMN circun_abdomen abdominal_circumference DECIMAL(10,2),
    CHANGE COLUMN circun_cintura waist_circumference DECIMAL(10,2),
    CHANGE COLUMN circun_quadril hip_circumference DECIMAL(10,2),
    CHANGE COLUMN circun_braco arm_circumference DECIMAL(10,2),
    CHANGE COLUMN circun_joelho knee_circumference DECIMAL(10,2),
    CHANGE COLUMN circun_torax thorax_circumference DECIMAL(10,2),
    CHANGE COLUMN preparo_alimentos meal_preparation VARCHAR(255),
    CHANGE COLUMN local_refeicao meal_location VARCHAR(255),
    CHANGE COLUMN horario_trabalho work_schedule VARCHAR(255),
    CHANGE COLUMN horario_estudo study_schedule VARCHAR(255),
    CHANGE COLUMN ingestao_hidrica water_intake VARCHAR(255),
    CHANGE COLUMN atividade_fisica physical_activity VARCHAR(255),
    CHANGE COLUMN frequencia activity_frequency VARCHAR(255),
    CHANGE COLUMN duracao activity_duration VARCHAR(255),
    CHANGE COLUMN fuma smokes TINYINT(1),
    CHANGE COLUMN bebe drinks_alcohol TINYINT(1),
    CHANGE COLUMN suplementos supplements VARCHAR(255),
    CHANGE COLUMN sono sleep_quality VARCHAR(255),
    CHANGE COLUMN mastigacao chewing_quality VARCHAR(255),
    CHANGE COLUMN habitos_observacoes habit_notes VARCHAR(2000),
    CHANGE COLUMN diagnostico diagnosis VARCHAR(500),
    CHANGE COLUMN resumo_dieta diet_summary VARCHAR(4000);

ALTER TABLE anamneses
    DROP COLUMN colesterol,
    DROP COLUMN hipertensao,
    DROP COLUMN diabetes1,
    DROP COLUMN diabetes2,
    DROP COLUMN trigliceridemia,
    DROP COLUMN anemia,
    DROP COLUMN intestinal,
    DROP COLUMN gastrica,
    DROP COLUMN renal,
    DROP COLUMN hepatica,
    DROP COLUMN vesicular;

ALTER TABLE anamneses
    DROP INDEX idx_anamnese_user_id,
    ADD INDEX idx_anamneses_user_id (user_id);

-- Join table for anamnesis <-> pathologies relationship
CREATE TABLE IF NOT EXISTS anamnesis_pathologies (
    anamnesis_id CHAR(36) NOT NULL,
    pathology_id CHAR(36) NOT NULL,
    PRIMARY KEY (anamnesis_id, pathology_id),
    CONSTRAINT fk_anamnesis_pathology_anamnesis FOREIGN KEY (anamnesis_id) REFERENCES anamneses(id) ON DELETE CASCADE,
    CONSTRAINT fk_anamnesis_pathology_pathology FOREIGN KEY (pathology_id) REFERENCES pathologies(id) ON DELETE CASCADE
);

-- Rename biochemical results table and align columns
ALTER TABLE exame_bioquimico RENAME TO anamnesis_biochemical_results;

ALTER TABLE anamnesis_biochemical_results
    DROP FOREIGN KEY fk_exame_anamnese,
    CHANGE COLUMN anamnese_id anamnesis_id CHAR(36),
    ADD COLUMN biochemical_exam_id CHAR(36) NULL AFTER anamnesis_id,
    CHANGE COLUMN valor result_value VARCHAR(255),
    CHANGE COLUMN data_exame result_date DATE;

UPDATE anamnesis_biochemical_results abr
LEFT JOIN biochemical_exams be ON LOWER(TRIM(be.name)) = LOWER(TRIM(abr.nome_exame))
SET abr.biochemical_exam_id = be.id
WHERE abr.biochemical_exam_id IS NULL;

ALTER TABLE anamnesis_biochemical_results
    DROP COLUMN nome_exame,
    DROP INDEX idx_exame_anamnese_id,
    ADD INDEX idx_anamnesis_biochemical_result_anamnesis (anamnesis_id),
    ADD CONSTRAINT fk_anamnesis_biochemical_result_anamnesis FOREIGN KEY (anamnesis_id) REFERENCES anamneses(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_anamnesis_biochemical_result_exam FOREIGN KEY (biochemical_exam_id) REFERENCES biochemical_exams(id) ON DELETE SET NULL;

-- Rename food recall table and preserve legacy notes
ALTER TABLE refeicao_24h RENAME TO anamnesis_food_recalls;

ALTER TABLE anamnesis_food_recalls
    DROP FOREIGN KEY fk_refeicao_anamnese,
    CHANGE COLUMN anamnese_id anamnesis_id CHAR(36),
    CHANGE COLUMN nome_refeicao meal_name VARCHAR(255),
    ADD COLUMN observation TEXT NULL;

UPDATE anamnesis_food_recalls
SET observation = TRIM(CONCAT_WS(' | ', NULLIF(alimentos, ''), NULLIF(quantidades, '')))
WHERE (alimentos IS NOT NULL AND TRIM(alimentos) <> '') OR (quantidades IS NOT NULL AND TRIM(quantidades) <> '');

ALTER TABLE anamnesis_food_recalls
    DROP COLUMN alimentos,
    DROP COLUMN quantidades,
    DROP INDEX idx_refeicao_anamnese_id,
    ADD INDEX idx_anamnesis_food_recall_anamnesis (anamnesis_id),
    ADD CONSTRAINT fk_anamnesis_food_recall_anamnesis FOREIGN KEY (anamnesis_id) REFERENCES anamneses(id) ON DELETE CASCADE;

CREATE TABLE IF NOT EXISTS anamnesis_food_recall_items (
    id CHAR(36) PRIMARY KEY,
    food_recall_id CHAR(36) NOT NULL,
    food_id CHAR(36) NULL,
    measurement_unit_id CHAR(36) NULL,
    quantity DECIMAL(10,2),
    CONSTRAINT fk_food_recall_item_recall FOREIGN KEY (food_recall_id) REFERENCES anamnesis_food_recalls(id) ON DELETE CASCADE,
    CONSTRAINT fk_food_recall_item_food FOREIGN KEY (food_id) REFERENCES foods(id),
    CONSTRAINT fk_food_recall_item_unit FOREIGN KEY (measurement_unit_id) REFERENCES measurement_units(id)
);

CREATE INDEX idx_food_recall_item_recall ON anamnesis_food_recall_items(food_recall_id);

-- Food preferences table with legacy migration
CREATE TABLE IF NOT EXISTS anamnesis_food_preferences (
    id CHAR(36) PRIMARY KEY,
    anamnesis_id CHAR(36) NOT NULL,
    food_id CHAR(36) NULL,
    preference_type VARCHAR(20) NOT NULL,
    notes VARCHAR(1000),
    CONSTRAINT fk_anamnesis_food_preference_anamnesis FOREIGN KEY (anamnesis_id) REFERENCES anamneses(id) ON DELETE CASCADE,
    CONSTRAINT fk_anamnesis_food_preference_food FOREIGN KEY (food_id) REFERENCES foods(id)
);

INSERT INTO anamnesis_food_preferences (id, anamnesis_id, food_id, preference_type, notes)
SELECT UUID(), id, NULL, 'PREFERRED', alimentos_preferidos
FROM anamneses
WHERE alimentos_preferidos IS NOT NULL AND TRIM(alimentos_preferidos) <> '';

INSERT INTO anamnesis_food_preferences (id, anamnesis_id, food_id, preference_type, notes)
SELECT UUID(), id, NULL, 'DISLIKED', alimentos_nao_gosta
FROM anamneses
WHERE alimentos_nao_gosta IS NOT NULL AND TRIM(alimentos_nao_gosta) <> '';

ALTER TABLE anamneses
    DROP COLUMN alimentos_preferidos,
    DROP COLUMN alimentos_nao_gosta;
