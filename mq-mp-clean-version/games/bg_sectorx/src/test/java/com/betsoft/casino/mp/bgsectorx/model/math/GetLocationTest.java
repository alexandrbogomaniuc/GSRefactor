package com.betsoft.casino.mp.bgsectorx.model.math;

import com.betsoft.casino.mp.bgsectorx.model.Enemy;
import com.betsoft.casino.mp.bgsectorx.model.GameMap;
import com.betsoft.casino.mp.bgsectorx.model.math.config.PredefinedPathParam;
import com.betsoft.casino.mp.bgsectorx.model.math.config.SpawnConfig;
import com.betsoft.casino.mp.bgsectorx.model.math.config.SpawnConfigLoader;
import com.betsoft.casino.mp.model.movement.BezierTrajectory;
import com.betsoft.casino.mp.model.movement.Point;
import com.dgphoenix.casino.common.util.RNG;
import org.junit.Test;
import org.kynosarges.tektosyne.geometry.PointD;

import java.util.ArrayList;
import java.util.List;

import static com.betsoft.casino.mp.bgsectorx.model.math.EnemyType.S1;

public class GetLocationTest {
    @Test
    public void testGetLocation() {
        SpawnConfig spawnConfig = new SpawnConfigLoader().loadDefaultConfig();
        List<PredefinedPathParam> predefinedPaths = spawnConfig.getPredefinedPaths();
        PredefinedPathParam param = predefinedPaths.get(RNG.nextInt(predefinedPaths.size()));
        GameMap gameMap = new GameMap();
        List<Point> points = gameMap.generatePointsOfBezierTrajectory(param.getTrajectoryPoints(), param.getTrajectoryType(), 1, false, false);
        BezierTrajectory bezierTrajectory = new BezierTrajectory(0, points);
        Enemy enemy = gameMap.addEnemyWithTrajectory(S1, bezierTrajectory);
        List<Long> timesLocation = new ArrayList<>();
        List<PointD> expected = new ArrayList<>();
        int i = 0;
        while (i < points.size() - 1) {
            long time = points.get(i).getTime();
            if (!timesLocation.contains(time)) {
                timesLocation.add(time);
                expected.add(new PointD(points.get(i).getX(), points.get(i).getY()));
                i++;
            }
        }

        long rndTime = timesLocation.get(0) + RNG.nextInt(20000);
        System.out.println("_____________________________________________");
        System.out.println("suitable points:");
        System.out.println(rndTime);
        PointD pointD = enemy.getEnemyLocation(rndTime);
        for (Point point : points) {
            if (Math.abs(point.getTime() - rndTime) < 200) {
                System.out.println("X: " + point.getX());
                System.out.println("Y: " + point.getY());
                System.out.println(point.getTime());
                System.out.println(point.getTime() - rndTime);
            }
        }
        System.out.println("_____________________________________________");
        System.out.println("search result X: " + pointD.x);
        System.out.println("search result Y: " + pointD.y);

        System.out.println("_______________________________________________________________________________________________________________________");
        System.out.println("Hybrid points searching");
        List<Enemy> enemies = gameMap.spawnHybridFormation(spawnConfig);
        Enemy circular = enemies.get(1);
        long rndHybridTime = circular.getTrajectory().getPoints().get(0).getTime() + RNG.nextInt(20000);
        double dt = 0.01;
        List<PointD> hybridPoints = new ArrayList<>();
        for (double t = 0; t < 1; t += dt) {
            PointD genPointD = circular.getNextPoint(circular.getTrajectory().getPoints(), t);
            System.out.println(genPointD);
            hybridPoints.add(genPointD);
        }
        PointD result = circular.getEnemyLocation(rndHybridTime);
        System.out.println("Result: " + result);
        System.out.println("Is generated points contains: " + hybridPoints.contains(result));
        /*System.out.println(timesLocation.contains(rndTime));*/

        //assertEquals(expected, result);
        /*for (Point point : points) {
            times.add(point.getTime());
        }
        long timeLoc = points.get(15).getTime();
        System.out.println("time: " + timeLoc);
        System.out.println("trajectory X: " + points.get(15).getX());
        System.out.println("trajectory Y: " + points.get(15).getY());

        PointD pointD = enemy.getLocationByCurve(timeLoc);
        System.out.println("getLocation result X: " + pointD.x);
        System.out.println("getLocation result Y: " + pointD.y);*/
    }
}
