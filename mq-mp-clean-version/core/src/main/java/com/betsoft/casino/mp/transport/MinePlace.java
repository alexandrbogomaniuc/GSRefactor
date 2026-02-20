package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.IMinePlace;
import com.betsoft.casino.utils.TObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MinePlace extends TObject implements IMinePlace {
    private int seatId;
    private float x;
    private float y;
    private String mineId;

    /**
     *
     * @param date place date
     * @param rid request id of client
     * @param seatId seatId of player
     * @param x x - coordinate (internal coordinate system)
     * @param y y - coordinate (internal coordinate system)
     * @param mineId = mineId
     */
    public MinePlace(long date, int rid, int seatId, float x, float y, String mineId) {
        super(date, rid);
        this.seatId = seatId;
        this.x = x;
        this.y = y;
        this.mineId = mineId;
    }

    public static List<MinePlace> convert(List<IMinePlace> mines) {
        List<MinePlace> result = new ArrayList<>();
        for (IMinePlace mine : mines) {
            if (mine instanceof MinePlace) {
                result.add((MinePlace) mine);
            } else {
                result.add(new MinePlace(mine.getDate(), mine.getRid(), mine.getSeatId(), mine.getX(),
                        mine.getY(), mine.getMineId()));
            }
        }
        return result;
    }

    @Override
    public int getSeatId() {
        return seatId;
    }

    @Override
    public void setSeatId(int seatId) {
        this.seatId = seatId;
    }

    @Override
    public float getX() {
        return x;
    }

    @Override
    public void setX(float x) {
        this.x = x;
    }

    @Override
    public float getY() {
        return y;
    }

    @Override
    public void setY(float y) {
        this.y = y;
    }

    @Override
    public String getMineId() {
        return mineId;
    }

    @Override
    public void setMineId(String mineId) {
        this.mineId = mineId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        MinePlace minePlace = (MinePlace) o;
        return seatId == minePlace.seatId &&
                Float.compare(minePlace.x, x) == 0 &&
                Float.compare(minePlace.y, y) == 0 &&
                Objects.equals(mineId, minePlace.mineId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), seatId, x, y, mineId);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MinePlace [");
        sb.append("rid=").append(rid);
        sb.append(", date=").append(date);
        sb.append(", seatId=").append(seatId);
        sb.append(", x=").append(x);
        sb.append(", y=").append(y);
        sb.append(", mineId=").append(mineId);
        sb.append(']');
        return sb.toString();
    }
}

