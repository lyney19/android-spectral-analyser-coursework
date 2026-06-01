package com.mirea.kt.ribo.notescope.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.mirea.kt.ribo.notescope.R;
import com.mirea.kt.ribo.notescope.model.Task;
import com.mirea.kt.ribo.notescope.network.LoginApiClient;
import com.mirea.kt.ribo.notescope.network.callback.ResponseCallback;
import com.mirea.kt.ribo.notescope.network.exception.ApiException;
import com.mirea.kt.ribo.notescope.network.model.ApiResponse;
import com.mirea.kt.ribo.notescope.ui.state.login.LoginUiState;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class LoginViewModel extends ViewModel {
    private final MutableLiveData<String> username = new MutableLiveData<>("");
    private final MutableLiveData<String> password = new MutableLiveData<>("");
    private final MutableLiveData<String> group = new MutableLiveData<>("RIBO-04-24");
    private final MutableLiveData<LoginUiState> loginState = new MutableLiveData<>();

    private final LoginApiClient loginApiClient = new LoginApiClient();

    public LiveData<String> getUsername() {
        return username;
    }

    public LiveData<String> getPassword() {
        return password;
    }

    public LiveData<LoginUiState> getLoginState() {
        return loginState;
    }

    public void onUsernameChanged(String text) {
        username.setValue(text);
    }

    public void onPasswordChanged(String text) {
        password.setValue(text);
    }

    public void onLoginClicked() {
        Log.i("LoginViewModel", "Login button clicked");

        var username = this.username.getValue();
        var password = this.password.getValue();
        var group = this.group.getValue();

        if (
                username == null || username.isBlank()
                || password == null || password.isBlank()
                || group == null || group.isBlank()
        ) {
            loginState.setValue(
                    new LoginUiState.InputError((R.string.blank_inputs))
            );
            return;
        }

        loginState.setValue(new LoginUiState.Loading());

        loginApiClient.login(
                username,
                password,
                group,
                new ResponseCallback<Task>() {
                    @Override
                    public void onSuccess(ApiResponse<Task> response) {
                        loginState.postValue(
                                new LoginUiState.Success(response.body())
                        );
                    }

                    @Override
                    public void onError(Throwable error) {
                        if (error instanceof UnknownHostException) {
                            loginState.postValue(
                                    new LoginUiState.NetworkError(R.string.unknown_host_exception)
                            );
                        } else if (error instanceof SocketTimeoutException) {
                            loginState.postValue(
                                    new LoginUiState.NetworkError(R.string.socket_timeout_exception)
                            );
                        } else if (error instanceof ApiException) {
                            loginState.postValue(
                                    new LoginUiState.NetworkError(R.string.api_exception)
                            );
                        } else {
                            Log.w("LOGIN_VIEW_MODEL", String.valueOf(error));
                            loginState.postValue(
                                    new LoginUiState.NetworkError(R.string.unknown_network_exception)
                            );
                        }
                    }
                }
        );
    }
}
