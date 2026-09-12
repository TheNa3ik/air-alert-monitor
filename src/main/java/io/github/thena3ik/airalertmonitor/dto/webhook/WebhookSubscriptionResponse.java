package io.github.thena3ik.airalertmonitor.dto.webhook;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

@Schema(description = "api.model.webhookSub.desc")
public record WebhookSubscriptionResponse(
        @Schema(description = "api.model.webhookSub.id", example = "42") Long id,
        @Schema(description = "api.model.webhookSub.url") String url,
        @Schema(description = "api.model.webhookSub.secret") String secret,
        @Schema(description = "api.model.webhookSub.managementToken") String managementToken,
        @Schema(description = "api.model.webhookSub.active") boolean active,
        @Schema(description = "api.model.webhookSub.regionIds") List<Long> regionIds,
        @Schema(description = "api.model.webhookSub.createdAt") Instant createdAt) {
}