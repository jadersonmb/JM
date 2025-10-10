CREATE TABLE IF NOT EXISTS nutrition_goal_template (
    id CHAR(36) PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(255),
    goal_type VARCHAR(40) NOT NULL,
    target_value DECIMAL(12,2) NOT NULL,
    unit_id CHAR(36) NOT NULL,
    periodicity VARCHAR(20) NOT NULL,
    custom_period_days INT,
    target_mode VARCHAR(20) NOT NULL,
    notes VARCHAR(500),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_goal_template_unit FOREIGN KEY (unit_id) REFERENCES measurement_units(id)
);

CREATE INDEX IF NOT EXISTS idx_goal_template_type ON nutrition_goal_template(goal_type);
CREATE INDEX IF NOT EXISTS idx_goal_template_periodicity ON nutrition_goal_template(periodicity);
CREATE INDEX IF NOT EXISTS idx_goal_template_active ON nutrition_goal_template(is_active);

CREATE TABLE IF NOT EXISTS nutrition_goal (
    id CHAR(36) PRIMARY KEY,
    goal_type VARCHAR(40) NOT NULL,
    target_value DECIMAL(12,2) NOT NULL,
    unit_id CHAR(36) NOT NULL,
    periodicity VARCHAR(20) NOT NULL,
    custom_period_days INT,
    target_mode VARCHAR(20) NOT NULL,
    start_date DATE,
    end_date DATE,
    created_by_user_id CHAR(36) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    notes VARCHAR(1000),
    template_id CHAR(36),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_nutrition_goal_unit FOREIGN KEY (unit_id) REFERENCES measurement_units(id),
    CONSTRAINT fk_nutrition_goal_user FOREIGN KEY (created_by_user_id) REFERENCES user_entity(id),
    CONSTRAINT fk_nutrition_goal_template FOREIGN KEY (template_id) REFERENCES nutrition_goal_template(id)
);

CREATE INDEX IF NOT EXISTS idx_nutrition_goal_type ON nutrition_goal(goal_type);
CREATE INDEX IF NOT EXISTS idx_nutrition_goal_periodicity ON nutrition_goal(periodicity);
CREATE INDEX IF NOT EXISTS idx_nutrition_goal_active ON nutrition_goal(is_active);
CREATE INDEX IF NOT EXISTS idx_nutrition_goal_owner ON nutrition_goal(created_by_user_id);

INSERT INTO nutrition_goal_template (id, name, description, goal_type, target_value, unit_id, periodicity, custom_period_days, target_mode, notes, is_active)
SELECT UUID(), 'Standard Protein', 'Daily protein goal of 1.6 g/kg', 'PROTEIN', 1.60, mu.id, 'DAILY', NULL, 'PER_KG', 'Adjust based on training volume.', TRUE
FROM measurement_units mu
WHERE mu.code = 'GRAM'
  AND NOT EXISTS (SELECT 1 FROM nutrition_goal_template WHERE name = 'Standard Protein')
LIMIT 1;

INSERT INTO nutrition_goal_template (id, name, description, goal_type, target_value, unit_id, periodicity, custom_period_days, target_mode, notes, is_active)
SELECT UUID(), 'Standard Water', 'Hydration goal of 35 ml/kg', 'WATER', 35.00, mu.id, 'DAILY', NULL, 'PER_KG', 'Increase on warm days.', TRUE
FROM measurement_units mu
WHERE mu.code IN ('ML', 'MILLILITER')
  AND NOT EXISTS (SELECT 1 FROM nutrition_goal_template WHERE name = 'Standard Water')
LIMIT 1;

INSERT INTO nutrition_goal_template (id, name, description, goal_type, target_value, unit_id, periodicity, custom_period_days, target_mode, notes, is_active)
SELECT UUID(), 'Balanced Carbohydrates', '50% of total calories from carbs', 'CARBOHYDRATE', 50.00, mu.id, 'DAILY', NULL, 'ABSOLUTE', 'Use as guidance for active clients.', TRUE
FROM measurement_units mu
WHERE mu.code IN ('PERCENT', 'PCT')
  AND NOT EXISTS (SELECT 1 FROM nutrition_goal_template WHERE name = 'Balanced Carbohydrates')
LIMIT 1;

INSERT INTO nutrition_goal_template (id, name, description, goal_type, target_value, unit_id, periodicity, custom_period_days, target_mode, notes, is_active)
SELECT UUID(), 'Healthy Fats', 'Target 1 g/kg of healthy fats weekly', 'FAT', 1.00, mu.id, 'WEEKLY', NULL, 'PER_KG', 'Spread across meals with good sources.', TRUE
FROM measurement_units mu
WHERE mu.code = 'GRAM'
  AND NOT EXISTS (SELECT 1 FROM nutrition_goal_template WHERE name = 'Healthy Fats')
LIMIT 1;
