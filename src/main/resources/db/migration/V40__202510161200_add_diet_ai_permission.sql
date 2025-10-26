-- Add permission to generate diets via AI and assign to default roles
INSERT INTO permissions (id, code, description, action_id, object_id)
SELECT UUID(), 'ROLE_DIETS_GENERATE_AI', 'Generate diet plans using AI suggestions', a.id, o.id
FROM actions a
         JOIN objects o ON o.name = 'DIETS'
WHERE a.name = 'APROVAR'
  AND NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ROLE_DIETS_GENERATE_AI');

INSERT INTO role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
         JOIN permissions p ON p.code = 'ROLE_DIETS_GENERATE_AI'
WHERE r.name = 'ADMIN'
  AND NOT EXISTS (
    SELECT 1
    FROM role_permission rp
    WHERE rp.role_id = r.id
      AND rp.permission_id = p.id
  );

INSERT INTO role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
         JOIN permissions p ON p.code = 'ROLE_DIETS_GENERATE_AI'
WHERE r.name = 'CLIENT'
  AND NOT EXISTS (
    SELECT 1
    FROM role_permission rp
    WHERE rp.role_id = r.id
      AND rp.permission_id = p.id
  );
