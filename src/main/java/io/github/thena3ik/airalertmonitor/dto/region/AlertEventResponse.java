package io.github.thena3ik.airalertmonitor.dto.region;

import java.time.Duration;
import java.time.ZonedDateTime;

public record AlertEventResponse(
        ZonedDateTime startedAt,
        ZonedDateTime endedAt,
        Long durationSeconds,
        String source) {

    public static AlertEventResponse from (ZonedDateTime startedAt, ZonedDateTime endedAt, String source) {
        Long duration = endedAt != null
                ? Duration.between(startedAt, endedAt).getSeconds()
                : null;
        return new AlertEventResponse(startedAt, endedAt, duration, source);
    }
}