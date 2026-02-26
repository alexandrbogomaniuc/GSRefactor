package com.abs.casino.controller.frbonus.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.beans.ConstructorProperties;

public class BaseRequest<T> {
    @Valid
    @NotNull(message = "missing required object auth")
    private final Auth authInfo;

    @Valid
    @NotNull(message = "missing required object request")
    @JsonProperty("request")
    private final T request;

    @ConstructorProperties({"auth", "request"})
    public BaseRequest(Auth authInfo, T request) {
        this.authInfo = authInfo;
        this.request = request;
    }

    public Auth getAuthInfo() {
        return authInfo;
    }

    public T getRequest() {
        return request;
    }

    @Override
    public String toString() {
        return "BaseRequest{" +
                "authInfo=" + authInfo +
                ", request=" + request +
                '}';
    }
}
