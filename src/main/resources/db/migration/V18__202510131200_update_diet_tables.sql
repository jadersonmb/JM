ALTER TABLE diet_meal_item DROP FOREIGN KEY fk_diet_meal_item_food;
ALTER TABLE diet_meal_item DROP FOREIGN KEY fk_diet_meal_item_unit;

DROP INDEX idx_diet_meal_item_food ON diet_meal_item;
DROP INDEX idx_diet_meal_item_unit ON diet_meal_item;

ALTER TABLE diet_meal_item CHANGE COLUMN food_item_id food_id CHAR(36) NOT NULL;
ALTER TABLE diet_meal_item CHANGE COLUMN unit_id measurement_unit_id CHAR(36) NOT NULL;

-- Attempt to align existing references with the new master data
UPDATE diet_meal_item d
JOIN food_item fi ON d.food_id = fi.id
JOIN foods f ON LOWER(TRIM(f.name)) = LOWER(TRIM(fi.name))
SET d.food_id = f.id;

UPDATE diet_meal_item d
JOIN unit_of_measure u ON d.measurement_unit_id = u.id
LEFT JOIN measurement_units mu_desc ON LOWER(TRIM(mu_desc.description)) = LOWER(TRIM(u.display_name))
LEFT JOIN measurement_units mu_code ON LOWER(TRIM(mu_code.code)) = LOWER(TRIM(u.code))
LEFT JOIN measurement_units mu_symbol ON LOWER(TRIM(mu_symbol.symbol)) = LOWER(TRIM(u.display_name))
SET d.measurement_unit_id = COALESCE(mu_desc.id, mu_code.id, mu_symbol.id)
WHERE COALESCE(mu_desc.id, mu_code.id, mu_symbol.id) IS NOT NULL;

-- Fallback to safe defaults when a matching reference was not found
UPDATE diet_meal_item d
LEFT JOIN foods f ON d.food_id = f.id
SET d.food_id = COALESCE((SELECT id FROM foods ORDER BY name LIMIT 1), d.food_id)
WHERE f.id IS NULL;

UPDATE diet_meal_item d
LEFT JOIN measurement_units mu ON d.measurement_unit_id = mu.id
SET d.measurement_unit_id = COALESCE((
    SELECT id FROM measurement_units WHERE code = 'UNIT' LIMIT 1
), d.measurement_unit_id)
WHERE mu.id IS NULL;

ALTER TABLE diet_meal_item
    ADD CONSTRAINT fk_diet_meal_item_food FOREIGN KEY (food_id) REFERENCES foods(id),
    ADD CONSTRAINT fk_diet_meal_item_unit FOREIGN KEY (measurement_unit_id) REFERENCES measurement_units(id);

CREATE INDEX idx_diet_meal_item_food ON diet_meal_item(food_id);
CREATE INDEX idx_diet_meal_item_unit ON diet_meal_item(measurement_unit_id);

DROP TABLE IF EXISTS food_item;
DROP TABLE IF EXISTS unit_of_measure;
