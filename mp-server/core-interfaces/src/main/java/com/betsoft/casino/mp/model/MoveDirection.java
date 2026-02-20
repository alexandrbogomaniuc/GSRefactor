package com.betsoft.casino.mp.model;

public enum MoveDirection {
    LEFT(0, -1), TOP(-1, 0), RIGHT(0, 1), BOTTOM(1, 0);

    private int dx;
    private int dy;

    MoveDirection(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    public int getDx() {
        return dx;
    }

    public int getDy() {
        return dy;
    }
}
