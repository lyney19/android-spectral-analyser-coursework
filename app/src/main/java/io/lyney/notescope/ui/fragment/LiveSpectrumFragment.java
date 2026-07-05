package io.lyney.notescope.ui.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import io.lyney.notescope.R;
import io.lyney.notescope.databinding.FragmentLiveSpectrumBinding;
import io.lyney.notescope.permission.PermissionHelper;
import io.lyney.notescope.viewmodel.SpectrumViewModel;

public class LiveSpectrumFragment extends Fragment {

    private FragmentLiveSpectrumBinding binding;
    private SpectrumViewModel viewModel;
    private PermissionHelper permissionHelper;

    public LiveSpectrumFragment() {
        super(R.layout.fragment_live_spectrum);
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentLiveSpectrumBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.i("LIVE_SPECTRUM_FRAGMENT", "LiveSpectrumFragment created");

        viewModel = new ViewModelProvider(requireActivity()).get(SpectrumViewModel.class);

        permissionHelper = new PermissionHelper();

        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(getViewLifecycleOwner());

        viewModel.getSpectrumData().observe(
                getViewLifecycleOwner(),
                spectrum -> binding.fftView.setSpectrum(spectrum)
        );

        binding.btnRecord.setOnClickListener(v -> {
            if (permissionHelper.askAudioRecordPermission(requireActivity())) {
                viewModel.onRecordClicked();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;

        Log.i("LIVE_SPECTRUM_FRAGMENT", "LiveSpectrumFragment destroyed");
    }
}