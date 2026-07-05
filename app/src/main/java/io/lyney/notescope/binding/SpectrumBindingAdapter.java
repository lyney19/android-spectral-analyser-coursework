package io.lyney.notescope.binding;

import androidx.databinding.BindingAdapter;

import io.lyney.notescope.ui.view.MaterialSpectrumView;

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