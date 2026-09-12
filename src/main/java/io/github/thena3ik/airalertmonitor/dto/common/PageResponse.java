package io.github.thena3ik.airalertmonitor.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "api.model.page.desc")
public record PageResponse<T>(
        @Schema(description = "api.model.page.content") List<T> content,
        @Schema(description = "api.model.page.pageNumber", example = "0") Integer pageNumber,
        @Schema(description = "api.model.page.pageSize", example = "20") Integer pageSize,
        @Schema(description = "api.model.page.totalElements", example = "134") Long totalElements,
        @Schema(description = "api.model.page.totalPages", example = "7") Integer totalPages,
        @Schema(description = "api.model.page.isLastPage") Boolean isLastPage) {
}