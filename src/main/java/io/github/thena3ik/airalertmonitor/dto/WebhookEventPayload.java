package io.github.thena3ik.airalertmonitor.dto;

import io.github.thena3ik.airalertmonitor.entity.EventType;

import java.time.Instant;

public record WebhookEventPayload(
        EventType eventType,
        Long regionId,
        String regionName,
        Instant occurredAt) {
}
