package com.betsoft.casino.mp.common;

import com.betsoft.casino.mp.model.IGameMapShape;
import com.betsoft.casino.mp.model.movement.Trajectory;
import com.abs.casino.common.util.RNG;
import org.kynosarges.tektosyne.geometry.PointI;

public class WeaponCarrierTrajectoryGenerator extends TrajectoryGenerator {

    public WeaponCarrierTrajectoryGenerator(IGameMapShape map, PointI source, double speed) {
        super(map, source, speed);
    }

    public Trajectory generate(long startTime, int durationTime) {
        trajectory = new Trajectory(speed);
        time = startTime;

        buildFirstStep();

        while ((time < (startTime + durationTime))) {
            randomStep(startTime + durationTime);
        }

        return trajectory;
    }

    private void buildFirstStep() {
        trajectory.addPoint(x, y, time);
        if (y < 50) {
            y += 30;
        } else {
            y -= 20;
        }
        time += 7000;
        trajectory.addPoint(x, y, time);
    }

    protected void randomStep(long maxTime) {
        int dir1 = (dir + 1) % 4;
        int dir2 = (dir + 3) % 4;
        int distance1 = getMaxDistanceToBorder(x, y, DIRS[dir1].x, DIRS[dir1].y);
        int distance2 = getMaxDistanceToBorder(x, y, DIRS[dir2].x, DIRS[dir2].y);
        int distance;
        if (distance1 > distance2) {
            dir = dir1;
            distance = distance1 > 10 ? RNG.nextInt(distance1 - 10, distance1) : distance1;
        } else {
            dir = dir2;
            distance = distance2 > 10 ? RNG.nextInt(distance2 - 10, distance2) : distance2;
        }
        addPoint(distance, maxTime);
    }

    protected void addPoint(int distance, long maxTime) {
        trajectory.addPoint(x, y, time);
        double currentTime = time + (distance / (speed / 1000));
        if (currentTime > maxTime) {
            int finalDistance = (int) (maxTime - time) / 250;
            time = maxTime;
            x += finalDistance * DIRS[dir].x;
            y += finalDistance * DIRS[dir].y;
            trajectory.addPoint(x, y, time);
        } else {
            x += distance * DIRS[dir].x;
            y += distance * DIRS[dir].y;
            time += distance / (speed / 1000);
        }
    }
}
