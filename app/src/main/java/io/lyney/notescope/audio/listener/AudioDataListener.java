package io.lyney.notescope.audio.listener;

@FunctionalInterface
public interface AudioDataListener {
    void onAudioData(short[] data);
}
