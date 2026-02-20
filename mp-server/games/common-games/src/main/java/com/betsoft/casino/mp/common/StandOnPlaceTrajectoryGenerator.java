package com.betsoft.casino.mp.common;

import com.betsoft.casino.mp.model.movement.Point;
import com.betsoft.casino.mp.model.movement.Trajectory;
import org.kynosarges.tektosyne.geometry.PointI;

public class StandOnPlaceTrajectoryGenerator extends TrajectoryGenerator {

    public StandOnPlaceTrajectoryGenerator(GameMapShape map, PointI source, double speed) {
        super(map, source, speed);
    }

    @Override
    public Trajectory generate(Trajectory trajectory, long startTime, int minSteps, boolean needFinalSteps) {
        this.trajectory = trajectory;
        long time = startTime;
        for (int i = 0; i < 100; i++) {
            this.trajectory.addPoint(new Point(source.x, source.y, time));
            time += 3000;
        }
        return this.trajectory;
    }
}
