package com.dgphoenix.casino.common.client.canex.request.onlinerooms;

import com.dgphoenix.casino.common.client.canex.request.CanexJsonRequest;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PushRoomsPlayersRequest extends CanexJsonRequest {
    @JsonProperty("rooms")
    @SerializedName("rooms")
    private List<Room> rooms;

    public PushRoomsPlayersRequest() {
    }

    public PushRoomsPlayersRequest(List<Room> rooms) {
        this.rooms = rooms;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public void setRooms(List<Room> rooms) {
        this.rooms = rooms;
    }

    @Override
    public String toString() {
        return "PushRoomsPlayersRequest{" +
                "rooms=" + rooms +
                '}';
    }
}
