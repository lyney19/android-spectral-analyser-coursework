package io.lyney.notescope.binding.listener;

@FunctionalInterface
public interface SliderChanged {
    void onChanged(float value, boolean fromUser);
}
