package com.shmoney.common.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record PageResponse<T>(
        long count,
        Integer next,
        Integer previous,
        List<T> results
) {

    public static <T, R> PageResponse<R> fromPage(Page<T> page, List<R> results) {
        Integer next = page.hasNext() ? page.getNumber() + 1 : null;
        Integer previous = page.hasPrevious() ? page.getNumber() - 1 : null;
        return new PageResponse<>(page.getTotalElements(), next, previous, results);
    }

    public static <R> PageResponse<R> of(long total, int page, int size, List<R> results) {
        long shown = (long) (page + 1) * size;
        Integer next = shown < total ? page + 1 : null;
        Integer previous = page > 0 ? page - 1 : null;
        return new PageResponse<>(total, next, previous, results);
    }
}
