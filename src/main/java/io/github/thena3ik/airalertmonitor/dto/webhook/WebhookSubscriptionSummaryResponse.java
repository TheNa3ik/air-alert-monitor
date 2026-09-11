package io.github.thena3ik.airalertmonitor.dto.webhook;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

@Schema(description = "Webhook subscription summary, safe to return on repeated lookups (excludes the secret)")
public record WebhookSubscriptionSummaryResponse(
        @Schema(description = "Subscription id", example = "42") Long id,
        @Schema(description = "Destination URL that receives event payloads") String url,
        @Schema(description = "Whether the subscription is currently active") boolean active,
        @Schema(description = "Number of consecutive delivery failures; subscription auto-disables at the configured threshold") int consecutiveFailures,
        @Schema(description = "Ids of regions this subscription is subscribed to") List<Long> regionIds,
        @Schema(description = "When the subscription was created") Instant createdAt) {
}