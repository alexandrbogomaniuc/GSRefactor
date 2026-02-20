package com.dgphoenix.casino.controller.frbonus.transport;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ErrorResponse extends AbstractResponse {
    @JsonProperty("error")
    private String errorDescription;

    public ErrorResponse(String errorDescription) {
        super(false);
        this.errorDescription = errorDescription;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    @Override
    public String toString() {
        return "ErrorResponse{" +
                "success=" + success +
                ", errorDescription='" + errorDescription + '\'' +
                '}';
    }
}
