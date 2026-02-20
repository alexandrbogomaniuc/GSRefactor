package com.betsoft.casino.mp.model.movement;

import java.util.Map;

public class MathUtils {

    public static boolean isOverlaps(long a1, long a2, long b1, long b2) {
        return a1 >= b1
                ? a2 <= b2 || b2 >= a1
                : b2 <= a2 || a2 >= b1;
    }

    public static boolean isOverlaps(double a1, double a2, double b1, double b2) {
        return a1 >= b1
                ? a2 <= b2 || b2 >= a1
                : b2 <= a2 || a2 >= b1;
    }

    public static double toDegrees(double radians) {
        return (360 - 180 * radians / Math.PI) % 360;
    }

    public static double toRadians(double degrees) {
        return degrees * Math.PI / 180;
    }

    public static double sin(double degrees) {
        return Math.sin(toRadians(degrees));
    }

    public static double cos(double degrees) {
        return Math.cos(toRadians(degrees));
    }

    public static int atan2(double dx, double dy) {
        return (360 - (int) toDegrees(Math.atan2(dy, dx))) % 360;
    }

    public static double sumProduct(Map<Integer, Double> map) {
        return map.entrySet().stream()
                .mapToDouble(entry -> entry.getKey() * entry.getValue())
                .sum();
    }
}
