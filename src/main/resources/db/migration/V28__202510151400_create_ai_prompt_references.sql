CREATE TABLE IF NOT EXISTS ai_prompt_references (
    id UUID PRIMARY KEY,
    code VARCHAR(64) NOT NULL,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(512),
    provider VARCHAR(40) NOT NULL,
    model VARCHAR(120) NOT NULL,
    owner VARCHAR(120),
    prompt TEXT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_ai_prompt_reference_key
    ON ai_prompt_references(code, provider, model, owner);
