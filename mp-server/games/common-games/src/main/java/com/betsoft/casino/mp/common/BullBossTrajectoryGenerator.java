package com.betsoft.casino.mp.common;

import com.betsoft.casino.mp.model.movement.Point;
import com.betsoft.casino.mp.model.movement.Trajectory;
import com.dgphoenix.casino.common.util.Pair;
import com.dgphoenix.casino.common.util.RNG;
import org.kynosarges.tektosyne.geometry.PointI;
import java.util.List;

public class BullBossTrajectoryGenerator extends TrajectoryGenerator {
    public static final int TIME_STEP_1_TURN = 1000;
    public static final int TIME_STEP_2_HOOF = 4000;

    public BullBossTrajectoryGenerator(GameMapShape map, PointI source, double speed) {
        super(map, source, speed);
    }

    @Override
    protected boolean firstStep() {
        Pair<PointI, Integer> bullPoint = getRandomBullPoint();
        trajectory.addPoint(new Point(x, y, time));
//        trajectory.addPoint(new Point(x, y, time + TIME_STEP_1_TURN));
      //  trajectory.addPoint(new Point(x, y, time + 3000));
        x = bullPoint.getKey().x;
        y = bullPoint.getKey().y;
        time += bullPoint.getValue() / (speed / 1000);
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
        if (needFinalSteps)
            finalSteps();
        return trajectory;
    }

    @Override
    protected void finalSteps() {
        Point lastPoint = trajectory.getLastPoint();
        List<PointI> list = map.getSpawnPoints();
        PointI randomSpawnPoint = list.get(RNG.nextInt(list.size()));
        double dist = Math.sqrt(Math.pow((lastPoint.getX() - randomSpawnPoint.x), 2) + Math.pow((lastPoint.getY() - randomSpawnPoint.y), 2));
        int needTime = (int) (dist / (speed / 1000));
        Point lastSpawnPoint = new Point(randomSpawnPoint.x, randomSpawnPoint.y, time + TIME_STEP_2_HOOF + needTime);
        trajectory.addPoint(lastSpawnPoint);
    }


    @Override
    protected void randomStep() {
        addBullPoint(getRandomBullPoint());
    }

    private Pair<PointI, Integer> getRandomBullPoint() {
        int distance = -1;
        for (int dirCurrent = 0; dirCurrent < DIRS.length; dirCurrent++) {
            int currentDistance = getMaxDistanceToBorder(x, y, DIRS[dirCurrent].x, DIRS[dirCurrent].y);
            if (currentDistance > distance) {
                distance = currentDistance;
                dir = dirCurrent;
            }
        }
        int xPoint = this.x + distance * DIRS[dir].x;
        int yPoint = this.y + distance * DIRS[dir].y;
        return new Pair<>(new PointI(xPoint, yPoint), distance);
    }

    private void addBullPoint(Pair<PointI, Integer> target) {
        trajectory.addPoint(new Point(x, y, time));
        trajectory.addPoint(new Point(x, y, time + TIME_STEP_1_TURN));
        trajectory.addPoint(new Point(x, y, time + TIME_STEP_2_HOOF));
        x = target.getKey().x;
        y = target.getKey().y;
        time += target.getValue() / (speed / 1000);
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

}
