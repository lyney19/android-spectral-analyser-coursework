package com.mirea.kt.ribo.notescope.ui.state.login;

import com.mirea.kt.ribo.notescope.model.Task;

public sealed interface LoginUiState {

    final class Loading implements LoginUiState {}

    record Success(Task task) implements LoginUiState {}

    record InputError(int messageId) implements LoginUiState {}

    record NetworkError(String message) implements LoginUiState {}
}