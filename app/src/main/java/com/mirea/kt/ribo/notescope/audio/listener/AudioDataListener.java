package com.mirea.kt.ribo.notescope.audio.listener;

@FunctionalInterface
public interface AudioDataListener {
    void onAudioData(short[] data);
}
