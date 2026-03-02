package com.betsoft.casino.mp.common;

import com.betsoft.casino.mp.model.movement.InvulnerablePoint;
import com.betsoft.casino.mp.model.movement.TeleportPoint;
import com.betsoft.casino.mp.model.movement.Trajectory;
import com.abs.casino.common.util.RNG;
import org.kynosarges.tektosyne.geometry.PointI;

import java.util.List;

public class TeleportTrajectoryGenerator extends TrajectoryGenerator {

    private final long animationStartTime;
    private final long animationFinishTime;
    private final long teleportCooldown;
    private final long teleportDuration;
    private final boolean canTeleportIntoMap;

    private boolean needFinalTeleport = false;
    private long teleportTime = 0;

    public TeleportTrajectoryGenerator(GameMapShape map, PointI source, double speed,
                                       long animationStartTime, long animationFinishTime,
                                       long teleportCooldown, long teleportDuration, boolean canTeleportIntoMap) {
        super(map, source, speed);
        this.animationStartTime = animationStartTime;
        this.animationFinishTime = animationFinishTime;
        this.teleportCooldown = teleportCooldown;
        this.teleportDuration = teleportDuration;
        this.canTeleportIntoMap = canTeleportIntoMap;
    }

    @Override
    public Trajectory generate(Trajectory trajectory, long startTime, int minSteps, boolean needFinalSteps) {
        if (!canTeleportIntoMap) {
            teleportTime = startTime;
        }
        return super.generate(trajectory, startTime, minSteps, needFinalSteps);
    }

    @Override
    protected boolean firstStep() {
        if (canTeleportIntoMap && shouldTeleport()) {
            teleportTime = time;
            addTeleportPoint(getRandomTeleportPoint());
            return true;
        }
        return super.firstStep();
    }

    @Override
    protected void finalSteps() {
        if (needFinalTeleport) {
            trajectory.addPoint(new TeleportPoint(x, y, time, true));
            trajectory.addPoint(new InvulnerablePoint(x, y, time + animationStartTime));
        } else if (shouldTeleport()) {
            List<PointI> spawns = map.getSpawnPoints();
            addTeleportPoint(spawns.get(RNG.nextInt(spawns.size())));
            addPoint(0);
        } else {
            super.finalSteps();
        }
    }

    @Override
    protected void randomStep() {
        if (shouldTeleport()) {
            teleportTime = time;
            addTeleportPoint(getRandomTeleportPoint());
        } else {
            super.randomStep();
        }
    }

    public TeleportTrajectoryGenerator setNeedFinalTeleport(boolean value) {
        this.needFinalTeleport = value;
        return this;
    }

    private boolean shouldTeleport() {
        return teleportTime + teleportCooldown < time && RNG.nextInt(2) == 0;
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

    private void addTeleportPoint(PointI target) {
        trajectory.addPoint(new TeleportPoint(x, y, time, true));
        trajectory.addPoint(new InvulnerablePoint(x, y, time + animationStartTime));
        x = target.x;
        y = target.y;
        time = time + animationStartTime + teleportDuration;
        trajectory.addPoint(new InvulnerablePoint(x, y, time));
        time += animationFinishTime;
    }
}
