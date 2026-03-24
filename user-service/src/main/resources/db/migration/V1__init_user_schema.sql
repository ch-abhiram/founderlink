-- V1__init_user_schema.sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR UNIQUE NOT NULL,
    name VARCHAR,
    role VARCHAR NOT NULL DEFAULT 'USER',
    bio TEXT,
    experience TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_skills (
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    skills VARCHAR
);

CREATE TABLE user_portfolio_links (
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    portfolio_links VARCHAR
);
