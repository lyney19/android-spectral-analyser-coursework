package io.lyney.notescope.audio.model;

import io.lyney.notescope.audio.util.WindowFunction;

public record DspConfig(
        int sampleRate,
        int frameSize,
        float hopSizeInPercent,
        int historyInSec,
        WindowFunction windowFunction
) {
}
