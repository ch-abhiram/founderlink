-- V1__init_team_schema.sql
CREATE TABLE team_member (
    id BIGSERIAL PRIMARY KEY,
    startup_id BIGINT NOT NULL,
    user_email VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
