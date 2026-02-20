package com.betsoft.casino.mp.common;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class Coords implements KryoSerializable {

    private double zeroX;
    private double zeroY;
    private double cellWidth;
    private double cellHeight;

    public Coords() {}

    /**
     * Screen parameters of map (960, 540, 96, 96)
     * @param screenWidth Width  960
     * @param screenHeight Height 540
     * @param width cell count by X (internal coordinate system)
     * @param height cell count by Y (internal coordinate system)
     */
    public Coords(int screenWidth, int screenHeight, int width, int height) {
        this.zeroX = (double) screenWidth / 2;
        this.zeroY = (double) -screenHeight / 2;
        this.cellWidth = (double) screenWidth / width;
        this.cellHeight = (double) screenHeight / height;
    }

    public double toScreenX(double x) {
        throw new UnsupportedOperationException();
    }

    public double toScreenY(double y) {
        throw new UnsupportedOperationException();
    }

    public double toX(double screenX) {
        throw new UnsupportedOperationException();
    }

    public double toY(double screenY) {
        throw new UnsupportedOperationException();
    }

    /**
     * Convert internal coordinates to coordinate X of client
     * @param x internal coordinate X
     * @param y internal coordinate Y
     * @return X screen coordinate of client
     */
    public double toScreenX(double x, double y) {
        return zeroX + cellWidth * (y - x);
    }

    /**
     * Convert internal coordinates to coordinate Y of client
     * @param x internal coordinate X
     * @param y internal coordinate Y
     * @return Y screen coordinate of client
     */
    public double toScreenY(double x, double y) {
        return zeroY + cellHeight * (x + y);
    }

    /**
     * Convert screen coordinate from client to internal X
     * @param screenX screen X coordinate
     * @param screenY screen Y coordinate
     * @return X coordinate of internal coordinate system
     */
    public double toX(double screenX, double screenY) {
        return ((screenY - zeroY) / cellHeight - (screenX - zeroX) / cellWidth) / 2;
    }

    /**
     * Convert screen coordinate from client to internal Y
     * @param screenX screen X coordinate
     * @param screenY screen Y coordinate
     * @return Y coordinate of internal coordinate system
     */
    public double toY(double screenX, double screenY) {
        return ((screenY - zeroY) / cellHeight + (screenX - zeroX) / cellWidth) / 2;
    }

    public double getCellWidth() {
        return cellWidth;
    }

    public double getCellHeight() {
        return cellHeight;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeDouble(zeroX);
        output.writeDouble(zeroY);
        output.writeDouble(cellWidth);
        output.writeDouble(cellHeight);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        zeroX = input.readDouble();
        zeroY = input.readDouble();
        cellWidth = input.readDouble();
        cellHeight = input.readDouble();
    }
}
