package com.mirea.kt.ribo.notescope.bindings.listener;

@FunctionalInterface
public interface SliderChanged {
    void onChanged(float value, boolean fromUser);
}
