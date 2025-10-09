CREATE TABLE IF NOT EXISTS unit_of_measure (
    id CHAR(36) PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE,
    display_name VARCHAR(120) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS food_item (
    id CHAR(36) PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS diet_plan (
    id CHAR(36) PRIMARY KEY,
    created_by_user_id CHAR(36) NOT NULL,
    patient_name VARCHAR(150),
    notes TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_diet_plan_user FOREIGN KEY (created_by_user_id) REFERENCES user_entity(id)
);

CREATE INDEX IF NOT EXISTS idx_diet_plan_user ON diet_plan(created_by_user_id);
CREATE INDEX IF NOT EXISTS idx_diet_plan_active ON diet_plan(is_active);

CREATE TABLE IF NOT EXISTS diet_meal (
    id CHAR(36) PRIMARY KEY,
    diet_plan_id CHAR(36) NOT NULL,
    meal_type VARCHAR(40) NOT NULL,
    scheduled_time TIME,
    CONSTRAINT fk_diet_meal_plan FOREIGN KEY (diet_plan_id) REFERENCES diet_plan(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_diet_meal_plan ON diet_meal(diet_plan_id);
CREATE INDEX IF NOT EXISTS idx_diet_meal_type ON diet_meal(meal_type);

CREATE TABLE IF NOT EXISTS diet_meal_item (
    id CHAR(36) PRIMARY KEY,
    diet_meal_id CHAR(36) NOT NULL,
    food_item_id CHAR(36) NOT NULL,
    unit_id CHAR(36) NOT NULL,
    quantity DECIMAL(10,2) NOT NULL,
    notes VARCHAR(255),
    CONSTRAINT fk_diet_meal_item_meal FOREIGN KEY (diet_meal_id) REFERENCES diet_meal(id) ON DELETE CASCADE,
    CONSTRAINT fk_diet_meal_item_food FOREIGN KEY (food_item_id) REFERENCES food_item(id),
    CONSTRAINT fk_diet_meal_item_unit FOREIGN KEY (unit_id) REFERENCES unit_of_measure(id)
);

CREATE INDEX IF NOT EXISTS idx_diet_meal_item_meal ON diet_meal_item(diet_meal_id);
CREATE INDEX IF NOT EXISTS idx_diet_meal_item_food ON diet_meal_item(food_item_id);
CREATE INDEX IF NOT EXISTS idx_diet_meal_item_unit ON diet_meal_item(unit_id);

INSERT IGNORE INTO unit_of_measure (id, code, display_name)
VALUES
    (UUID(), 'GRAM', 'Gram'),
    (UUID(), 'ML', 'Milliliter'),
    (UUID(), 'SLICE', 'Slice'),
    (UUID(), 'UNIT', 'Unit'),
    (UUID(), 'CUP', 'Cup'),
    (UUID(), 'TBSP', 'Tablespoon');

INSERT IGNORE INTO food_item (id, name)
VALUES
    (UUID(), 'Whole-wheat bread'),
    (UUID(), 'Egg'),
    (UUID(), 'Chicken breast'),
    (UUID(), 'Pasta'),
    (UUID(), 'Banana'),
    (UUID(), 'Whey protein'),
    (UUID(), 'Peanut butter'),
    (UUID(), 'Oats'),
    (UUID(), 'Green salad'),
    (UUID(), 'Beans');
