-- Create the custom ENUM type for the login_provider column
CREATE TYPE login_provider_type AS ENUM ('GITHUB', 'ROOT');

---

-- Drop existing columns
ALTER TABLE accounts DROP COLUMN email;
ALTER TABLE accounts DROP COLUMN password;

-- Add the new email column (PostgreSQL uses VARCHAR for H2's VARCHAR)
ALTER TABLE accounts ADD COLUMN email VARCHAR(50) NULL;

-- Add the login_id column (PostgreSQL uses BIGINT for H2's BIGINT)
ALTER TABLE accounts ADD COLUMN login_id BIGINT NOT NULL DEFAULT 0;

-- Add the login_provider column using the custom ENUM type
ALTER TABLE accounts ADD COLUMN login_provider login_provider_type NOT NULL DEFAULT 'ROOT';
-- Note: 'ROOT' is automatically cast to the 'login_provider_type'

---

-- Insert the default user
INSERT INTO accounts (user_id, username, email, login_id, login_provider)
VALUES (0, 'Guest', 'guest@mut-ink.io', 0, 'ROOT')
    ON CONFLICT (user_id) DO NOTHING; -- Use ON CONFLICT if the row might already exist

---

-- Add the unique constraint
ALTER TABLE accounts ADD CONSTRAINT uk_provider_accounts UNIQUE (login_provider, login_id);

---

-- Add the user_id column to the 'list' table
ALTER TABLE list ADD COLUMN user_id BIGINT NOT NULL DEFAULT 0;

-- Add the foreign key constraint
ALTER TABLE list ADD CONSTRAINT fk_list
    FOREIGN KEY (user_id) REFERENCES accounts(user_id);