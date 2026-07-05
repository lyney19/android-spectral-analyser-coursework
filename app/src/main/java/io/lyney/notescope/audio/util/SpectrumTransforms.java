package io.lyney.notescope.audio.util;

public final class SpectrumTransforms {

    private SpectrumTransforms() {}

    public static void logScale(float[] data) {
        for (int i = 0; i < data.length; i++) {
            data[i] = 10f * (float)Math.log10(data[i] + 1e-12f);
        }
    }

    public static void clamp(float[] data, float min, float max) {
        for (int i = 0; i < data.length; i++) {
            data[i] = Math.max(min, Math.min(max, data[i]));
        }
    }

    // alpha = 0 -> only current; alpha = 1 -> only previous
    public static void smooth(float[] current, float[] previous, float alpha) {
        for (int i = 0; i < current.length; i++) {
            previous[i] = previous[i] * alpha + current[i] * (1 - alpha);
            current[i] = previous[i];
        }
    }

    public static float[] hannWindow(int size) {
        float[] window = new float[size];

        for (int i = 0; i < size; i++) {
            window[i] =
                    (float) (0.5 * (1 - Math.cos(2 * Math.PI * i / (size - 1))));
        }

        return window;
    }

    public static float computeWindowEnergy(float[] function) {
        float sum = 0;
        for (float num : function) {
            sum += num * num;
        }

        return sum;
    }
}
