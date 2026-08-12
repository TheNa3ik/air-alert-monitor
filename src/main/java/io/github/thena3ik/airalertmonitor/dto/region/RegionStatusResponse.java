package io.github.thena3ik.airalertmonitor.dto.region;

import java.time.ZonedDateTime;

public record RegionStatusResponse(
        Long id,
        String name,
        Boolean alertActive,
        ZonedDateTime since) {
}
