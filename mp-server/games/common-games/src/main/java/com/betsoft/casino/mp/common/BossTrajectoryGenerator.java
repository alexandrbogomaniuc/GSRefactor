package com.betsoft.casino.mp.common;

import com.betsoft.casino.mp.model.movement.Trajectory;
import com.dgphoenix.casino.common.util.RNG;
import org.kynosarges.tektosyne.geometry.PointI;

public class BossTrajectoryGenerator extends TrajectoryGenerator {

    public BossTrajectoryGenerator(GameMapShape map, PointI source, double speed) {
        super(map, source, speed);
    }

    @Override
    protected int getMaxDistance(int x, int y, int dx, int dy) {
        int result = 0;
        int nx = x + dx;
        int ny = y + dy;
        while (nx >= 0 && ny >= 0 && nx < map.getWidth() && ny < map.getHeight() && map.isBossPath(nx, ny)) {
            result++;
            nx += dx;
            ny += dy;
        }
        return result;
    }

    @Override
    public int getMaxDistanceToBorder(int x, int y, int dx, int dy) {
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
        while (nx >= 0 && ny >= 0 && nx < map.getWidth() && ny < map.getHeight() && map.isBossPath(nx, ny)
                && !map.isBorder(nx, ny)) {
            result++;
            nx += dx;
            ny += dy;
        }
        return result;
    }

    @Override
    protected boolean firstStep() {
        // Boss should move to the east-south after spawn for correct animation
        dir = 1;
        int maxDistance = getMaxDistance(source.x, source.y, DIRS[dir].x, DIRS[dir].y);
        int distance = RNG.nextInt(maxDistance - 10) + 10;
        addPoint(distance);
        return true;
    }

    @Override
    public Trajectory generate(Trajectory trajectory, long startTime, int minSteps, boolean needFinalSteps) {
        this.trajectory = trajectory;
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
}
