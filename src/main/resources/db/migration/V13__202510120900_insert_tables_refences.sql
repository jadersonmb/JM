-- Insert countries
INSERT IGNORE INTO countries (id, code, name, nationality) VALUES
(UUID(), 'US', 'United States', 'American'),
(UUID(), 'BR', 'Brazil', 'Brazilian'),
(UUID(), 'GB', 'United Kingdom', 'British'),
(UUID(), 'CA', 'Canada', 'Canadian'),
(UUID(), 'AU', 'Australia', 'Australian'),
(UUID(), 'PT', 'Portugal', 'Portuguese'),
(UUID(), 'ES', 'Spain', 'Spanish'),
(UUID(), 'FR', 'France', 'French'),
(UUID(), 'DE', 'Germany', 'German'),
(UUID(), 'IT', 'Italy', 'Italian');

-- Insert education levels
INSERT IGNORE INTO education_levels (id, code, name, description) VALUES
(UUID(), 'ELEMENTARY', 'Elementary School', 'Basic education up to 5th grade'),
(UUID(), 'MIDDLE', 'Middle School', 'Education from 6th to 9th grade'),
(UUID(), 'HIGH', 'High School', 'Secondary education'),
(UUID(), 'TECHNICAL', 'Technical Education', 'Professional technical training'),
(UUID(), 'BACHELOR', 'Bachelor Degree', 'University bachelor degree'),
(UUID(), 'MASTER', 'Master Degree', 'Postgraduate master degree'),
(UUID(), 'PHD', 'Doctorate', 'PhD or doctorate degree'),
(UUID(), 'POST_DOC', 'Postdoctoral', 'Postdoctoral studies');

-- Insert professions
INSERT IGNORE INTO professions (id, code, name, description) VALUES
(UUID(), 'STUDENT', 'Student', 'Currently studying'),
(UUID(), 'TEACHER', 'Teacher', 'Education professional'),
(UUID(), 'ENGINEER', 'Engineer', 'Engineering professional'),
(UUID(), 'DOCTOR', 'Doctor', 'Medical doctor'),
(UUID(), 'NURSE', 'Nurse', 'Nursing professional'),
(UUID(), 'LAWYER', 'Lawyer', 'Legal professional'),
(UUID(), 'ACCOUNTANT', 'Accountant', 'Accounting professional'),
(UUID(), 'DEVELOPER', 'Software Developer', 'IT and software development'),
(UUID(), 'DESIGNER', 'Designer', 'Design professional'),
(UUID(), 'MANAGER', 'Manager', 'Management position'),
(UUID(), 'SALES', 'Sales Representative', 'Sales professional'),
(UUID(), 'CONSULTANT', 'Consultant', 'Consulting services'),
(UUID(), 'ENTREPRENEUR', 'Entrepreneur', 'Business owner'),
(UUID(), 'RETIRED', 'Retired', 'Retired professional'),
(UUID(),'UNEMPLOYED', 'Unemployed', 'Currently seeking employment');

-- Insert measurement units
INSERT IGNORE INTO measurement_units (id, code, name, symbol, unit_type) VALUES
-- Weight units
(UUID(), 'G', 'Gram', 'g', 'WEIGHT'),
(UUID(), 'KG', 'Kilogram', 'kg', 'WEIGHT'),
(UUID(), 'MG', 'Milligram', 'mg', 'WEIGHT'),
(UUID(), 'OZ', 'Ounce', 'oz', 'WEIGHT'),
(UUID(), 'LB', 'Pound', 'lb', 'WEIGHT'),
-- Volume units
(UUID(),'ML', 'Milliliter', 'ml', 'VOLUME'),
(UUID(), 'L', 'Liter', 'l', 'VOLUME'),
(UUID(), 'CUP', 'Cup', 'cup', 'VOLUME'),
(UUID(), 'TSP', 'Teaspoon', 'tsp', 'VOLUME'),
(UUID(), 'TBSP', 'Tablespoon', 'tbsp', 'VOLUME'),
-- Length units
(UUID(),'CM', 'Centimeter', 'cm', 'LENGTH'),
(UUID(), 'M', 'Meter', 'm', 'LENGTH'),
(UUID(), 'IN', 'Inch', 'in', 'LENGTH'),
-- Other units
(UUID(), 'UNIT', 'Unit', 'unit', 'COUNT'),
(UUID(), 'SLICE', 'Slice', 'slice', 'COUNT'),
(UUID(), 'PIECE', 'Piece', 'pc', 'COUNT');

-- Insert food categories
INSERT IGNORE INTO food_categories (id, name, description, color) VALUES
(UUID(), 'Fruits', 'Fresh and dried fruits', '#FF6B6B'),
(UUID(), 'Vegetables', 'Leafy greens and vegetables', '#51CF66'),
(UUID(), 'Dairy', 'Milk, cheese and dairy products', '#339AF0'),
(UUID(), 'Grains', 'Cereals and grains', '#FCC419'),
(UUID(), 'Proteins', 'Meat, fish and protein sources', '#FF8787'),
(UUID(), 'Legumes', 'Beans, lentils and legumes', '#E599F7'),
(UUID(), 'Nuts & Seeds', 'Nuts and seeds', '#FFA94D'),
(UUID(), 'Fats & Oils', 'Oils and fats', '#FFD43B'),
(UUID(), 'Beverages', 'Drinks and beverages', '#74C0FC'),
(UUID(), 'Sweets', 'Sweets and desserts', '#B197FC');

-- Insert foods
INSERT IGNORE INTO foods (id, code, name, description, food_category_id, average_calories, average_protein, average_carbs, average_fat, common_portion, common_portion_unit_id) VALUES
-- Fruits
(UUID(), 'APPLE', 'Apple', 'Fresh apple', (SELECT id FROM food_categories WHERE name = 'Fruits'), 52, 0.3, 14, 0.2, 150, (SELECT id FROM measurement_units WHERE code = 'G')),
(UUID(), 'BANANA', 'Banana', 'Medium banana', (SELECT id FROM food_categories WHERE name = 'Fruits'), 89, 1.1, 23, 0.3, 120, (SELECT id FROM measurement_units WHERE code = 'G')),
(UUID(), 'ORANGE', 'Orange', 'Fresh orange', (SELECT id FROM food_categories WHERE name = 'Fruits'), 47, 0.9, 12, 0.1, 130, (SELECT id FROM measurement_units WHERE code = 'G')),
-- Vegetables
(UUID(), 'CARROT', 'Carrot', 'Raw carrot', (SELECT id FROM food_categories WHERE name = 'Vegetables'), 41, 0.9, 10, 0.2, 100, (SELECT id FROM measurement_units WHERE code = 'G')),
(UUID(), 'BROCCOLI', 'Broccoli', 'Steamed broccoli', (SELECT id FROM food_categories WHERE name = 'Vegetables'), 55, 3.7, 11, 0.6, 150, (SELECT id FROM measurement_units WHERE code = 'G')),
(UUID(), 'SPINACH', 'Spinach', 'Fresh spinach', (SELECT id FROM food_categories WHERE name = 'Vegetables'), 23, 2.9, 3.6, 0.4, 100, (SELECT id FROM measurement_units WHERE code = 'G')),
-- Dairy
(UUID(), 'MILK', 'Milk', 'Whole milk', (SELECT id FROM food_categories WHERE name = 'Dairy'), 61, 3.2, 4.8, 3.3, 200, (SELECT id FROM measurement_units WHERE code = 'ML')),
(UUID(), 'CHEESE', 'Cheese', 'Cheddar cheese', (SELECT id FROM food_categories WHERE name = 'Dairy'), 404, 25, 1.3, 33, 30, (SELECT id FROM measurement_units WHERE code = 'G')),
(UUID(), 'YOGURT', 'Yogurt', 'Natural yogurt', (SELECT id FROM food_categories WHERE name = 'Dairy'), 61, 3.5, 4.7, 3.3, 150, (SELECT id FROM measurement_units WHERE code = 'G')),
-- Proteins
(UUID(), 'CHICKEN', 'Chicken Breast', 'Grilled chicken breast', (SELECT id FROM food_categories WHERE name = 'proteins'), 165, 31, 0, 3.6, 150, (SELECT id FROM measurement_units WHERE code = 'G')),
(UUID(), 'BEEF', 'Beef', 'Lean beef steak', (SELECT id FROM food_categories WHERE name = 'proteins'), 250, 26, 0, 15, 150, (SELECT id FROM measurement_units WHERE code = 'G')),
(UUID(), 'SALMON', 'Salmon', 'Baked salmon', (SELECT id FROM food_categories WHERE name = 'proteins'), 208, 20, 0, 13, 150, (SELECT id FROM measurement_units WHERE code = 'G')),
-- Grains
(UUID(), 'RICE', 'Rice', 'Cooked white rice', (SELECT id FROM food_categories WHERE name = 'grains'), 130, 2.7, 28, 0.3, 150, (SELECT id FROM measurement_units WHERE code = 'G')),
(UUID(), 'PASTA', 'Pasta', 'Cooked pasta', (SELECT id FROM food_categories WHERE name = 'grains'), 131, 5, 25, 1, 150, (SELECT id FROM measurement_units WHERE code = 'G')),
(UUID(), 'BREAD', 'Bread', 'Whole wheat bread', (SELECT id FROM food_categories WHERE name = 'grains'), 265, 13, 49, 3.5, 100, (SELECT id FROM measurement_units WHERE code = 'G'));

-- Insert meals
INSERT IGNORE INTO meals (id, code, name, description, typical_time, sort_order) VALUES
(UUID(), 'BREAKFAST', 'Breakfast', 'First meal of the day', '08:00:00', 1),
(UUID(), 'MORNING_SNACK', 'Morning Snack', 'Mid-morning snack', '10:30:00', 2),
(UUID(), 'LUNCH', 'Lunch', 'Main midday meal', '12:30:00', 3),
(UUID(), 'AFTERNOON_SNACK', 'Afternoon Snack', 'Mid-afternoon snack', '15:30:00', 4),
(UUID(), 'DINNER', 'Dinner', 'Evening meal', '19:00:00', 5),
(UUID(), 'SUPPER', 'Supper', 'Late evening snack', '21:30:00', 6);

-- Insert pathologies
INSERT IGNORE INTO pathologies (id, code, name, description, category, is_chronic, requires_monitoring) VALUES
(UUID(), 'HYPERTENSION', 'Hypertension', 'High blood pressure', 'CARDIOVASCULAR', TRUE, TRUE),
(UUID(), 'DIABETES_1', 'Diabetes Type 1', 'Type 1 diabetes mellitus', 'METABOLIC', TRUE, TRUE),
(UUID(), 'DIABETES_2', 'Diabetes Type 2', 'Type 2 diabetes mellitus', 'METABOLIC', TRUE, TRUE),
(UUID(), 'HYPERCHOLESTEROLEMIA', 'Hypercholesterolemia', 'High cholesterol levels', 'METABOLIC', TRUE, TRUE),
(UUID(), 'HYPERTRIGLYCERIDEMIA', 'Hypertriglyceridemia', 'High triglycerides', 'METABOLIC', TRUE, TRUE),
(UUID(), 'ANEMIA', 'Anemia', 'Low hemoglobin levels', 'HEMATOLOGICAL', FALSE, TRUE),
(UUID(), 'HYPOTHYROIDISM', 'Hypothyroidism', 'Underactive thyroid', 'ENDOCRINE', TRUE, TRUE),
(UUID(), 'CELIAC', 'Celiac Disease', 'Gluten intolerance', 'DIGESTIVE', TRUE, FALSE),
(UUID(), 'LACTOSE_INTOLERANCE', 'Lactose Intolerance', 'Dairy intolerance', 'DIGESTIVE', TRUE, FALSE),
(UUID(), 'OBESITY', 'Obesity', 'Excess body weight', 'METABOLIC', TRUE, TRUE);

-- Insert biochemical exams
INSERT IGNORE INTO biochemical_exams (id, code, name, description, measurement_unit_id, min_reference_value, max_reference_value) VALUES
(UUID(), 'GLUCOSE', 'Glucose', 'Blood glucose level', (SELECT id FROM measurement_units WHERE code = 'MG'), 70, 99),
(UUID(), 'TOTAL_CHOLESTEROL', 'Total Cholesterol', 'Total cholesterol', (SELECT id FROM measurement_units WHERE code = 'MG'), 0, 200),
(UUID(), 'HDL_CHOLESTEROL', 'HDL Cholesterol', 'Good cholesterol', (SELECT id FROM measurement_units WHERE code = 'MG'), 40, NULL),
(UUID(), 'LDL_CHOLESTEROL', 'LDL Cholesterol', 'Bad cholesterol', (SELECT id FROM measurement_units WHERE code = 'MG'), 0, 100),
(UUID(), 'TRIGLYCERIDES', 'Triglycerides', 'Blood triglycerides', (SELECT id FROM measurement_units WHERE code = 'MG'), 0, 150),
(UUID(), 'HEMOGLOBIN', 'Hemoglobin', 'Blood hemoglobin', (SELECT id FROM measurement_units WHERE code = 'MG'), 12, 16),
(UUID(), 'HEMATOCRIT', 'Hematocrit', 'Blood hematocrit', (SELECT id FROM measurement_units WHERE code = 'CUP'), 36, 48),
(UUID(), 'TGO_AST', 'TGO/AST', 'Liver enzyme', (SELECT id FROM measurement_units WHERE code = 'CUP'), 0, 40),
(UUID(), 'TGP_ALT', 'TGP/ALT', 'Liver enzyme', (SELECT id FROM measurement_units WHERE code = 'MG'), 0, 41),
(UUID(), 'UREA', 'Urea', 'Blood urea', (SELECT id FROM measurement_units WHERE code = 'MG'), 10, 50),
(UUID(), 'CREATININE', 'Creatinine', 'Kidney function', (SELECT id FROM measurement_units WHERE code = 'MG'), 0.5, 1.2),
(UUID(), 'URIC_ACID', 'Uric Acid', 'Uric acid levels', (SELECT id FROM measurement_units WHERE code = 'MG'), 2.4, 6.0);
