package io.github.thena3ik.airalertmonitor.dto.region;

import java.time.Duration;
import java.time.Instant;

public record AlertEventResponse(
        Instant startedAt,
        Instant endedAt,
        Long durationSeconds,
        String source) {

    public static AlertEventResponse from (Instant startedAt, Instant endedAt, String source) {
        Long duration = endedAt != null
                ? Duration.between(startedAt, endedAt).getSeconds()
                : null;
        return new AlertEventResponse(startedAt, endedAt, duration, source);
    }
}