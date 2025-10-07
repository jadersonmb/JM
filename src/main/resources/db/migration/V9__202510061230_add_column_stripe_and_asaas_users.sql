alter table user_entity
    add column if not exists stripe_customer_id VARCHAR(50),
    add column if not exists asaas_customer_id VARCHAR(50);

