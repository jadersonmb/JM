CREATE TABLE IF NOT EXISTS diet_plan_ai_jobs (
    id CHAR(36) PRIMARY KEY,
    diet_plan_id CHAR(36),
    requested_by_user_id CHAR(36),
    status VARCHAR(30) NOT NULL,
    error_message VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_diet_ai_jobs_status ON diet_plan_ai_jobs (status);
CREATE INDEX IF NOT EXISTS idx_diet_ai_jobs_diet_plan ON diet_plan_ai_jobs (diet_plan_id);
