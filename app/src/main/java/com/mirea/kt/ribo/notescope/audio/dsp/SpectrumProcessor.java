package com.mirea.kt.ribo.notescope.audio.dsp;

import com.mirea.kt.ribo.notescope.audio.util.SpectrumTransforms;

import java.util.Arrays;

public class SpectrumProcessor {
    private final float[] smoothBuffer;
    private final float[] peakBuffer;

    private final float[] result;
    private final float[] sourcePositions;

    private static final float MIN_SPECTRUM_VALUE = -80f;

    private static final float TAU         = 0.15f; // temporal smoothing constant (100 ms)
    private static final float TAU_DECAY   = 0.3f;  // peak decay constant (300ms)
    private static final float OVERLAP     = 0.75f;
    private static final float SAMPLE_RATE = 44100;
    private static final int   OUTPUT_SIZE = 300;

    private final float smoothing;
    private final float peakDecay;

    public SpectrumProcessor(int frameSize) {
        smoothBuffer = new float[frameSize];
        peakBuffer = new float[frameSize];

        result = new float[OUTPUT_SIZE];
        sourcePositions = new float[OUTPUT_SIZE];
        precomputeLogScalePositions(frameSize / 2);

        Arrays.fill(peakBuffer, MIN_SPECTRUM_VALUE);

        float dt = (1 - OVERLAP) * frameSize / SAMPLE_RATE;
        smoothing = (float)(1 - Math.exp(-dt / TAU));
        peakDecay = (float) Math.exp(-dt / TAU_DECAY);
    }

    public float[] process(float[] input) {
        // артефакты
//        applyPeakHold(result);
        SpectrumTransforms.logScale(input);
        resampleLogarithmic(input, result);
        SpectrumTransforms.smooth(result, smoothBuffer, smoothing);

        return result;
    }

    private void applyPeakHold(float[] data) {
        for (int i = 3; i < data.length; i++) {
            if (data[i] > peakBuffer[i]) {
                peakBuffer[i] = data[i];
            } else {
                peakBuffer[i] *= peakDecay;
            }

            data[i] = Math.max(data[i], peakBuffer[i]);
        }
    }

    private void precomputeLogScalePositions(int fftSize) {
        float minFreq = 20f;
        float maxFreq = SAMPLE_RATE * 0.5f;

        float logMin = (float) Math.log10(minFreq);
        float logMax = (float) Math.log10(maxFreq);

        for (int i = 0; i < OUTPUT_SIZE; i++) {

            float t = (float) i / (OUTPUT_SIZE - 1);

            float freq = (float) Math.pow(10f, logMin + t * (logMax - logMin));
            float fftIndex = freq * (fftSize - 1f) / maxFreq;

            sourcePositions[i] = fftIndex;
        }
    }

    private void resampleLogarithmic(float[] input, float[] output) {
        int max = input.length - 1;
        for (int i = 0; i < OUTPUT_SIZE; i++) {

            float pos = sourcePositions[i];

            int x1 = (int) pos;
            float t = pos - x1;

            int x0 = Math.max(0, x1 - 1);
            int x2 = Math.min(max, x1 + 1);
            int x3 = Math.min(max, x1 + 2);

            output[i] = catmullRom(input[x0], input[x1], input[x2], input[x3], t);
        }
    }

    private static float catmullRom(float p0, float p1, float p2, float p3, float t) {

        float t2 = t * t;
        float t3 = t2 * t;

        return 0.5f * (
                2f * p1 +
                        (-p0 + p2) * t +
                        (2f * p0 - 5f * p1 + 4f * p2 - p3) * t2 +
                        (-p0 + 3f * p1 - 3f * p2 + p3) * t3
        );
    }

    public void reset() {
        Arrays.fill(smoothBuffer, 0);
        Arrays.fill(peakBuffer, MIN_SPECTRUM_VALUE);
    }
}
