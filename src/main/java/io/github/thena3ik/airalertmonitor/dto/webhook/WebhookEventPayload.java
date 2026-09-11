package io.github.thena3ik.airalertmonitor.dto.webhook;

import io.github.thena3ik.airalertmonitor.entity.EventType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Payload delivered to a subscriber's webhook URL when an alert starts or ends")
public record WebhookEventPayload(
        @Schema(description = "Type of event") EventType eventType,
        @Schema(description = "Region id the event relates to", example = "12") Long regionId,
        @Schema(description = "Localized region name", example = "Київська область") String regionName,
        @Schema(description = "When the event occurred") Instant occurredAt) {
}