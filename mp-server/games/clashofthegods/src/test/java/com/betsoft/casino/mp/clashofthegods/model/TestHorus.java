package com.betsoft.casino.mp.clashofthegods.model;
import com.betsoft.casino.mp.common.GameMapShape;
import com.betsoft.casino.mp.common.GameMapStore;
import com.betsoft.casino.mp.common.HorusTrajectoryGenerator;
import com.betsoft.casino.mp.model.movement.Point;
import com.betsoft.casino.mp.model.movement.Trajectory;
import org.junit.Before;
import org.junit.Test;
import org.kynosarges.tektosyne.geometry.PointI;

import java.util.List;

public class TestHorus {

    private GameMapStore gameMapStore;


    @Before
    public void setUp() {
        gameMapStore = new GameMapStore();
        gameMapStore.init();
    }

    @Test
    public void testFreeze() {
        GameMapShape map = gameMapStore.getMap(703);
        Trajectory trajectory = new HorusTrajectoryGenerator(map, new PointI(), 2.3f)
                .generate(System.currentTimeMillis() + 1000, 7, true);
        List<Point> points = trajectory.getPoints();
        for (int i = 0; i < points.size(); i++) {
            Point point = points.get(i);
            System.out.println(point);
        }
    }
}
