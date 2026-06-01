package com.mirea.kt.ribo.notescope.binding.listener;

@FunctionalInterface
public interface SliderChanged {
    void onChanged(float value, boolean fromUser);
}
