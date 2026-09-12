package io.github.thena3ik.airalertmonitor.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "api.model.error.desc")
public record ErrorResponse(
        @Schema(description = "api.model.error.status", example = "404") Integer status,
        @Schema(description = "api.model.error.message", example = "Region not found") String message,
        @Schema(description = "api.model.error.timestamp") Instant timestamp) {
}