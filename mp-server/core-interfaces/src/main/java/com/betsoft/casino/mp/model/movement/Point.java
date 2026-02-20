package com.betsoft.casino.mp.model.movement;

import com.betsoft.casino.mp.common.Coords;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.Serializable;
import java.util.Objects;

public class Point implements KryoSerializable, Serializable {
    protected double x;
    protected double y;
    protected long time;

    public Point() {}

    public Point(double x, double y, long time) {
        this.x = x;
        this.y = y;
        this.time = time;
    }

    public Point(Point point) {
        this.x = point.x;
        this.y = point.y;
        this.time = point.time;
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

    public void setTime(long time) {
        this.time = time;
    }

    public long getTime() {
        return time;
    }

    public boolean isInvulnerable() {
        return false;
    }

    public boolean isFixed() {
        return false;
    }

    public Point convert(Coords coords) {
        return new Point(
                coords.toScreenX(x + 0.5, y + 0.5),
                coords.toScreenY(x + 0.5, y + 0.5),
                time);
    }

    public Point create(double x, double y, long time) {
        return new Point(x, y, time);
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeDouble(x);
        output.writeDouble(y);
        output.writeLong(time, true);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        x = input.readDouble();
        y = input.readDouble();
        time = input.readLong(true);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Point point = (Point) o;
        return Double.compare(point.x, x) == 0 &&
                Double.compare(point.y, y) == 0 &&
                time == point.time;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    public boolean isFreezePoint(){
        return false;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Point [");
        sb.append("x=").append(x);
        sb.append(", y=").append(y);
        sb.append(", time=").append(time);
        sb.append(", isFreezePoint=").append(isFreezePoint());
        sb.append(']');
        return sb.toString();
    }
}
