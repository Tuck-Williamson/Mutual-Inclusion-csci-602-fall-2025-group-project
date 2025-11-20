CREATE TABLE users (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    provider VARCHAR(50) NOT NULL,
    provider_id VARCHAR(100) NOT NULL,
    username VARCHAR(100) NOT NULL,
    display_name VARCHAR(150),
    email VARCHAR(200),
    avatar_url VARCHAR(500),

    -- Ensure unique identity per OAuth provider
    CONSTRAINT uq_provider_providerid UNIQUE (provider, provider_id)
);

