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

import com.mirea.kt.ribo.notescope.databinding.FragmentHistorySpectrumBinding;
import com.mirea.kt.ribo.notescope.permission.PermissionHelper;
import com.mirea.kt.ribo.notescope.viewmodel.SpectrumViewModel;

public class HistorySpectrumFragment extends Fragment {

    private FragmentHistorySpectrumBinding binding;
    private SpectrumViewModel viewModel;
    private PermissionHelper permissionHelper;

    public static HistorySpectrumFragment newInstance() {
        return new HistorySpectrumFragment();
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentHistorySpectrumBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.i("HISTORY_SPECTRUM_FRAGMENT", "HistorySpectrumFragment created");

        viewModel = new ViewModelProvider(requireActivity()).get(SpectrumViewModel.class);

        permissionHelper = new PermissionHelper();

        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(getViewLifecycleOwner());

        binding.btnRecord.setOnClickListener(v -> {
            if (permissionHelper.askAudioRecordPermission(requireActivity())) {
                viewModel.onRecordClicked();
            }
        });

        viewModel.getSpectrogramSnapshot().observe(
                getViewLifecycleOwner(),
                snapshot -> {
                    binding.spectrogramView.submit(
                            snapshot.data(),
                            snapshot.bins(),
                            snapshot.history(),

                            snapshot.validFrames(),

                            snapshot.startTime(),
                            snapshot.endTime()
                    );
                }
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;

        Log.i("HISTORY_SPECTRUM_FRAGMENT", "HistorySpectrumFragment destroyed");
    }

}