package com.betsoft.casino.mp.common;

public class Portal {
    private int x;
    private int y;
    private int direction;

    public Portal(int x, int y, int direction) {
        this.x = x;
        this.y = y;
        this.direction = direction;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getDirection() {
        return direction;
    }
}
