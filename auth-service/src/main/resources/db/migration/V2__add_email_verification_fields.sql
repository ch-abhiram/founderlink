ALTER TABLE users
ADD COLUMN IF NOT EXISTS email_verified BOOLEAN DEFAULT TRUE;

ALTER TABLE users
ADD COLUMN IF NOT EXISTS verification_token VARCHAR(255);

ALTER TABLE users
ADD COLUMN IF NOT EXISTS verification_token_expiry TIMESTAMP;

UPDATE users
SET email_verified = TRUE
WHERE email_verified IS NULL;
