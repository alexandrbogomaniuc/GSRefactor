package com.betsoft.casino.mp.sectorx.model.math;

import com.betsoft.casino.mp.model.movement.BezierTrajectory;
import com.betsoft.casino.mp.model.movement.Point;
import com.betsoft.casino.mp.model.movement.Trajectory;
import com.betsoft.casino.mp.sectorx.model.Enemy;
import com.betsoft.casino.mp.sectorx.model.GameMap;
import com.betsoft.casino.mp.sectorx.model.ValidatedBezierPoint;
import com.betsoft.casino.mp.sectorx.model.math.config.PredefinedPathParam;
import com.betsoft.casino.mp.sectorx.model.math.config.SpawnConfig;
import com.betsoft.casino.mp.sectorx.model.math.config.SpawnConfigLoader;
import com.dgphoenix.casino.common.util.RNG;
import org.junit.Test;
import org.kynosarges.tektosyne.geometry.PointD;

import java.util.ArrayList;
import java.util.List;

import static com.betsoft.casino.mp.sectorx.model.math.EnemyType.S1;

public class ApproximateTrajectoryTest {

    @Test
    public void testApproximate() {
        SpawnConfig spawnConfig = new SpawnConfigLoader().loadDefaultConfig();
        List<PredefinedPathParam> predefinedPaths = spawnConfig.getPredefinedPaths();
        PredefinedPathParam param = predefinedPaths.get(18);
        GameMap gameMap = new GameMap();

        //calculated points with formula
        List<Point> calculatedPoints = gameMap.generatePointsOfBezierTrajectory(param.getTrajectoryPoints(), param.getTrajectoryType(), 1, false, false);
        BezierTrajectory bezierTrajectory = new BezierTrajectory(0, calculatedPoints);

        //enemy with trajectory with anchor points
        Trajectory trajectory = gameMap.getTrajectory(param, true, 20000);
        Enemy enemy = gameMap.addEnemyWithTrajectory(S1, trajectory);
        List<Point> anchorPoints = trajectory.getPoints();
        Trajectory traj = new BezierTrajectory(0, anchorPoints);
        System.out.println(anchorPoints);
        boolean flag = gameMap.isPointOnMapApproximate(trajectory, 15000, 10);
        /*for (ValidatedBezierPoint point : validatedBezierPoints) {
            System.out.println("X: " + point.getX());
            System.out.println("Y: " + point.getY());
            System.out.println("percent: " + point.getCorrectPercent());
        }*/
        System.out.println(flag);
    }
}
