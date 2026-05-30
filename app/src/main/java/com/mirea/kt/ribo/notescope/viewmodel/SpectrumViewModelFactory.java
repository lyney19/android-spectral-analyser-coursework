package com.mirea.kt.ribo.notescope.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.mirea.kt.ribo.notescope.storage.DspConfigRepository;

public class SpectrumViewModelFactory implements ViewModelProvider.Factory {
    private final DspConfigRepository repository;

    public SpectrumViewModelFactory(DspConfigRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {

        if (modelClass.isAssignableFrom(SpectrumViewModel.class)) {
            return (T) new SpectrumViewModel(repository);
        }

        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
