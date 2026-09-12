package io.github.thena3ik.airalertmonitor.dto.region;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.ZonedDateTime;

@Schema(description = "api.model.regionStatus.desc")
public record RegionStatusResponse(
        @Schema(description = "api.model.regionStatus.id", example = "12") Long id,
        @Schema(description = "api.model.regionStatus.name", example = "Київська область") String name,
        @Schema(description = "api.model.regionStatus.alertActive") Boolean alertActive,
        @Schema(description = "api.model.regionStatus.since", nullable = true) ZonedDateTime since) {
}