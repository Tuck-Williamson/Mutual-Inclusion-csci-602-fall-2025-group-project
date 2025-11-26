CREATE TABLE shares
(
    share_id    UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    list_id     BIGINT    NOT NULL,
    user_id     BIGINT    NULL,
    expiry_time TIMESTAMP NOT NULL DEFAULT (CURRENT_TIMESTAMP + INTERVAL '10' MINUTE),
    created_on  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_shares_user FOREIGN KEY (user_id) REFERENCES accounts(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_shares_list FOREIGN KEY (list_id) REFERENCES list(list_id) ON DELETE CASCADE
);

-- Create an index on expiry_time
CREATE INDEX idx_shares_expiry_time ON shares(expiry_time);