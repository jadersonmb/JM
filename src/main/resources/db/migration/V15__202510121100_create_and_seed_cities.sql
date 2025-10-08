ALTER TABLE food_categories
    ADD COLUMN IF NOT EXISTS language VARCHAR(5) DEFAULT NULL AFTER color;

UPDATE food_categories SET language = 'en' WHERE language IS NULL;

CREATE TABLE IF NOT EXISTS cities (
    id CHAR(36) PRIMARY KEY,
    country_id CHAR(36) NOT NULL,
    state_code VARCHAR(10) NOT NULL,
    state_name VARCHAR(100) NOT NULL,
    city_code VARCHAR(20),
    name VARCHAR(100) NOT NULL,
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    population INT,
    timezone VARCHAR(50),
    is_capital BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    language VARCHAR(5) DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_cities_country FOREIGN KEY (country_id) REFERENCES countries(id)
);

CREATE INDEX IF NOT EXISTS idx_cities_country ON cities(country_id);
CREATE INDEX IF NOT EXISTS idx_cities_name ON cities(name);

ALTER TABLE user_entity
    ADD COLUMN IF NOT EXISTS city_id CHAR(36) NULL AFTER street;

ALTER TABLE user_entity
    ADD COLUMN IF NOT EXISTS education_level_id CHAR(36) NULL AFTER age;

ALTER TABLE user_entity
    ADD COLUMN IF NOT EXISTS profession_id CHAR(36) NULL AFTER education_level_id;



ALTER TABLE user_entity DROP CONSTRAINT fk_user_entity_city;
ALTER TABLE user_entity DROP CONSTRAINT fk_user_entity_education;
ALTER TABLE user_entity DROP CONSTRAINT fk_user_entity_profession;

ALTER TABLE user_entity
    ADD CONSTRAINT fk_user_entity_city FOREIGN KEY (city_id) REFERENCES cities(id);

ALTER TABLE user_entity
    ADD CONSTRAINT fk_user_entity_education FOREIGN KEY (education_level_id) REFERENCES education_levels(id);

ALTER TABLE user_entity
    ADD CONSTRAINT fk_user_entity_profession FOREIGN KEY (profession_id) REFERENCES professions(id);

CREATE INDEX IF NOT EXISTS idx_user_entity_city_id ON user_entity (city_id);
CREATE INDEX IF NOT EXISTS idx_user_entity_education_id ON user_entity (education_level_id);
CREATE INDEX IF NOT EXISTS idx_user_entity_profession_id ON user_entity (profession_id);

INSERT INTO cities (id, country_id, state_code, state_name, city_code, name, latitude, longitude, population, timezone,
                    is_capital, language)
VALUES
    (UUID(), (SELECT id FROM countries WHERE code = 'US' LIMIT 1), 'CA', 'California', 'US-LAX', 'Los Angeles',
     34.052235, -118.243683, 3898747, 'America/Los_Angeles', FALSE, 'en-US'),
    (UUID(), (SELECT id FROM countries WHERE code = 'US' LIMIT 1), 'NY', 'New York', 'US-NYC', 'New York',
     40.712776, -74.005974, 8804190, 'America/New_York', FALSE, 'en-US'),
    (UUID(), (SELECT id FROM countries WHERE code = 'US' LIMIT 1), 'DC', 'District of Columbia', 'US-WAS', 'Washington',
     38.907192, -77.036873, 689545, 'America/New_York', TRUE, 'en-US'),
    (UUID(), (SELECT id FROM countries WHERE code = 'BR' LIMIT 1), 'SP', 'São Paulo', 'BR-SAO', 'São Paulo',
     -23.550520, -46.633308, 12330000, 'America/Sao_Paulo', FALSE, 'pt-BR'),
    (UUID(), (SELECT id FROM countries WHERE code = 'BR' LIMIT 1), 'RJ', 'Rio de Janeiro', 'BR-RIO', 'Rio de Janeiro',
     -22.906847, -43.172897, 6718903, 'America/Sao_Paulo', FALSE, 'pt-BR'),
    (UUID(), (SELECT id FROM countries WHERE code = 'BR' LIMIT 1), 'DF', 'Distrito Federal', 'BR-BSB', 'Brasília',
     -15.780148, -47.929169, 3055149, 'America/Sao_Paulo', TRUE, 'pt-BR'),
    (UUID(), (SELECT id FROM countries WHERE code = 'PT' LIMIT 1), 'LX', 'Lisboa', 'PT-LIS', 'Lisboa',
     38.722252, -9.139337, 544851, 'Europe/Lisbon', TRUE, 'pt-PT'),
    (UUID(), (SELECT id FROM countries WHERE code = 'PT' LIMIT 1), 'PRT', 'Porto', 'PT-PRT', 'Porto',
     41.157944, -8.629105, 237559, 'Europe/Lisbon', FALSE, 'pt-PT'),
    (UUID(), (SELECT id FROM countries WHERE code = 'CA' LIMIT 1), 'ON', 'Ontario', 'CA-TOR', 'Toronto',
     43.653225, -79.383186, 2731571, 'America/Toronto', FALSE, 'en-US'),
    (UUID(), (SELECT id FROM countries WHERE code = 'CA' LIMIT 1), 'BC', 'British Columbia', 'CA-VAN', 'Vancouver',
     49.282729, -123.120738, 662248, 'America/Vancouver', FALSE, 'en-US');
