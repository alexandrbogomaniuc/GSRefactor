package com.betsoft.casino.mp.common;

import com.betsoft.casino.mp.model.movement.InvulnerablePoint;
import com.betsoft.casino.mp.model.movement.TeleportPoint;
import com.betsoft.casino.mp.model.movement.Trajectory;
import com.dgphoenix.casino.common.util.RNG;
import org.kynosarges.tektosyne.geometry.PointI;

import static com.betsoft.casino.mp.common.TrajectoryUtils.distance;

public class GargoyleTrajectoryGenerator extends TrajectoryGenerator {
    private static final int ARRIVAL_FINISH_TIME = 500;
    private static final int FLY_START_TIME = ARRIVAL_FINISH_TIME + 2700;
    private static final int FLY_MAX_TIME = 1700;
    private static final int TELEPORT_START_TIME = 3200;
    private static final int TELEPORT_FINISH_TIME = TELEPORT_START_TIME + 700;
    private static final int INVISIBILITY_MIN_TIME = 15000;
    private static final int INVISIBILITY_MAX_TIME = 15000;

    public GargoyleTrajectoryGenerator(GameMapShape map, double speed) {
        super(map, new PointI(), speed);
    }

    @Override
    protected boolean firstStep() {
        return true;
    }

    @Override
    protected void finalSteps() {
        // Gargoyle leaves field by finishing current movement phase
    }

    @Override
    protected void randomStep() {
        PointI point = getRandomTeleportPoint();
        trajectory.addPoint(new InvulnerablePoint(point.x, point.y, time));
        trajectory.addPoint(point.x, point.y, time + ARRIVAL_FINISH_TIME);
        trajectory.addPoint(point.x, point.y, time + FLY_START_TIME);
        PointI nextPoint = getRandomTeleportPoint();
        time += FLY_START_TIME + getFlyTime(point, nextPoint);
        trajectory.addPoint(nextPoint.x, nextPoint.y, time);
        trajectory.addPoint(nextPoint.x, nextPoint.y, time + TELEPORT_START_TIME);
        trajectory.addPoint(new TeleportPoint(nextPoint.x, nextPoint.y, time + TELEPORT_START_TIME, true));
        trajectory.addPoint(new InvulnerablePoint(nextPoint.x, nextPoint.y, time + TELEPORT_FINISH_TIME));
        time += TELEPORT_FINISH_TIME + RNG.nextInt(INVISIBILITY_MIN_TIME, INVISIBILITY_MAX_TIME);
        trajectory.addPoint(new InvulnerablePoint(nextPoint.x, nextPoint.y, time));
    }

    private long getFlyTime(PointI source, PointI target) {
        long flyTime = (long) (distance(source, target) * 1000 / speed);
        return Math.min(flyTime, FLY_MAX_TIME);
    }

    private PointI getRandomTeleportPoint() {
        while (true) {
            int x = RNG.nextInt(map.getWidth());
            int y = RNG.nextInt(map.getHeight());
            if (map.isValid(x, y) && map.isNotMarked(x, y)) {
                return new PointI(x, y);
            }
        }
    }

    public Trajectory generate(long startTime, int minSteps) {
        trajectory = new Trajectory(speed);
        time = startTime;
        firstStep();
        for (int i = 0; i < minSteps; i++) {
            randomStep();
        }
        return trajectory;
    }
}
