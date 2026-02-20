package com.betsoft.casino.bots.model;

import java.util.Arrays;

public enum TurretPositions {
    TOP_LEFT(4,-1, 112, 64, 36, -14),
    TOP_CENTER(1, -1,447.5,64, 36, -14),
    TOP_RIGHT(3, -1, 783,64, 36, -14),

    BOTTOM_LEFT(5, 1,112,493, 36, 2),
    BOTTOM_CENTER(0, 1, 447.5,493, 36, 2),
    //BOTTOM_CENTER(0,0, 447.5,550),
    //BOTTOM_CENTER(0,1, 483,461),
    BOTTOM_RIGHT(2, 1, 783,493, 36, 2);

    private int seatId;
    private int direct;
    private double coordinateX;
    private double coordinateY;
    private double masterOffsetX;
    private double masterOffsetY;

    TurretPositions(int seatId, int direct, double coordinateX, double coordinateY, double masterOffsetX, double masterOffsetY) {
        this.seatId = seatId;
        this.direct = direct;
        this.coordinateX = coordinateX;
        this.coordinateY = coordinateY;
        this.masterOffsetX = masterOffsetX;
        this.masterOffsetY = masterOffsetY;
    }

    public int getSeatId() {
        return seatId;
    }

    public int getDirect() {
        return direct;
    }

    public double getCoordinateX() {
        return coordinateX;
    }

    public double getCoordinateY() {
        return coordinateY;
    }

    public double getCentreCoordinateX() {
        return coordinateX + masterOffsetX;
    }

    public double getCentreCoordinateY() {
        return coordinateY + masterOffsetY;
    }

    public static TurretPositions getTurretPositionBySeatID(int seatId) {
        return Arrays.stream(TurretPositions.values())
                .filter(
                        item -> item.getSeatId() == seatId)
                .findFirst()
                .orElse(null);
    }


    @Override
    public String toString() {
        return "TurretPositions{" +
                "seatId=" + seatId +
                ", direct=" + direct +
                ", coordinateX=" + coordinateX +
                ", coordinateY=" + coordinateY +
                '}';
    }
}







