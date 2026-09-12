package io.github.thena3ik.airalertmonitor.dto.webhook;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

@Schema(description = "api.model.webhookSubSummary.desc")
public record WebhookSubscriptionSummaryResponse(
        @Schema(description = "api.model.webhookSubSummary.id", example = "42") Long id,
        @Schema(description = "api.model.webhookSubSummary.url") String url,
        @Schema(description = "api.model.webhookSubSummary.active") boolean active,
        @Schema(description = "api.model.webhookSubSummary.consecutiveFailures") int consecutiveFailures,
        @Schema(description = "api.model.webhookSubSummary.regionIds") List<Long> regionIds,
        @Schema(description = "api.model.webhookSubSummary.createdAt") Instant createdAt) {
}