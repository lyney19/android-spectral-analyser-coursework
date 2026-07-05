package io.lyney.notescope;

import io.lyney.notescope.audio.dsp.SpectrumAnalyser;

import org.junit.Test;
import org.knowm.xchart.*;

import java.io.File;
import java.util.Arrays;
import java.util.Random;

public class SpectrumAnalyserVisualizationTest {

    int sampleRate = 44100;
    int frameSize = 4096;

    // ===== TESTS =====

    @Test
    public void testOnSine() {
        double freq = 440;

        runTest("sine", i ->
                sin(freq, i / (double) sampleRate) * 30000
        );
    }

    @Test
    public void testOnDualTone() {
        double f1 = 440;
        double f2 = f1 * 10;

        runTest("dualTone", i -> {
            double t = i / (double) sampleRate;
            return (sin(f1, t) + sin(f2, t)) * 15000;
        });
    }

    @Test
    public void testOnNoise() {
        Random rnd = new Random();

        runTest("white_noise", i ->
                (rnd.nextDouble() * 2 - 1) * 30000
        );
    }

    @Test
    public void testOnNoisySine() {
        Random rnd = new Random();

        runTest("noisy_sine", i -> {
            double t = i / (double) sampleRate;
            double signal = Math.sin(2 * Math.PI * 440 * t);
            double noise = (rnd.nextDouble() * 2 - 1) * 0.3;
            return (signal + noise) * 20000;
        });
    }

    @Test
    public void testOnSquare() {
        double freq = 440;

        runTest("square", i -> {
            double t = i / (double) sampleRate;
            double s = sin(freq, t);
            return Math.signum(s) * 30000;
        });
    }

    @Test
    public void testOnSawtooth() {
        double freq = 440;

        runTest("sawtooth", i -> {
            double t = i / (double) sampleRate;
            double period = 1.0 / freq;
            double value = 2 * ((t / period) - Math.floor(0.5 + t / period));
            return value * 30000;
        });
    }

    @Test
    public void testOnTriangle() {
        double freq = 440;

        runTest("triangle", i -> {
            double t = i / (double) sampleRate;
            double value = 2 * Math.asin(sin(freq, t)) / Math.PI;
            return value * 30000;
        });
    }

    @Test
    public void testOnImpulse() {
        runTest("impulse", i ->
                i == 0 ? 30000 : 0
        );
    }

    @Test
    public void testOnSweep() {
        double f0 = 20;
        double f1 = 20000;
        double T = (double) frameSize / sampleRate;
        double k = (f1 - f0) / T;

        runTest("sweep", i -> {
            double t = i / (double) sampleRate;
            double phase = 2 * Math.PI * (f0 * t + 0.5 * k * t * t);
            return Math.sin(phase) * 30000;
        });
    }

    // ===== COMMON LOGIC =====

    interface SignalGenerator {
        double generate(int i);
    }

    void runTest(String filename, SignalGenerator generator) {
        var buffer = new short[frameSize];

        for (int i = 0; i < frameSize; i++) {
            buffer[i] = (short) generator.generate(i);
        }

        var analyser = new SpectrumAnalyser(
                frameSize,
                frameSize / 4,
                spectrum -> {
                    try {
                        createGraphic(spectrum, filename);
                        System.out.println(Arrays.toString(spectrum));
                    } catch (Exception e) {
                        throw new AssertionError(e);
                    }
                });

        analyser.addSamples(buffer);
    }

    double sin(double freq, double t) {
        return Math.sin(2 * Math.PI * freq * t);
    }

    // ===== VISUALISATION =====

    void createGraphic(float[] spectrum, String filename) throws Exception {
        var x = new double[spectrum.length - 1];
        var y = new double[spectrum.length - 1];

        for (int i = 1; i < spectrum.length; i++) {
            x[i - 1] = (double) i * sampleRate / frameSize;
            y[i - 1] = Math.max(spectrum[i], -120);
        }

        File dir = new File("build/test-output");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        var chart = QuickChart.getChart(
                "FFT Spectrum",
                "Frequency (Hz)",
                "FFT",
                "fft spectrum",
                x, y
        );

        BitmapEncoder.saveBitmap(
                chart,
                "build/test-output/" + filename,
                BitmapEncoder.BitmapFormat.PNG
        );
    }
}