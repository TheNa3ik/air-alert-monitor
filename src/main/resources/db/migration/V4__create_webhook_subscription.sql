CREATE TABLE webhook_subscription (
                                      id BIGSERIAL PRIMARY KEY,
                                      url VARCHAR(2048) NOT NULL,
                                      secret VARCHAR(255) NOT NULL UNIQUE,
                                      is_active BOOLEAN NOT NULL DEFAULT TRUE,
                                      consecutive_failures INT NOT NULL DEFAULT 0,
                                      created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE webhook_subscription_region (
                                             webhook_subscription_id BIGINT NOT NULL,
                                             region_id BIGINT NOT NULL,
                                             PRIMARY KEY (webhook_subscription_id, region_id),
                                             CONSTRAINT fk_wsr_subscription FOREIGN KEY (webhook_subscription_id) REFERENCES webhook_subscription(id) ON DELETE CASCADE,
                                             CONSTRAINT fk_wsr_region FOREIGN KEY (region_id) REFERENCES region(id) ON DELETE CASCADE
);

CREATE INDEX idx_wsr_region ON webhook_subscription_region(region_id);