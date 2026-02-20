package com.betsoft.casino.mp.clashofthegods;

import com.betsoft.casino.mp.common.*;
import com.betsoft.casino.mp.common.scenarios.SpawnGroup;
import com.betsoft.casino.mp.common.scenarios.SpawnScenario;
import com.betsoft.casino.mp.model.movement.Point;
import com.betsoft.casino.mp.model.movement.Trajectory;
import com.dgphoenix.casino.common.util.Triple;
import org.kynosarges.tektosyne.geometry.PointD;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestPoints {
    private static GameMapStore store = new GameMapStore();
    private static PathMatchingResourcePatternResolver resolver;

    public static void main(String[] args) throws IOException {
//        ClassLoader classLoader = TestPoints.class.getClassLoader();
//        resolver = new PathMatchingResourcePatternResolver(classLoader);
//        GameMapMeta meta = store.loadMeta(resolver.getResource("classpath:maps/clashofthegods/701.json"));
//        List<SpawnScenario> scenarios = meta.getScenarios();
//        SpawnScenario spawnScenario = scenarios.get(0);
//        SpawnGroup spawnGroup = spawnScenario.getGroups().get(0);
        // testDragonFly();

        int x = 85;  int y = -8;
        Coords coords = new Coords(960, 540, 96, 96);
        System.out.print(coords.toScreenX(x, y) + "," + coords.toScreenY(x, y)+ "    ");
        System.out.println(coords.toScreenX(y, x) + "," + coords.toScreenY(y, x));

        x = 105;  y = 13;
        System.out.print(coords.toScreenX(x, y) + "," + coords.toScreenY(x, y)+ "    ");
        System.out.println(coords.toScreenX(y, x) + "," + coords.toScreenY(y, x));


        List<Point> newPoints;
        newPoints = new ArrayList<>();
        List<Point> points = new ArrayList<>();

        points.add(new Point(8, 69, 0));
        points.add(new Point(69, 8, 0));
        System.out.println(points);

        Point firstPoint = new Point(points.get(0));
        Point lastPoint = new Point(points.get(points.size() - 1));

        boolean needIncrement = firstPoint.getX() < firstPoint.getY();
        newPoints.add(firstPoint);
        int cnt = 100;
        double lastX = firstPoint.getX();
        double lastY = firstPoint.getY();
        while (cnt-- > 0) {
            lastX += needIncrement ? 1 : -1;
            lastY += needIncrement ? -1 : 1;
            newPoints.add(new Point(lastX, lastY, 0));
            if ((!needIncrement && lastX <= firstPoint.getY())
                    || (needIncrement && lastX >= firstPoint.getY()))
                break;
        }

        System.out.println(newPoints);

    }

    private static void testDragonFly() {
        int i = 0;
        int groupStartTime = 1200;

        List<Point> basePoints = new ArrayList<>();
        basePoints.add(new Point(81.0, 30.0, 1611632210367L));
        basePoints.add(new Point(64.0, 30.0, 1611632212255L));
        basePoints.add(new Point(64.0, 30.0, 1611632212255L));
        basePoints.add(new Point(47.0, 30.0, 1611632215143L));
        basePoints.add(new Point(47.0, 30.0, 1611632215143L));
        basePoints.add(new Point(30.0, 30.0, 1611632218031L));
        basePoints.add(new Point(30.0, 30.0, 1611632218031L));
        basePoints.add(new Point(13.0, 30.0, 1611632220919L));

        Trajectory baseTrajectory = new Trajectory(9, basePoints);
        double speed = 9;
        long spawnTime = 1611632210367L;

        List<Triple<PointD, Double, Integer>> enemyOffsets = new ArrayList<>();
        enemyOffsets.add(new Triple<>(new PointD(2.12132, -2.12132), 3., 45));
        enemyOffsets.add(new Triple<>(new PointD(-1.02606, -2.81908), 3., 110));
        enemyOffsets.add(new Triple<>(new PointD(-2.94488, 0.572427), 3., 191));
        enemyOffsets.add(new Triple<>(new PointD(-0.365608, 2.97764), 3., 263));


        Trajectory trajectory = TrajectoryUtils.generateSimilarTrajectory(baseTrajectory,
                enemyOffsets.get(i).first().x, enemyOffsets.get(i).first().y, 0, 0,
                speed, 0, spawnTime, groupStartTime, false);

        System.out.println("1: " + trajectory);
        List<Point> points = trajectory.getPoints();
        List<Point> newPoints = new ArrayList<>();
        long shift = 0;
        for (int j = 0; j < points.size(); j++) {
            Point point = points.get(j);
            if (j == 2 || j == 4 || j == 6) {
                shift += 1000;
            }
            point.setTime(point.getTime() + shift);
            newPoints.add(point);
        }
        trajectory = new Trajectory(trajectory.getSpeed(), newPoints);
        System.out.println("2: " + trajectory);

    }

}
