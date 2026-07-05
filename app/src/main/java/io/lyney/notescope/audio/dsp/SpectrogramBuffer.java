package io.lyney.notescope.audio.dsp;

import io.lyney.notescope.audio.model.SpectrogramSnapshot;

import java.util.Arrays;

public class SpectrogramBuffer {

    private static final float MIN_AMPLITUDE_DB = -80f;

    private final int bins;
    private final int historySize;
    private final int sampleRate;
    private final int hopSize;

    private final float[] data;

    private int writeIndex = 0;

    private int totalFrames = 0;

    private float[] snapshotBuffer;

    public SpectrogramBuffer(int bins, int historySize, int sampleRate, int hopSize) {
        this.bins = bins;
        this.historySize = historySize;
        this.sampleRate = sampleRate;
        this.hopSize = hopSize;

        this.data = new float[bins * historySize];
        Arrays.fill(data, MIN_AMPLITUDE_DB);
    }

    public synchronized void push(float[] spectrum) {
        if (spectrum.length != bins) {
            throw new IllegalArgumentException("Invalid spectrum size");
        }

        int offset = writeIndex * bins;
        System.arraycopy(spectrum, 0, data, offset, bins);

        writeIndex = (writeIndex + 1) % historySize;
        totalFrames++;
    }
    public synchronized SpectrogramSnapshot snapshot() {

        if (snapshotBuffer == null || snapshotBuffer.length != data.length) {
            snapshotBuffer = new float[data.length];
        }

        int dst = 0;
        for (int i = writeIndex; i < historySize; i++) {
            System.arraycopy(data, i * bins, snapshotBuffer, dst, bins);
            dst += bins;
        }

        for (int i = 0; i < writeIndex; i++) {
            System.arraycopy(data, i * bins, snapshotBuffer, dst, bins);
            dst += bins;
        }

        int startFrame = Math.max(0, totalFrames - historySize);
        int endFrame = totalFrames;

        double frameDurationSec = (double) hopSize / sampleRate;
        long startTimeSec = (long) (startFrame * frameDurationSec);
        long endTimeSec = (long) (endFrame * frameDurationSec);

        return new SpectrogramSnapshot(
                snapshotBuffer,
                bins,
                historySize,

                startTimeSec,
                endTimeSec,

                startFrame,
                endFrame,
                endFrame - startFrame
        );
    }

    public synchronized void clear() {
        Arrays.fill(data, MIN_AMPLITUDE_DB);
        writeIndex = 0;
        totalFrames = 0;
    }
}