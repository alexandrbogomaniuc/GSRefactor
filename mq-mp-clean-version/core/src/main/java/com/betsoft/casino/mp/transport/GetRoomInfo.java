package com.betsoft.casino.mp.transport;

import com.betsoft.casino.utils.TInboundObject;

/**
 * User: flsh
 * Date: 06.06.17.
 */
public class GetRoomInfo extends TInboundObject {
    private long roomId;

    public GetRoomInfo(long date, long roomId, int rid) {
        super(date, rid);
        this.roomId = roomId;
    }

    public long getRoomId() {
        return roomId;
    }

    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GetRoomInfo that = (GetRoomInfo) o;

        if (roomId != that.roomId) return false;
        return rid == that.rid;

    }

    @Override
    public int hashCode() {
        int result = (int) (roomId ^ (roomId >>> 32));
        result = 31 * result + rid;
        return result;
    }

    @Override
    public String toString() {
        return "GetRoomInfo[" +
                "roomId=" + roomId +
                ", rid=" + rid +
                ", date=" + date +
                ']';
    }
}
