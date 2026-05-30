package com.mirea.kt.ribo.notescope.network;

import androidx.annotation.NonNull;

import com.mirea.kt.ribo.notescope.network.body.ContentType;
import com.mirea.kt.ribo.notescope.network.body.RequestBodyFactory;
import com.mirea.kt.ribo.notescope.network.callback.ResponseCallback;
import com.mirea.kt.ribo.notescope.network.exception.ApiException;
import com.mirea.kt.ribo.notescope.network.exception.NetworkException;
import com.mirea.kt.ribo.notescope.network.model.ApiBody;
import com.mirea.kt.ribo.notescope.network.model.ApiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

import org.simpleframework.xml.core.Persister;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.Map;

public class HttpService {
    private final OkHttpClient client;
    private final RequestBodyFactory factory;
    private final ObjectMapper jsonMapper;
    private final Persister xmlSerializer;

    public HttpService() {
        client = new OkHttpClient.Builder()
                .followRedirects(true)
                .build();

        jsonMapper = new ObjectMapper();
        jsonMapper.registerModule(new ParameterNamesModule());
        jsonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        xmlSerializer = new Persister();

        factory = new RequestBodyFactory();
    }

    public <T> void requestAsync(
            HttpMethod httpMethod,
            String url,
            ApiBody requestBody,
            Map<String, String> headers,
            Class<T> responseType,
            ResponseCallback<T> callback
    ) {
        var request = buildRequest(httpMethod, url, requestBody, headers);

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onError(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                callback.onSuccess(new ApiResponse<>(
                        response.code(),
                        parseResponse(response, responseType)
                ));
            }
        });
    }

    public <T> void requestAsync(
            HttpMethod httpMethod,
            String url,
            ApiBody requestBody,
            Class<T> responseType,
            ResponseCallback<T> callback
    ) {
        requestAsync(httpMethod, url, requestBody, null, responseType, callback);
    }

    public <T> void requestAsync(
            HttpMethod httpMethod,
            String url,
            Class<T> responseType,
            ResponseCallback<T> callback
    ) {
        requestAsync(httpMethod, url, null, null, responseType, callback);
    }

    public <T> ApiResponse<T> request(
            HttpMethod httpMethod,
            String url,
            ApiBody requestBody,
            Class<T> responseType,
            Map<String, String> headers
    ) {
        var request = buildRequest(httpMethod, url, requestBody, headers);

        try (var response = client.newCall(request).execute()) {

            return new ApiResponse<>(
                    response.code(),
                    parseResponse(response, responseType)
            );

        } catch (IOException e) {
            throw new NetworkException(e.getMessage());
        }
    }

    public <T> ApiResponse<T> request(HttpMethod method, String url,  ApiBody requestBody, Class<T> responseType) {
        return request(method, url, requestBody, responseType, null);
    }

    public <T> ApiResponse<T> request(HttpMethod method, String url, Class<T> responseType) {
        return request(method, url, null, responseType, null);
    }

    private Request buildRequest(
            HttpMethod httpMethod,
            String url,
            ApiBody requestBody,
            Map<String, String> headers
    ) {
        var request = new Request.Builder()
                .url(url);

        switch (httpMethod) {
            case GET -> request.get();
            case POST -> request.post(factory.createBody(requestBody));
            case PUT -> request.put(factory.createBody(requestBody));
            case DELETE -> {
                if (requestBody == null) {
                    request.delete();
                } else {
                    request.delete(factory.createBody(requestBody));
                }
            }
            case PATCH -> request.patch(factory.createBody(requestBody));
        }

        if (headers != null) {
            headers.forEach(request::addHeader);
        }

        return request.build();
    }

    private <T> T parseResponse(
            Response response,
            Class<T> responseType
    ) {
        var contentType = ContentType.fromHeader(
                response.header("Content-Type")
        );

        String responseBody;
        try {
            responseBody = response.body().string();
        } catch (IOException e) {
            throw new NetworkException("Response body is null");
        }

        if (contentType == null) {
            throw new NetworkException(
                    "Unsupported content type: " +
                    response.header("Content-type")
            );
        }

        try {
            return switch (contentType) {

                // ./images/xd.png
                case JSON, HTML -> jsonMapper.readValue(responseBody, responseType);

                case SOAP_XML, XML -> parseXml(responseBody, responseType);

                default -> throw new ApiException(
                        "Unsupported response type: " + contentType
                );
            };
        } catch (JsonProcessingException e) {
            throw new NetworkException(
                    "An error was occurred while parsing data: "
                            + responseBody
            );
        }
    }

    private <T> T parseXml(String xml, Class<T> type) {
        try {
            xml = unwrapSoap(xml);

            return xmlSerializer.read(type, xml);

        } catch (Exception e) {
            throw new NetworkException(
                    "XML parsing error: " + e.getMessage()
            );
        }
    }

    private String unwrapSoap(String xml) {
        int start = xml.indexOf("<soap:Body>");

        if (start == -1) {
            return xml;
        }

        xml = xml.substring(start);

        int innerStart = xml.indexOf('>') + 1;
        int end = xml.indexOf("</soap:Body>");

        if (end == -1) {
            return xml;
        }

        return xml.substring(innerStart, end).trim();
    }
}
