package io.lyney.notescope.network.exception;

public class NetworkException extends RuntimeException {
    public NetworkException(String message) {
        super(message);
    }

    public NetworkException(String message, Throwable cause) {
        super(message, cause);
    }
}
