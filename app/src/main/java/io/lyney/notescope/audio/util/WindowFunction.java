package io.lyney.notescope.audio.util;

public enum WindowFunction {
    HANN("Hann");

    private final String name;

    WindowFunction(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
