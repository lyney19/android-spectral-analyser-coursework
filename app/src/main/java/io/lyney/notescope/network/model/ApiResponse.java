package io.lyney.notescope.network.model;

public record ApiResponse<T>(
        int code,
        T body
) {
}
