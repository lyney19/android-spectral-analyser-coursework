package com.mirea.kt.ribo.notescope.ui.fragment;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.mirea.kt.ribo.notescope.R;
import com.mirea.kt.ribo.notescope.audio.util.WindowFunction;
import com.mirea.kt.ribo.notescope.databinding.FragmentSettingsBinding;
import com.mirea.kt.ribo.notescope.ui.view.SampleRateAdapter;
import com.mirea.kt.ribo.notescope.viewmodel.SpectrumViewModel;

import java.util.ArrayList;
import java.util.Locale;

public class SettingsFragment extends Fragment {

    private SpectrumViewModel viewModel;
    private FragmentSettingsBinding binding;
    private SampleRateAdapter sampleRateAdapter;
    private ArrayAdapter<WindowFunction> windowFunctionAdapter;

    public SettingsFragment() {
        super(R.layout.fragment_settings);
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.i("SETTINGS_FRAGMENT", "SettingsFragment created");

        viewModel = new ViewModelProvider(requireActivity()).get(SpectrumViewModel.class);

        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(getViewLifecycleOwner());

        setupDropdowns();
        observeViewModel();
    }

    private void setupDropdowns() {
        sampleRateAdapter = new SampleRateAdapter(
                requireContext(),
                new ArrayList<>()
        );
        binding.dropdownSampleRate.setAdapter(sampleRateAdapter);
        binding.dropdownSampleRate.setOnClickListener(view ->
                binding.dropdownSampleRate.showDropDown()
        );

        binding.dropdownSampleRate.setOnItemClickListener(
                (parent, view, position, id) -> {
                    var selected = (int) parent.getItemAtPosition(position);
                    viewModel.onSampleRateChoose(selected);
                }
        );

        windowFunctionAdapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.dropdown_item,
                new ArrayList<>()
        );
        binding.dropdownWindowFunction.setAdapter(windowFunctionAdapter);
        binding.dropdownWindowFunction.setOnClickListener(view ->
                binding.dropdownWindowFunction.showDropDown()
        );

        binding.dropdownWindowFunction.setOnItemClickListener(
                (parent, view, position, id) -> {
                    var selected = (WindowFunction) parent.getItemAtPosition(position);
                    viewModel.onWindowFunctionChoose(selected);
                }
        );
    }

    private void observeViewModel() {
        viewModel.getAvailableSampleRate().observe(
                getViewLifecycleOwner(),
                availableSampleRate -> {
                    sampleRateAdapter.clear();
                    sampleRateAdapter.addAll(availableSampleRate);
                    sampleRateAdapter.notifyDataSetChanged();
                }
        );

        viewModel.getUiSampleRate().observe(
                getViewLifecycleOwner(),
                uiSampleRate -> binding.dropdownSampleRate.setText(
                        String.format(Locale.getDefault(), "%d Hz", uiSampleRate),
                        false
                )
        );

        viewModel.getAvailableWindowFunction().observe(
                getViewLifecycleOwner(),
                availableWindowFunction -> {
                    windowFunctionAdapter.clear();
                    windowFunctionAdapter.addAll(availableWindowFunction);
                    windowFunctionAdapter.notifyDataSetChanged();
                }
        );

        viewModel.getUiWindowFunction().observe(
                getViewLifecycleOwner(),
                uiWindowFunction -> binding.dropdownWindowFunction.setText(
                        uiWindowFunction.toString(),
                        false
                )
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;

        Log.i("SETTINGS_FRAGMENT", "SettingsFragment destroyed");
    }
}