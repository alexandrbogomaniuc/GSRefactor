package com.betsoft.casino.mp.model.movement;

import com.betsoft.casino.mp.common.Coords;

public class FreezePoint extends Point {
    public FreezePoint() {}

    public FreezePoint(double x, double y, long time) {
        super(x, y, time);
    }

    public boolean isFreezePoint(){
        return true;
    }

    @Override
    public Point create(double x, double y, long time) {
        return new FreezePoint(x, y, time);
    }

    @Override
    public Point convert(Coords coords) {
        return new FreezePoint(
                coords.toScreenX(x + 0.5, y + 0.5),
                coords.toScreenY(x + 0.5, y + 0.5),
                time);
    }
}
