CREATE TABLE IF NOT EXISTS actions (
    id CHAR(36) PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS objects (
    id CHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS permissions (
    id CHAR(36) PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    action_id CHAR(36) NOT NULL,
    object_id CHAR(36) NOT NULL,
    CONSTRAINT fk_permission_action FOREIGN KEY (action_id) REFERENCES actions(id),
    CONSTRAINT fk_permission_object FOREIGN KEY (object_id) REFERENCES objects(id)
);

CREATE TABLE IF NOT EXISTS role_permission (
    role_id CHAR(36) NOT NULL,
    permission_id CHAR(36) NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_permission_role FOREIGN KEY (role_id) REFERENCES roles(id),
    CONSTRAINT fk_role_permission_permission FOREIGN KEY (permission_id) REFERENCES permissions(id)
);

INSERT INTO actions (id, name)
SELECT UUID(), 'INSERIR'
WHERE NOT EXISTS (SELECT 1 FROM actions WHERE name = 'INSERIR');

INSERT INTO actions (id, name)
SELECT UUID(), 'ALTERAR'
WHERE NOT EXISTS (SELECT 1 FROM actions WHERE name = 'ALTERAR');

INSERT INTO actions (id, name)
SELECT UUID(), 'CONSULTAR'
WHERE NOT EXISTS (SELECT 1 FROM actions WHERE name = 'CONSULTAR');

INSERT INTO actions (id, name)
SELECT UUID(), 'EXCLUIR'
WHERE NOT EXISTS (SELECT 1 FROM actions WHERE name = 'EXCLUIR');

INSERT INTO actions (id, name)
SELECT UUID(), 'FINALIZAR'
WHERE NOT EXISTS (SELECT 1 FROM actions WHERE name = 'FINALIZAR');

INSERT INTO actions (id, name)
SELECT UUID(), 'APROVAR'
WHERE NOT EXISTS (SELECT 1 FROM actions WHERE name = 'APROVAR');

INSERT INTO objects (id, name, description)
SELECT UUID(), 'USERS', 'User management screens'
WHERE NOT EXISTS (SELECT 1 FROM objects WHERE name = 'USERS');

INSERT INTO objects (id, name, description)
SELECT UUID(), 'ROLES', 'Role management screens'
WHERE NOT EXISTS (SELECT 1 FROM objects WHERE name = 'ROLES');

INSERT INTO permissions (id, code, description, action_id, object_id)
SELECT UUID(), 'ROLE_USERS_CREATE', 'Permission to create users', a.id, o.id
FROM actions a
         JOIN objects o ON o.name = 'USERS'
WHERE a.name = 'INSERIR'
  AND NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ROLE_USERS_CREATE');

INSERT INTO permissions (id, code, description, action_id, object_id)
SELECT UUID(), 'ROLE_USERS_READ', 'Permission to read users', a.id, o.id
FROM actions a
         JOIN objects o ON o.name = 'USERS'
WHERE a.name = 'CONSULTAR'
  AND NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ROLE_USERS_READ');

INSERT INTO permissions (id, code, description, action_id, object_id)
SELECT UUID(), 'ROLE_USERS_UPDATE', 'Permission to update users', a.id, o.id
FROM actions a
         JOIN objects o ON o.name = 'USERS'
WHERE a.name = 'ALTERAR'
  AND NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ROLE_USERS_UPDATE');

INSERT INTO permissions (id, code, description, action_id, object_id)
SELECT UUID(), 'ROLE_USERS_DELETE', 'Permission to delete users', a.id, o.id
FROM actions a
         JOIN objects o ON o.name = 'USERS'
WHERE a.name = 'EXCLUIR'
  AND NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ROLE_USERS_DELETE');

INSERT INTO permissions (id, code, description, action_id, object_id)
SELECT UUID(), 'ROLE_ADMIN_MANAGE_ROLES', 'Permission to manage user roles', a.id, o.id
FROM actions a
         JOIN objects o ON o.name = 'ROLES'
WHERE a.name = 'ALTERAR'
  AND NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ROLE_ADMIN_MANAGE_ROLES');

INSERT INTO role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
         JOIN permissions p ON p.code = 'ROLE_USERS_CREATE'
WHERE r.name = 'ADMIN'
  AND NOT EXISTS (SELECT 1 FROM role_permission WHERE role_id = r.id AND permission_id = p.id);

INSERT INTO role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
         JOIN permissions p ON p.code = 'ROLE_USERS_READ'
WHERE r.name IN ('ADMIN', 'CLIENT')
  AND NOT EXISTS (SELECT 1 FROM role_permission WHERE role_id = r.id AND permission_id = p.id);

INSERT INTO role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
         JOIN permissions p ON p.code = 'ROLE_USERS_UPDATE'
WHERE r.name = 'ADMIN'
  AND NOT EXISTS (SELECT 1 FROM role_permission WHERE role_id = r.id AND permission_id = p.id);

INSERT INTO role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
         JOIN permissions p ON p.code = 'ROLE_USERS_DELETE'
WHERE r.name = 'ADMIN'
  AND NOT EXISTS (SELECT 1 FROM role_permission WHERE role_id = r.id AND permission_id = p.id);

INSERT INTO role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
         JOIN permissions p ON p.code = 'ROLE_ADMIN_MANAGE_ROLES'
WHERE r.name = 'ADMIN'
  AND NOT EXISTS (SELECT 1 FROM role_permission WHERE role_id = r.id AND permission_id = p.id);
