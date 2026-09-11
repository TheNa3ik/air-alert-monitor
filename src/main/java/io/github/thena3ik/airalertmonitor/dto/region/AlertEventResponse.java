package io.github.thena3ik.airalertmonitor.dto.region;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Duration;
import java.time.ZonedDateTime;

@Schema(description = "A single alert event (start and, if finished, end) for a region")
public record AlertEventResponse(
        @Schema(description = "Region id", example = "12") Long regionId,
        @Schema(description = "Localized region name", example = "Київська область") String regionName,
        @Schema(description = "When the alert started") ZonedDateTime startedAt,
        @Schema(description = "When the alert ended; null if still active", nullable = true) ZonedDateTime endedAt,
        @Schema(description = "Duration of the alert in seconds; null if still active", nullable = true) Long durationSeconds,
        @Schema(description = "Source system that reported the event", example = "ukr-alert-api") String source) {

    public static AlertEventResponse from (Long regionId,
                                           String regionName,
                                           ZonedDateTime startedAt,
                                           ZonedDateTime endedAt,
                                           String source) {
        Long duration = endedAt != null
                ? Duration.between(startedAt, endedAt).getSeconds()
                : null;
        return new AlertEventResponse(regionId, regionName, startedAt, endedAt, duration, source);
    }
}