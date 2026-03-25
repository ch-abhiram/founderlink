-- V1__init_investment_schema.sql
CREATE TABLE investment (
    id BIGSERIAL PRIMARY KEY,
    startup_id BIGINT NOT NULL,
    investor_email VARCHAR(255) NOT NULL,
    amount DOUBLE PRECISION NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
