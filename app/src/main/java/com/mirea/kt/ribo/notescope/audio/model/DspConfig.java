package com.mirea.kt.ribo.notescope.audio.model;

import com.mirea.kt.ribo.notescope.audio.util.WindowFunction;

public record DspConfig(
        int sampleRate,
        int frameSize,
        int hopSize,
        int historyInSec,
        WindowFunction windowFunction
) {
}
