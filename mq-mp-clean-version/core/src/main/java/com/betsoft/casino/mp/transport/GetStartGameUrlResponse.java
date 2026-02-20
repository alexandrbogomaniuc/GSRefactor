package com.betsoft.casino.mp.transport;

import com.betsoft.casino.utils.TObject;

/**
 * User: flsh
 * Date: 03.06.17.
 */
public class GetStartGameUrlResponse extends TObject {
    private long roomId;
    private String startGameUrl;

    public GetStartGameUrlResponse(long date, int rid, long roomId, String startGameUrl) {
        super(date, rid);
        this.roomId = roomId;
        this.startGameUrl = startGameUrl;
    }

    public long getRoomId() {
        return roomId;
    }

    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }

    public String getStartGameUrl() {
        return startGameUrl;
    }

    public void setStartGameUrl(String startGameUrl) {
        this.startGameUrl = startGameUrl;
    }

    @Override
    public String toString() {
        return "GetStartGameUrlResponse[" +
                "date=" + date +
                ", rid=" + rid +
                ", roomId=" + roomId +
                ", startGameUrl='" + startGameUrl + '\'' +
                ']';
    }
}
