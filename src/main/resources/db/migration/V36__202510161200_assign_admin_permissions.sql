INSERT INTO user_roles (user_id, role_id)
SELECT 'c0a80177-99a9-1187-8199-a945929c0000', r.id
FROM roles r
WHERE r.name = 'ADMIN'
  AND NOT EXISTS (
    SELECT 1 FROM user_roles ur WHERE ur.user_id = 'c0a80177-99a9-1187-8199-a945929c0000' AND ur.role_id = r.id
  );

INSERT INTO role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'ADMIN'
  AND NOT EXISTS (
    SELECT 1
    FROM role_permission rp
    WHERE rp.role_id = r.id
      AND rp.permission_id = p.id
  );
