package com.betsoft.casino.mp.transport;

import com.betsoft.casino.utils.IServerMessage;
import com.betsoft.casino.utils.TObject;

/**
 * User: flsh
 * Date: 01.06.18.
 */
public class RoomMoved extends TObject implements IServerMessage {
    private long roomId;
    private int newServerId;
    private String newStartGameUrl;

    public RoomMoved(long date, int rid, long roomId, int newServerId, String newStartGameUrl) {
        super(date, rid);
        this.roomId = roomId;
        this.newServerId = newServerId;
        this.newStartGameUrl = newStartGameUrl;
    }

    public long getRoomId() {
        return roomId;
    }

    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }

    public int getNewServerId() {
        return newServerId;
    }

    public void setNewServerId(int newServerId) {
        this.newServerId = newServerId;
    }

    public String getNewStartGameUrl() {
        return newStartGameUrl;
    }

    public void setNewStartGameUrl(String newStartGameUrl) {
        this.newStartGameUrl = newStartGameUrl;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RoomMoved [");
        sb.append("date=").append(date);
        sb.append(", rid=").append(rid);
        sb.append(", roomId=").append(roomId);
        sb.append(", newServerId=").append(newServerId);
        sb.append(", newStartGameUrl='").append(newStartGameUrl).append('\'');
        sb.append(']');
        return sb.toString();
    }
}
