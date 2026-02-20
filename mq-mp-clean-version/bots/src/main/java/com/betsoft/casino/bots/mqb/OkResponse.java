package com.betsoft.casino.bots.mqb;

/**
 * User: flsh
 * Date: 13.07.2022.
 */
public class OkResponse {
    private String status;

    public OkResponse() {
    }

    public OkResponse(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public boolean isSuccess() {
        return "ok".equalsIgnoreCase(status) || "successful".equalsIgnoreCase(status);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("OkResponse [");
        sb.append("status='").append(status).append('\'');
        sb.append(']');
        return sb.toString();
    }
}
