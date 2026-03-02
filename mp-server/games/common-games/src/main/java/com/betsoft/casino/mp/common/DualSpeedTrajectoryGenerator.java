package com.betsoft.casino.mp.common;

import com.abs.casino.common.util.RNG;
import org.kynosarges.tektosyne.geometry.PointI;

public class DualSpeedTrajectoryGenerator extends TrajectoryGenerator {

    private double walkSpeed;

    public DualSpeedTrajectoryGenerator(GameMapShape map, PointI source, double speed, double walkSpeed) {
        super(map, source, speed);
        this.walkSpeed = walkSpeed;
    }

    @Override
    protected void addPoint(int distance) {
        trajectory.addPoint(x, y, time);
        x += distance * DIRS[dir].x;
        y += distance * DIRS[dir].y;
        time += distance / ((RNG.nextBoolean() ? speed : walkSpeed) / 1000);
    }
}
