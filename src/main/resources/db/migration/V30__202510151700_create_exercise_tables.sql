CREATE TABLE IF NOT EXISTS exercise_reference (
    id CHAR(36) PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(500),
    muscle_group VARCHAR(120),
    equipment VARCHAR(120),
    language VARCHAR(5),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS exercises (
    id CHAR(36) PRIMARY KEY,
    reference_id CHAR(36),
    user_id CHAR(36),
    custom_name VARCHAR(150),
    duration_minutes INT,
    intensity VARCHAR(20),
    calories_burned INT,
    equipment VARCHAR(120),
    muscle_group VARCHAR(120),
    notes VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_exercises_reference FOREIGN KEY (reference_id) REFERENCES exercise_reference(id),
    CONSTRAINT fk_exercises_user FOREIGN KEY (user_id) REFERENCES user_entity(id)
);

INSERT INTO exercise_reference (id, code, name, description, muscle_group, equipment, language)
VALUES
    ('11111111-1111-1111-1111-111111111111', 'PUSH_UP', 'Push-up', 'Bodyweight push exercise for upper body strength.', 'Chest', 'Bodyweight', 'en-US'),
    ('22222222-2222-2222-2222-222222222222', 'SQUAT', 'Squat', 'Compound lower body strength movement.', 'Legs', 'Bodyweight', 'en-US'),
    ('33333333-3333-3333-3333-333333333333', 'PLANK', 'Plank', 'Core stability exercise focusing on endurance.', 'Core', 'Bodyweight', 'en-US'),
    ('44444444-4444-4444-4444-444444444444', 'BURPEE', 'Burpee', 'Full body conditioning exercise combining squat, plank and jump.', 'Full body', 'Bodyweight', 'en-US'),
    ('55555555-5555-5555-5555-555555555555', 'JUMP_ROPE', 'Jump rope', 'Cardio exercise using a rope to improve endurance.', 'Full body', 'Jump rope', 'en-US');
