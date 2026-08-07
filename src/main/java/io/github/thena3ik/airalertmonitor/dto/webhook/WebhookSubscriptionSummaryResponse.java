package io.github.thena3ik.airalertmonitor.dto.webhook;

import java.time.Instant;
import java.util.List;

public record WebhookSubscriptionSummaryResponse(
        Long id,
        String url,
        boolean active,
        int consecutiveFailures,
        List<Long> regionIds,
        Instant createdAt) {
}