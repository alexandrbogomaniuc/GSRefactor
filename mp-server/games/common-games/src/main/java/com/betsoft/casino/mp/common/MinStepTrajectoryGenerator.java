package com.betsoft.casino.mp.common;

import com.betsoft.casino.mp.model.movement.Point;
import com.betsoft.casino.mp.model.movement.Trajectory;
import com.abs.casino.common.util.RNG;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kynosarges.tektosyne.geometry.PointI;

import java.util.ArrayList;
import java.util.List;

public class MinStepTrajectoryGenerator extends TrajectoryGenerator {

    private static final Logger LOG = LogManager.getLogger(MinStepTrajectoryGenerator.class);
    private static final Integer PRESERVED_STEPS = 2;

    private final int minStep;
    private final int maxStep;

    public MinStepTrajectoryGenerator(GameMapShape map, PointI source, double speed, int minStep, int maxStep) {
        super(map, source, speed);
        this.minStep = minStep;
        this.maxStep = maxStep;
    }

    public Trajectory generate(long startTime, int minSteps) {
        trajectory = new Trajectory(speed);
        time = startTime;
        if (firstStep()) {
            int steps = RNG.nextInt(minSteps, minSteps + 3);
            for (int i = 0; i < steps; i++) {
                randomStep();
            }
            finalSteps();
        }
        return trajectory;
    }

    protected boolean firstStep() {
        int maxDistance = 0;
        for (int i = 0; i < 4; i++) {
            int distance = getMaxDistance(source.x, source.y, DIRS[i].x, DIRS[i].y);
            if (distance > maxDistance) {
                maxDistance = distance;
                dir = i;
            }
        }
        List<Integer> steps = getAvailableSteps(source.x, source.y, dir);
        if (steps.isEmpty()) {
            return false;
        }
        addPoint(getRandomStep(steps));
        return true;
    }

    protected void randomStep() {
        int dir1 = (dir + 1) % 4;
        int dir2 = (dir + 3) % 4;
        List<Integer> steps = getAvailableSteps(x, y, dir);
        int distance;
        if (!steps.isEmpty()) {
            distance = getRandomStep(steps);
        } else {
            List<Integer> steps1 = getAvailableSteps(x, y, dir1);
            List<Integer> steps2 = getAvailableSteps(x, y, dir2);
            if (steps1.size() > steps2.size()) {
                dir = dir1;
                distance = getRandomStep(steps1);
            } else if (steps2.size() > 0) {
                dir = dir2;
                distance = getRandomStep(steps2);
            } else {
                dir = (dir + 2) % 4;
                distance = getRandomStep(getAvailableSteps(x, y, dir));
            }
        }
        if (distance > 0) {
            addPoint(distance);
        } else {
            throw new BadTrajectoryException();
        }
    }

    protected void finalSteps() {
        while (!finalStep(x, y, dir) && !finalStep(x, y, nextDir(dir, 1)) && !finalStep(x, y, nextDir(dir, -1))) {
            randomStep();
        }
        addPoint(0);
    }

    private boolean finalStep(int x, int y, int dir) {
        int distance = getDistanceToSpawnPoint(x, y, dir);
        if (distance >= 0) {
            this.dir = dir;
            addPoint(distance);
            return true;
        }
        return false;
    }

    int getDistanceToSpawnPoint(int x, int y, int dir) {
        int distance = 0;
        while (map.isPassable(x, y) && !map.isSpawnPoint(x, y)) {
            x += DIRS[dir].x;
            y += DIRS[dir].y;
            distance++;
        }
        return map.isSpawnPoint(x, y) ? distance : -1;
    }

    private int getRandomStep(List<Integer> steps) {
        if (steps.isEmpty()) {
            return 0;
        }
        return steps.get(RNG.nextInt(steps.size()));
    }

    /**
     * We assume that step is possible if enemy is able to return
     * to the same point using 4 steps with 90 degrees rotation
     * SubTrajectory in this case will have rectangle form
     */
    public List<Integer> getAvailableSteps(int x, int y, int direction) {
        if (!canWalk(x, y, direction, minStep)) {
            return new ArrayList<>();
        }
        List<Integer> result = new ArrayList<>();
        int i = minStep;
        while (i <= maxStep && (canRotate(x, y, direction, i, 1) || canRotate(x, y, direction, i, -1))) {
            result.add(i);
            i++;
        }
        return result;
    }

    boolean canWalk(int x, int y, int direction, int length) {
        for (int i = 0; i <= length; i++) {
            if (!map.isPassable(x + DIRS[direction].x * i, y + DIRS[direction].y * i)) {
                return false;
            }
        }
        return true;
    }

    boolean canRotate(int x, int y, int direction, int length, int clockwise) {
        int sx = x + DIRS[direction].x * length;
        int sy = y + DIRS[direction].y * length;
        int nd = nextDir(direction, clockwise);
        if (length <= maxStep && canWalk(sx, sy, nd, minStep)) {
            int step = minStep;
            int nx = sx + DIRS[nd].x * step;
            int ny = sy + DIRS[nd].y * step;
            while (step <= maxStep && map.isPassable(nx, ny)) {
                if (canWalk(nx, ny, nextDir(nd, clockwise), length) && canWalk(x, y, nd, step)) {
                    return true;
                }
                nx += DIRS[nd].x;
                ny += DIRS[nd].y;
                step++;
            }
        }
        return false;
    }

    int nextDir(int current, int clockwise) {
        return (current + clockwise + 4) % 4;
    }

    public Trajectory generateLeaveTrajectory(Trajectory sourceTrajectory, long time, long freezeTime) {
        for (Point point : sourceTrajectory.getPoints()) {
            point.setTime(point.getTime() - freezeTime);
        }
        List<Point> points = sourceTrajectory.getPoints();
        int i = 0;
        while (i < points.size() && points.get(i).getTime() < time) {
            i++;
        }
        if (points.size() - i < PRESERVED_STEPS) {
            // no need to update trajectory as it is already close to an exit
            return null;
        }

        trajectory = new Trajectory(sourceTrajectory.getSpeed());
        for (int j = Math.min(0, i - 1); j < i + PRESERVED_STEPS; j++) {
            trajectory.addPoint(points.get(j));
        }
        setCurrentPoint(trajectory.getLastPoint());
        finalSteps();
        return trajectory;
    }
}
