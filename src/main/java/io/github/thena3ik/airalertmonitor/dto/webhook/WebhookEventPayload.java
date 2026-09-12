package io.github.thena3ik.airalertmonitor.dto.webhook;

import io.github.thena3ik.airalertmonitor.entity.EventType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "api.model.webhookEventPayload.desc")
public record WebhookEventPayload(
        @Schema(description = "api.model.webhookEventPayload.eventType") EventType eventType,
        @Schema(description = "api.model.webhookEventPayload.regionId", example = "12") Long regionId,
        @Schema(description = "api.model.webhookEventPayload.regionName", example = "Київська область") String regionName,
        @Schema(description = "api.model.webhookEventPayload.occurredAt") Instant occurredAt) {
}