package com.betsoft.casino.mp.common;

import com.betsoft.casino.mp.model.movement.Point;
import com.betsoft.casino.mp.model.movement.Trajectory;
import com.dgphoenix.casino.common.util.RNG;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kynosarges.tektosyne.geometry.PointI;

import java.util.List;

public class JumpTrajectoryGenerator extends TrajectoryGenerator {
    private static final Logger LOG = LogManager.getLogger(JumpTrajectoryGenerator.class);
    private static final int DEFAULT_MIN_JUMP_LENGTH = 18;
    private static final int DEFAULT_MAX_JUMP_LENGTH = 23;

    protected int minJumpLength;
    protected int maxJumpLength;

    public JumpTrajectoryGenerator(GameMapShape map, PointI source, double speed) {
        super(map, source, speed);
        minJumpLength = DEFAULT_MIN_JUMP_LENGTH;
        maxJumpLength = DEFAULT_MAX_JUMP_LENGTH;
    }

    public JumpTrajectoryGenerator(GameMapShape map, PointI source, double speed, int minJumpLength, int maxJumpLength) {
        super(map, source, speed);
        this.minJumpLength = minJumpLength;
        this.maxJumpLength = maxJumpLength;
    }

    @Override
    public Trajectory generate(long startTime, int minSteps, boolean needFinalSteps) {
        Trajectory trajectory = new Trajectory(speed);
        return generate(trajectory, startTime, minSteps, needFinalSteps);
    }

    @Override
    public Trajectory generate(Trajectory trajectory, long startTime, int minSteps, boolean needFinalSteps) {
        this.trajectory = trajectory;
        time = startTime;
        int steps = RNG.nextInt(minSteps, minSteps + 3);
        for (int i = 0; i < steps; i++) {
            jumpStep();
        }

        if (needFinalSteps) {
            finalSteps();
        }
        return this.trajectory;
    }

    @Override
    public Trajectory generateWithDuration(long startTime, int durationTime, boolean needFinalSteps) {
        trajectory = new Trajectory(speed);
        time = startTime;
        int minSteps = 15;
        while ((time < (startTime + durationTime)) || minSteps-- > 0) {
            jumpStep();
        }

        if (needFinalSteps) {
            finalSteps();
        }
        return trajectory;
    }

    private void jumpStep() {
        int distance;
        int sameDirectoryMaxDistance = getMaxDistanceToBorder(x, y, DIRS[dir].x, DIRS[dir].y);
        if (sameDirectoryMaxDistance >= minJumpLength) {
            distance = RNG.nextInt(minJumpLength, Math.min(maxJumpLength, sameDirectoryMaxDistance));
        } else {
            int dir1 = (dir + 1) % 4;
            int dir2 = (dir + 3) % 4;
            int distance1 = getMaxDistanceToBorder(x, y, DIRS[dir1].x, DIRS[dir1].y);
            int distance2 = getMaxDistanceToBorder(x, y, DIRS[dir2].x, DIRS[dir2].y);
            if (distance1 >= minJumpLength && distance2 >= minJumpLength) {
                if (RNG.nextBoolean()) {
                    distance = RNG.nextInt(minJumpLength, Math.min(distance1, maxJumpLength));
                    dir = dir1;
                } else {
                    distance = RNG.nextInt(minJumpLength, Math.min(distance2, maxJumpLength));
                    dir = dir2;
                }
            } else if (distance1 >= minJumpLength) {
                distance = RNG.nextInt(minJumpLength, Math.min(distance1, maxJumpLength));
                dir = dir1;
            } else if (distance2 >= minJumpLength) {
                distance = RNG.nextInt(minJumpLength, Math.min(distance2, maxJumpLength));
                dir = dir2;
            } else {
                int sameDirDistance = getMaxDistanceToBorder(x, y, DIRS[dir].x, DIRS[dir].y);
                if (sameDirDistance > distance1 && sameDirDistance > distance2) {
                    distance = sameDirDistance;
                } else if (distance1 > distance2) {
                    distance = distance1;
                    dir = dir1;
                } else {
                    distance = distance2;
                    dir = dir2;
                }
            }
        }

        addPoint(distance);
    }

    @Override
    protected void finalSteps() {
        WaypointTrajectoryGenerator generator = new WaypointTrajectoryGenerator(map);
        Trajectory finalTrajectory = generator.generate(x, y, time, speed);
        List<Point> points = finalTrajectory.getPoints();

        if (points.size() > 1) {
            for (int index = 0; index < points.size() - 1; index++) {
                trajectory.addPoint(points.get(index));
            }

            Point lastPoint = points.get(points.size() - 1);
            Point prevPoint = points.get(points.size() - 2);

            if (getDistance(prevPoint, lastPoint) > maxJumpLength) {
                double x = (prevPoint.getX() + lastPoint.getX()) / 2;
                double y = (prevPoint.getY() + lastPoint.getY()) / 2;
                long time = (prevPoint.getTime() + lastPoint.getTime()) / 2;
                trajectory.addPoint(x, y, time);
            }

            trajectory.addPoint(lastPoint);
        } else {
            for (Point point : points) {
                trajectory.addPoint(point);
            }
        }
    }

    private double getTempX(Point prev) {
        return prev.getX() + maxJumpLength;
    }

    private double getTempY(Point prev) {
        return prev.getY() + maxJumpLength;
    }

    private double getDistance(Point a, Point b) {
        double dx = b.getX() - a.getX();
        double dy = b.getY() - a.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }

}
