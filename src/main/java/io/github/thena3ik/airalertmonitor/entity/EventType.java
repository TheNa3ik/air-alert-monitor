package io.github.thena3ik.airalertmonitor.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "api.model.eventType.desc")
public enum EventType {
    ALERT_STARTED,
    ALERT_ENDED
}