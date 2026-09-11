package io.github.thena3ik.airalertmonitor.dto.region;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.ZonedDateTime;

@Schema(description = "Current alert status of a region")
public record RegionStatusResponse(
        @Schema(description = "Region id", example = "12") Long id,
        @Schema(description = "Localized region name", example = "Київська область") String name,
        @Schema(description = "Whether an air raid alert is currently active in this region") Boolean alertActive,
        @Schema(description = "Timestamp the current alert started, if active; null otherwise", nullable = true) ZonedDateTime since) {
}