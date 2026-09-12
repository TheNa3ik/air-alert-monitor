package io.github.thena3ik.airalertmonitor.dto.region;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Duration;
import java.time.ZonedDateTime;

@Schema(description = "api.model.alertEvent.desc")
public record AlertEventResponse(
        @Schema(description = "api.model.alertEvent.regionId", example = "12") Long regionId,
        @Schema(description = "api.model.alertEvent.regionName", example = "Київська область") String regionName,
        @Schema(description = "api.model.alertEvent.startedAt") ZonedDateTime startedAt,
        @Schema(description = "api.model.alertEvent.endedAt", nullable = true) ZonedDateTime endedAt,
        @Schema(description = "api.model.alertEvent.durationSeconds", nullable = true) Long durationSeconds,
        @Schema(description = "api.model.alertEvent.source", example = "ukr-alert-api") String source) {

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