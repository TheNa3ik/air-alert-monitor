CREATE TABLE region (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL UNIQUE
);

CREATE TABLE alert_event (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    region_id BIGINT NOT NULL,
    started_at DATETIME NOT NULL,
    ended_at DATETIME NULL,
    source VARCHAR(100) NOT NULL,
    CONSTRAINT fk_alert_event_region FOREIGN KEY (region_id) REFERENCES region(id)
);

CREATE INDEX idx_alert_event_region_started ON alert_event(region_id, started_at);