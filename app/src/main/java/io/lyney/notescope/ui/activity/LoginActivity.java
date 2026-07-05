package io.lyney.notescope.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import io.lyney.notescope.databinding.ActivityLoginBinding;
import io.lyney.notescope.ui.state.login.LoginUiState;
import io.lyney.notescope.viewmodel.LoginViewModel;
import io.lyney.notescope.R;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private LoginViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        Log.i("LOGIN_ACTIVITY", "LoginActivity created");

        binding = DataBindingUtil.setContentView(
                this,
                R.layout.activity_login
        );

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(this);

        viewModel.getLoginState().observe(this, state -> {

            if (state instanceof LoginUiState.Loading) {
                binding.btnLogin.setEnabled(false);
                binding.btnLogin.setText("");
                binding.progressLogin.setVisibility(View.VISIBLE);
            } else {
                binding.btnLogin.setEnabled(true);
                binding.btnLogin.setText(getString(R.string.log_in));
                binding.progressLogin.setVisibility(View.GONE);
            }

            if (state instanceof LoginUiState.Success success) {
                var intent = new Intent(this, TaskInfoActivity.class);
                intent.putExtra("task", success.task());
                startActivity(intent);
            }

            if (state instanceof LoginUiState.NetworkError error) {
                Toast.makeText(this, getString(error.messageId()), Toast.LENGTH_LONG).show();
            }

            if (state instanceof LoginUiState.InputError error) {
                Toast.makeText(this, getString(error.messageId()), Toast.LENGTH_LONG).show();
            }
        });
    }
}