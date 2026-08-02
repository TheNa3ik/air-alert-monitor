package io.github.thena3ik.airalertmonitor.dto;

import java.util.List;

public record PageResponse<T>(
        List<T> content,
        Integer pageNumber,
        Integer pageSize,
        Long totalElements,
        Integer totalPages,
        Boolean isLastPage) {
}

