CREATE TABLE url_mappings (
    id BIGSERIAL PRIMARY KEY,
    short_code VARCHAR(10) UNIQUE NOT NULL,
    original_url TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMP,
    user_id BIGINT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_url_mappings_short_code 
    ON url_mappings(short_code);

CREATE INDEX idx_url_mappings_expires_at 
    ON url_mappings(expires_at);
