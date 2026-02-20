package com.betsoft.casino.mp.model.movement;

import com.betsoft.casino.mp.common.Coords;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class PortalPoint extends Point {

    private int portal;

    public PortalPoint() {}

    public PortalPoint(double x, double y, long time, int portal) {
        super(x, y, time);
        this.portal = portal;
    }

    public PortalPoint(Point point, int portal) {
        super(point);
        this.portal = portal;
    }

    public int getPortal() {
        return portal;
    }

    @Override
    public boolean isInvulnerable() {
        return true;
    }

    @Override
    public Point convert(Coords coords) {
        return new PortalPoint(
                coords.toScreenX(x + 0.5, y + 0.5),
                coords.toScreenY(x + 0.5, y + 0.5),
                time,
                portal);
    }

    @Override
    public Point create(double x, double y, long time) {
        return new PortalPoint(x, y, time, portal);
    }

    @Override
    public void write(Kryo kryo, Output output) {
        super.write(kryo, output);
        output.writeInt(portal, true);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        super.read(kryo, input);
        portal = input.readInt(true);
    }

    @Override
    public String toString() {
        return "PortalPoint{" +
                "portal=" + portal +
                ", x=" + x +
                ", y=" + y +
                ", time=" + time +
                '}';
    }
}
