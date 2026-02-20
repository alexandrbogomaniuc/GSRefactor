package com.betsoft.casino.mp.common;

import com.betsoft.casino.mp.model.IGameMapShape;
import com.betsoft.casino.mp.model.movement.Point;
import com.betsoft.casino.mp.model.movement.Trajectory;
import com.dgphoenix.casino.common.util.RNG;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kynosarges.tektosyne.geometry.PointI;

public class TrajectoryGenerator {

    private static final Logger LOG = LogManager.getLogger(TrajectoryGenerator.class);

    protected static PointI[] DIRS = {
            new PointI(1, 0), new PointI(0, 1),
            new PointI(-1, 0), new PointI(0, -1)
    };

    protected final IGameMapShape map;
    protected final PointI source;
    protected final double speed;
    protected Trajectory trajectory;
    protected int x;
    protected int y;
    protected long time;
    protected int dir;

    public TrajectoryGenerator(IGameMapShape map, PointI source, double speed) {
        this.map = map;
        this.source = source;
        this.speed = speed;
        x = source.x;
        y = source.y;
    }

    public Trajectory generate(long startTime, int minSteps, boolean needFinalSteps) {
        trajectory = new Trajectory(speed);
        time = startTime;
        firstStep();
        int steps = RNG.nextInt(minSteps, minSteps + 3);
        for (int i = 0; i < steps; i++) {
            randomStep();
        }

        if (needFinalSteps) {
            finalSteps();
        }

        return trajectory;
    }

    public Trajectory generateWithDuration(long startTime, int durationTime, boolean needFinalSteps) {
        trajectory = new Trajectory(speed);
        time = startTime;
        firstStep();
        int minSteps = 15;
        while (minSteps-- > 0 || (time < (startTime + durationTime))) {
            randomStep();
        }
        if (needFinalSteps) {
            finalSteps();
        }
        return trajectory;
    }

    public Trajectory generateWithDurationAndMinSteps(long startTime, int durationTime, boolean needFinalSteps,
                                                      int minSteps) {
        LOG.debug("generateWithDurationAndMinSteps: durationTime: {},  minSteps: {}, needFinalSteps: {}",
                durationTime,minSteps, needFinalSteps);

        trajectory = new Trajectory(speed);
        time = startTime;
        if (firstStep()) {
            int i = 0;
            while ((i++ < 100) && (minSteps-- > 0 || (time < (startTime + durationTime)))) {
                randomStep();
            }
            if (needFinalSteps) {
                finalSteps();
            }
        }
        return trajectory;
    }

    protected void finalSteps() {
        WaypointTrajectoryGenerator generator = new WaypointTrajectoryGenerator(map);
        Trajectory finalTrajectory = generator.generate(x, y, time, speed);
        for (Point point : finalTrajectory.getPoints()) {
            trajectory.addPoint(point);
        }
    }

    public Trajectory generate(Trajectory trajectory, long startTime, int minSteps, boolean needFinalSteps) {
        this.trajectory = trajectory;
        time = startTime;
        int steps = RNG.nextInt(minSteps, minSteps + 3);
        for (int i = 0; i < steps; i++) {
            randomStep();
        }
        if (needFinalSteps) {
            finalSteps();
        }
        return trajectory;
    }

    public Trajectory generate(Trajectory trajectory, long startTime, int minSteps, long screenTime) {
        this.trajectory = trajectory;
        time = startTime;
        int steps = RNG.nextInt(minSteps, minSteps + 3);
        int i = 0;
        while ((time <= startTime + screenTime) || steps < i++) {
            randomStep();
        }
        return trajectory;
    }

    public Trajectory generateWithoutFirstStepWithDuration(long startTime, int durationTime) {
        trajectory = new Trajectory(speed);
        time = startTime;

        int minSteps = 15;
        while ((time < (startTime + durationTime)) || minSteps-- > 0) {
            randomStep();
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
        int distance = maxDistance > 10 ? RNG.nextInt(maxDistance - 10) + 10 : maxDistance;
        if (distance > 0) {
            addPoint(distance);
            return true;
        } else {
            return false;
        }
    }

    protected void randomStep() {
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
        addPoint(distance);
    }

    protected int getMaxDistance(int x, int y, int dx, int dy) {
        int result = 0;
        int nx = x + dx;
        int ny = y + dy;
        while (nx >= 0 && ny >= 0 && nx < map.getWidth() && ny < map.getHeight() && map.isPassable(nx, ny)) {
            result++;
            nx += dx;
            ny += dy;
        }
        return result;
    }

    public int getMaxDistanceToBorder(int x, int y, int dx, int dy) {
        if (!map.isValid(x, y)) {
            return 0;
        }
        if (map.isBorder(x, y) || map.isSpawnPoint(x, y)) {
            int distance = getMaxDistance(x, y, dx, dy);
            int nx = x + dx * distance;
            int ny = y + dy * distance;
            for (int dist = distance; dist > 0; dist--) {
                if (!map.isBorder(nx, ny) && !map.isSpawnPoint(nx, ny)) {
                    return dist;
                }
                nx -= dx;
                ny -= dy;
            }
            return distance;
        }

        int result = 0;
        int nx = x + dx;
        int ny = y + dy;
        while (nx >= 0 && ny >= 0 && nx < map.getWidth() && ny < map.getHeight() && map.isPassable(nx, ny)
                && !map.isBorder(nx, ny)) {
            result++;
            nx += dx;
            ny += dy;
        }
        return result;
    }

    protected void addPoint(int distance) {
        trajectory.addPoint(x, y, time);
        x += distance * DIRS[dir].x;
        y += distance * DIRS[dir].y;
        time += distance / (speed / 1000);
    }

    protected void addPoint(int dx, int dy) {
        x += dx;
        y += dy;
        time += Math.sqrt(dx * dx + dy * dy) * 1000 / speed;
        trajectory.addPoint(x, y, time);
    }

    protected void setCurrentPoint(Point point) {
        x = (int) point.getX();
        y = (int) point.getY();
        time = point.getTime();
    }
}
