package com.mirea.kt.ribo.notescope.network.model;

public record ApiResponse<T>(
        int code,
        T body
) {
}
