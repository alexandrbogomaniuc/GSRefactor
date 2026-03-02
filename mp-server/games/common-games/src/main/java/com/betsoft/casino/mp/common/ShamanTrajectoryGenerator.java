package com.betsoft.casino.mp.common;

import com.betsoft.casino.mp.model.movement.InvulnerablePoint;
import com.betsoft.casino.mp.model.movement.Point;
import com.betsoft.casino.mp.model.movement.TeleportPoint;
import com.betsoft.casino.mp.model.movement.Trajectory;
import com.abs.casino.common.util.RNG;
import org.kynosarges.tektosyne.geometry.PointD;
import org.kynosarges.tektosyne.geometry.PointI;

import java.util.ArrayList;
import java.util.List;

/**
 * This class uses specific timings for Shaman animations in Amazon
 * It should not be reused for other enemies
 */
public class ShamanTrajectoryGenerator extends TrajectoryGenerator {

    private static final int INVISIBILITY_START_TIME = 945;
    private static final int TELEPORT_START_TIME = 1268;
    private static final int TELEPORT_FINISH_TIME = 1269;
    private static final int INVISIBILITY_FINISH_TIME = 1393;
    private static final int ANIMATION_DURATION = 2504;

    private int invisibilityStartTime;
    private int teleportStartTime;
    private int teleportFinishTime;
    private int invisibilityFinishTime;
    private int animationDuration;

    public ShamanTrajectoryGenerator(GameMapShape map, PointI source, double speed) {
        super(map, source, speed);
        this.invisibilityStartTime = INVISIBILITY_START_TIME;
        this.teleportStartTime = TELEPORT_START_TIME;
        this.teleportFinishTime = TELEPORT_FINISH_TIME;
        this.invisibilityFinishTime = INVISIBILITY_FINISH_TIME;
        this.animationDuration = ANIMATION_DURATION;
    }

    @Override
    protected boolean firstStep() {
        addTeleportPoint(getRandomTeleportPoint());
        return true;
    }

    @Override
    protected void finalSteps() {
        trajectory.addPoint(new TeleportPoint(x, y, time, false));
        trajectory.addPoint(new InvulnerablePoint(x, y, time + invisibilityStartTime));
        trajectory.addPoint(new InvulnerablePoint(x, y, time + teleportStartTime));
    }

    @Override
    protected void randomStep() {
        addTeleportPoint(getRandomTeleportPoint());
    }

    protected PointD getRandomTeleportPoint() {
        while (true) {
            int x = RNG.nextInt(map.getWidth());
            int y = RNG.nextInt(map.getHeight());
            if (map.isValid(x, y) && map.isNotMarked(x, y)) {
                return new PointD(x, y);
            }
        }
    }

    protected void addTeleportPoint(PointD target) {
        trajectory.addPoint(new TeleportPoint(x, y, time, false));
        trajectory.addPoint(new InvulnerablePoint(x, y, time + invisibilityStartTime));
        trajectory.addPoint(new InvulnerablePoint(x, y, time + teleportStartTime));
        x = (int) target.x;
        y = (int) target.y;
        trajectory.addPoint(new InvulnerablePoint(x, y, time + teleportFinishTime));
        trajectory.addPoint(new Point(x, y, time + invisibilityFinishTime));
        time += animationDuration;
        trajectory.addPoint(new Point(x, y, time));
    }

    public ShamanTrajectoryGenerator setInvisibilityStartTime(int invisibilityStartTime) {
        this.invisibilityStartTime = invisibilityStartTime;
        return this;
    }

    public ShamanTrajectoryGenerator setTeleportStartTime(int teleportStartTime) {
        this.teleportStartTime = teleportStartTime;
        return this;
    }

    public ShamanTrajectoryGenerator setTeleportFinishTime(int teleportFinishTime) {
        this.teleportFinishTime = teleportFinishTime;
        return this;
    }

    public ShamanTrajectoryGenerator setInvisibilityFinishTime(int invisibilityFinishTime) {
        this.invisibilityFinishTime = invisibilityFinishTime;
        return this;
    }

    public ShamanTrajectoryGenerator setAnimationDuration(int animationDuration) {
        this.animationDuration = animationDuration;
        return this;
    }

    public Trajectory generateLeaveTrajectory(long startTime, Trajectory baseTrajectory) {
        List<Point> points = baseTrajectory.getPoints();
        List<Point> leavePoints = new ArrayList<>();
        int i = getIndexOfActiveTeleportPoint(points, startTime);
        Point point = points.get(i);
        int j = 0;
        while (j < 6 && i + 1 < points.size()) {
            point = points.get(i);
            if (points.get(i + 1).getTime() >= startTime) {
                leavePoints.add(point);
            }
            if (!point.isFreezePoint()) {
                j++;
            }
            i++;
        }
        leavePoints.add(new TeleportPoint(point.getX(), point.getY(), point.getTime(), false));
        leavePoints.add(new InvulnerablePoint(point.getX(), point.getY(), point.getTime() + invisibilityStartTime));
        leavePoints.add(new InvulnerablePoint(point.getX(), point.getY(), point.getTime() + teleportStartTime));
        return new Trajectory(baseTrajectory.getSpeed(), leavePoints);
    }

    private int getIndexOfActiveTeleportPoint(List<Point> points, long time) {
        int i = 0;
        int teleportIndex = 0;
        while (i < points.size() && points.get(i).getTime() < time) {
            if (points.get(i) instanceof TeleportPoint) {
                teleportIndex = i;
            }
            i++;
        }
        return teleportIndex;
    }
}
