package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.IMineCoordinates;
import com.betsoft.casino.utils.TInboundObject;

import java.util.Objects;

public class MineCoordinates extends TInboundObject implements IMineCoordinates {
    private float x;
    private float y;
    private boolean isPaidSpecialShot;

    public MineCoordinates(long date, int rid, float x, float y, boolean isPaidSpecialShot) {
        super(date, rid);
        this.x = x;
        this.y = y;
        this.isPaidSpecialShot = isPaidSpecialShot;
    }

    @Override
    public float getX() {
        return x;
    }

    @Override
    public float getY() {
        return y;
    }

    @Override
    public boolean isPaidSpecialShot() {
        return isPaidSpecialShot;
    }

    @Override
    public void setPaidSpecialShot(boolean paidSpecialShot) {
        isPaidSpecialShot = paidSpecialShot;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        MineCoordinates mineCoordinates = (MineCoordinates) o;
        return x == mineCoordinates.x &&
                y == mineCoordinates.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), x, y);
    }


    @Override
    public String toString() {
        return "MineCoordinates[" +
                "x=" + x +
                ", y=" + y +
                ", isPaidSpecialShot=" + isPaidSpecialShot +
                "] " + super.toString();
    }
}

