package io.github.thena3ik.airalertmonitor.dto.region;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.ZonedDateTime;

@Schema(description = "api.model.regionStats.desc")
public record RegionAlertStatsResponse(
        @Schema(description = "api.model.regionStats.id", example = "12") Long id,
        @Schema(description = "api.model.regionStats.name", example = "Київська область") String name,
        @Schema(description = "api.model.regionStats.from") ZonedDateTime from,
        @Schema(description = "api.model.regionStats.to") ZonedDateTime to,
        @Schema(description = "api.model.regionStats.timezone", example = "Europe/Kyiv") String timezone,
        @Schema(description = "api.model.regionStats.eventCount", example = "14") long eventCount,
        @Schema(description = "api.model.regionStats.totalAlertSeconds", example = "43200") long totalAlertSeconds,
        @Schema(description = "api.model.regionStats.longestEventSeconds", example = "5400") long longestEventSeconds) {
}