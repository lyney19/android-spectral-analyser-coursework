package com.mirea.kt.ribo.notescope.network.body;

public enum ContentType {
    JSON("application/json"),
    FORM_URLENCODE("application/x-www-form-urlencoded"),
    SOAP_XML("application/soap+xml"),
    XML("application/xml"),
    HTML("text/html"),
    TEXT("text/plain");

    private final String value;

    ContentType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public boolean matches(String header) {
        if (header == null) {
            return false;
        }

        return header.toLowerCase()
                .contains(value.toLowerCase());
    }

    public static ContentType fromHeader(String header) {

        for (var type : values()) {
            if (type.matches(header)) {
                return type;
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return value;
    }
}