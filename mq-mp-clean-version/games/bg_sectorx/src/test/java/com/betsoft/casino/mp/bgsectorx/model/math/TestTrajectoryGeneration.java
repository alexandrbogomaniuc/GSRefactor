package com.betsoft.casino.mp.bgsectorx.model.math;

import com.betsoft.casino.mp.bgsectorx.model.GameMap;
import com.betsoft.casino.mp.bgsectorx.model.math.config.PredefinedPathParam;
import com.betsoft.casino.mp.bgsectorx.model.math.config.SpawnConfig;
import com.betsoft.casino.mp.bgsectorx.model.math.config.SpawnConfigLoader;
import com.betsoft.casino.mp.common.GameMapStore;
import com.betsoft.casino.mp.model.movement.Point;
import com.betsoft.casino.mp.model.movement.PossiblePoints;
import com.betsoft.casino.mp.model.movement.Trajectory;
import com.betsoft.casino.mp.movement.common.SpawnCurvePoint;
import com.betsoft.casino.mp.movement.generators.BezierCurveGenerator;
import com.betsoft.casino.mp.movement.generators.PathParam;
import com.dgphoenix.casino.common.util.KryoHelper;
import com.dgphoenix.casino.common.util.RNG;
import com.dgphoenix.casino.common.util.Triple;
import org.apache.commons.codec.binary.Base64;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class TestTrajectoryGeneration {
    static int resX = 960;
    static int resY = 540;
    static BezierCurveGenerator bezierCurveGenerator = new BezierCurveGenerator(resX, resY, 50, 30);

    public static void main(String[] args) {
        DecimalFormat decimalFormat = new DecimalFormat("#.");
        //test1();
        //test2();
        //test3();
        //test4();
        //testS2();
        //generateTrajectoriesForS2(EnemyType.S2);
        //generateTrajectoriesForS2(EnemyType.S26);
        //testPathLength();

//        for (int i = 0; i < 44; i++) {
//            SpawnCurvePoint spawnCurvePointByIndex = bezierCurveGenerator.getSpawnCurvePointByIndex(i);
//            System.out.println("idx: "  + (i+1) + ", x: " +   decimalFormat.format(spawnCurvePointByIndex.getX())
//                    + ", y: " + decimalFormat.format(spawnCurvePointByIndex.getY()));
//        }

        SpawnConfig spawnConfig = new SpawnConfigLoader().loadDefaultConfig();
        List<PredefinedPathParam> predefinedPaths = spawnConfig.getPredefinedPaths();
        /*predefinedPaths.forEach((enemyType, predefinedPathParams) -> System.out.println(predefinedPathParams));*/
        for (int i = 0; i < predefinedPaths.size(); ++i) {
            System.out.println(predefinedPaths.get(i).toString());
        }
        System.out.println("end");

        GameMapStore gameMapStore = new GameMapStore();
        gameMapStore.init();
        GameMap currentMap = new GameMap(EnemyRange.BASE_ENEMIES, gameMapStore.getMap(1401));
        Trajectory randomBezierTrajectory = currentMap.getRandomTrajectory(EnemyType.F5, spawnConfig);
        System.out.println("randomBezierTrajectory: " + randomBezierTrajectory);
    }

    private static void test3() {
        for (int idx1 = 0; idx1 < 43; idx1++) {
            for (int idx2 = 0; idx2 < 43; idx2++) {
                int cnt = 0;
                for (int i = 0; i < 1000; i++) {
                    if (testCheckTangentialDirection(idx1, idx2, RNG.nextInt(resX), RNG.nextInt(resY))) {
                        cnt++;
                    }
                }
                if (cnt > 0) {
                    System.out.println("idx1: " + idx1 + ", idx2: " + idx2 + ", cnt: " + cnt);
                }
            }
        }
    }

    private static boolean testCheckTangentialDirection(int idxPoint1, int idxPoint2, double randomMiddleX, double randomMiddleY) {
        EnemyType enemyType = EnemyType.S5;
        PathParam paramByEnemy = MathData.getPathParamByEnemy(enemyType);
        //System.out.println("paramByEnemy: " + paramByEnemy);

        SpawnCurvePoint pointByIndex1 = bezierCurveGenerator.getSpawnCurvePointByIndex(idxPoint1);
        SpawnCurvePoint pointByIndex2 = bezierCurveGenerator.getSpawnCurvePointByIndex(idxPoint2);
        ArrayList<Point> points = new ArrayList<>();
        long timePath = 10000 + RNG.nextInt(10000);
        long startTime = System.currentTimeMillis() + 1000;

        points.add(new Point(pointByIndex1.getX(), pointByIndex1.getY(), startTime));
        points.add(new Point(randomMiddleX, randomMiddleY, startTime + timePath / 2));
        points.add(new Point(pointByIndex2.getX(), pointByIndex2.getY(), startTime + timePath));

        boolean checkTangentialDirection = bezierCurveGenerator.checkTangentialDirection(points);

        if (checkTangentialDirection) {
//            System.out.println("checkTangentialDirection found, points: " + points);
//            System.out.println("point 1: x: " + points.get(0).getX() / 60 + ", y: " + points.get(0).getY() / 60);
//            System.out.println("point 2: x: " + points.get(1).getX() / 60 + ", y: " + points.get(1).getY() / 60);
//            System.out.println("point 3: x: " + points.get(2).getX() / 60 + ", y: " + points.get(2).getY() / 60);
        }

        return checkTangentialDirection;
    }

    private static void testPathLength() {
        EnemyType enemyType = EnemyType.S5;
        PathParam paramByEnemy = MathData.getPathParamByEnemy(enemyType);
        SpawnCurvePoint pointByIndex1 = bezierCurveGenerator.getSpawnCurvePointByIndex(0);
        SpawnCurvePoint pointByIndex2 = bezierCurveGenerator.getSpawnCurvePointByIndex(1);
        ArrayList<Point> points = new ArrayList<>();
        long timePath = 10000 + RNG.nextInt(10000);
        long startTime = System.currentTimeMillis() + 1000;

        points.add(new Point(pointByIndex1.getX(), pointByIndex1.getY(), startTime));
        points.add(new Point(100, 30, startTime + timePath / 2));
        points.add(new Point(pointByIndex2.getX(), pointByIndex2.getY(), startTime + timePath));

        System.out.println("points: " + points);

        List<Triple<Double, Double, Double>> interSetPoints = bezierCurveGenerator.getListInterSetPoints(points);
        double lengthAndPoints = bezierCurveGenerator.checkLengthAndPoints(interSetPoints, false, points);

        System.out.println("interSetPoints: " + interSetPoints);
        System.out.println("paramByEnemy: " + paramByEnemy);
        System.out.println("lengthAndPoints: " + lengthAndPoints + ", lengthAndPoints norm: " + lengthAndPoints / (resX / 16));


    }

    private static void test1() {
        for (int i = 0; i <= 43; i++) {
            List<EnemyType> enemies = EnemyRange.BASE_ENEMIES.getEnemies();
            EnemyType enemyType = enemies.get(RNG.nextInt(enemies.size()));
            PathParam paramByEnemy = MathData.getPathParamByEnemy(enemyType);
            Trajectory trajectory = bezierCurveGenerator.generateQuadratic(enemyType.getId(), paramByEnemy);
            System.out.println("i: " + i + ", enemyType: " + enemyType + ",  trajectory: " + trajectory.getPoints());
        }
    }

    private static void testS2() {
        for (int i = 0; i <= 10; i++) {
            EnemyType enemyType = EnemyType.S2;
            PathParam paramByEnemy = MathData.getPathParamByEnemy(enemyType);
            Trajectory trajectory = bezierCurveGenerator.generateQuadratic(enemyType.getId(), paramByEnemy);
            //  System.out.println("i: " +   i +  ", enemyType: "  + enemyType + ",  trajectory: " + trajectory.getPoints());
        }
    }

    private static void test4() {
        for (int i = 0; i <= 43; i++) {
            List<EnemyType> enemies = EnemyRange.BASE_ENEMIES.getEnemies();
            EnemyType enemyType = enemies.get(RNG.nextInt(enemies.size()));
            PathParam paramByEnemy = MathData.getPathParamByEnemy(enemyType);
            Trajectory trajectory = bezierCurveGenerator.generateLinear(enemyType.getId(), 10000, false, paramByEnemy);
            System.out.println("i: " + i + ", enemyType: " + enemyType + ",  trajectory: " + trajectory.getPoints());
        }
    }

    private static void test2() {
        for (int i = 0; i < 100; i++) {

            EnemyType enemyType = EnemyType.S15;
            PathParam paramByEnemy = MathData.getPathParamByEnemy(enemyType);
            Trajectory trajectory = bezierCurveGenerator.generateQuadratic(enemyType.getId(), paramByEnemy);
            System.out.println("i: " + i + ", trajectory: " + trajectory.getPoints());
        }
    }


    private static void generateTrajectoriesForS2(EnemyType enemyType) {
        PathParam paramByEnemy = MathData.getPathParamByEnemy(enemyType);
        PossiblePoints possiblePoints = bezierCurveGenerator.generatePossibleQuadraticPoints(paramByEnemy);
        System.out.println("listsPoints.size(): " + possiblePoints.getListPoints().size());
        String listsPointsData = Base64.encodeBase64String(KryoHelper.serializeToBytes(possiblePoints));
        System.out.println("listsPointsData=" + listsPointsData);

        PossiblePoints possiblePointsDeserialize = KryoHelper.deserializeFrom(Base64.decodeBase64(listsPointsData), PossiblePoints.class);
        int idx = 0;
        for (List<Point> points : possiblePointsDeserialize.getListPoints()) {
            System.out.println("idx: " + idx + ", trajectory points: " + points);
            idx++;
        }

    }


}
