-- Countries table
CREATE TABLE IF NOT EXISTS countries (
    id CHAR(36) PRIMARY KEY,
    code VARCHAR(2) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    nationality VARCHAR(100) NOT NULL,
    language VARCHAR(20) DEFAULT 'en',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Education levels table
CREATE TABLE IF NOT EXISTS education_levels (
    id CHAR(36) PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    language VARCHAR(20) DEFAULT 'en',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Professions table
CREATE TABLE IF NOT EXISTS professions (
    id CHAR(36) PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    language VARCHAR(20) DEFAULT 'en',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);


-- Measurement units table
CREATE TABLE IF NOT EXISTS measurement_units (
    id CHAR(36) PRIMARY KEY,
    code VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(50) NOT NULL,
    symbol VARCHAR(10) NOT NULL,
    unit_type VARCHAR(20) NOT NULL,
    language VARCHAR(20) DEFAULT 'en', -- WEIGHT, VOLUME, LENGTH, etc
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);


-- Foods table
CREATE TABLE IF NOT EXISTS foods (
    id CHAR(36) PRIMARY KEY,
    code VARCHAR(100) UNIQUE NOT NULL,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(255),
    food_category_id CHAR(36) NOT NULL,
    average_calories DECIMAL(8,2),
    average_protein DECIMAL(8,2),
    average_carbs DECIMAL(8,2),
    average_fat DECIMAL(8,2),
    common_portion DECIMAL(8,2),
    common_portion_unit_id CHAR(36),
    is_active BOOLEAN DEFAULT TRUE,
    language VARCHAR(20) DEFAULT 'en',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (food_category_id) REFERENCES food_categories(id),
    FOREIGN KEY (common_portion_unit_id) REFERENCES measurement_units(id)
);

-- Meals table
CREATE TABLE IF NOT EXISTS meals (
    id CHAR(36) PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    typical_time TIME,
    sort_order INT DEFAULT 0,
    language VARCHAR(20) DEFAULT 'en',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Pathologies table
CREATE TABLE IF NOT EXISTS pathologies (
    id CHAR(36) PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    category VARCHAR(50), -- METABOLIC, CARDIOVASCULAR, etc
    is_chronic BOOLEAN DEFAULT FALSE,
    requires_monitoring BOOLEAN DEFAULT FALSE,
    language VARCHAR(20) DEFAULT 'en',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Biochemical exams table
CREATE TABLE IF NOT EXISTS biochemical_exams (
    id CHAR(36) PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    measurement_unit_id CHAR(36) NOT NULL,
    min_reference_value DECIMAL(10,2),
    max_reference_value DECIMAL(10,2),
    is_active BOOLEAN DEFAULT TRUE,
    language VARCHAR(20) DEFAULT 'en-US',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (measurement_unit_id) REFERENCES measurement_units(id)
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_foods_category ON foods(food_category_id);
CREATE INDEX IF NOT EXISTS idx_foods_active ON foods(is_active);
CREATE INDEX IF NOT EXISTS idx_pathologies_category ON pathologies(category);
CREATE INDEX IF NOT EXISTS idx_exams_active ON biochemical_exams(is_active);