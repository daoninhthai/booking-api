CREATE TABLE time_slots (
    id BIGSERIAL PRIMARY KEY,
    provider_id BIGINT NOT NULL REFERENCES providers(id),
    day_of_week VARCHAR(20) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    available BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_time_slots_provider_id ON time_slots(provider_id);
CREATE INDEX idx_time_slots_day_of_week ON time_slots(day_of_week);
CREATE INDEX idx_time_slots_available ON time_slots(available);
