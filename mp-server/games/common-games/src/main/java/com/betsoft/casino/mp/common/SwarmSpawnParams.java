package com.betsoft.casino.mp.common;

import java.util.Objects;

public class SwarmSpawnParams {

    private final int id;
    private final int sourceX;
    private final int sourceY;
    private final int sourceDeltaX;
    private final int sourceDeltaY;
    private final int targetX;
    private final int targetY;
    private final int targetDeltaX;
    private final int targetDeltaY;
    private final int travelTime;
    private final int startDeltaTime;
    private final int finishDeltaTime;
    private final double minSpread;

    public SwarmSpawnParams(int sourceX, int sourceY, int sourceDeltaX, int sourceDeltaY,
                            int targetX, int targetY, int targetDeltaX, int targetDeltaY,
                            int travelTime, int startDeltaTime, int finishDeltaTime, double minSpread) {
        id = 0;
        this.sourceX = sourceX;
        this.sourceY = sourceY;
        this.sourceDeltaX = sourceDeltaX;
        this.sourceDeltaY = sourceDeltaY;
        this.targetX = targetX;
        this.targetY = targetY;
        this.targetDeltaX = targetDeltaX;
        this.targetDeltaY = targetDeltaY;
        this.travelTime = travelTime;
        this.startDeltaTime = startDeltaTime;
        this.finishDeltaTime = finishDeltaTime;
        this.minSpread = minSpread;
    }

    public SwarmSpawnParams(int id, int sourceX, int sourceY, int sourceDeltaX, int sourceDeltaY,
                            int targetX, int targetY, int targetDeltaX, int targetDeltaY,
                            int travelTime, int startDeltaTime, int finishDeltaTime, double minSpread) {
        this.id = id;
        this.sourceX = sourceX;
        this.sourceY = sourceY;
        this.sourceDeltaX = sourceDeltaX;
        this.sourceDeltaY = sourceDeltaY;
        this.targetX = targetX;
        this.targetY = targetY;
        this.targetDeltaX = targetDeltaX;
        this.targetDeltaY = targetDeltaY;
        this.travelTime = travelTime;
        this.startDeltaTime = startDeltaTime;
        this.finishDeltaTime = finishDeltaTime;
        this.minSpread = minSpread;
    }

    public int getId() {
        return id;
    }

    public int getSourceX() {
        return sourceX;
    }

    public int getSourceY() {
        return sourceY;
    }

    public int getSourceDeltaX() {
        return sourceDeltaX;
    }

    public int getSourceDeltaY() {
        return sourceDeltaY;
    }

    public int getTargetX() {
        return targetX;
    }

    public int getTargetY() {
        return targetY;
    }

    public int getTargetDeltaX() {
        return targetDeltaX;
    }

    public int getTargetDeltaY() {
        return targetDeltaY;
    }

    public int getTravelTime() {
        return travelTime;
    }

    public int getStartDeltaTime() {
        return startDeltaTime;
    }

    public int getFinishDeltaTime() {
        return finishDeltaTime;
    }

    public double getMinSpread() {
        return minSpread;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SwarmSpawnParams params = (SwarmSpawnParams) o;
        return sourceX == params.sourceX &&
                sourceY == params.sourceY &&
                sourceDeltaX == params.sourceDeltaX &&
                sourceDeltaY == params.sourceDeltaY &&
                targetX == params.targetX &&
                targetY == params.targetY &&
                targetDeltaX == params.targetDeltaX &&
                targetDeltaY == params.targetDeltaY &&
                travelTime == params.travelTime &&
                startDeltaTime == params.startDeltaTime &&
                finishDeltaTime == params.finishDeltaTime &&
                Double.compare(params.minSpread, minSpread) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceX, sourceY, sourceDeltaX, sourceDeltaY, targetX, targetY, targetDeltaX, targetDeltaY, travelTime, startDeltaTime, finishDeltaTime, minSpread);
    }

    @Override
    public String toString() {
        return "SwarmSpawnParams{" +
                "sourceX=" + sourceX +
                ", sourceY=" + sourceY +
                ", sourceDeltaX=" + sourceDeltaX +
                ", sourceDeltaY=" + sourceDeltaY +
                ", targetX=" + targetX +
                ", targetY=" + targetY +
                ", targetDeltaX=" + targetDeltaX +
                ", targetDeltaY=" + targetDeltaY +
                ", travelTime=" + travelTime +
                ", startDeltaTime=" + startDeltaTime +
                ", finishDeltaTime=" + finishDeltaTime +
                ", minSpread=" + minSpread +
                '}';
    }
}
