CREATE TABLE providers (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    business_name VARCHAR(255) NOT NULL,
    description TEXT,
    phone VARCHAR(20),
    address VARCHAR(500),
    rating NUMERIC(3, 2) DEFAULT 0.00,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE provider_services (
    provider_id BIGINT NOT NULL REFERENCES providers(id),
    service_id BIGINT NOT NULL REFERENCES services(id),
    PRIMARY KEY (provider_id, service_id)
);

CREATE INDEX idx_providers_user_id ON providers(user_id);
CREATE INDEX idx_providers_active ON providers(active);
