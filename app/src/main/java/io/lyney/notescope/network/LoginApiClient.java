package io.lyney.notescope.network;

import io.lyney.notescope.model.Task;
import io.lyney.notescope.network.body.ContentType;
import io.lyney.notescope.network.callback.ResponseCallback;
import io.lyney.notescope.network.exception.ApiException;
import io.lyney.notescope.network.exception.NetworkException;
import io.lyney.notescope.network.model.ApiBody;
import io.lyney.notescope.network.model.ApiResponse;
import io.lyney.notescope.network.model.TaskResponse;

import java.util.Map;

public class LoginApiClient {
    private static final String API_ADDRESS = "https://android-for-students.ru/coursework/login.php";
    private final HttpService httpService;

    public LoginApiClient() {
        this.httpService = new HttpService();
    }

    public void login(
            String login,
            String password,
            String group,
            ResponseCallback<Task> callback
    ) {

        httpService.requestAsync(
                HttpMethod.POST,
                API_ADDRESS,
                new ApiBody(
                        ContentType.FORM_URLENCODE,
                        Map.of(
                                "lgn", login,
                                "pwd", password,
                                "g", group
                        )
                ),
                TaskResponse.class,
                new ResponseCallback<TaskResponse>() {

                    @Override
                    public void onSuccess(ApiResponse<TaskResponse> response) {
                        var dto = response.body();

                        if (dto.getResultCode() == -1) {
                            callback.onError(
                                    new ApiException(dto.getErrorMessage())
                            );
                            return;
                        }

                        var task = new Task(
                                dto.getTitle(),
                                dto.getTask(),
                                dto.getVariant()
                        );

                        if (response.code() == 200) {
                            callback.onSuccess(
                                    new ApiResponse<>(
                                            response.code(),
                                            task
                                    )
                            );
                        } else {
                            callback.onError(new NetworkException("Server response code is not 200: " + response.code()));
                        }
                    }

                    @Override
                    public void onError(Throwable error) {
                        callback.onError(error);
                    }
                }
        );
    }

}
