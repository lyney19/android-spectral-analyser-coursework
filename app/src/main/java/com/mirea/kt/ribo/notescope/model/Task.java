package com.mirea.kt.ribo.notescope.model;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.Locale;

public record Task(
        String title,
        String task,
        int variant
) implements Serializable {
    @NonNull
    @Override
    public String toString() {
        return String.format(
                Locale.getDefault(),
                "%s\nВариант: %d\n%s",
                title, variant, task
        );
    }
}
