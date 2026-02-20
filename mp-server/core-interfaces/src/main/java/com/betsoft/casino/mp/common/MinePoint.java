package com.betsoft.casino.mp.common;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import java.io.Serializable;
import java.util.Objects;

public class MinePoint implements KryoSerializable, Serializable {
    private static final byte VERSION = 0;
    private double x;
    private double y;
    private long timePlace;

    public MinePoint() {}

    public MinePoint(double x, double y) {
        this.x = x;
        this.y = y;
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.timePlace = System.currentTimeMillis();
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getX() {
        return x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getY() {
        return y;
    }


    public long getTimePlace() {
        return timePlace;
    }

    public void setTimePlace(long timePlace) {
        this.timePlace = timePlace;
    }

    public String getMineId(int seatId) {
        return seatId + "_" + timePlace;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeDouble(x);
        output.writeDouble(y);
        output.writeLong(timePlace);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        x = input.readDouble();
        y = input.readDouble();
        timePlace = input.readLong();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MinePoint minePoint = (MinePoint) o;
        return Double.compare(minePoint.x, x) == 0 &&
                Double.compare(minePoint.y, y) == 0 &&
                timePlace == minePoint.timePlace;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, timePlace);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MinePoint [");
        sb.append("x=").append(x);
        sb.append(", y=").append(y);
        sb.append(", timePlace=").append(timePlace);
        sb.append(']');
        return sb.toString();
    }
}
