package com.mirea.kt.ribo.notescope.network.callback;

import com.mirea.kt.ribo.notescope.network.model.ApiResponse;

public interface ResponseCallback<T> {
    void onSuccess(ApiResponse<T> response);

    void onError(Throwable error);
}
