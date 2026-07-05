package io.lyney.notescope.binding;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.databinding.BindingAdapter;

import io.lyney.notescope.binding.listener.TextChanged;

public class TextBindingAdapter {
    @BindingAdapter("textChanged")
    public static void bindText(EditText view, TextChanged listener) {

        view.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (listener != null) {
                    listener.onChanged(s.toString());
                }
            }

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
    }
}
