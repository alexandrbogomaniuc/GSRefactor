package com.betsoft.casino.mp.bgdragonstone;

import com.betsoft.casino.mp.common.Coords;

public class CheckPoints {
    static final transient Coords coords = new Coords(960, 540, 96, 96);

    public static void main(String[] args) {
//        { "points": [{ "x": 74, "y": 24 },  { "x": 45, "y": 33 }, { "x": 3, "y": 43 }]},
//        { "points": [{ "x": 93, "y": 59 },  { "x": 50, "y": 60 }, { "x": 10, "y": 60 } ]},
//        { "points": [{ "x": 87, "y": 65 },  { "x": 54, "y": 65 }, { "x": 17, "y": 65 } ]},
//        { "points": [{ "x": 80, "y": 72 },  { "x": 54, "y": 72 }, { "x": 26, "y": 72 }]},

        int[][][] points = new int[][][]{
                {{74, 24}, {3, 43}},
                {{93, 59}, {10, 60}},
                {{87, 65}, {17, 65}},
                {{80, 72}, {26, 72}}
        };

        for (int[][] point : points) {
            for (int[] ints : point) {
                double screenX = coords.toScreenX(ints[0], ints[1]);
                double screenY = coords.toScreenY(ints[0], ints[1]);
                System.out.print("x: " + screenX + " y: " + screenY);
                System.out.println("   ");
            }
            System.out.println("");
        }

    }
}
