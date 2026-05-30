package com.mirea.kt.ribo.notescope.storage;

import android.content.Context;
import android.content.SharedPreferences;

import com.mirea.kt.ribo.notescope.audio.model.DspConfig;
import com.mirea.kt.ribo.notescope.audio.util.WindowFunction;

import java.util.List;

public class DspConfigRepository {
    private static final String PREFS_NAME = "dsp_settings";

    private static final String KEY_SAMPLE_RATE = "sample_rate";
    private static final String KEY_FRAME_SIZE = "frame_size";
    private static final String KEY_HOP_SIZE = "hop_size";
    private static final String KEY_HISTORY_IN_SEC = "history_in_sec";
    private static final String KEY_WINDOW_FUNCTION = "window_function";

    private final SharedPreferences prefs;

    public DspConfigRepository(Context context) {
        prefs = context.getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
        );
    }

    public DspConfig getConfig() {
        return new DspConfig(
                prefs.getInt(KEY_SAMPLE_RATE, 44100),
                prefs.getInt(KEY_FRAME_SIZE, 4096),
                prefs.getInt(KEY_HOP_SIZE, 4096 / 4),
                prefs.getInt(KEY_HISTORY_IN_SEC, 15),
                WindowFunction.valueOf(
                        prefs.getString(KEY_WINDOW_FUNCTION, WindowFunction.HANN.name())
                )
        );
    }

    public void saveConfig(DspConfig config) {
        prefs.edit()
                .putInt(KEY_SAMPLE_RATE, config.sampleRate())
                .putInt(KEY_FRAME_SIZE, config.frameSize())
                .putInt(KEY_HOP_SIZE, config.hopSize())
                .putInt(KEY_HISTORY_IN_SEC, config.historyInSec())
                .putString(KEY_WINDOW_FUNCTION, config.windowFunction().name())
                .apply();
    }

    public List<WindowFunction> getAvailableWindowFunction() {
        return List.of(
                WindowFunction.HANN
        );
    }

    public List<Integer> getAvailableSampleRate() {
        return List.of(
                8000, 16000, 22050,
                44100, 48000, 96000
        );
    }

}
