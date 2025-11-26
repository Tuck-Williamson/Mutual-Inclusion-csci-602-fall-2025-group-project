CREATE TABLE shares
(
    share_id    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    list_id     BIGINT    NOT NULL,
    user_id     BIGINT    NULL,
    expiry_time TIMESTAMP NOT NULL DEFAULT (now() + INTERVAL '10 minutes'),
    created_on  TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT fk_shares_list FOREIGN KEY (list_id) REFERENCES "list" (list_id) ON DELETE CASCADE,
    CONSTRAINT fk_shares_user FOREIGN KEY (user_id) REFERENCES accounts (user_id) ON DELETE CASCADE
);

-- Create an index on expiry_time for the scheduled delete job
CREATE INDEX idx_shares_expiry_time ON shares(expiry_time);

-- Create a function to delete expired anonymous shares at midnight
CREATE OR REPLACE FUNCTION delete_expired_shares()
RETURNS void AS $$
BEGIN
    DELETE FROM shares 
    WHERE user_id IS NULL 
    AND expiry_time < now();
END;
$$ LANGUAGE plpgsql;
