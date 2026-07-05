package io.lyney.notescope.audio.dsp;

import io.lyney.notescope.audio.model.Note;
import io.lyney.notescope.audio.model.PitchInfo;

public class PitchDetector {

    private final float binSize;

    public PitchDetector(int frameSize, int sampleRate) {
        this.binSize = (float) sampleRate / frameSize;
    }

    public PitchInfo detect(float[] spectrum) {

        if (spectrum == null || spectrum.length < 3) {
            return null;
        }

        int peakIndex = 1;

        for (int i = 2; i < spectrum.length - 1; i++) {
            if (spectrum[i] > spectrum[peakIndex]) {
                peakIndex = i;
            }
        }

        // quadratic interpolation for better result
        float alpha = spectrum[peakIndex - 1];
        float beta  = spectrum[peakIndex];
        float gamma = spectrum[peakIndex + 1];

        float denominator = alpha - 2f * beta + gamma;
        float p = 0f;

        if (Math.abs(denominator) > 1e-6f) {
            p = 0.5f * (alpha - gamma) / denominator;
        }

        float frequency = (peakIndex + p) * binSize;

        double note = 69.0 + 12.0 * (Math.log(frequency / 440.0) / Math.log(2.0));

        int nearestMidi = (int) Math.round(note);
        float cents = (float) ((note - nearestMidi) * 100.0);

        return new PitchInfo(
                frequency,
                Note.fromMidi(nearestMidi),
                cents
        );
    }
}