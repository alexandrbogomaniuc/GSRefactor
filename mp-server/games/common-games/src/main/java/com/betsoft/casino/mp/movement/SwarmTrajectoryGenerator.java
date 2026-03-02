package com.betsoft.casino.mp.movement;

import com.betsoft.casino.mp.common.TrajectoryGenerator;
import com.betsoft.casino.mp.common.WaypointTrajectoryGenerator;
import com.betsoft.casino.mp.model.IGameMapShape;
import com.betsoft.casino.mp.model.movement.Point;
import com.betsoft.casino.mp.model.movement.Trajectory;
import com.betsoft.casino.mp.movement.common.Offset;
import com.abs.casino.common.util.RNG;
import org.kynosarges.tektosyne.geometry.PointI;

import java.util.ArrayList;
import java.util.List;

public class SwarmTrajectoryGenerator extends TrajectoryGenerator {
    private List<Offset> enemyOffsets = new ArrayList<>();
    private List<Trajectory> trajectories;

    public SwarmTrajectoryGenerator(IGameMapShape map, PointI source, double speed) {
        super(map, source, speed);
        enemyOffsets.add(new Offset(0, 0, 0));
    }

    public SwarmTrajectoryGenerator addEnemyWithOffset(Offset offset) {
        enemyOffsets.add(offset);
        return this;
    }

    public List<Trajectory> generateAll(long spawnTime, int minSteps, boolean needFinalSteps) {
        trajectories = new ArrayList<>();
        for (int i = 0; i < enemyOffsets.size(); i++) {
            trajectories.add(new Trajectory(speed));
        }
        time = spawnTime;
        addPoint(0);
        if (!firstStep()) {
            return null;
        }
        int steps = RNG.nextInt(minSteps, minSteps + 3);
        for (int i = 0; i < steps; i++) {
            randomStep();
        }
        if (needFinalSteps) {
            finalSteps();
        }
        return trajectories;
    }

    @Override
    protected int getMaxDistance(int x, int y, int dx, int dy) {
        int result = 0;
        int nx = x + dx;
        int ny = y + dy;
        while (nx >= 0 && ny >= 0 && nx < map.getWidth() && ny < map.getHeight() && isPassableForAllEnemies(nx, ny)) {
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
        while (nx >= 0 && ny >= 0 && nx < map.getWidth() && ny < map.getHeight() && isPassableForAllEnemies(nx, ny)
                && !map.isBorder(nx, ny)) {
            result++;
            nx += dx;
            ny += dy;
        }
        return result;
    }

    @Override
    protected void addPoint(int distance) {
        addPoint(distance * DIRS[dir].x, distance * DIRS[dir].y);
    }

    @Override
    protected void addPoint(int dx, int dy) {
        x += dx;
        y += dy;
        time += Math.sqrt(dx * dx + dy * dy) * 1000 / speed;
        for (int i = 0; i < trajectories.size(); i++) {
            Offset offset = enemyOffsets.get(i);
            trajectories.get(i).addPoint(x + offset.getDx(), y + offset.getDy(), time + offset.getDt());
        }
    }

    @Override
    protected void finalSteps() {
        WaypointTrajectoryGenerator generator = new WaypointTrajectoryGenerator(map);
        for (int i = 0; i < trajectories.size(); i++) {
            Offset offset = enemyOffsets.get(i);
            Trajectory baseTrajectory = trajectories.get(i);
            Trajectory finalTrajectory = generator.generate((int) (x + offset.getDx()), (int) (y + offset.getDy()), time + offset.getDt(), speed);
            for (Point point : finalTrajectory.getPoints()) {
                baseTrajectory.addPoint(point);
            }
        }

    }

    private boolean isPassableForAllEnemies(double x, double y) {
        for (Offset offset : enemyOffsets) {
            if (!map.isPassable((int) Math.round(x + offset.getDx()), (int) Math.round(y + offset.getDy()))) {
                return false;
            }
        }
        return true;
    }
}

