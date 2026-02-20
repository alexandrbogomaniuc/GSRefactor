package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.ICloseRoom;
import com.betsoft.casino.utils.TObject;

/**
 * User: flsh
 * Date: 06.06.17.
 */
public class CloseRoom extends TObject implements ICloseRoom {
    private long roomId;

    public CloseRoom(long date, long roomId, int rid) {
        super(date, rid);
        this.roomId = roomId;
    }

    @Override
    public long getRoomId() {
        return roomId;
    }

    @Override
    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        CloseRoom closeRoom = (CloseRoom) o;

        return roomId == closeRoom.roomId;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (int) (roomId ^ (roomId >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "CloseRoom[" +
                "roomId=" + roomId +
                ", rid=" + rid +
                ", date=" + date +
                ']';
    }
}
