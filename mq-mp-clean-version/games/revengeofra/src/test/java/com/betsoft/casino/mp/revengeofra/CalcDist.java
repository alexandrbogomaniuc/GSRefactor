package com.betsoft.casino.mp.revengeofra;

import com.betsoft.casino.mp.common.Coords;
import org.kynosarges.tektosyne.geometry.PointI;

public class CalcDist {
    protected static final transient Coords coords = new Coords(960, 540, 96, 96);

    public static void main(String[] args) {
        PointI start = new PointI(67,80);
        PointI end = new PointI(30,12);

        double startX = coords.toScreenX(start.x, start.y);
        double startY = coords.toScreenY(start.x, start.y);
        double endX = coords.toScreenX(end.x, end.y);
        double endY = coords.toScreenY(end.x, end.y);

        double distance = getDistance(startX, startY, endX, endY);
        System.out.println("distance: " + distance);

    }

    private static double getDistance(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return Math.sqrt(dx * dx + dy * dy);
    }
}
