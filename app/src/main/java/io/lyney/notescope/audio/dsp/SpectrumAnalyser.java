package io.lyney.notescope.audio.dsp;

import io.lyney.notescope.audio.listener.SpectrumListener;
import io.lyney.notescope.audio.util.SpectrumTransforms;

import org.jtransforms.fft.FloatFFT_1D;

import java.util.Arrays;

public class SpectrumAnalyser {
    private float[] buffer;
    private int bufferFill = 0;

    private final int frameSize;
    private final int hopSize;
    private final FloatFFT_1D fft;

    private final float[] windowFunction;
    private final float windowEnergy;
    private final float[] frame;
    private final float[] powerSpectrum;

    private final SpectrumListener listener;

    public SpectrumAnalyser(int frameSize, int hopSize, SpectrumListener listener) {
        this.frameSize = frameSize;
        this.listener = listener;

        this.hopSize = hopSize;

        buffer = new float[frameSize*2];

        windowFunction = SpectrumTransforms.hannWindow(frameSize);
        windowEnergy = SpectrumTransforms.computeWindowEnergy(windowFunction);
        frame = new float[frameSize];

        powerSpectrum = new float[frameSize/2];

        fft = new FloatFFT_1D(frameSize);
    }

    public void addSamples(short[] input) {
        if (bufferFill + input.length > buffer.length) {
            int newSize = Math.max(buffer.length * 2, bufferFill + input.length);
            buffer = Arrays.copyOf(buffer, newSize);
        }

        normalize(input);
        bufferFill += input.length;

        processFrames();
    }

    private void processFrames() {
        while (bufferFill >= frameSize) {
            for (int i = 0; i < frameSize; i++) {
                frame[i] = buffer[i] * windowFunction[i];;
            }

            fft.realForward(frame);

            computePower(frame);
            listener.onSpectrum(powerSpectrum);

            System.arraycopy(buffer, hopSize, buffer, 0, bufferFill - hopSize);
            bufferFill -= hopSize;
        }
    }

    private void normalize(short[] input) {
        int offset = bufferFill;

        for (int i = 0; i < input.length; i++) {
            buffer[offset + i] = (float) input[i] / 32768f;
        }
    }

    private void computePower(float[] spectrum) {
        int n = spectrum.length;

        powerSpectrum[0] = spectrum[0] * spectrum[0] / windowEnergy;

        for (int k = 1; k < n / 2; k++) {
            float re = spectrum[2 * k];
            float im = spectrum[2 * k + 1];

            float power = re * re + im * im;
            power *= 2;
            power /= windowEnergy;

            powerSpectrum[k] = power;
        }
    }
}
