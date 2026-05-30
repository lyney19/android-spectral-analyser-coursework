package com.mirea.kt.ribo.notescope.ui.activity;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.mirea.kt.ribo.notescope.R;
import com.mirea.kt.ribo.notescope.storage.DspConfigRepository;
import com.mirea.kt.ribo.notescope.ui.fragment.HistorySpectrumFragment;
import com.mirea.kt.ribo.notescope.ui.fragment.LiveSpectrumFragment;
import com.mirea.kt.ribo.notescope.ui.fragment.SettingsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.mirea.kt.ribo.notescope.viewmodel.SpectrumViewModel;
import com.mirea.kt.ribo.notescope.viewmodel.SpectrumViewModelFactory;

public class SpectrumActivity extends AppCompatActivity {

    private SpectrumViewModel viewModel;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

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

        loadFragment(new LiveSpectrumFragment());

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

            return loadFragment(selectedFragment);
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

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }
}