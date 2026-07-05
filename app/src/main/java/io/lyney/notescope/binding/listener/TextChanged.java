package io.lyney.notescope.binding.listener;

@FunctionalInterface
public interface TextChanged {
    void onChanged(String text);
}
