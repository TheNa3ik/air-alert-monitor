package io.github.thena3ik.airalertmonitor.dto.region;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.ZonedDateTime;

@Schema(description = "Aggregated alert statistics for a region over a time window")
public record RegionAlertStatsResponse(
        @Schema(description = "Region id", example = "12") Long id,
        @Schema(description = "Localized region name", example = "Київська область") String name,
        @Schema(description = "Start of the statistics window") ZonedDateTime from,
        @Schema(description = "End of the statistics window") ZonedDateTime to,
        @Schema(description = "IANA timezone used to render `from`/`to`", example = "Europe/Kyiv") String timezone,
        @Schema(description = "Number of alert events in the window", example = "14") long eventCount,
        @Schema(description = "Total time under alert in the window, in seconds", example = "43200") long totalAlertSeconds,
        @Schema(description = "Duration of the longest single alert in the window, in seconds", example = "5400") long longestEventSeconds) {
}