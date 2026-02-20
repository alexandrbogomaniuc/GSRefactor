package com.betsoft.casino.mp.model.movement;

import com.betsoft.casino.mp.common.Coords;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class TeleportPoint extends Point {

    private boolean teleport;
    private boolean invulnerable;

    public TeleportPoint() {}

    public TeleportPoint(double x, double y, long time, boolean invulnerable) {
        super(x, y, time);
        this.teleport = true;
        this.invulnerable = invulnerable;
    }

    public boolean isTeleport() {
        return teleport;
    }

    public void setTeleport(boolean teleport) {
        this.teleport = teleport;
    }

    @Override
    public boolean isInvulnerable() {
        return invulnerable;
    }

    public void setInvulnerable(boolean invulnerable) {
        this.invulnerable = invulnerable;
    }

    @Override
    public Point convert(Coords coords) {
        return new TeleportPoint(
                coords.toScreenX(x + 0.5, y + 0.5),
                coords.toScreenY(x + 0.5, y + 0.5),
                time,
                invulnerable);
    }

    @Override
    public Point create(double x, double y, long time) {
        return new TeleportPoint(x, y, time, invulnerable);
    }

    @Override
    public void write(Kryo kryo, Output output) {
        super.write(kryo, output);
        output.writeBoolean(teleport);
        output.writeBoolean(invulnerable);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        super.read(kryo, input);
        teleport = input.readBoolean();
        invulnerable = input.readBoolean();
    }

    @Override
    public String toString() {
        return "TeleportPoint{" +
                "teleport=" + teleport +
                ", x=" + x +
                ", y=" + y +
                ", time=" + time +
                ", invulnerable=" + invulnerable +
                '}';
    }
}
