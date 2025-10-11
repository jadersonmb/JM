ALTER TABLE nutrition_analysis
    ADD COLUMN meal_id CHAR(36),
    ADD COLUMN food_id CHAR(36),
    ADD COLUMN liquid_volume DECIMAL(12, 2),
    ADD COLUMN liquid_unit_id CHAR(36);

ALTER TABLE nutrition_analysis
    ADD CONSTRAINT fk_nutrition_analysis_meal FOREIGN KEY (meal_id) REFERENCES meals(id);

ALTER TABLE nutrition_analysis
    ADD CONSTRAINT fk_nutrition_analysis_food FOREIGN KEY (food_id) REFERENCES foods(id);

ALTER TABLE nutrition_analysis
    ADD CONSTRAINT fk_nutrition_analysis_liquid_unit FOREIGN KEY (liquid_unit_id) REFERENCES measurement_units(id);

CREATE INDEX IF NOT EXISTS idx_nutrition_analysis_meal ON nutrition_analysis(meal_id);
CREATE INDEX IF NOT EXISTS idx_nutrition_analysis_food ON nutrition_analysis(food_id);
CREATE INDEX IF NOT EXISTS idx_nutrition_analysis_liquid_unit ON nutrition_analysis(liquid_unit_id);

UPDATE measurement_units
SET conversion_factor = 1, base_unit = TRUE, unit_type = 'VOLUME'
WHERE code = 'ML';

UPDATE measurement_units
SET conversion_factor = 1000, base_unit = FALSE, unit_type = 'VOLUME'
WHERE code = 'L';

UPDATE measurement_units
SET conversion_factor = 240, base_unit = FALSE, unit_type = 'VOLUME'
WHERE code = 'CUP';

UPDATE measurement_units
SET conversion_factor = 15, base_unit = FALSE, unit_type = 'VOLUME'
WHERE code = 'TBSP';

UPDATE measurement_units
SET conversion_factor = 5, base_unit = FALSE, unit_type = 'VOLUME'
WHERE code = 'TSP';
