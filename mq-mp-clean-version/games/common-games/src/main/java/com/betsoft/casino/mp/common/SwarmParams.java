package com.betsoft.casino.mp.common;

import com.betsoft.casino.mp.common.scenarios.TypeAndSkin;

import java.util.List;

public class SwarmParams {
    private int id;
    private int startX;
    private int startY;
    private int deltaX;
    private int deltaY;
    private int angle;
    private int distance;
    private int minSize;
    private int maxSize;
    private List<TypeAndSkin> enemies;
    private long cooldown;
    private boolean fromPortal;

    public SwarmParams(int id, int startX, int startY, int deltaX, int deltaY, int angle, int distance,
                       int minSize, int maxSize, List<TypeAndSkin> enemies, long cooldown, boolean fromPortal) {
        this.id = id;
        this.startX = startX;
        this.startY = startY;
        this.deltaX = deltaX;
        this.deltaY = deltaY;
        this.angle = angle;
        this.distance = distance;
        this.minSize = minSize;
        this.maxSize = maxSize;
        this.enemies = enemies;
        this.cooldown = cooldown;
        this.fromPortal = fromPortal;
    }

    public int getId() {
        return id;
    }

    public int getStartX() {
        return startX;
    }

    public int getStartY() {
        return startY;
    }

    public int getDeltaX() {
        return deltaX;
    }

    public int getDeltaY() {
        return deltaY;
    }

    public int getAngle() {
        return angle;
    }

    public int getDistance() {
        return distance;
    }

    public int getMinSize() {
        return minSize;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public List<TypeAndSkin> getEnemies() {
        return enemies;
    }

    public long getCooldown() {
        return cooldown;
    }

    public boolean isFromPortal() {
        return fromPortal;
    }

    @Override
    public String toString() {
        return "SwarmParams{" +
                "id=" + id +
                ", startX=" + startX +
                ", startY=" + startY +
                ", deltaX=" + deltaX +
                ", deltaY=" + deltaY +
                ", angle=" + angle +
                ", distance=" + distance +
                ", minSize=" + minSize +
                ", maxSize=" + maxSize +
                ", enemies=" + enemies +
                ", cooldown=" + cooldown +
                ", fromPortal=" + fromPortal +
                '}';
    }
}
