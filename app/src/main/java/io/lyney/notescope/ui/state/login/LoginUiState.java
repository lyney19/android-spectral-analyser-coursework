package io.lyney.notescope.ui.state.login;

import io.lyney.notescope.model.Task;

public sealed interface LoginUiState {

    final class Loading implements LoginUiState {}

    record Success(Task task) implements LoginUiState {}

    record InputError(int messageId) implements LoginUiState {}

    record NetworkError(int messageId) implements LoginUiState {}
}