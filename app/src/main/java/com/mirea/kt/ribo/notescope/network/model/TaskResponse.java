package com.mirea.kt.ribo.notescope.network.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "GetTaskResponse", strict = false)
public class TaskResponse {

    @Element(name = "title", required = false)
    private String title;

    @Element(name = "task", required = false)
    private String task;

    @JsonProperty("error_message")
    @Element(name = "error", required = false)
    private String errorMessage;

    @JsonProperty("result_code")
    @Element(name = "result_code", required = false)
    private int resultCode;

    @Element(name = "variant", required = false)
    private int variant;

    public TaskResponse() {
    }

    public String getTitle() {
        return title;
    }

    public String getTask() {
        return task;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public int getResultCode() {
        return resultCode;
    }

    public int getVariant() {
        return variant;
    }

    @Override
    public String toString() {
        return "TaskResponse[" +
                "title='" + title + '\'' +
                ", task='" + task + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                ", resultCode=" + resultCode +
                ", variant=" + variant +
                ']';
    }
}