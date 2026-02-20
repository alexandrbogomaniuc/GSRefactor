package com.dgphoenix.casino.controller.frbonus.transport;

import com.google.gson.annotations.SerializedName;

public class SuccessResponse extends AbstractResponse {
    @SerializedName("message")
    private String messageDescription;

    public SuccessResponse(String messageDescription) {
        super(true);
        this.messageDescription = messageDescription;
    }

    public String getMessageDescription() {
        return messageDescription;
    }

    @Override
    public String toString() {
        return "SuccessResponse{" +
                "success=" + success +
                ", messageDescription='" + messageDescription + '\'' +
                '}';
    }
}
