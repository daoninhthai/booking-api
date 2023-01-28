CREATE TABLE reviews (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL REFERENCES bookings(id),
    customer_id BIGINT NOT NULL REFERENCES users(id),
    provider_id BIGINT NOT NULL REFERENCES providers(id),
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_reviews_booking_id ON reviews(booking_id);
CREATE INDEX idx_reviews_customer_id ON reviews(customer_id);
CREATE INDEX idx_reviews_provider_id ON reviews(provider_id);
CREATE UNIQUE INDEX idx_reviews_booking_unique ON reviews(booking_id);
