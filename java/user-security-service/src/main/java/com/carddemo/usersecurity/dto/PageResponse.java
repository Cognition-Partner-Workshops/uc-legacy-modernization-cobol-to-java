package com.carddemo.usersecurity.dto;

import java.util.List;
import org.springframework.data.domain.Page;

/** Page envelope mirroring the ten-rows-per-screen browse of COUSR00C. */
public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages,
                              boolean first, boolean last) {

    public static <E, T> PageResponse<T> of(Page<E> source, List<T> content) {
        return new PageResponse<>(content, source.getNumber(), source.getSize(), source.getTotalElements(),
                source.getTotalPages(), source.isFirst(), source.isLast());
    }
}
