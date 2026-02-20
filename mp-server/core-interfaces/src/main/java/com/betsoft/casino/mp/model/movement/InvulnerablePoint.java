package com.betsoft.casino.mp.model.movement;

import com.betsoft.casino.mp.common.Coords;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class InvulnerablePoint extends Point {

    private boolean invulnerable;

    public InvulnerablePoint() {}

    public InvulnerablePoint(double x, double y, long time) {
        super(x, y, time);
        this.invulnerable = true;
    }

    @Override
    public boolean isInvulnerable() {
        return invulnerable;
    }

    @Override
    public Point convert(Coords coords) {
        return new InvulnerablePoint(coords.toScreenX(x + 0.5, y + 0.5),
                coords.toScreenY(x + 0.5, y + 0.5),
                time);
    }

    @Override
    public Point create(double x, double y, long time) {
        return new InvulnerablePoint(x, y, time);
    }

    @Override
    public void write(Kryo kryo, Output output) {
        super.write(kryo, output);
        output.writeBoolean(invulnerable);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        super.read(kryo, input);
        invulnerable = input.readBoolean();
    }

    @Override
    public String toString() {
        return "InvulnerablePoint{" +
                "invulnerable=" + invulnerable +
                ", x=" + x +
                ", y=" + y +
                ", time=" + time +
                '}';
    }
}
