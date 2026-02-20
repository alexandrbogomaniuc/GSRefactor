package com.betsoft.casino.mp.sectorx.model.math;

import com.betsoft.casino.mp.model.movement.BezierTrajectory;
import com.betsoft.casino.mp.model.movement.Point;
import com.betsoft.casino.mp.model.movement.Trajectory;
import com.betsoft.casino.mp.sectorx.model.Enemy;
import com.betsoft.casino.mp.sectorx.model.GameMap;
import com.betsoft.casino.mp.sectorx.model.math.config.PredefinedPathParam;
import com.betsoft.casino.mp.sectorx.model.math.config.SpawnConfig;
import com.betsoft.casino.mp.sectorx.model.math.config.SpawnConfigLoader;
import com.dgphoenix.casino.common.util.RNG;
import org.junit.Test;
import org.kynosarges.tektosyne.geometry.PointD;

import java.util.ArrayList;
import java.util.List;

import static com.betsoft.casino.mp.sectorx.model.math.EnemyType.S1;

public class GetLocationTest {
    @Test
    public void testGetLocation() {
        SpawnConfig spawnConfig = new SpawnConfigLoader().loadDefaultConfig();
        List<PredefinedPathParam> predefinedPaths = spawnConfig.getPredefinedPaths();
        PredefinedPathParam param = predefinedPaths.get(2);
        GameMap gameMap = new GameMap();

        //calculated points with formula
        List<Point> calculatedPoints = gameMap.generatePointsOfBezierTrajectory(param.getTrajectoryPoints(), param.getTrajectoryType(), 1, false, false);
        BezierTrajectory bezierTrajectory = new BezierTrajectory(0, calculatedPoints);

        //enemy with trajectory with anchor points
        Trajectory trajectory = gameMap.getTrajectory(param, true, 20000);
        Enemy enemy = gameMap.addEnemyWithTrajectory(S1, trajectory);
        List<Point> anchorPoints = trajectory.getPoints();
        System.out.println(anchorPoints.get(1));

        int rndNum = 49/*RNG.nextInt(0, calculatedPoints.size())*/;
        Point rndCalculatedPoint = calculatedPoints.get(rndNum);
        System.out.println("rndNum: " + rndNum);
        long rndCalculatedTime = rndCalculatedPoint.getTime();
        System.out.println("Time to search: " + rndCalculatedTime);
        System.out.println("Calculated x: " + rndCalculatedPoint.getX());
        System.out.println("Calculated y: " + rndCalculatedPoint.getY());

        PointD resultPoint = enemy.getEnemyLocation(rndCalculatedTime);
        System.out.println("Result x: " + resultPoint.x);
        System.out.println("Result y: " + resultPoint.y);


       /* System.out.println("_____________________________________________");
        System.out.println("suitable points:");
        for (Point point : points) {
            *//*if (Math.abs(point.getTime() - timeToSearh) < 200) {*//*
            System.out.println("X: " + point.getX());
            System.out.println("Y: " + point.getY());
            System.out.println(point.getTime());
            //System.out.println(point.getTime() - timeToSearh);
            *//*}*//*
        }
        PointD pointD = enemy.getEnemyLocation(timeToSearh);
        System.out.println("_____________________________________________");
        System.out.println("search result X: " + pointD.x);
        System.out.println("search result Y: " + pointD.y);*/

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
        /*System.out.println(timesLocation.contains(timeToSearh));*/

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
