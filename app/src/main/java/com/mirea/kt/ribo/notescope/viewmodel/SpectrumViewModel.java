package com.mirea.kt.ribo.notescope.viewmodel;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.mirea.kt.ribo.notescope.audio.dsp.DspRuntime;
import com.mirea.kt.ribo.notescope.audio.model.DspConfig;
import com.mirea.kt.ribo.notescope.audio.model.PitchInfo;
import com.mirea.kt.ribo.notescope.audio.model.SpectrogramSnapshot;
import com.mirea.kt.ribo.notescope.audio.util.WindowFunction;
import com.mirea.kt.ribo.notescope.storage.DspConfigRepository;

import java.util.List;
import java.util.Locale;

public class SpectrumViewModel extends ViewModel {

    private static final int UI_TEXT_UPDATE_INTERVAL_IN_MS = 200;
    private static final int UI_SPECTROGRAM_UPDATE_INTERVAL_IN_MS = 33;

    private final MutableLiveData<Integer> uiSampleRate = new MutableLiveData<>();
    private final MutableLiveData<Integer> uiFrameSize = new MutableLiveData<>();
    private final MutableLiveData<Integer> uiHopSize = new MutableLiveData<>();
    private final MutableLiveData<Integer> uiHistoryInSec = new MutableLiveData<>();
    private final MutableLiveData<WindowFunction> uiWindowFunction = new MutableLiveData<>();
    private final MutableLiveData<List<Integer>> availableSampleRate = new MutableLiveData<>();
    private final MutableLiveData<List<WindowFunction>> availableWindowFunction = new MutableLiveData<>();
    private final MutableLiveData<Boolean> allowCustomFftSize = new MutableLiveData<>();
    private final MutableLiveData<String> uiCustomFftSize = new MutableLiveData<>();

    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> infoMessage = new MutableLiveData<>();

    private final LiveData<Float> fftSizeSliderPosition = Transformations.map(
            uiFrameSize, value -> (float) Math.round(Math.max(0, (Math.log(value) / Math.log(2)) - 7f))
    );

    private final MediatorLiveData<Float> overlapSliderPosition = new MediatorLiveData<>();

    private final LiveData<Float> historyInSecSliderPosition = Transformations.map(
            uiHistoryInSec, value -> {
                if (value == null || value == 0) {
                    return 0f;
                }

                return (float) Math.round(value / 5f);
            }
    );

    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private final MutableLiveData<Boolean> isRecordingLive = new MutableLiveData<>(false);
    private final MutableLiveData<float[]> spectrumData = new MutableLiveData<>();
    private final MutableLiveData<SpectrogramSnapshot> spectrogramSnapshot = new MutableLiveData<>();
    private final MutableLiveData<PitchInfo> pitchInfo = new MutableLiveData<>();
    private final MutableLiveData<DspConfig> dspConfig = new MutableLiveData<>();

    private final DspConfig defaultDspConfig = new DspConfig(
            44100,
            4096,
            4096 / 4,
            15,
            WindowFunction.HANN
    );

    private final LiveData<String> nyquistFrequency = Transformations.map(
            uiSampleRate, value -> String.format(
                    Locale.getDefault(),
                    "Nyquist: %.02f kHz",
                    value / 2f / 1000f
            )
    );

    private final MediatorLiveData<String> windowTemporalPrams = new MediatorLiveData<>();
    private final MediatorLiveData<String> memoryUsage = new MediatorLiveData<>();
    private final MediatorLiveData<String> temporalResolution = new MediatorLiveData<>();

    private final DspConfigRepository configRepository;
    private DspRuntime dspRuntime = new DspRuntime(spectrumData::postValue);

    private final LiveData<String> peakFrequency = Transformations.map(
            pitchInfo, info -> String.format(
                    Locale.getDefault(),
                    "%.2f Hz",
                    info.frequency()
            )
    );

    private final LiveData<String> musicalNote = Transformations.map(
            pitchInfo, info -> String.format(
                    Locale.getDefault(),
                    "%s %+.2f ¢",
                    info.note(), info.cents()
            )
    );

    public SpectrumViewModel(DspConfigRepository repository) {
        this.configRepository = repository;
        loadConfig();

        overlapSliderPosition.addSource(uiFrameSize, value -> computeHopSizeSliderPosition());
        overlapSliderPosition.addSource(uiHopSize, value -> computeHopSizeSliderPosition());

        windowTemporalPrams.addSource(uiFrameSize, value -> computeWindowTemporalParams());
        windowTemporalPrams.addSource(uiSampleRate, value -> computeWindowTemporalParams());

        memoryUsage.addSource(uiSampleRate, value -> computeMemoryUsage());
        memoryUsage.addSource(uiFrameSize, value -> computeMemoryUsage());
        memoryUsage.addSource(uiHopSize, value -> computeMemoryUsage());
        memoryUsage.addSource(uiHistoryInSec, value -> computeMemoryUsage());

        temporalResolution.addSource(uiSampleRate, value -> computeTemporalResolution());
        temporalResolution.addSource(uiHopSize, value -> computeTemporalResolution());

        var pitchTask = new Runnable() {
            @Override
            public void run() {
                var latest = dspRuntime.getLatestPitch();

                if (latest != null) {
                    pitchInfo.setValue(latest);
                }
                uiHandler.postDelayed(this, UI_TEXT_UPDATE_INTERVAL_IN_MS);
            }
        };
        uiHandler.post(pitchTask);

        var spectrogramTask = new Runnable() {
            @Override
            public void run() {
                spectrogramSnapshot.setValue(dspRuntime.getSpectrogramSnapshot());
                uiHandler.postDelayed(this, UI_SPECTROGRAM_UPDATE_INTERVAL_IN_MS);
            }
        };
        uiHandler.post(spectrogramTask);
    }

    private void computeHopSizeSliderPosition() {
        Integer frameSize = uiFrameSize.getValue();
        Integer hopSize = uiHopSize.getValue();

        if (frameSize == null || hopSize == null) {
            overlapSliderPosition.setValue(0f);
            return;
        }

        float overlap = 1f - (float) hopSize / frameSize;

        if (overlap >= 0.875f) {
            overlapSliderPosition.setValue(4f);

        } else if (overlap >= 0.75f) {
            overlapSliderPosition.setValue(3f);

        } else if (overlap >= 0.50f) {
            overlapSliderPosition.setValue(2f);

        } else if (overlap >= 0.25f) {
            overlapSliderPosition.setValue(1f);

        } else {
            overlapSliderPosition.setValue(0f);
        }
    }

    private void computeWindowTemporalParams() {
        Integer sampleRate = uiSampleRate.getValue();
        Integer frameSize = uiFrameSize.getValue();

        if (sampleRate != null && frameSize != null) {
            double frequencyRes = (double) sampleRate / frameSize;
            double windowLen = 1000 / frequencyRes;

            windowTemporalPrams.setValue( String.format(
                    Locale.getDefault(),
                    "Frequency Resolution: %.02f Hz\nWindow Length: %.02f ms",
                    frequencyRes, windowLen
            ));
            return;
        }

        windowTemporalPrams.setValue("");
    }

    public void computeMemoryUsage() {
        Integer sampleRate = uiSampleRate.getValue();
        Integer frameSize = uiFrameSize.getValue();
        Integer hopSize = uiHopSize.getValue();
        Integer historyInSec = uiHistoryInSec.getValue();

        if (sampleRate != null && frameSize != null && hopSize != null && historyInSec != null) {
            long framesPerSec = sampleRate / hopSize;
            long bins = frameSize / 2;
            long bytesPerFrame = bins * 4;

            long bytes = framesPerSec * historyInSec * bytesPerFrame;
            long megabytes = bytes / 1024 / 1024;

            memoryUsage.setValue(String.format(
                    Locale.getDefault(),
                    "Estimated Memory Usage: %d MB/s",
                    megabytes * 30
            ));
            return;
        }

        memoryUsage.setValue("");
    }

    private void computeTemporalResolution() {
        Integer sampleRate = uiSampleRate.getValue();
        Integer hopSize = uiHopSize.getValue();

        if (sampleRate != null && hopSize != null) {
            double windowLen = 1000d * hopSize / sampleRate;

            temporalResolution.setValue(String.format(
                    Locale.getDefault(),
                    "Hop Size: %d samples\nTemporal Resolution: %.02f ms",
                    hopSize, windowLen
            ));
            return;
        }

        temporalResolution.setValue("");
    }

    public LiveData<Boolean> getIsRecordingLive() {
        return isRecordingLive;
    }

    public LiveData<float[]> getSpectrumData() {
        return spectrumData;
    }

    public LiveData<String> getPeakFrequency() {
        return peakFrequency;
    }

    public LiveData<String> getMusicalNote() {
        return musicalNote;
    }

    public LiveData<SpectrogramSnapshot> getSpectrogramSnapshot() {
        return spectrogramSnapshot;
    }

    public LiveData<Integer> getUiSampleRate() {
        return uiSampleRate;
    }

    public LiveData<Integer> getUiFrameSize() {
        return uiFrameSize;
    }

    public LiveData<Integer> getUiHopSize() {
        return uiHopSize;
    }

    public LiveData<Integer> getUiHistoryInSec() {
        return uiHistoryInSec;
    }

    public LiveData<WindowFunction> getUiWindowFunction() {
        return uiWindowFunction;
    }

    public LiveData<DspConfig> getDspConfig() {
        return dspConfig;
    }

    public LiveData<String> getNyquistFrequency() {
        return nyquistFrequency;
    }

    public LiveData<List<Integer>> getAvailableSampleRate() {
        return availableSampleRate;
    }

    public LiveData<List<WindowFunction>> getAvailableWindowFunction() {
        return availableWindowFunction;
    }

    public LiveData<String> getWindowTemporalPrams() {
        return windowTemporalPrams;
    }

    public LiveData<String> getTemporalResolution() {
        return temporalResolution;
    }

    public LiveData<String> getMemoryUsage() {
        return memoryUsage;
    }

    public LiveData<Float> getFftSizeSliderPosition() {
        return fftSizeSliderPosition;
    }

    public LiveData<Float> getOverlapSliderPosition() {
        return overlapSliderPosition;
    }

    public LiveData<Float> getHistoryInSecSliderPosition() {
        return historyInSecSliderPosition;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getAllowCustomFftSize() {
        return allowCustomFftSize;
    }

    public LiveData<String> getUiCustomFftSize() {
        return uiCustomFftSize;
    }

    public LiveData<String> getInfoMessage() {
        return infoMessage;
    }

    public void startRecording() {
        dspRuntime.startRecording();
        isRecordingLive.postValue(true);
    }

    public void stopRecording() {
        dspRuntime.stopRecording();
        isRecordingLive.postValue(false);
    }

    public void onRecordClicked() {
        if (Boolean.FALSE.equals(isRecordingLive.getValue())) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    public void onClearBufferClicked() {
        spectrogramSnapshot.setValue(dspRuntime.clearSpectrogramBufferAndGetSnapshot());
    }

    public void onSampleRateChoose(Integer sampleRate) {
        if (sampleRate != null) {
            uiSampleRate.setValue(sampleRate);
        }
    }

    public void onWindowFunctionChoose(WindowFunction windowFunction) {
        if (windowFunction != null) {
            uiWindowFunction.setValue(windowFunction);
        }
    }

    public void onFftSizeSlide(Float position) {
        Integer oldFrameSize = uiFrameSize.getValue();
        Integer oldHopSize = uiHopSize.getValue();

        if (position != null && oldFrameSize != null && oldHopSize != null) {
            int newFrameSize = (int) Math.pow(2, 7 + position);
            uiFrameSize.setValue(newFrameSize);

            float ratio = (float) oldHopSize/oldFrameSize;
            uiHopSize.setValue((int) (newFrameSize * ratio));
        }
    }

    public void onOverlapSlide(Float position) {
        Integer frameSize = uiFrameSize.getValue();

        if (position != null && frameSize != null) {
            uiHopSize.setValue(
                    switch (position.intValue()) {
                        case 0 -> frameSize;
                        case 1 -> frameSize * 3 / 4;
                        case 2 -> frameSize / 2;
                        case 3 -> frameSize / 4;
                        case 4 -> frameSize / 8;
                        default -> frameSize;
                    }
            );
        }
    }

    public void onAllowCustomFftSizeChecked(boolean checked) {
        allowCustomFftSize.setValue(checked);
    }

    public void onUiCustomFftSizeChanged(String text) {
        if (text != null) {
            uiCustomFftSize.setValue(text);
        }
    }

    public void onHistoryInSecSlide(Float position) {
        if (position != null) {
            if (position == 0f) {
                uiHistoryInSec.setValue(1);
                return;
            }

            uiHistoryInSec.setValue((int) (position * 5));
        }
    }

    private void loadConfig() {
        DspConfig config = configRepository.getConfig();
        dspRuntime.loadConfig(config);

        uiSampleRate.setValue(config.sampleRate());
        uiFrameSize.setValue(config.frameSize());
        uiHopSize.setValue(config.hopSize());
        uiHistoryInSec.setValue(config.historyInSec());
        uiWindowFunction.setValue(config.windowFunction());
        availableSampleRate.setValue(configRepository.getAvailableSampleRate());
        availableWindowFunction.setValue(configRepository.getAvailableWindowFunction());
    }

    public void onApplySettings() {
        isRecordingLive.setValue(false);
        Integer sampleRate = uiSampleRate.getValue();
        Integer frameSize = uiFrameSize.getValue();
        Integer hopSize = uiHopSize.getValue();
        Integer historyInSec = uiHistoryInSec.getValue();
        WindowFunction windowFunction = uiWindowFunction.getValue();

        if (
                sampleRate != null
                && frameSize != null
                && hopSize != null
                && historyInSec != null
                && windowFunction != null
        ) {

            if (Boolean.TRUE.equals(allowCustomFftSize.getValue())) {
                String customFftSize = uiCustomFftSize.getValue();

                if (customFftSize == null || customFftSize.isBlank()) {
                    errorMessage.setValue("Enter fft size or disable 'Allow custom fft size'");
                    return;
                }

                frameSize = Integer.parseInt(customFftSize);
            }


            var config = new DspConfig(
                    sampleRate,
                    frameSize,
                    hopSize,
                    historyInSec,
                    windowFunction
            );
            Log.i("CONFIG_CHANGED", String.valueOf(config));

            dspConfig.setValue(config);

            dspRuntime.reset();
            dspRuntime = new DspRuntime(spectrumData::postValue);
            dspRuntime.loadConfig(config);

            configRepository.saveConfig(config);
            infoMessage.setValue("Settings applied");
        }
    }

    public void onResetConfig() {
        uiSampleRate.setValue(defaultDspConfig.sampleRate());
        uiFrameSize.setValue(defaultDspConfig.frameSize());
        uiHopSize.setValue(defaultDspConfig.hopSize());
        uiHistoryInSec.setValue(defaultDspConfig.historyInSec());
        uiWindowFunction.setValue(defaultDspConfig.windowFunction());

        allowCustomFftSize.setValue(false);
        uiCustomFftSize.setValue("");

        onApplySettings();
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        dspRuntime.reset();
        uiHandler.removeCallbacksAndMessages(null);
    }
}