package io.github.thena3ik.airalertmonitor.dto.region;

import java.time.ZonedDateTime;

public record RegionAlertStatsResponse(
        Long regionId,
        String regionName,
        ZonedDateTime from,
        ZonedDateTime to,
        String timezone,
        long eventCount,
        long totalAlertSeconds,
        long longestEventSeconds) {
}
