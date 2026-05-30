package com.mirea.kt.ribo.notescope.audio.listener;

@FunctionalInterface
public interface SpectrumListener {
    void onSpectrum(float[] spectrum);
}
