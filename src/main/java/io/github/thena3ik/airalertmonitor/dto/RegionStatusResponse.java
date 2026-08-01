package io.github.thena3ik.airalertmonitor.dto;

import java.time.Instant;

public record RegionStatusResponse(
        Long id,
        String name,
        Boolean alertActive,
        Instant since) {
}
