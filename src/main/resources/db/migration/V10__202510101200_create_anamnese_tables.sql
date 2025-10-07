-- Add personal information columns to user_entity for anamnesis linkage
ALTER TABLE user_entity
    ADD COLUMN birth_date DATE,
    ADD COLUMN age INTEGER,
    ADD COLUMN education VARCHAR(150),
    ADD COLUMN occupation VARCHAR(150),
    ADD COLUMN consultation_goal VARCHAR(1000);

-- Create anamnese table
CREATE TABLE IF NOT EXISTS anamnese (
    id CHAR(36) PRIMARY KEY,
    mucosa VARCHAR(255),
    membros VARCHAR(255),
    cirurgias VARCHAR(255),
    avaliacao_observacoes VARCHAR(1000),
    colesterol BOOLEAN,
    hipertensao BOOLEAN,
    diabetes1 BOOLEAN,
    diabetes2 BOOLEAN,
    trigliceridemia BOOLEAN,
    anemia BOOLEAN,
    intestinal BOOLEAN,
    gastrica BOOLEAN,
    renal BOOLEAN,
    hepatica BOOLEAN,
    vesicular BOOLEAN,
    peso DECIMAL(10,2),
    estatura DECIMAL(10,2),
    imc DECIMAL(10,2),
    gordura_percent DECIMAL(5,2),
    musculo_percent DECIMAL(5,2),
    tmb DECIMAL(10,2),
    circun_abdomen DECIMAL(10,2),
    circun_cintura DECIMAL(10,2),
    circun_quadril DECIMAL(10,2),
    circun_braco DECIMAL(10,2),
    circun_joelho DECIMAL(10,2),
    circun_torax DECIMAL(10,2),
    preparo_alimentos VARCHAR(255),
    local_refeicao VARCHAR(255),
    horario_trabalho VARCHAR(255),
    horario_estudo VARCHAR(255),
    apetite VARCHAR(255),
    ingestao_hidrica VARCHAR(255),
    atividade_fisica VARCHAR(255),
    frequencia VARCHAR(255),
    duracao VARCHAR(255),
    fuma BOOLEAN,
    bebe BOOLEAN,
    suplementos VARCHAR(255),
    sono VARCHAR(255),
    mastigacao VARCHAR(255),
    habitos_observacoes VARCHAR(1000),
    alimentos_preferidos VARCHAR(500),
    alimentos_nao_gosta VARCHAR(500),
    diagnostico VARCHAR(500),
    resumo_dieta VARCHAR(4000),
    user_id CHAR(36),
    CONSTRAINT fk_anamnese_user FOREIGN KEY (user_id) REFERENCES user_entity(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_anamnese_user_id ON anamnese(user_id);

-- Create exame_bioquimico table
CREATE TABLE IF NOT EXISTS exame_bioquimico (
    id CHAR(36) PRIMARY KEY,
    nome_exame VARCHAR(255),
    valor VARCHAR(255),
    data_exame DATE,
    anamnese_id CHAR(36),
    CONSTRAINT fk_exame_anamnese FOREIGN KEY (anamnese_id) REFERENCES anamnese(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_exame_anamnese_id ON exame_bioquimico(anamnese_id);

-- Create refeicao_24h table
CREATE TABLE IF NOT EXISTS refeicao_24h (
    id CHAR(36) PRIMARY KEY,
    nome_refeicao VARCHAR(255),
    alimentos VARCHAR(1000),
    quantidades VARCHAR(1000),
    anamnese_id CHAR(36),
    CONSTRAINT fk_refeicao_anamnese FOREIGN KEY (anamnese_id) REFERENCES anamnese(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_refeicao_anamnese_id ON refeicao_24h(anamnese_id);
