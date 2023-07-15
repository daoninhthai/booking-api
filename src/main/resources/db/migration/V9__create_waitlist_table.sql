CREATE TABLE waitlist_entries (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES users(id),
    service_id BIGINT NOT NULL REFERENCES services(id),
    provider_id BIGINT NOT NULL REFERENCES providers(id),
    preferred_date DATE NOT NULL,
    preferred_time TIME,
    status VARCHAR(20) NOT NULL DEFAULT 'WAITING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_waitlist_customer_id ON waitlist_entries(customer_id);
CREATE INDEX idx_waitlist_provider_date ON waitlist_entries(provider_id, preferred_date);
CREATE INDEX idx_waitlist_status ON waitlist_entries(status);
