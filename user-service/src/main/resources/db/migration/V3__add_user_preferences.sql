CREATE TABLE IF NOT EXISTS user_preferences (
    id BIGSERIAL PRIMARY KEY,
    user_email VARCHAR(255) UNIQUE NOT NULL REFERENCES users(email),
    industries TEXT,
    stages TEXT,
    funding_range VARCHAR(100),
    collab_style VARCHAR(100),
    linkedin_url VARCHAR(512),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
