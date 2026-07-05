package io.lyney.notescope.network.exception;

public class ApiException extends RuntimeException {
    public ApiException(String message) {
        super(message);
    }
}
