package io.lyney.notescope.binding;

import androidx.databinding.BindingAdapter;

import com.google.android.material.slider.Slider;
import io.lyney.notescope.binding.listener.SliderChanged;

public class SliderBindingAdapter {
    @BindingAdapter("sliderMode")
    public static void setSliderMode(Slider slider, SliderMode mode) {
        switch (mode) {
            case FFT_SIZE -> slider.setLabelFormatter(
                    value -> String.valueOf((int) Math.pow(2, 7 + value))
            );

            case OVERLAP -> slider.setLabelFormatter(
                    value -> switch((int) value) {
                        case 0 -> "0%";
                        case 1 -> "25%";
                        case 2 -> "50%";
                        case 3 -> "75%";
                        case 4 -> "87.5%";
                        default -> "?";
                    }
            );

            case SPECTROGRAM_LENGTH -> slider.setLabelFormatter(
                    value -> switch ((int) value) {
                        case 0 -> "1";
                        default -> String.valueOf((int) (5 * value));
                    }
            );
        }
    }

    @BindingAdapter("onValueChanged")
    public static void setOnValueChangeListener(Slider slider, final SliderChanged listener) {
        slider.clearOnChangeListeners();

        slider.addOnChangeListener((s, value, fromUser) -> {
            if (fromUser && listener != null) {
                listener.onChanged(value, true);
            }
        });
    }

    @BindingAdapter("value")
    public static void setSliderValue(Slider slider, Float value) {
        if (value != null && Math.abs(slider.getValue() - value) > 0.0001f) {
            slider.setValue(value);
        }
    }
}
