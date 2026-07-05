package io.lyney.notescope.network.callback;

import io.lyney.notescope.network.model.ApiResponse;

public interface ResponseCallback<T> {
    void onSuccess(ApiResponse<T> response);

    void onError(Throwable error);
}
