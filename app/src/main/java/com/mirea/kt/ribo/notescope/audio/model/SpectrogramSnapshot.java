package com.mirea.kt.ribo.notescope.audio.model;

public record SpectrogramSnapshot(
        float[] data,
        int bins,
        int history,

        long startTime,
        long endTime,

        int startFrame,
        int endFrame,
        int validFrames
) {

}
