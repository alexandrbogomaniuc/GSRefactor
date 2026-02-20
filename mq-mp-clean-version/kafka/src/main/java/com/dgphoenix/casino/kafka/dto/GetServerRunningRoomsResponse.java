package com.dgphoenix.casino.kafka.dto;

import java.util.Map;

public class GetServerRunningRoomsResponse extends BasicKafkaResponse {
    private int serverId;
    private Map<Long, RunningRoomDto> runningRoomsDtoMap;

    public GetServerRunningRoomsResponse() {}

    public GetServerRunningRoomsResponse(int serverId, Map<Long, RunningRoomDto> runningRoomDtoMap) {
        super(true, 0, "");
        this.serverId = serverId;
        this.runningRoomsDtoMap = runningRoomDtoMap;
    }

    public GetServerRunningRoomsResponse(boolean success, int statusCode, String reasonPhrases) {
        super(success, statusCode, reasonPhrases);
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public Map<Long, RunningRoomDto> getRunningRoomsDtoMap() {
        return runningRoomsDtoMap;
    }

    public void setRunningRoomsDtoMap(Map<Long, RunningRoomDto> runningRoomsDtoMap) {
        this.runningRoomsDtoMap = runningRoomsDtoMap;
    }

    @Override
    public String toString() {
        return "GetServerRunningRoomsResponse{" +
                "serverId=" + serverId +
                ", runningRoomsDtoMap=" + runningRoomsDtoMap +
                '}';
    }
}
