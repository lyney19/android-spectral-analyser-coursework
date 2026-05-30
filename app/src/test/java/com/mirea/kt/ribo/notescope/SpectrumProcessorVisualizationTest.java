package com.mirea.kt.ribo.notescope;

import com.mirea.kt.ribo.notescope.audio.dsp.SpectrumAnalyser;
import com.mirea.kt.ribo.notescope.audio.dsp.SpectrumProcessor;

import org.junit.Test;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.QuickChart;

import java.io.File;
import java.util.Random;

public class SpectrumProcessorVisualizationTest {

    int sampleRate = 44100;
    int frameSize = 4096;
    int frames = 15;

    // ===== TESTS =====

    @Test
    public void testOnSine() {
        double freq = 440;

        runTest("sine_processed", (i) -> {
            double t = (double) i / sampleRate;
            return Math.sin(2 * Math.PI * freq * t) * 30000;
        });
    }

    @Test
    public void testOnDualTone() {
        double f1 = 440;
        double f2 = 4400;

        runTest("dualTone_processed", (i) -> {
            double t = (double) i / sampleRate;
            return (Math.sin(2 * Math.PI * f1 * t)
                    + Math.sin(2 * Math.PI * f2 * t)) * 15000;
        });
    }

    @Test
    public void testOnNoise() {
        Random rnd = new Random();

        runTest("noise_processed", (i) ->
                (rnd.nextDouble() * 2 - 1) * 30000
        );
    }

    @Test
    public void testOnNoisySine() {
        Random rnd = new Random();
        double freq = 440;

        runTest("noisy_sine_processed", (i) -> {
            double t = (double) i / sampleRate;
            double signal = Math.sin(2 * Math.PI * freq * t);
            double noise = (rnd.nextDouble() * 2 - 1) * 0.3;
            return (signal + noise) * 20000;
        });
    }

    @Test
    public void testOnSquare() {
        double freq = 440;

        runTest("square_processed", (i) -> {
            double t = (double) i / sampleRate;
            double s = Math.sin(2 * Math.PI * freq * t);
            return Math.signum(s) * 30000;
        });
    }

    @Test
    public void testOnSawtooth() {
        double freq = 440;

        runTest("sawtooth_processed", (i) -> {
            double t = (double) i / sampleRate;
            double period = 1.0 / freq;
            double value = 2 * ((t / period) - Math.floor(0.5 + t / period));
            return value * 30000;
        });
    }

    @Test
    public void testOnTriangle() {
        double freq = 440;

        runTest("triangle_processed", (i) -> {
            double t = (double) i / sampleRate;
            double value = 2 * Math.asin(Math.sin(2 * Math.PI * freq * t)) / Math.PI;
            return value * 30000;
        });
    }

    @Test
    public void testOnImpulse() {
        runTest("impulse_processed", (i) ->
                i == 0 ? 30000 : 0
        );
    }

    @Test
    public void testOnSweep() {
        double f0 = 20;
        double f1 = 20000;
        double T = (double) frameSize / sampleRate;
        double k = (f1 - f0) / T;

        runTest("sweep_processed", (i) -> {
            double t = (double) i / sampleRate;
            double phase = 2 * Math.PI * (f0 * t + 0.5 * k * t * t);
            return Math.sin(phase) * 20000;
        });
    }

    // ===== COMMON LOGIC =====

    interface SignalGenerator {
        double generate(int i);
    }

    void runTest(String filename, SignalGenerator generator) {

        var processor = new SpectrumProcessor(frameSize);
        var analyser = new SpectrumAnalyser(frameSize, spectrum -> {
            try {
                var processed = processor.process(spectrum);
                createGraphic(processed, filename);
            } catch (Exception e) {
                throw new AssertionError(e);
            }
        });

        var buffer = new short[frameSize];

        for (int j = 0; j < frames; j++) {
            for (int i = 0; i < frameSize; i++) {
                buffer[i] = (short) generator.generate(i);
            }
            analyser.addSamples(buffer);
        }
    }

    // ===== VISUALISATION =====

    void createGraphic(float[] spectrum, String filename) throws Exception {
        var x = new double[spectrum.length - 1];
        var y = new double[spectrum.length - 1];

        for (int i = 1; i < spectrum.length; i++) {
            x[i - 1] = (double) i * sampleRate / frameSize;
            y[i - 1] = spectrum[i];
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

        chart.getStyler().setXAxisLogarithmic(true);

        BitmapEncoder.saveBitmap(
                chart,
                "build/test-output/" + filename,
                BitmapEncoder.BitmapFormat.PNG
        );
    }
}