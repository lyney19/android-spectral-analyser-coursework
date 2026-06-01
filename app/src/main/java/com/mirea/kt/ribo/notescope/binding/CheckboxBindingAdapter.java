package com.mirea.kt.ribo.notescope.binding;

import android.widget.CheckBox;

import androidx.databinding.BindingAdapter;

import com.mirea.kt.ribo.notescope.binding.listener.CheckboxChanged;

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
