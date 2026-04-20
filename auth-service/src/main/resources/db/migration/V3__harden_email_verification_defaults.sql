ALTER TABLE users
ALTER COLUMN email_verified SET DEFAULT FALSE;

UPDATE users
SET email_verified = FALSE
WHERE verification_token IS NOT NULL;
