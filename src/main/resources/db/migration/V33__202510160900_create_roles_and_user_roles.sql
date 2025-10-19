CREATE TABLE IF NOT EXISTS roles (
    id CHAR(36) PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id CHAR(36) NOT NULL,
    role_id CHAR(36) NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES user_entity(id),
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES roles(id)
);

INSERT INTO roles (id, name, description)
SELECT UUID(), 'ADMIN', 'System administrator'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ADMIN');

INSERT INTO roles (id, name, description)
SELECT UUID(), 'CLIENT', 'Default client role'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'CLIENT');
