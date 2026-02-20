package com.betsoft.casino.mp.clashofthegods.model;

import com.betsoft.casino.mp.clashofthegods.model.math.EnemyType;
import com.betsoft.casino.mp.common.BullBossTrajectoryGenerator;
import com.betsoft.casino.mp.common.GameMapShape;
import com.betsoft.casino.mp.common.GameMapStore;
import com.betsoft.casino.mp.model.movement.InvulnerablePoint;
import com.betsoft.casino.mp.model.movement.Point;
import com.betsoft.casino.mp.model.movement.Trajectory;
import org.kynosarges.tektosyne.geometry.PointI;

import java.util.ArrayList;
import java.util.List;

public class TestBullBoss {
    protected static final Long BOSS_INVULNERABILITY_TIME = 3000L;

    public static void main(String[] args) {
        GameMapStore gameMapStore  = new GameMapStore();;
        gameMapStore.init();
        float speed = EnemyType.Boss.getSkins().get(2).getSpeed();
        long spawnTime = System.currentTimeMillis() + 2000;
        GameMapShape map = gameMapStore.getMap(703);
        PointI spawnPoint = map.getBossSpawnPoint();
        List<Point> points = new ArrayList<>();
        points.add(new InvulnerablePoint(spawnPoint.x, spawnPoint.y, spawnTime));

        long time = spawnTime + BOSS_INVULNERABILITY_TIME;
//        points.add(new Point(spawnPoint.x, spawnPoint.y, time));
        BullBossTrajectoryGenerator bullBossTrajectoryGenerator = new BullBossTrajectoryGenerator(map, spawnPoint, speed);
        Trajectory trajectory = bullBossTrajectoryGenerator.generate(new Trajectory(speed, points, 800), time, 7, true);
        System.out.println(trajectory);

        Point prev = null;
        for (Point point : trajectory.getPoints()) {
            System.out.println(point + " diff time with prev: " +  (prev != null ? point.getTime() - prev.getTime(): 0));
            prev = point;
        }

    }
}
