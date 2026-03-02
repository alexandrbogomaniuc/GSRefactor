package com.betsoft.casino.mp.common;

import com.betsoft.casino.mp.model.movement.Point;
import com.betsoft.casino.mp.model.movement.PortalPoint;
import com.betsoft.casino.mp.model.movement.Trajectory;
import com.abs.casino.common.util.RNG;
import org.kynosarges.tektosyne.geometry.PointD;

import java.util.ArrayList;
import java.util.List;

public class PortalSwarmTrajectoryGenerator extends RatSwarmTrajectoryGenerator {

    private static final int PRE_PORTAL_DISTANCE = 9;
    private static final int TOTAL_PORTAL_DISTANCE = 12;
    private static final double CELL_SIZE = 15;

    private final int portalId;

    public PortalSwarmTrajectoryGenerator(GameMapShape map, Coords coords, int moveAngle, int portalId) {
        super(map, coords, moveAngle);
        this.portalId = portalId;
    }

    @Override
    public Trajectory generate(PointD start, long totalDistance, double baseSpeed, double deltaSpeed, long startTime, int deltaTime) {
        double speed = baseSpeed + RNG.rand() * deltaSpeed;
        List<Point> points = new ArrayList<>();
        points.add(new PortalPoint(coords.toScreenX(start.x, start.y), coords.toScreenY(start.x, start.y), 0, portalId));
        if (moveAngle < 270) {
            points.add(createPoint(start.x + PRE_PORTAL_DISTANCE, start.y, (long) (PRE_PORTAL_DISTANCE * 50 * CELL_SIZE / speed)));
            points.add(createPoint(start.x + TOTAL_PORTAL_DISTANCE, start.y, (long) (TOTAL_PORTAL_DISTANCE * 50 * CELL_SIZE / speed)));
            return generate(points, new PointD(start.x + TOTAL_PORTAL_DISTANCE, start.y), totalDistance, speed, startTime, deltaTime);
        } else {
            points.add(createPoint(start.x, start.y + PRE_PORTAL_DISTANCE, (long) (PRE_PORTAL_DISTANCE * 50 * CELL_SIZE / speed)));
            points.add(createPoint(start.x, start.y + TOTAL_PORTAL_DISTANCE, (long) (TOTAL_PORTAL_DISTANCE * 50 * CELL_SIZE / speed)));
            return generate(points, new PointD(start.x, start.y + TOTAL_PORTAL_DISTANCE), totalDistance, speed, startTime, deltaTime);
        }
    }

    private Point createPoint(double x, double y, long time) {
        return new Point(coords.toScreenX(x, y), coords.toScreenY(x, y), time);
    }
}
