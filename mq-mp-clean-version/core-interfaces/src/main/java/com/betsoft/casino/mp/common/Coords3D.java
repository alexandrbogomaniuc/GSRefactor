package com.betsoft.casino.mp.common;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class Coords3D extends Coords {

    private double zeroX;
    private double zeroY;

    public Coords3D() {}

    public Coords3D(int width, int height) {
        super(width, height, width, height);
        this.zeroX = (double) -width / 2;
        this.zeroY = (double) -height / 2;
    }

    @Override
    public double toScreenX(double x) {
        return x / 2 + zeroX;
    }

    @Override
    public double toScreenY(double y) {
        return y / 2 + zeroY;
    }

    @Override
    public double toX(double screenX) {
        return (screenX - zeroX) * 2;
    }

    @Override
    public double toY(double screenY) {
        return (screenY - zeroY) * 2;
    }

    @Override
    public double toScreenX(double x, double y) {
        return toScreenX(x);
    }

    @Override
    public double toScreenY(double x, double y) {
        return toScreenY(y);
    }

    @Override
    public double toX(double screenX, double screenY) {
        return toX(screenX);
    }

    @Override
    public double toY(double screenX, double screenY) {
        return toY(screenY);
    }

    @Override
    public void write(Kryo kryo, Output output) {
        super.write(kryo, output);
        output.writeDouble(zeroX);
        output.writeDouble(zeroY);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        super.read(kryo, input);
        zeroX = input.readDouble();
        zeroY = input.readDouble();
    }
}
