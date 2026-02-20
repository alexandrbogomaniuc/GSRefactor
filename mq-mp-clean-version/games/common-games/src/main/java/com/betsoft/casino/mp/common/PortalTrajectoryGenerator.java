package com.betsoft.casino.mp.common;

import com.betsoft.casino.mp.model.movement.Point;
import com.betsoft.casino.mp.model.movement.PortalPoint;
import com.dgphoenix.casino.common.util.RNG;
import org.kynosarges.tektosyne.geometry.PointI;

public class PortalTrajectoryGenerator extends TrajectoryGenerator {
    private static final int PRE_PORTAL_DISTANCE = 12;
    private static final int POST_PORTAL_DISTANCE = 3;

    private final int portalId;
    private final int direction;

    public PortalTrajectoryGenerator(GameMapShape map, Coords coords, double speed, int portalId) {
        super(map, getRandomMiddlePoint(map, coords), speed);
        this.portalId = portalId;
        this.direction = source.x < source.y ? 0 : 1;
    }

    public PortalTrajectoryGenerator(GameMapShape map, Portal portal, double speed, int portalId) {
        super(map, new PointI(portal.getX(), portal.getY()), speed);
        this.portalId = portalId;
        this.direction = portal.getDirection();
    }

    @Override
    protected boolean firstStep() {
        dir = direction;
        addPortalPoint(PRE_PORTAL_DISTANCE);
        addPoint(POST_PORTAL_DISTANCE);
        return true;
    }

    private void addPortalPoint(int distance) {
        trajectory.addPoint(new PortalPoint(x, y, time, portalId));
        x += distance * DIRS[dir].x;
        y += distance * DIRS[dir].y;
        time += distance / (speed / 1000);
    }

    static PointI getRandomMiddlePoint(GameMapShape map, Coords coords) {
        while (true) {
            int x = RNG.nextInt(map.getWidth());
            int y = RNG.nextInt(map.getHeight());
            int dir = x < y ? 0 : 1;
            int dx = (PRE_PORTAL_DISTANCE + POST_PORTAL_DISTANCE) * DIRS[dir].x;
            int dy = (PRE_PORTAL_DISTANCE + POST_PORTAL_DISTANCE) * DIRS[dir].y;
            if (isValidPortalPoint(map, coords, x, y) && isValidPortalPoint(map, coords, x + dx, y + dy)) {
                return new PointI(x, y);
            }
        }
    }

    static boolean isValidPortalPoint(GameMapShape map, Coords coords, int x, int y) {
        double sx = coords.toScreenX(x, y);
        double sy = coords.toScreenY(x, y);
        return map.isAvailableAndPassable(new Point(x, y, 0)) && sx > 100 && sy > 50 && sx < 860 && sy < 450;
    }
}
