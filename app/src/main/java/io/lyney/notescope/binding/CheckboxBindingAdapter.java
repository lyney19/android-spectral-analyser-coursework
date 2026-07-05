package io.lyney.notescope.binding;

import android.widget.CheckBox;

import androidx.databinding.BindingAdapter;

import io.lyney.notescope.binding.listener.CheckboxChanged;

public class CheckboxBindingAdapter {

    @BindingAdapter("checkedChanged")
    public static void setChecked(CheckBox checkbox, CheckboxChanged listener) {
        if (listener != null) {
            checkbox.setOnCheckedChangeListener((view, isChecked) -> {
                listener.onChanged(isChecked);
            });
        }
    }

}
