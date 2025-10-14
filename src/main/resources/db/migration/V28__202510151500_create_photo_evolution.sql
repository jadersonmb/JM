CREATE TABLE IF NOT EXISTS photo_evolution (
    id CHAR(36) PRIMARY KEY,
    user_id CHAR(36) NOT NULL,
    image_id CHAR(36) NOT NULL,
    body_part VARCHAR(50) NOT NULL,
    captured_at DATE,
    weight DECIMAL(6,2),
    body_fat_percentage DECIMAL(5,2),
    muscle_mass DECIMAL(6,2),
    visceral_fat DECIMAL(5,2),
    waist_circumference DECIMAL(6,2),
    hip_circumference DECIMAL(6,2),
    chest_circumference DECIMAL(6,2),
    left_arm_circumference DECIMAL(6,2),
    right_arm_circumference DECIMAL(6,2),
    left_thigh_circumference DECIMAL(6,2),
    right_thigh_circumference DECIMAL(6,2),
    caloric_intake DECIMAL(7,2),
    protein_intake DECIMAL(6,2),
    carbohydrate_intake DECIMAL(6,2),
    fat_intake DECIMAL(6,2),
    notes TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL,
    CONSTRAINT FK_photo_evolution_user FOREIGN KEY (user_id) REFERENCES user_entity(id),
    CONSTRAINT FK_photo_evolution_image FOREIGN KEY (image_id) REFERENCES image(id)
);

CREATE INDEX IF NOT EXISTS IDX_photo_evolution_user ON photo_evolution(user_id);
CREATE INDEX IF NOT EXISTS IDX_photo_evolution_body_part ON photo_evolution(body_part);
CREATE INDEX IF NOT EXISTS IDX_photo_evolution_captured_at ON photo_evolution(captured_at);
