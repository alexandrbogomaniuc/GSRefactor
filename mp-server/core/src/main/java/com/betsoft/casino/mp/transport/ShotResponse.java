package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.IShotResponse;
import com.betsoft.casino.utils.TObject;

import java.util.Objects;

public class ShotResponse extends TObject implements IShotResponse {
    private int seatId;
    private int weaponId;
    private int remainingSWShots;

    public ShotResponse(long date, int rid, int seatId, int weaponId, int remainingSWShots) {
        super(date, rid);
        this.seatId = seatId;
        this.weaponId = weaponId;
        this.remainingSWShots = remainingSWShots;
    }

    @Override
    public int getSeatId() {
        return seatId;
    }

    @Override
    public int getWeaponId() {
        return weaponId;
    }

    @Override
    public int getRemainingSWShots() {
        return remainingSWShots;
    }

    @Override
    public void setRemainingSWShots(int remainingSWShots) {
        this.remainingSWShots = remainingSWShots;
    }

    @Override
    public String toString() {
        return "ShotResponse[" +
                "seatId=" + seatId +
                ", weaponId=" + weaponId +
                ", date=" + date +
                ", rid=" + rid +
                ']';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ShotResponse that = (ShotResponse) o;
        return seatId == that.seatId &&
                weaponId == that.weaponId &&
                remainingSWShots == that.remainingSWShots;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), seatId, weaponId, remainingSWShots);
    }
}
