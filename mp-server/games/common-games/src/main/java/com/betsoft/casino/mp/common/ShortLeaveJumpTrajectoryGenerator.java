package com.betsoft.casino.mp.common;

import com.betsoft.casino.mp.model.movement.Trajectory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kynosarges.tektosyne.geometry.PointI;

public class ShortLeaveJumpTrajectoryGenerator extends JumpTrajectoryGenerator {
    private static final Logger LOG = LogManager.getLogger(ShortLeaveJumpTrajectoryGenerator.class);

    private final int mainDirection;

    public ShortLeaveJumpTrajectoryGenerator(GameMapShape map, PointI source, double speed, int minJumpLength, int maxJumpLength, int mainDirection) {
        super(map, source, speed, minJumpLength, maxJumpLength);
        this.mainDirection = mainDirection;
    }

    public Trajectory generate(long startTime) {
        if (!map.isPassable(source.x, source.y)) {
            return null;
        }
        time = startTime;
        trajectory = new Trajectory(speed);
        trajectory.addPoint(x, y, time);
        PointI target = getClosestSpawnPoint(getTarget(source));
        int dx = target.x - x;
        int dy = target.y - y;
        int attempts = 0;
        while ((dx > 0 || dy > 0) && attempts < 15) {
            if (Math.abs(dx) > Math.abs(dy)) {
                stepY(dy);
                stepX(dx);
                if (Math.abs(target.x - x) > 0 || Math.abs(target.y - y) > 0) {
                    stepY(getMaxDistance(x, y, 0, -1) > getMaxDistance(x, y, 0, 1) ? -maxJumpLength : maxJumpLength);
                    stepX(sign(dx) * maxJumpLength);
                }
            } else {
                stepX(dx);
                stepY(dy);
                if (Math.abs(target.x - x) > 0 || Math.abs(target.y - y) > 0) {
                    stepX(getMaxDistance(x, y, -1, 0) > getMaxDistance(x, y, 1, 0) ? -maxJumpLength : maxJumpLength);
                    stepY(sign(dy) * maxJumpLength);
                }
            }
            dx = target.x - x;
            dy = target.y - y;
            attempts++;
        }
        if (attempts >= 15) {
            LOG.error("Failed to find trajectory from point {}: {}", source, trajectory);
            return null;
        }
        return trajectory;
    }

    private int sign(int number) {
        return number < 0 ? -1 : 1;
    }

    private void stepX(int dx) {
        int sign = sign(dx);
        int maxDistance = getMaxDistance(x, y, sign, 0);
        dx = Math.min(Math.abs(dx), maxDistance);
        if (dx > 0) {
            if (dx < maxJumpLength) {
                addPoint(dx * sign, 0);
            } else {
                int jumps = dx / maxJumpLength + 1;
                int jumpSize = dx / jumps + 1;
                for (int i = 0; i < jumps; i++) {
                    addPoint(Math.min(jumpSize, dx) * sign, 0);
                    dx -= jumpSize;
                }
            }
        }
    }

    private void stepY(int dy) {
        int sign = sign(dy);
        int maxDistance = getMaxDistance(x, y, 0, sign);
        dy = Math.min(Math.abs(dy), maxDistance);
        if (dy > 0) {
            if (dy < maxJumpLength) {
                addPoint(0, dy * sign);
            } else {
                int jumps = dy / maxJumpLength + 1;
                int jumpSize = dy / jumps + 1;
                for (int i = 0; i < jumps; i++) {
                    addPoint(0, Math.min(jumpSize, dy) * sign);
                    dy -= jumpSize;
                }
            }
        }
    }

    private PointI getTarget(PointI source) {
        switch (mainDirection) {
            case 0:
                return getClosestSpawnPoint(new PointI(map.getWidth(), source.y));
            case 1:
                return getClosestSpawnPoint(new PointI(source.x, map.getHeight()));
            case 2:
                return getClosestSpawnPoint(new PointI(0, source.y));
            case 3:
                return getClosestSpawnPoint(new PointI(source.x, 0));
        }
        return PointI.EMPTY;
    }

    private PointI getClosestSpawnPoint(PointI optimal) {
        PointI result = new PointI();
        int minDistance = Integer.MAX_VALUE;
        for (PointI spawnPoint : map.getSpawnPoints()) {
            int distance = getSquareDistance(optimal, spawnPoint);
            if (distance < minDistance && isOpenPoint(spawnPoint)) {
                minDistance = distance;
                result = spawnPoint;
            }
        }
        return result;
    }

    private int getSquareDistance(PointI source, PointI target) {
        int dx = target.x - source.x;
        int dy = target.y - source.y;
        return dx * dx + dy * dy;
    }

    private boolean isOpenPoint(PointI point) {
        int px = point.x;
        int py = point.y;
        for (int i = 0; i < map.getWidth() / 2; i++) {
            px -= DIRS[mainDirection].x;
            py -= DIRS[mainDirection].y;
            if (!map.isPassable(px, py)) {
                return false;
            }
        }
        return true;
    }
}
