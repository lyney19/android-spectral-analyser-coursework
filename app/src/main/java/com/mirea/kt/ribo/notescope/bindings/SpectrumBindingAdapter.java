package com.mirea.kt.ribo.notescope.bindings;

import androidx.databinding.BindingAdapter;

import com.mirea.kt.ribo.notescope.ui.view.MaterialSpectrumView;

public class SpectrumBindingAdapter {

    @BindingAdapter("spectrumSampleRate")
    public static void setSampleRate(MaterialSpectrumView view, Integer value) {
        view.setSampleRate(value);
    }

    @BindingAdapter("spectrumFrameSize")
    public static void setFrameSize(MaterialSpectrumView view, Integer value) {
        view.setFrameSize(value);
    }

}