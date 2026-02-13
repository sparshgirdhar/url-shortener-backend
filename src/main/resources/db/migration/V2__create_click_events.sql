CREATE TABLE click_events (
    id BIGSERIAL PRIMARY KEY,
    short_code VARCHAR(10) NOT NULL,
    clicked_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45),
    user_agent TEXT,
    referrer TEXT
);

-- Index for analytics queries
CREATE INDEX idx_click_short_code ON click_events(short_code, clicked_at DESC);
CREATE INDEX idx_click_timestamp ON click_events(clicked_at DESC);