package io.github.thena3ik.airalertmonitor.dto.webhook;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

@Schema(description = "Full webhook subscription details, returned only once at creation time")
public record WebhookSubscriptionResponse(
        @Schema(description = "Subscription id", example = "42") Long id,
        @Schema(description = "Destination URL that receives event payloads") String url,
        @Schema(description = "Shared secret used to sign delivered payloads (HMAC). Shown only at creation.") String secret,
        @Schema(description = "Bearer token used to manage this subscription later. Shown only at creation.") String managementToken,
        @Schema(description = "Whether the subscription is currently active") boolean active,
        @Schema(description = "Ids of regions this subscription is subscribed to") List<Long> regionIds,
        @Schema(description = "When the subscription was created") Instant createdAt) {
}