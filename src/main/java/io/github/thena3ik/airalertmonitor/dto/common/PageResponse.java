package io.github.thena3ik.airalertmonitor.dto.common;

import java.util.List;

public record PageResponse<T>(
        List<T> content,
        Integer pageNumber,
        Integer pageSize,
        Long totalElements,
        Integer totalPages,
        Boolean isLastPage) {
}

