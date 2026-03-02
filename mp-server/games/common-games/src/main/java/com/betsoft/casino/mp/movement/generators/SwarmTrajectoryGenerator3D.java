package com.betsoft.casino.mp.movement.generators;

import com.betsoft.casino.mp.model.IGameMapShape;
import com.betsoft.casino.mp.model.movement.Trajectory;
import com.betsoft.casino.mp.movement.common.Offset;
import com.betsoft.casino.mp.movement.common.Step;
import com.abs.casino.common.util.RNG;
import org.kynosarges.tektosyne.geometry.PointI;

import java.util.ArrayList;
import java.util.List;

import static com.betsoft.casino.mp.model.movement.MathUtils.cos;
import static com.betsoft.casino.mp.model.movement.MathUtils.sin;

public class SwarmTrajectoryGenerator3D extends TrajectoryGenerator3D {
    private List<Offset> enemyOffsets = new ArrayList<>();
    private List<Trajectory> trajectories;

    public SwarmTrajectoryGenerator3D(IGameMapShape map, PointI source, double speed) {
        super(map, source, speed);
        enemyOffsets.add(new Offset(0, 0, 0));
    }

    public SwarmTrajectoryGenerator3D addEnemyWithOffset(Offset offset) throws Exception {
        for (Offset o : enemyOffsets) {
            if (o.equals(offset)) {
                throw new Exception("Enemy with such offset already exists");
            }
        }
        enemyOffsets.add(offset);
        return this;
    }

    public List<Trajectory> generateAll(long spawnTime, int minSteps, boolean needFinalSteps) {
        trajectories = new ArrayList<>();
        for (int i = 0; i < enemyOffsets.size(); i++) {
            trajectories.add(new Trajectory(speed));
        }
        time = spawnTime;
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

    protected Step getStepToExit(double sourceX, double sourceY, int angle) {
        double distance = 0;
        double dx = cos(angle) / 2;
        double dy = sin(angle) / 2;
        double x = sourceX + dx;
        double y = sourceY + dy;
        while (isPassableForAllEnemies(x, y) && isNotSpawnPointForAnyEnemy(x, y)) {
            distance += 0.5;
            x += dx;
            y += dy;
        }
        return map.isSpawnPoint((int) Math.round(x), (int) Math.round(y)) ? new Step(distance, angle) : null;
    }

    protected double calculateDistanceToWall(int angle) {
        double distance = 0;
        double dx = cos(angle) / 2;
        double dy = sin(angle) / 2;
        double x = currentX + dx;
        double y = currentY + dy;
        while (isPassableForAllEnemies(x, y)) {
            distance += 0.5;
            x += dx;
            y += dy;
        }
        return distance;
    }

    private boolean isPassableForAllEnemies(double x, double y) {
        for (Offset offset : enemyOffsets) {
            if (!map.isPassable((int) Math.round(x + offset.getDx()), (int) Math.round(y + offset.getDy()))) {
                return false;
            }
        }
        return true;
    }

    private boolean isNotSpawnPointForAnyEnemy(double x, double y) {
        for (Offset offset : enemyOffsets) {
            if (map.isSpawnPoint((int) (x + offset.getDx()), (int) (y + offset.getDy()))) {
                return false;
            }
        }
        return true;
    }

    protected void addPoint(double distance, int angle) {
        currentAngle = angle;
        currentX += distance * cos(angle);
        currentY += distance * sin(angle);
        time += distance / (speed / 1000) + animationDelay;
        for (int i = 0; i < trajectories.size(); i++) {
            Offset offset = enemyOffsets.get(i);
            trajectories.get(i).addPoint(currentX + offset.getDx(), currentY + offset.getDy(), time + offset.getDt());
        }
    }
}
