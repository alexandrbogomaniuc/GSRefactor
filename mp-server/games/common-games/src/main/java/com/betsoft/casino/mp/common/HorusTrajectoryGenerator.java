package com.betsoft.casino.mp.common;

import com.betsoft.casino.mp.model.movement.InvulnerablePoint;
import com.betsoft.casino.mp.model.movement.Point;
import com.betsoft.casino.mp.model.movement.TeleportPoint;
import com.dgphoenix.casino.common.util.RNG;
import org.kynosarges.tektosyne.geometry.PointI;

public class HorusTrajectoryGenerator  extends TrajectoryGenerator {
    public static final int INVISIBILITY_START_TIME = 945;
    public static final int TELEPORT_START_TIME = 1945;
    public static final int TELEPORT_FINISH_TIME = 1946;
    public static final int INVISIBILITY_FINISH_TIME = 2646;
    public static final int ANIMATION_DURATION = 2504;
    public static final int LIVE_TIME = 4000;

    public HorusTrajectoryGenerator(GameMapShape map, PointI source, double speed) {
        super(map, source, speed);
    }

    @Override
    protected boolean firstStep() {
        addTeleportPoint(getRandomTeleportPoint());
        return true;
    }

    @Override
    protected void finalSteps() {
        trajectory.addPoint(new TeleportPoint(x, y, time, false));
        trajectory.addPoint(new InvulnerablePoint(x, y, time + INVISIBILITY_START_TIME));
        trajectory.addPoint(new InvulnerablePoint(x, y, time + TELEPORT_START_TIME));
    }

    @Override
    protected void randomStep() {
        addTeleportPoint(getRandomTeleportPoint());
    }

    private PointI getRandomTeleportPoint() {
        while (true) {
            int x = RNG.nextInt(map.getWidth());
            int y = RNG.nextInt(map.getHeight());
            if (map.isValid(x, y) && map.isNotMarked(x, y) && map.isBossPath(x, y)) {
                return new PointI(x, y);
            }
        }
    }

    private void addTeleportPoint(PointI target) {
        trajectory.addPoint(new TeleportPoint(x, y, time, true));
        trajectory.addPoint(new InvulnerablePoint(x, y, time + INVISIBILITY_START_TIME));
        trajectory.addPoint(new InvulnerablePoint(x, y, time + TELEPORT_START_TIME));

        x = target.x;
        y = target.y;
        trajectory.addPoint(new InvulnerablePoint(x, y, time + TELEPORT_FINISH_TIME));
        trajectory.addPoint(new Point(x, y, time + INVISIBILITY_FINISH_TIME));
        time += ANIMATION_DURATION + LIVE_TIME;
        trajectory.addPoint(new Point(x, y, time));
    }
}
