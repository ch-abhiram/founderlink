-- V1__init_startup_schema.sql
CREATE TABLE startup (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    description TEXT,
    founder_email VARCHAR(255),
    funding_goal DOUBLE PRECISION,
    current_funding DOUBLE PRECISION DEFAULT 0.0,
    category VARCHAR(255),
    current_round VARCHAR(255),
    valuation DOUBLE PRECISION,
    status VARCHAR(50) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE startup_followers (
    startup_id BIGINT REFERENCES startup(id) ON DELETE CASCADE,
    followers VARCHAR(255)
);
