package com.betsoft.casino.mp.common;

import com.abs.casino.common.util.RNG;
import org.kynosarges.tektosyne.geometry.PointI;

public class FreeAngleTrajectoryGenerator extends TrajectoryGenerator {
    static PointI[] DIRS45 = {
            new PointI(1, 0), new PointI(1, 1), new PointI(0, 1), new PointI(-1, 1),
            new PointI(-1, 0), new PointI(-1, -1), new PointI(0, -1), new PointI(1, -1)
    };

    public FreeAngleTrajectoryGenerator(GameMapShape map, PointI source, double speed) {
        super(map, source, speed);
    }

    protected boolean firstStep() {
        int maxDistance = 0;
        for (int i = 0; i < 8; i++) {
            int distance = getMaxDistance(source.x, source.y, DIRS45[i].x, DIRS45[i].y);
            if (distance > maxDistance) {
                maxDistance = distance;
                dir = i;
            }
        }
        int distance = RNG.nextInt(maxDistance - 10) + 10;
        addPoint(distance);
        return true;
    }

    protected void randomStep() {
        int maxDistance;
        dir = dir + 5 + RNG.nextInt(4);
        int i = 0;
        do {
            dir = (dir + 1) % 8;
            maxDistance = getMaxDistanceToBorder(x, y, DIRS45[dir].x, DIRS45[dir].y);
            i++;
        } while (maxDistance < 1 && i < 8);
        if (maxDistance > 0) {
            addPoint(RNG.nextInt(maxDistance - 1) + 1);
        }
    }

    protected void addPoint(int distance) {
        if (distance > 0) {
            trajectory.addPoint(x, y, time);
            int dx = distance * DIRS45[dir].x;
            int dy = distance * DIRS45[dir].y;
            x += dx;
            y += dy;
            time += Math.sqrt(dx * dx + dy * dy) / (speed / 1000);
        }
    }
}
