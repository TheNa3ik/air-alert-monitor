package io.github.thena3ik.airalertmonitor.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Generic paginated response wrapper")
public record PageResponse<T>(
        @Schema(description = "Items in the current page") List<T> content,
        @Schema(description = "Zero-based page number", example = "0") Integer pageNumber,
        @Schema(description = "Number of items per page", example = "20") Integer pageSize,
        @Schema(description = "Total number of items across all pages", example = "134") Long totalElements,
        @Schema(description = "Total number of pages", example = "7") Integer totalPages,
        @Schema(description = "Whether this is the last page") Boolean isLastPage) {
}