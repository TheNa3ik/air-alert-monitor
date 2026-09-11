package io.github.thena3ik.airalertmonitor.dto.common;

import java.time.Instant;

public record ErrorResponse(
        Integer status,
        String message,
        Instant timestamp) {
}