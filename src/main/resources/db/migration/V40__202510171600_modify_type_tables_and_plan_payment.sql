ALTER TABLE jm.ollama
MODIFY prompt TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

ALTER TABLE jm.ai_prompt_references 
MODIFY prompt TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

ALTER TABLE jm.nutrition_goal  
MODIFY notes TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

UPDATE jm.payment_plans set active=0
WHERE code IN ('plan_quarterly');

UPDATE jm.payment_plans
SET code='plan_weekly', name='Weekly Plan', description='Recurring 2 weekly billing', amount=5.00, currency='USD', intervals='WEEKLY', stripe_price_id=NULL, asaas_plan_id=NULL, active=1
WHERE code='plan_monthly';

UPDATE jm.payment_plans
SET  amount=120.00, currency='USD'
WHERE code='plan_semi_annual';

UPDATE jm.payment_plans
SET  amount=220.00, currency='USD'
WHERE code='plan_annual';