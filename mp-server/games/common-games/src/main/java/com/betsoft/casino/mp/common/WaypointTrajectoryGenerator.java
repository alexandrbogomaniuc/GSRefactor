package com.betsoft.casino.mp.common;

import com.betsoft.casino.mp.model.IGameMapShape;
import com.betsoft.casino.mp.model.MoveDirection;
import com.betsoft.casino.mp.model.movement.Point;
import com.betsoft.casino.mp.model.movement.Trajectory;

import java.util.List;

public class WaypointTrajectoryGenerator implements ITrajectoryGenerator {

    private static final int MIN_TURN = 3;

    private final IGameMapShape map;

    public WaypointTrajectoryGenerator(IGameMapShape map) {
        this.map = map;
    }

    public Trajectory generate(int x, int y, long startTime, double speed) {
        Trajectory trajectory = new Trajectory(speed);
        long time = startTime;
        trajectory.addPoint(x, y, time);

        MoveDirection prevDirection = map.getMoveDirection(x, y);
        int pathLength = 0;
        boolean honorWalls = !map.isWall(x, y);

        while (!isFinish(honorWalls, x, y)) {
            MoveDirection direction = map.getMoveDirection(x, y);
            int nx, ny;
            if ((!direction.equals(prevDirection) && pathLength > MIN_TURN) || isWall(x + prevDirection.getDx(), y + prevDirection.getDy())) {
                nx = x + direction.getDx();
                ny = y + direction.getDy();
                pathLength = 0;
                prevDirection = direction;
            } else {
                nx = x + prevDirection.getDx();
                ny = y + prevDirection.getDy();
                pathLength++;
            }
            while (map.isValid(nx, ny) && !map.isSpawnPoint(nx, ny) && map.getMoveDirection(nx, ny).equals(direction)) {
                nx += direction.getDx();
                ny += direction.getDy();
                pathLength++;
            }
            time += (long) (Math.max(Math.abs(nx - x), Math.abs(ny - y)) / (speed / 1000));
            x = nx;
            y = ny;
            trajectory.addPoint(x, y, time);
        }

        return trajectory;
    }

    private boolean isFinish(boolean honorWalls, int x, int y) {
        if (honorWalls) {
            return map.isSpawnPoint(x, y) || map.isWall(x, y);
        } else {
            return !map.isValid(x, y) || map.isSpawnPoint(x, y);
        }
    }

    private boolean isWall(int x, int y) {
        return x < 0 || y < 0 || x >= map.getWidth() || y >= map.getHeight() || map.isWall(x, y);
    }

    public Trajectory generate(int x, int y, long startTime, long endTime, double minSpeed) {
        Trajectory trajectory = generate(x, y, startTime, 1.0);
        List<Point> points = trajectory.getPoints();
        double speed = ((double) points.get(points.size() - 1).getTime() - startTime) / (endTime - startTime);
        if (speed < minSpeed) {
            speed = minSpeed;
        }
        return generate(x, y, startTime, speed);
    }
}
