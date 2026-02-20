package com.betsoft.casino.mp.transport;

import com.betsoft.casino.utils.TInboundObject;

public class UpdateWeaponPaidMultiplier extends TInboundObject {
    private long roomId;

    public UpdateWeaponPaidMultiplier(long date, int rid, long roomId) {
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
        if (!super.equals(o)) return false;

        UpdateWeaponPaidMultiplier that = (UpdateWeaponPaidMultiplier) o;

        return roomId == that.roomId;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (int) (roomId ^ (roomId >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "UpdateWeaponPaidMultiplier" + "[" +
                "date=" + date +
                ", rid=" + rid +
                ", roomId=" + roomId +
                ']';
    }
}
