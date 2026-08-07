package io.github.thena3ik.airalertmonitor.dto.webhook;

import java.time.Instant;
import java.util.List;

public record WebhookSubscriptionResponse(
        Long id,
        String url,
        String secret,
        String managementToken,
        boolean active,
        List<Long> regionIds,
        Instant createdAt) {
}
