package com.betsoft.casino.mp.common;

import org.kynosarges.tektosyne.geometry.PointI;

public class LargeEnemyFreeAngleTrajectoryGenerator extends FreeAngleTrajectoryGenerator {

    public LargeEnemyFreeAngleTrajectoryGenerator(GameMapShape map, PointI source, double speed) {
        super(map, source, speed);
    }

    @Override
    protected int getMaxDistance(int x, int y, int dx, int dy) {
        int result = 0;
        int nx = x + dx;
        int ny = y + dy;
        while (nx >= 0 && ny >= 0 && nx < map.getWidth() && ny < map.getHeight() && map.isPassableForLargeEnemies(nx, ny)) {
            result++;
            nx += dx;
            ny += dy;
        }
        return result;
    }

    @Override
    public int getMaxDistanceToBorder(int x, int y, int dx, int dy) {
        if (!map.isValid(x, y)) {
            return 0;
        }
        if (map.isBorder(x, y) || map.isSpawnPoint(x, y) || map.isWallForLargeEnemies(x, y)) {
            int distance = getMaxDistance(x, y, dx, dy);
            int nx = x + dx * distance;
            int ny = y + dy * distance;
            for (int dist = distance; dist > 0; dist--) {
                if (!map.isBorder(nx, ny) && !map.isSpawnPoint(nx, ny) && !map.isWallForLargeEnemies(nx, ny)) {
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
        while (nx >= 0 && ny >= 0 && nx < map.getWidth() && ny < map.getHeight() && map.isPassableForLargeEnemies(nx, ny)
                && !map.isBorder(nx, ny)) {
            result++;
            nx += dx;
            ny += dy;
        }
        return result;
    }
}
