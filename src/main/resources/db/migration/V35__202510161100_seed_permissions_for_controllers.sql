-- Seed permissions for application controllers

-- Ensure objects exist
INSERT INTO objects (id, name, description)
SELECT UUID_TO_BIN(UUID()), 'ANALYTICS', 'Analytics dashboards and reports'
WHERE NOT EXISTS (SELECT 1 FROM objects WHERE name = 'ANALYTICS');

INSERT INTO objects (id, name, description)
SELECT UUID_TO_BIN(UUID()), 'ANAMNESIS', 'Nutrition anamnesis management'
WHERE NOT EXISTS (SELECT 1 FROM objects WHERE name = 'ANAMNESIS');

INSERT INTO objects (id, name, description)
SELECT UUID_TO_BIN(UUID()), 'CLOUD_STORAGE', 'Cloudflare R2 storage operations'
WHERE NOT EXISTS (SELECT 1 FROM objects WHERE name = 'CLOUD_STORAGE');

INSERT INTO objects (id, name, description)
SELECT UUID_TO_BIN(UUID()), 'DIETS', 'Diet plan management'
WHERE NOT EXISTS (SELECT 1 FROM objects WHERE name = 'DIETS');

INSERT INTO objects (id, name, description)
SELECT UUID_TO_BIN(UUID()), 'EXERCISES', 'Exercise library management'
WHERE NOT EXISTS (SELECT 1 FROM objects WHERE name = 'EXERCISES');

INSERT INTO objects (id, name, description)
SELECT UUID_TO_BIN(UUID()), 'FOODS', 'Food catalog access'
WHERE NOT EXISTS (SELECT 1 FROM objects WHERE name = 'FOODS');

INSERT INTO objects (id, name, description)
SELECT UUID_TO_BIN(UUID()), 'GOOGLE_STORAGE', 'Google Cloud Storage operations'
WHERE NOT EXISTS (SELECT 1 FROM objects WHERE name = 'GOOGLE_STORAGE');

INSERT INTO objects (id, name, description)
SELECT UUID_TO_BIN(UUID()), 'AI_OPERATIONS', 'AI services integrations'
WHERE NOT EXISTS (SELECT 1 FROM objects WHERE name = 'AI_OPERATIONS');

INSERT INTO objects (id, name, description)
SELECT UUID_TO_BIN(UUID()), 'GOALS', 'Nutrition goal management'
WHERE NOT EXISTS (SELECT 1 FROM objects WHERE name = 'GOALS');

INSERT INTO objects (id, name, description)
SELECT UUID_TO_BIN(UUID()), 'GOAL_TEMPLATES', 'Nutrition goal templates'
WHERE NOT EXISTS (SELECT 1 FROM objects WHERE name = 'GOAL_TEMPLATES');

INSERT INTO objects (id, name, description)
SELECT UUID_TO_BIN(UUID()), 'OLLAMA', 'Ollama AI operations'
WHERE NOT EXISTS (SELECT 1 FROM objects WHERE name = 'OLLAMA');

INSERT INTO objects (id, name, description)
SELECT UUID_TO_BIN(UUID()), 'PAYMENTS', 'Payment processing and history'
WHERE NOT EXISTS (SELECT 1 FROM objects WHERE name = 'PAYMENTS');

INSERT INTO objects (id, name, description)
SELECT UUID_TO_BIN(UUID()), 'PAYMENT_METHODS', 'Payment methods management'
WHERE NOT EXISTS (SELECT 1 FROM objects WHERE name = 'PAYMENT_METHODS');

INSERT INTO objects (id, name, description)
SELECT UUID_TO_BIN(UUID()), 'PAYMENT_PLANS', 'Subscription plans catalog'
WHERE NOT EXISTS (SELECT 1 FROM objects WHERE name = 'PAYMENT_PLANS');

INSERT INTO objects (id, name, description)
SELECT UUID_TO_BIN(UUID()), 'PHOTO_EVOLUTION', 'Photo evolution tracking'
WHERE NOT EXISTS (SELECT 1 FROM objects WHERE name = 'PHOTO_EVOLUTION');

INSERT INTO objects (id, name, description)
SELECT UUID_TO_BIN(UUID()), 'REFERENCE_DATA', 'Reference lookup data'
WHERE NOT EXISTS (SELECT 1 FROM objects WHERE name = 'REFERENCE_DATA');

INSERT INTO objects (id, name, description)
SELECT UUID_TO_BIN(UUID()), 'REFERENCE_MANAGEMENT', 'Administrative reference data management'
WHERE NOT EXISTS (SELECT 1 FROM objects WHERE name = 'REFERENCE_MANAGEMENT');

INSERT INTO objects (id, name, description)
SELECT UUID_TO_BIN(UUID()), 'REMINDERS', 'Reminder scheduling'
WHERE NOT EXISTS (SELECT 1 FROM objects WHERE name = 'REMINDERS');

INSERT INTO objects (id, name, description)
SELECT UUID_TO_BIN(UUID()), 'UNITS', 'Units of measure catalog'
WHERE NOT EXISTS (SELECT 1 FROM objects WHERE name = 'UNITS');

INSERT INTO objects (id, name, description)
SELECT UUID_TO_BIN(UUID()), 'USER_SETTINGS', 'Per-user configuration'
WHERE NOT EXISTS (SELECT 1 FROM objects WHERE name = 'USER_SETTINGS');

INSERT INTO objects (id, name, description)
SELECT UUID_TO_BIN(UUID()), 'WHATSAPP_MANAGEMENT', 'WhatsApp nutrition management'
WHERE NOT EXISTS (SELECT 1 FROM objects WHERE name = 'WHATSAPP_MANAGEMENT');

-- Helper macro to insert permission
-- Analytics
INSERT INTO permissions (id, code, description, action_id, object_id)
SELECT UUID_TO_BIN(UUID()), 'ROLE_ANALYTICS_READ', 'View analytics dashboards', a.id, o.id
FROM actions a
         JOIN objects o ON o.name = 'ANALYTICS'
WHERE a.name = 'CONSULTAR'
  AND NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ROLE_ANALYTICS_READ');

-- Anamnesis CRUD
INSERT INTO permissions (id, code, description, action_id, object_id)
SELECT UUID_TO_BIN(UUID()), 'ROLE_ANAMNESIS_CREATE', 'Create anamnesis records', a.id, o.id
FROM actions a
         JOIN objects o ON o.name = 'ANAMNESIS'
WHERE a.name = 'INSERIR'
  AND NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ROLE_ANAMNESIS_CREATE');

INSERT INTO permissions (id, code, description, action_id, object_id)
SELECT UUID_TO_BIN(UUID()), 'ROLE_ANAMNESIS_READ', 'View anamnesis records', a.id, o.id
FROM actions a
         JOIN objects o ON o.name = 'ANAMNESIS'
WHERE a.name = 'CONSULTAR'
  AND NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ROLE_ANAMNESIS_READ');

INSERT INTO permissions (id, code, description, action_id, object_id)
SELECT UUID_TO_BIN(UUID()), 'ROLE_ANAMNESIS_UPDATE', 'Update anamnesis records', a.id, o.id
FROM actions a
         JOIN objects o ON o.name = 'ANAMNESIS'
WHERE a.name = 'ALTERAR'
  AND NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ROLE_ANAMNESIS_UPDATE');

INSERT INTO permissions (id, code, description, action_id, object_id)
SELECT UUID_TO_BIN(UUID()), 'ROLE_ANAMNESIS_DELETE', 'Delete anamnesis records', a.id, o.id
FROM actions a
         JOIN objects o ON o.name = 'ANAMNESIS'
WHERE a.name = 'EXCLUIR'
  AND NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ROLE_ANAMNESIS_DELETE');

-- Cloud storage
INSERT INTO permissions (id, code, description, action_id, object_id)
SELECT UUID_TO_BIN(UUID()), 'ROLE_CLOUD_STORAGE_CREATE', 'Upload files to Cloudflare storage', a.id, o.id
FROM actions a
         JOIN objects o ON o.name = 'CLOUD_STORAGE'
WHERE a.name = 'INSERIR'
  AND NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ROLE_CLOUD_STORAGE_CREATE');

INSERT INTO permissions (id, code, description, action_id, object_id)
SELECT UUID_TO_BIN(UUID()), 'ROLE_CLOUD_STORAGE_READ', 'Download or list Cloudflare files', a.id, o.id
FROM actions a
         JOIN objects o ON o.name = 'CLOUD_STORAGE'
WHERE a.name = 'CONSULTAR'
  AND NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ROLE_CLOUD_STORAGE_READ');

INSERT INTO permissions (id, code, description, action_id, object_id)
SELECT UUID_TO_BIN(UUID()), 'ROLE_CLOUD_STORAGE_DELETE', 'Delete Cloudflare files', a.id, o.id
FROM actions a
         JOIN objects o ON o.name = 'CLOUD_STORAGE'
WHERE a.name = 'EXCLUIR'
  AND NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ROLE_CLOUD_STORAGE_DELETE');

-- Diet plans
INSERT INTO permissions (id, code, description, action_id, object_id)
SELECT UUID_TO_BIN(UUID()), 'ROLE_DIETS_CREATE', 'Create diet plans', a.id, o.id
FROM actions a
         JOIN objects o ON o.name = 'DIETS'
WHERE a.name = 'INSERIR'
  AND NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ROLE_DIETS_CREATE');

INSERT INTO permissions (id, code, description, action_id, object_id)
SELECT UUID_TO_BIN(UUID()), 'ROLE_DIETS_READ', 'View diet plans', a.id, o.id
FROM actions a
         JOIN objects o ON o.name = 'DIETS'
WHERE a.name = 'CONSULTAR'
  AND NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ROLE_DIETS_READ');

INSERT INTO permissions (id, code, description, action_id, object_id)
SELECT UUID_TO_BIN(UUID()), 'ROLE_DIETS_UPDATE', 'Update diet plans', a.id, o.id
FROM actions a
         JOIN objects o ON o.name = 'DIETS'
WHERE a.name = 'ALTERAR'
  AND NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ROLE_DIETS_UPDATE');

INSERT INTO permissions (id, code, description, action_id, object_id)
SELECT UUID_TO_BIN(UUID()), 'ROLE_DIETS_DELETE', 'Delete diet plans', a.id, o.id
FROM actions a
         JOIN objects o ON o.name = 'DIETS'
WHERE a.name = 'EXCLUIR'
  AND NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ROLE_DIETS_DELETE');

-- Exercises
INSERT INTO permissions (id, code, description, action_id, object_id)
SELECT UUID_TO_BIN(UUID()), 'ROLE_EXERCISES_CREATE', 'Create exercises', a.id, o.id
FROM actions a
         JOIN objects o ON o.name = 'EXERCISES'
WHERE a.name = 'INSERIR'
  AND NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ROLE_EXERCISES_CREATE');

INSERT INTO permissions (id, code, description, action_id, object_id)
SELECT UUID_TO_BIN(UUID()), 'ROLE_EXERCISES_READ', 'View exercises', a.id, o.id
FROM actions a
         JOIN objects o ON o.name = 'EXERCISES'
WHERE a.name = 'CONSULTAR'
  AND NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ROLE_EXERCISES_READ');

INSERT INTO permissions (id, code, description, action_id, object_id)
SELECT UUID_TO_BIN(UUID()), 'ROLE_EXERCISES_UPDATE', 'Update exercises', a.id, o.id
FROM actions a
         JOIN objects o ON o.name = 'EXERCISES'
WHERE a.name = 'ALTERAR'
  AND NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ROLE_EXERCISES_UPDATE');

INSERT INTO permissions (id, code, description, action_id, object_id)
SELECT UUID_TO_BIN(UUID()), 'ROLE_EXERCISES_DELETE', 'Delete exercises', a.id, o.id
FROM actions a
         JOIN objects o ON o.name = 'EXERCISES'
WHERE a.name = 'EXCLUIR'
  AND NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ROLE_EXERCISES_DELETE');

-- Foods
INSERT INTO permissions (id, code, description, action_id, object_id)
SELECT UUID_TO_BIN(UUID()), 'ROLE_FOODS_READ', 'View food catalog', a.id, o.id
FROM actions a
         JOIN objects o ON o.name = 'FOODS'
WHERE a.name = 'CONSULTAR'
  AND NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ROLE_FOODS_READ');

-- Google storage
INSERT INTO permissions (id, code, description, action_id, object_id)
SELECT UUID_TO_BIN(UUID()), 'ROLE_GOOGLE_STORAGE_CREATE', 'Upload files to Google Storage', a.id, o.id
FROM actions a
         JOIN objects o ON o.name = 'GOOGLE_STORAGE'
WHERE a.name = 'INSERIR'
  AND NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ROLE_GOOGLE_STORAGE_CREATE');

INSERT INTO permissions (id, code, description, action_id, object_id)
SELECT UUID_TO_BIN(UUID()), 'ROLE_GOOGLE_STORAGE_READ', 'Download files from Google Storage', a.id, o.id
FROM actions a
         JOIN objects o ON o.name = 'GOOGLE_STORAGE'
WHERE a.name = 'CONSULTAR'
  AND NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ROLE_GOOGLE_STORAGE_READ');

-- AI operations
INSERT INTO permissions (id, code, description, action_id, object_id)
SELECT UUID_TO_BIN(UUID()), 'ROLE_AI_OPERATIONS_EXECUTE', 'Execute AI service operations', a.id, o.id
FROM actions a
         JOIN objects o ON o.name = 'AI_OPERATIONS'
WHERE a.name = 'APROVAR'
  AND NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ROLE_AI_OPERATIONS_EXECUTE');

-- Goals
INSERT INTO permissions (id, code, description, action_id, object_id)
SELECT UUID_TO_BIN(UUID()), 'ROLE_GOALS_CREATE', 'Create nutrition goals', a.id, o.id
FROM actions a
         JOIN objects o ON o.name = 'GOALS'
WHERE a.name = 'INSERIR'
  AND NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ROLE_GOALS_CREATE');

INSERT INTO permissions (id, code, description, action_id, object_id)
SELECT UUID_TO_BIN(UUID()), 'ROLE_GOALS_READ', 'View nutrition goals', a.id, o.id
FROM actions a
         JOIN objects o ON o.name = 'GOALS'
WHERE a.name = 'CONSULTAR'
  AND NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ROLE_GOALS_READ');

INSERT INTO permissions (id, code, description, action_id, object_id)
SELECT UUID_TO_BIN(UUID()), 'ROLE_GOALS_UPDATE', 'Update nutrition goals', a.id, o.id
FROM actions a
         JOIN objects o ON o.name = 'GOALS'
WHERE a.name = 'ALTERAR'
  AND NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ROLE_GOALS_UPDATE');

INSERT INTO permissions (id, code, description, action_id, object_id)
SELECT UUID_TO_BIN(UUID()), 'ROLE_GOALS_DELETE', 'Delete nutrition goals', a.id, o.id
FROM actions a
         JOIN objects o ON o.name = 'GOALS'
WHERE a.name = 'EXCLUIR'
  AND NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ROLE_GOALS_DELETE');

-- Goal templates
INSERT INTO permissions (id, code, description, action_id, object_id)
SELECT UUID_TO_BIN(UUID()), 'ROLE_GOAL_TEMPLATES_CREATE', 'Create goal templates', a.id, o.id
FROM actions a
         JOIN objects o ON o.name = 'GOAL_TEMPLATES'
WHERE a.name = 'INSERIR'
  AND NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ROLE_GOAL_TEMPLATES_CREATE');

INSERT INTO permissions (id, code, description, action_id, object_id)
SELECT UUID_TO_BIN(UUID()), 'ROLE_GOAL_TEMPLATES_READ', 'View goal templates', a.id, o.id
FROM actions a
         JOIN objects o ON o.name = 'GOAL_TEMPLATES'
WHERE a.name = 'CONSULTAR'
  AND NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ROLE_GOAL_TEMPLATES_READ');

INSERT INTO permissions (id, code, description, action_id, object_id)
SELECT UUID_TO_BIN(UUID()), 'ROLE_GOAL_TEMPLATES_UPDATE', 'Update goal templates', a.id, o.id
FROM actions a
         JOIN objects o ON o.name = 'GOAL_TEMPLATES'
WHERE a.name = 'ALTERAR'
  AND NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ROLE_GOAL_TEMPLATES_UPDATE');

INSERT INTO permissions (id, code, description, action_id, object_id)
SELECT UUID_TO_BIN(UUID()), 'ROLE_GOAL_TEMPLATES_DELETE', 'Delete goal templates', a.id, o.id
FROM actions a
         JOIN objects o ON o.name = 'GOAL_TEMPLATES'
WHERE a.name = 'EXCLUIR'
  AND NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ROLE_GOAL_TEMPLATES_DELETE');

-- Ollama operations
INSERT INTO permissions (id, code, description, action_id, object_id)
SELECT UUID_TO_BIN(UUID()), 'ROLE_OLLAMA_EXECUTE', 'Execute Ollama operations', a.id, o.id
FROM actions a
         JOIN objects o ON o.name = 'OLLAMA'
WHERE a.name = 'APROVAR'
  AND NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ROLE_OLLAMA_EXECUTE');

-- Payments
INSERT INTO permissions (id, code, description, action_id, object_id)
SELECT UUID_TO_BIN(UUID()), 'ROLE_PAYMENTS_CREATE', 'Create payment operations', a.id, o.id
FROM actions a
         JOIN objects o ON o.name = 'PAYMENTS'
WHERE a.name = 'INSERIR'
  AND NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ROLE_PAYMENTS_CREATE');

INSERT INTO permissions (id, code, description, action_id, object_id)
SELECT UUID_TO_BIN(UUID()), 'ROLE_PAYMENTS_READ', 'View payments', a.id, o.id
FROM actions a
         JOIN objects o ON o.name = 'PAYMENTS'
WHERE a.name = 'CONSULTAR'
  AND NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ROLE_PAYMENTS_READ');

INSERT INTO permissions (id, code, description, action_id, object_id)
SELECT UUID_TO_BIN(UUID()), 'ROLE_PAYMENTS_UPDATE', 'Update payments', a.id, o.id
FROM actions a
         JOIN objects o ON o.name = 'PAYMENTS'
WHERE a.name = 'ALTERAR'
  AND NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ROLE_PAYMENTS_UPDATE');

INSERT INTO permissions (id, code, description, action_id, object_id)
SELECT UUID_TO_BIN(UUID()), 'ROLE_PAYMENTS_DELETE', 'Cancel or delete payments', a.id, o.id
FROM actions a
         JOIN objects o ON o.name = 'PAYMENTS'
WHERE a.name = 'EXCLUIR'
  AND NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ROLE_PAYMENTS_DELETE');

-- Payment methods
INSERT INTO permissions (id, code, description, action_id, object_id)
SELECT UUID_TO_BIN(UUID()), 'ROLE_PAYMENT_METHODS_CREATE', 'Create payment methods', a.id, o.id
FROM actions a
         JOIN objects o ON o.name = 'PAYMENT_METHODS'
WHERE a.name = 'INSERIR'
  AND NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ROLE_PAYMENT_METHODS_CREATE');

INSERT INTO permissions (id, code, description, action_id, object_id)
SELECT UUID_TO_BIN(UUID()), 'ROLE_PAYMENT_METHODS_READ', 'View payment methods', a.id, o.id
FROM actions a
         JOIN objects o ON o.name = 'PAYMENT_METHODS'
WHERE a.name = 'CONSULTAR'
  AND NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ROLE_PAYMENT_METHODS_READ');

INSERT INTO permissions (id, code, description, action_id, object_id)
SELECT UUID_TO_BIN(UUID()), 'ROLE_PAYMENT_METHODS_DELETE', 'Delete payment methods', a.id, o.id
FROM actions a
         JOIN objects o ON o.name = 'PAYMENT_METHODS'
WHERE a.name = 'EXCLUIR'
  AND NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ROLE_PAYMENT_METHODS_DELETE');

-- Payment plans
INSERT INTO permissions (id, code, description, action_id, object_id)
SELECT UUID_TO_BIN(UUID()), 'ROLE_PAYMENT_PLANS_READ', 'View payment plans', a.id, o.id
FROM actions a
         JOIN objects o ON o.name = 'PAYMENT_PLANS'
WHERE a.name = 'CONSULTAR'
  AND NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ROLE_PAYMENT_PLANS_READ');

-- Photo evolution
INSERT INTO permissions (id, code, description, action_id, object_id)
SELECT UUID_TO_BIN(UUID()), 'ROLE_PHOTO_EVOLUTION_CREATE', 'Create photo evolution entries', a.id, o.id
FROM actions a
         JOIN objects o ON o.name = 'PHOTO_EVOLUTION'
WHERE a.name = 'INSERIR'
  AND NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ROLE_PHOTO_EVOLUTION_CREATE');

INSERT INTO permissions (id, code, description, action_id, object_id)
SELECT UUID_TO_BIN(UUID()), 'ROLE_PHOTO_EVOLUTION_READ', 'View photo evolution entries', a.id, o.id
FROM actions a
         JOIN objects o ON o.name = 'PHOTO_EVOLUTION'
WHERE a.name = 'CONSULTAR'
  AND NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ROLE_PHOTO_EVOLUTION_READ');

INSERT INTO permissions (id, code, description, action_id, object_id)
SELECT UUID_TO_BIN(UUID()), 'ROLE_PHOTO_EVOLUTION_UPDATE', 'Update photo evolution entries', a.id, o.id
FROM actions a
         JOIN objects o ON o.name = 'PHOTO_EVOLUTION'
WHERE a.name = 'ALTERAR'
  AND NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ROLE_PHOTO_EVOLUTION_UPDATE');

INSERT INTO permissions (id, code, description, action_id, object_id)
SELECT UUID_TO_BIN(UUID()), 'ROLE_PHOTO_EVOLUTION_DELETE', 'Delete photo evolution entries', a.id, o.id
FROM actions a
         JOIN objects o ON o.name = 'PHOTO_EVOLUTION'
WHERE a.name = 'EXCLUIR'
  AND NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ROLE_PHOTO_EVOLUTION_DELETE');

-- Reference data read
INSERT INTO permissions (id, code, description, action_id, object_id)
SELECT UUID_TO_BIN(UUID()), 'ROLE_REFERENCE_DATA_READ', 'Access reference lookup data', a.id, o.id
FROM actions a
         JOIN objects o ON o.name = 'REFERENCE_DATA'
WHERE a.name = 'CONSULTAR'
  AND NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ROLE_REFERENCE_DATA_READ');

-- Reference management CRUD
INSERT INTO permissions (id, code, description, action_id, object_id)
SELECT UUID_TO_BIN(UUID()), 'ROLE_REFERENCE_MANAGEMENT_READ', 'View administrative reference data', a.id, o.id
FROM actions a
         JOIN objects o ON o.name = 'REFERENCE_MANAGEMENT'
WHERE a.name = 'CONSULTAR'
  AND NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ROLE_REFERENCE_MANAGEMENT_READ');

INSERT INTO permissions (id, code, description, action_id, object_id)
SELECT UUID_TO_BIN(UUID()), 'ROLE_REFERENCE_MANAGEMENT_CREATE', 'Create administrative reference data', a.id, o.id
FROM actions a
         JOIN objects o ON o.name = 'REFERENCE_MANAGEMENT'
WHERE a.name = 'INSERIR'
  AND NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ROLE_REFERENCE_MANAGEMENT_CREATE');

INSERT INTO permissions (id, code, description, action_id, object_id)
SELECT UUID_TO_BIN(UUID()), 'ROLE_REFERENCE_MANAGEMENT_UPDATE', 'Update administrative reference data', a.id, o.id
FROM actions a
         JOIN objects o ON o.name = 'REFERENCE_MANAGEMENT'
WHERE a.name = 'ALTERAR'
  AND NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ROLE_REFERENCE_MANAGEMENT_UPDATE');

INSERT INTO permissions (id, code, description, action_id, object_id)
SELECT UUID_TO_BIN(UUID()), 'ROLE_REFERENCE_MANAGEMENT_DELETE', 'Delete administrative reference data', a.id, o.id
FROM actions a
         JOIN objects o ON o.name = 'REFERENCE_MANAGEMENT'
WHERE a.name = 'EXCLUIR'
  AND NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ROLE_REFERENCE_MANAGEMENT_DELETE');

-- Reminders
INSERT INTO permissions (id, code, description, action_id, object_id)
SELECT UUID_TO_BIN(UUID()), 'ROLE_REMINDERS_CREATE', 'Create reminders', a.id, o.id
FROM actions a
         JOIN objects o ON o.name = 'REMINDERS'
WHERE a.name = 'INSERIR'
  AND NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ROLE_REMINDERS_CREATE');

INSERT INTO permissions (id, code, description, action_id, object_id)
SELECT UUID_TO_BIN(UUID()), 'ROLE_REMINDERS_READ', 'View reminders', a.id, o.id
FROM actions a
         JOIN objects o ON o.name = 'REMINDERS'
WHERE a.name = 'CONSULTAR'
  AND NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ROLE_REMINDERS_READ');

INSERT INTO permissions (id, code, description, action_id, object_id)
SELECT UUID_TO_BIN(UUID()), 'ROLE_REMINDERS_UPDATE', 'Update reminders', a.id, o.id
FROM actions a
         JOIN objects o ON o.name = 'REMINDERS'
WHERE a.name = 'ALTERAR'
  AND NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ROLE_REMINDERS_UPDATE');

INSERT INTO permissions (id, code, description, action_id, object_id)
SELECT UUID_TO_BIN(UUID()), 'ROLE_REMINDERS_DELETE', 'Delete reminders', a.id, o.id
FROM actions a
         JOIN objects o ON o.name = 'REMINDERS'
WHERE a.name = 'EXCLUIR'
  AND NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ROLE_REMINDERS_DELETE');

-- Units of measure
INSERT INTO permissions (id, code, description, action_id, object_id)
SELECT UUID_TO_BIN(UUID()), 'ROLE_UNITS_READ', 'View measurement units', a.id, o.id
FROM actions a
         JOIN objects o ON o.name = 'UNITS'
WHERE a.name = 'CONSULTAR'
  AND NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ROLE_UNITS_READ');

-- User settings
INSERT INTO permissions (id, code, description, action_id, object_id)
SELECT UUID_TO_BIN(UUID()), 'ROLE_USER_SETTINGS_READ', 'View user settings', a.id, o.id
FROM actions a
         JOIN objects o ON o.name = 'USER_SETTINGS'
WHERE a.name = 'CONSULTAR'
  AND NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ROLE_USER_SETTINGS_READ');

INSERT INTO permissions (id, code, description, action_id, object_id)
SELECT UUID_TO_BIN(UUID()), 'ROLE_USER_SETTINGS_UPDATE', 'Update user settings', a.id, o.id
FROM actions a
         JOIN objects o ON o.name = 'USER_SETTINGS'
WHERE a.name = 'ALTERAR'
  AND NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ROLE_USER_SETTINGS_UPDATE');

-- WhatsApp management
INSERT INTO permissions (id, code, description, action_id, object_id)
SELECT UUID_TO_BIN(UUID()), 'ROLE_WHATSAPP_MANAGEMENT_CREATE', 'Create WhatsApp nutrition entries', a.id, o.id
FROM actions a
         JOIN objects o ON o.name = 'WHATSAPP_MANAGEMENT'
WHERE a.name = 'INSERIR'
  AND NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ROLE_WHATSAPP_MANAGEMENT_CREATE');

INSERT INTO permissions (id, code, description, action_id, object_id)
SELECT UUID_TO_BIN(UUID()), 'ROLE_WHATSAPP_MANAGEMENT_UPDATE', 'Update WhatsApp nutrition entries', a.id, o.id
FROM actions a
         JOIN objects o ON o.name = 'WHATSAPP_MANAGEMENT'
WHERE a.name = 'ALTERAR'
  AND NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ROLE_WHATSAPP_MANAGEMENT_UPDATE');

INSERT INTO permissions (id, code, description, action_id, object_id)
SELECT UUID_TO_BIN(UUID()), 'ROLE_WHATSAPP_MANAGEMENT_DELETE', 'Delete WhatsApp nutrition entries', a.id, o.id
FROM actions a
         JOIN objects o ON o.name = 'WHATSAPP_MANAGEMENT'
WHERE a.name = 'EXCLUIR'
  AND NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ROLE_WHATSAPP_MANAGEMENT_DELETE');

INSERT INTO permissions (id, code, description, action_id, object_id)
SELECT UUID_TO_BIN(UUID()), 'ROLE_WHATSAPP_MANAGEMENT_READ', 'View WhatsApp nutrition data', a.id, o.id
FROM actions a
         JOIN objects o ON o.name = 'WHATSAPP_MANAGEMENT'
WHERE a.name = 'CONSULTAR'
  AND NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ROLE_WHATSAPP_MANAGEMENT_READ');

-- Assign permissions to ADMIN role
INSERT INTO role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
         JOIN permissions p ON p.code IN (
    'ROLE_ANALYTICS_READ',
    'ROLE_ANAMNESIS_CREATE','ROLE_ANAMNESIS_READ','ROLE_ANAMNESIS_UPDATE','ROLE_ANAMNESIS_DELETE',
    'ROLE_CLOUD_STORAGE_CREATE','ROLE_CLOUD_STORAGE_READ','ROLE_CLOUD_STORAGE_DELETE',
    'ROLE_DIETS_CREATE','ROLE_DIETS_READ','ROLE_DIETS_UPDATE','ROLE_DIETS_DELETE',
    'ROLE_EXERCISES_CREATE','ROLE_EXERCISES_READ','ROLE_EXERCISES_UPDATE','ROLE_EXERCISES_DELETE',
    'ROLE_FOODS_READ',
    'ROLE_GOOGLE_STORAGE_CREATE','ROLE_GOOGLE_STORAGE_READ',
    'ROLE_AI_OPERATIONS_EXECUTE',
    'ROLE_GOALS_CREATE','ROLE_GOALS_READ','ROLE_GOALS_UPDATE','ROLE_GOALS_DELETE',
    'ROLE_GOAL_TEMPLATES_CREATE','ROLE_GOAL_TEMPLATES_READ','ROLE_GOAL_TEMPLATES_UPDATE','ROLE_GOAL_TEMPLATES_DELETE',
    'ROLE_OLLAMA_EXECUTE',
    'ROLE_PAYMENTS_CREATE','ROLE_PAYMENTS_READ','ROLE_PAYMENTS_UPDATE','ROLE_PAYMENTS_DELETE',
    'ROLE_PAYMENT_METHODS_CREATE','ROLE_PAYMENT_METHODS_READ','ROLE_PAYMENT_METHODS_DELETE',
    'ROLE_PAYMENT_PLANS_READ',
    'ROLE_PHOTO_EVOLUTION_CREATE','ROLE_PHOTO_EVOLUTION_READ','ROLE_PHOTO_EVOLUTION_UPDATE','ROLE_PHOTO_EVOLUTION_DELETE',
    'ROLE_REFERENCE_DATA_READ',
    'ROLE_REFERENCE_MANAGEMENT_READ','ROLE_REFERENCE_MANAGEMENT_CREATE','ROLE_REFERENCE_MANAGEMENT_UPDATE','ROLE_REFERENCE_MANAGEMENT_DELETE',
    'ROLE_REMINDERS_CREATE','ROLE_REMINDERS_READ','ROLE_REMINDERS_UPDATE','ROLE_REMINDERS_DELETE',
    'ROLE_UNITS_READ',
    'ROLE_USER_SETTINGS_READ','ROLE_USER_SETTINGS_UPDATE',
    'ROLE_WHATSAPP_MANAGEMENT_CREATE','ROLE_WHATSAPP_MANAGEMENT_UPDATE','ROLE_WHATSAPP_MANAGEMENT_DELETE','ROLE_WHATSAPP_MANAGEMENT_READ'
)
WHERE r.name = 'ADMIN'
  AND NOT EXISTS (
    SELECT 1
    FROM role_permission rp
    WHERE rp.role_id = r.id
      AND rp.permission_id = p.id
  );
