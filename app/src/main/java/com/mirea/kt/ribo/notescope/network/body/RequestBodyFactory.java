package com.mirea.kt.ribo.notescope.network.body;

import com.mirea.kt.ribo.notescope.network.model.ApiBody;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class RequestBodyFactory {
    private static final ObjectMapper jsonMapper = new ObjectMapper();

    public RequestBody createBody(ApiBody apiBody) {
        if (apiBody.bodyType() == ContentType.FORM_URLENCODE) {
            var builder = new FormBody.Builder();

            if (apiBody.body() instanceof Map<?, ?> data) {
                boolean valid = data.entrySet().stream()
                        .allMatch(e ->
                                e.getKey() instanceof String
                                        && e.getValue() instanceof String
                        );

                if (valid) {
                    data.forEach((k, v) ->
                            builder.add(
                                    (String) k,
                                    (String) v
                            )
                    );
                } else {
                    throw new IllegalArgumentException(
                            "Parameters is not valid: " + apiBody.body()
                    );
                }
            } else {
                throw new IllegalArgumentException(
                        "Body should be Map<String, String>"
                );
            }

            return builder.build();

        } else if (apiBody.bodyType() == ContentType.JSON) {
            String json;
            try {
                json = jsonMapper.writeValueAsString(apiBody.body());

            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException(
                        "Invalid data object: " + apiBody.body()
                );
            }

            return RequestBody.create(
                    json,
                    MediaType.parse(apiBody.bodyType().toString())
            );
        }

        throw new IllegalStateException(
                "Unsupported body type: " + apiBody.bodyType()
        );
    }
}
