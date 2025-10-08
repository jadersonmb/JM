-- Modificar a coluna country para referenciar a tabela countries
ALTER TABLE user_entity 
DROP COLUMN IF EXISTS country;

ALTER TABLE user_entity 
ADD COLUMN IF NOT EXISTS country_id CHAR(36) AFTER postal_code;

-- Adicionar a foreign key constraint
ALTER TABLE user_entity 
ADD CONSTRAINT fk_user_entity_country 
FOREIGN KEY (country_id) REFERENCES countries(id);

-- Criar índice para melhor performance
CREATE INDEX idx_user_entity_country_id ON user_entity (country_id);

-- Atualizar dados existentes (opcional - definir um país padrão)
UPDATE user_entity SET country_id = (SELECT id FROM countries WHERE code = 'BR') WHERE country_id IS NULL;