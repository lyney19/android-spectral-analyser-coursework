package io.lyney.notescope.ui.activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import io.lyney.notescope.R;
import io.lyney.notescope.storage.DspConfigRepository;
import io.lyney.notescope.ui.fragment.HistorySpectrumFragment;
import io.lyney.notescope.ui.fragment.LiveSpectrumFragment;
import io.lyney.notescope.ui.fragment.SettingsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import io.lyney.notescope.viewmodel.SpectrumViewModel;
import io.lyney.notescope.viewmodel.SpectrumViewModelFactory;

public class SpectrumActivity extends AppCompatActivity {

    private SpectrumViewModel viewModel;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        Log.i("SPECTRUM_ACTIVITY", "SpectrumActivity created");

        setContentView(R.layout.activity_spectrum);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        ViewCompat.setOnApplyWindowInsetsListener(bottomNavigationView, (view, windowInsets) -> {
             view.setPadding(view.getPaddingLeft(), view.getPaddingTop(), view.getPaddingRight(), 0);
             return WindowInsetsCompat.CONSUMED;
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        var dspConfigRepository = new DspConfigRepository(getApplicationContext());
        var viewModelFactory = new SpectrumViewModelFactory(dspConfigRepository);

        viewModel = new ViewModelProvider(this, viewModelFactory).get(SpectrumViewModel.class);

        viewModel.onFragmentUpdate(new LiveSpectrumFragment());
        bottomNavigationView.setOnItemSelectedListener(item -> {

            Fragment selectedFragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_live) {
                selectedFragment = new LiveSpectrumFragment();
            } else if (id == R.id.nav_history) {
                selectedFragment = new HistorySpectrumFragment();
            } else if (id == R.id.nav_settings) {
                selectedFragment = new SettingsFragment();
            }

            viewModel.onFragmentUpdate(selectedFragment);

            return selectedFragment != null;
        });

        viewModel.getCurrentFragment().observe(this, fragment -> {
            if (fragment != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit();
            }
        });

        viewModel.getErrorMessage().observe(this, message -> {
            if (message != null && !message.isBlank()) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getInfoMessage().observe(this, message -> {
            if (message != null && !message.isBlank()) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        });
    }
}