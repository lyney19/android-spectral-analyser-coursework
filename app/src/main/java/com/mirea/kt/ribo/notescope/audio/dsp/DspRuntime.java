package com.mirea.kt.ribo.notescope.audio.dsp;

import com.mirea.kt.ribo.notescope.audio.listener.SpectrumListener;
import com.mirea.kt.ribo.notescope.audio.model.DspConfig;
import com.mirea.kt.ribo.notescope.audio.model.PitchInfo;
import com.mirea.kt.ribo.notescope.audio.model.SpectrogramSnapshot;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DspRuntime {
    private int sampleRate = 44100;
    private int frameSize = 4096;
    private int hopSize = frameSize / 4;
    private int historyInSec = 15;

    private AudioRecorder audioRecorder;
    private SpectrumProcessor spectrumProcessor;
    private PitchDetector pitchDetector;
    private SpectrogramBuffer spectrogramBuffer;
    private SpectrumAnalyser spectrumAnalyser;

    private final SpectrumListener spectrumListener;

    private volatile PitchInfo latestPitch;

    private volatile boolean isRunning = false;
    private final BlockingQueue<short[]> audioQueue = new ArrayBlockingQueue<>(16);
    private final ExecutorService recorderThread = Executors.newSingleThreadExecutor();
    private ExecutorService processorThread;

    public DspRuntime(SpectrumListener spectrumListener) {
        this.spectrumListener = spectrumListener;
        buildPipeline();
    }

    public PitchInfo getLatestPitch() {
        return latestPitch;
    }

    public SpectrogramSnapshot getSpectrogramSnapshot() {
        return spectrogramBuffer.snapshot();
    }

    public SpectrogramSnapshot clearSpectrogramBufferAndGetSnapshot() {
        spectrogramBuffer.clear();
        return spectrogramBuffer.snapshot();
    }

    public void loadConfig(DspConfig config) {
        if (config != null) {
            sampleRate = config.sampleRate();
            frameSize = config.frameSize();
            hopSize = config.hopSize();
            historyInSec = config.historyInSec();
        }

        reload();
    }

    public void startRecording() {
        recorderThread.submit(() -> {
            audioRecorder = new AudioRecorder(
                    sampleRate,
                    data -> {
                        while (!audioQueue.offer(data)) {
                            audioQueue.poll();
                        }
                    }
            );

            audioRecorder.startRecording();
        });
    }

    public void stopRecording() {
        if (audioRecorder != null) {
            audioRecorder.stopRecording();
        }
        spectrumProcessor.reset();
        audioQueue.clear();
    }

    private void startProcessingLoop() {
        if (isRunning) return;

        isRunning = true;
        processorThread.submit(() -> {
            while (isRunning && !Thread.currentThread().isInterrupted()) {
                try {
                    short[] chunk = audioQueue.take();
                    spectrumAnalyser.addSamples(chunk);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

    private void stopPipeline() {
        stopRecording();

        if (processorThread != null) {
                processorThread.shutdownNow();
            try {
                processorThread.awaitTermination(200, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void buildPipeline() {
        pitchDetector = new PitchDetector(frameSize, sampleRate);
        spectrumProcessor = new SpectrumProcessor(frameSize);

        spectrogramBuffer = new SpectrogramBuffer(
                frameSize / 2,
                (sampleRate / hopSize) * historyInSec,
                sampleRate,
                hopSize
        );

        spectrumAnalyser = new SpectrumAnalyser(
                frameSize,
                spectrum -> {
                    float[] processed = spectrumProcessor.process(spectrum);

                    latestPitch = pitchDetector.detect(spectrum);
                    spectrogramBuffer.push(spectrum);
                    spectrumListener.onSpectrum(processed);
                }
        );

        processorThread = Executors.newSingleThreadExecutor();
    }

    private synchronized void reload() {
        stopPipeline();
        buildPipeline();
        startProcessingLoop();
    }

    public void reset() {
        isRunning = false;
        recorderThread.shutdownNow();
        processorThread.shutdownNow();

        stopRecording();
        spectrogramBuffer.clear();
    }
}
