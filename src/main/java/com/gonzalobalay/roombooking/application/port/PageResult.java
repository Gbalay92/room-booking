package com.gonzalobalay.roombooking.application.port;

import java.util.List;

public record PageResult<T>(List<T> content, int page, int size, long totalElements, int totalPages) {
    public PageResult(List<T> content, int page, int size, long totalElements) {
        this(content, page, size, totalElements, (int) Math.ceil(totalElements / (double) size));
    }
}
