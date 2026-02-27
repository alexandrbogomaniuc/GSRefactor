package com.abs.casino.common.client.canex.request.privateroom;

import com.abs.casino.common.client.canex.request.CanexJsonRequest;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class InvitePlayersRequest extends CanexJsonRequest {
    @JsonProperty("externalIds")
    @SerializedName("externalIds")
    private List<String> externalIds;


    @JsonProperty("privateRoomId")
    @SerializedName("privateRoomId")
    private String privateRoomId;

    public InvitePlayersRequest() {
    }

    public InvitePlayersRequest(List<String> externalIds, String privateRoomId) {
        this.externalIds = externalIds;
        this.privateRoomId = privateRoomId;
    }

    public List<String> getExternalIds() {
        return externalIds;
    }

    public void setExternalIds(List<String> externalIds) {
        this.externalIds = externalIds;
    }

    public String getPrivateRoomId() {
        return privateRoomId;
    }

    public void setPrivateRoomId(String privateRoomId) {
        this.privateRoomId = privateRoomId;
    }

    @Override
    public String toString() {
        return "InvitePlayersRequest{" +
                "externalIds=" + externalIds +
                ", privateRoomId='" + privateRoomId + '\'' +
                '}';
    }
}
