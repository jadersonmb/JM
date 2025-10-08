ALTER TABLE measurement_units
    ADD COLUMN IF NOT EXISTS conversion_factor DECIMAL(10,2) DEFAULT NULL AFTER unit_type;


ALTER TABLE measurement_units
    ADD COLUMN IF NOT EXISTS base_unit BOOLEAN DEFAULT FALSE AFTER conversion_factor;

ALTER TABLE measurement_units
    ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE AFTER base_unit;

ALTER TABLE measurement_units RENAME COLUMN name TO description;