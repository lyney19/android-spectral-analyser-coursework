package io.lyney.notescope.audio.listener;

@FunctionalInterface
public interface SpectrumListener {
    void onSpectrum(float[] spectrum);
}
