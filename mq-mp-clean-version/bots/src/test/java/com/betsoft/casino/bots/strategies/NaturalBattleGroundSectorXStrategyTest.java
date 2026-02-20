package com.betsoft.casino.bots.strategies;

import com.betsoft.casino.mp.model.movement.BezierTrajectory;
import com.betsoft.casino.mp.model.movement.Point;
import com.betsoft.casino.mp.model.movement.Trajectory;
import com.betsoft.casino.mp.transport.RoomEnemy;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

//@Ignore
public class NaturalBattleGroundSectorXStrategyTest {

    @Test
    public void NaturalBattleGroundSectorXStrategy_CompareGenericAndQuadraticBezierEnemyPositions_Test() throws Exception {

        NaturalBattleGroundSectorXStrategy strategy = new NaturalBattleGroundSectorXStrategy(100, 1, 100,
                String.valueOf(NaturalBattleGroundSectorXStrategy.ENEMY_TYPE_ID_SECTORX_CONCEALED_COINS));

        long serverTime1 = 1740679098807L;//first point time
        long serverTime2 = 1740679100000L;
        long serverTime3 = 1740679106385L;//second point time
        long serverTime4 = 1740679109000L;
        long serverTime5 = 1740679113963L;//third point time

        List<Point> points = new ArrayList<>();

        points.add(new Point(-104.143, -51.2095, 1740679098807L));
        points.add(new Point(992.82, 1040.93, 1740679106385L));
        points.add(new Point(708.234, -118.7, 1740679113963L));

        Trajectory trajectory = new BezierTrajectory(0, points, -1);

        RoomEnemy roomEnemy = new RoomEnemy(
                473792L,
                13L,
                false,
                7.627982139587402,
                "",
                0,
                1,
                1,
                trajectory,
                -1,
                1.0,
                new ArrayList<>(),
                0,
                0,
                -1
        );

        Point cubicBezierEnemyPosition1 = strategy.getQuadraticBezierEnemyPosition(roomEnemy, serverTime1);
        Point genericBezierEnemyPosition1 = strategy.getGenericBezierEnemyPosition(roomEnemy, serverTime1);

        Point cubicBezierEnemyPosition2 = strategy.getQuadraticBezierEnemyPosition(roomEnemy, serverTime2);
        Point genericBezierEnemyPosition2 = strategy.getGenericBezierEnemyPosition(roomEnemy, serverTime2);

        Point cubicBezierEnemyPosition3 = strategy.getQuadraticBezierEnemyPosition(roomEnemy, serverTime3);
        Point genericBezierEnemyPosition3 = strategy.getGenericBezierEnemyPosition(roomEnemy, serverTime3);

        Point cubicBezierEnemyPosition4 = strategy.getQuadraticBezierEnemyPosition(roomEnemy, serverTime4);
        Point genericBezierEnemyPosition4 = strategy.getGenericBezierEnemyPosition(roomEnemy, serverTime4);

        Point cubicBezierEnemyPosition5 = strategy.getQuadraticBezierEnemyPosition(roomEnemy, serverTime5);
        Point genericBezierEnemyPosition5 = strategy.getGenericBezierEnemyPosition(roomEnemy, serverTime5);
    }
}
