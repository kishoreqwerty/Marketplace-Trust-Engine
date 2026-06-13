CREATE TABLE IF NOT EXISTS sellers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    external_id VARCHAR(64) UNIQUE NOT NULL,
    username VARCHAR(128) NOT NULL,
    account_created_at TIMESTAMP NOT NULL,
    total_listings INT DEFAULT 0,
    total_sales INT DEFAULT 0,
    dispute_count INT DEFAULT 0,
    report_count INT DEFAULT 0,
    reputation_score FLOAT DEFAULT 0.5,
    last_updated TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS listings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    seller_id UUID REFERENCES sellers(id),
    title VARCHAR(256) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    category VARCHAR(64) NOT NULL,
    condition VARCHAR(32),
    location_city VARCHAR(64),
    location_state VARCHAR(32),
    image_hash VARCHAR(64),
    fraud_score FLOAT,
    trust_label VARCHAR(32) DEFAULT 'PENDING',
    shap_values JSONB,
    flagged_reason TEXT,
    created_at TIMESTAMP DEFAULT NOW(),
    scored_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS moderation_queue (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    listing_id UUID REFERENCES listings(id),
    fraud_score FLOAT NOT NULL,
    flagged_reason TEXT,
    status VARCHAR(32) DEFAULT 'PENDING',
    reviewed_by VARCHAR(64),
    reviewed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS listing_events (
    id BIGSERIAL PRIMARY KEY,
    listing_id UUID,
    event_type VARCHAR(64),
    payload JSONB,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_listings_seller ON listings(seller_id);
CREATE INDEX idx_listings_trust_label ON listings(trust_label);
CREATE INDEX idx_moderation_status ON moderation_queue(status);
CREATE INDEX idx_listing_events_listing ON listing_events(listing_id);
