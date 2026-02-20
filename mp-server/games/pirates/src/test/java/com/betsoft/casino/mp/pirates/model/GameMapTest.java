package com.betsoft.casino.mp.pirates.model;

import com.betsoft.casino.mp.common.GameMapStore;
import com.betsoft.casino.mp.model.movement.Point;
import com.betsoft.casino.mp.model.movement.Trajectory;
import com.betsoft.casino.mp.pirates.model.math.EnemyRange;
import com.betsoft.casino.mp.pirates.model.math.EnemyType;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class GameMapTest {

    private GameMapStore gameMapStore;

    @Before
    public void setUp() {
        gameMapStore = new GameMapStore();
        gameMapStore.init();
    }

    @Test
    public void shouldGenerateLeaveTrajectoryFromInvalidPoint() {
        long time = System.currentTimeMillis();
        Trajectory trajectory = new Trajectory(4.0f, Arrays.asList(
                new Point(97, 50, time + 10000),
                new Point(90, 50, time + 12000)));
        GameMap map = new GameMap(EnemyRange.BaseEnemies, gameMapStore.getMap(201));
        Enemy enemy = map.addEnemyByTypeNew(EnemyType.ENEMY_1, null, 1, -1, false, false, false);
        enemy.setTrajectory(trajectory);
        assertEquals(1, map.generateShortLeaveTrajectories().size());
    }
}
