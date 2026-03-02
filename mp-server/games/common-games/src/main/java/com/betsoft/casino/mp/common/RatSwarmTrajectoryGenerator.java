package com.betsoft.casino.mp.common;

import com.betsoft.casino.mp.model.movement.Point;
import com.betsoft.casino.mp.model.movement.Trajectory;
import com.abs.casino.common.util.RNG;
import org.kynosarges.tektosyne.geometry.PointD;
import org.kynosarges.tektosyne.geometry.PointI;

import java.util.ArrayList;
import java.util.List;

import static com.betsoft.casino.mp.model.movement.MathUtils.*;

public class RatSwarmTrajectoryGenerator {

    private static final int MAX_NON_ROTATE_ANGLE = 22;
    private static final int MAX_ROTATE_ANGLE = 67;
    private static final int MIN_DELTA_ANGLE = 5;
    private static final int MAX_DELTA_ANGLE = 30;
    private static final int DEFAULT_MAX_SIDE_DISTANCE = 50;
    private static final double MIN_STEP_DISTANCE = 0.5;

    private static final int DEFAULT_MIN_FORWARD_MOVE_DISTANCE = 75;
    private static final int DEFAULT_MAX_FORWARD_MOVE_DISTANCE = 150;
    private static final int DEFAULT_MIN_SIDE_MOVE_DISTANCE = 35;
    private static final int DEFAULT_MAX_SIDE_MOVE_DISTANCE = 50;

    protected final GameMapShape map;
    protected final Coords coords;
    protected final int moveAngle;
    protected final int shiftAngle;
    protected final int minSideMoveDistance;
    protected final int maxSideMoveDistance;
    protected final int maxSideDistance;
    protected final int minForwardMoveDistance;
    protected final int maxForwardMoveDistance;

    public RatSwarmTrajectoryGenerator(GameMapShape map, Coords coords, int moveAngle) {
        this.map = map;
        this.coords = coords;
        this.moveAngle = moveAngle;
        this.shiftAngle = moveAngle - getBaseAngle(moveAngle);
        this.minForwardMoveDistance = DEFAULT_MIN_FORWARD_MOVE_DISTANCE;
        this.maxForwardMoveDistance = DEFAULT_MAX_FORWARD_MOVE_DISTANCE;
        this.minSideMoveDistance = DEFAULT_MIN_SIDE_MOVE_DISTANCE;
        this.maxSideMoveDistance = DEFAULT_MAX_SIDE_MOVE_DISTANCE;
        this.maxSideDistance = DEFAULT_MAX_SIDE_DISTANCE;
    }

    public RatSwarmTrajectoryGenerator(GameMapShape map, Coords coords, int moveAngle, int shiftAngle) {
        this.map = map;
        this.coords = coords;
        this.moveAngle = moveAngle;
        this.shiftAngle = shiftAngle;
        this.minForwardMoveDistance = DEFAULT_MIN_FORWARD_MOVE_DISTANCE;
        this.maxForwardMoveDistance = DEFAULT_MAX_FORWARD_MOVE_DISTANCE;
        this.minSideMoveDistance = DEFAULT_MIN_SIDE_MOVE_DISTANCE;
        this.maxSideMoveDistance = DEFAULT_MAX_SIDE_MOVE_DISTANCE;
        this.maxSideDistance = DEFAULT_MAX_SIDE_DISTANCE;
    }

    public RatSwarmTrajectoryGenerator(GameMapShape map, Coords coords, int moveAngle,
                                       int minSideMoveDistance, int maxSideMoveDistance, int maxSideDistance) {
        this.map = map;
        this.coords = coords;
        this.moveAngle = moveAngle;
        this.shiftAngle = moveAngle - getBaseAngle(moveAngle);
        this.minForwardMoveDistance = DEFAULT_MIN_FORWARD_MOVE_DISTANCE;
        this.maxForwardMoveDistance = DEFAULT_MAX_FORWARD_MOVE_DISTANCE;
        this.minSideMoveDistance = minSideMoveDistance;
        this.maxSideMoveDistance = maxSideMoveDistance;
        this.maxSideDistance = maxSideDistance;
    }

    public RatSwarmTrajectoryGenerator(GameMapShape map, Coords coords, int moveAngle,
                                       int minForwardMoveDistance, int maxForwardMoveDistance,
                                       int minSideMoveDistance, int maxSideMoveDistance, int maxSideDistance) {
        this.map = map;
        this.coords = coords;
        this.moveAngle = moveAngle;
        this.shiftAngle = moveAngle - getBaseAngle(moveAngle);
        this.minForwardMoveDistance = minForwardMoveDistance;
        this.maxForwardMoveDistance = maxForwardMoveDistance;
        this.minSideMoveDistance = minSideMoveDistance;
        this.maxSideMoveDistance = maxSideMoveDistance;
        this.maxSideDistance = maxSideDistance;
    }

    /**
     * For rat swarms we need to ensure that most of the times rats move parallel to the main movement axis.
     * To simplify calculation, we calculate everything in screen coordinates and then convert them into game coordinates.
     *
     * We assume that (0, 0) is top left corner of the screen
     * Angle is counted counter-clockwise from the right direction
     */
    public Trajectory generate(PointD start, long totalDistance, double baseSpeed, double deltaSpeed, long startTime, int deltaTime) {
        double speed = baseSpeed + RNG.rand() * deltaSpeed;
        return generate(new ArrayList<>(), start, totalDistance, speed, startTime, deltaTime);
    }

    protected Trajectory generate(List<Point> points, PointD start, long totalDistance, double speed, long startTime, int deltaTime) {
        double startX = coords.toScreenX(start.x, start.y);
        double startY = coords.toScreenY(start.x, start.y);

        double x = startX;
        double y = startY;
        long time = getLastTime(points);
        if (points.isEmpty()) {
            points.add(new Point(x, y, time));
        }
        int angle = RNG.nextInt(-MAX_NON_ROTATE_ANGLE, MAX_NON_ROTATE_ANGLE + 1);
        double sideDistance = 0;
        double travelDistance = 0;

        do {
            double distance = getRandomMoveDistance(angle, sideDistance);
            if (distance > MIN_STEP_DISTANCE) {
                x += distance * cos(moveAngle + angle);
                y -= distance * sin(moveAngle + angle);
                time += distance * 50 / speed;
                sideDistance += getSideDistance(angle, distance);
                travelDistance += distance;
                points.add(new Point(x, y, time));
            }

            angle = getRandomAngle(angle);
        } while (getDistance(startX, startY, x, y) < totalDistance);

        double norm = totalDistance / travelDistance;
        return new Trajectory(speed / norm, convertPoints(points, norm, startTime + RNG.nextInt(deltaTime)));
    }

    private long getLastTime(List<Point> points) {
        if (points.isEmpty()) {
            return 0;
        }
        return points.get(points.size() - 1).getTime();
    }

    private double getDistance(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return Math.sqrt(dx * dx + dy * dy);
    }

    private List<Point> convertPoints(List<Point> points, double norm, long startTime) {
        List<Point> newPoints = new ArrayList<>();
        for (Point point : points) {
            double screenX = point.getX();
            double screenY = point.getY();
            double x = coords.toX(screenX, screenY);
            double y = coords.toY(screenX, screenY);

            if (map.isValidWithAssumption((int) x, (int) y, 5)) {
                newPoints.add(point.create(x, y, startTime + (long) (point.getTime() * norm)));
            }
        }
        return newPoints;
    }

    private double getRandomMoveDistance(double angle, double sideDistance) {
        double distance = Math.abs(angle + shiftAngle) <= MAX_NON_ROTATE_ANGLE
                ? RNG.nextInt(minForwardMoveDistance, maxForwardMoveDistance)
                : RNG.nextInt(minSideMoveDistance, maxSideMoveDistance);
        double sinA = sin(Math.abs(angle));
        if (sideDistance * angle > 0 && distance * sinA > maxSideDistance - Math.abs(sideDistance)) {
            distance = (maxSideDistance - Math.abs(sideDistance)) / sinA;
        }
        return distance;
    }

    private double getSideDistance(double angle, double distance) {
        return sin(angle) * distance;
    }

    private int getRandomAngle(int angle) {
        if (Math.abs(angle) > MIN_DELTA_ANGLE) {
            return angle > 0
                    ? Math.max(angle - RNG.nextInt(MIN_DELTA_ANGLE, MAX_DELTA_ANGLE), -MAX_ROTATE_ANGLE)
                    : Math.min(angle + RNG.nextInt(MIN_DELTA_ANGLE, MAX_DELTA_ANGLE), MAX_ROTATE_ANGLE);
        } else {
            return RNG.nextInt(-MAX_NON_ROTATE_ANGLE, MAX_NON_ROTATE_ANGLE + 1);
        }
    }

    private double screenAngle(PointI a, PointI b) {
        double x1 = coords.toScreenX(a.x, a.y);
        double y1 = coords.toScreenY(a.x, a.y);
        double x2 = coords.toScreenX(b.x, b.y);
        double y2 = coords.toScreenY(b.x, b.y);

        return screenAngle(x1, y1, x2, y2);
    }

    private double screenAngle(double x1, double y1, double x2, double y2) {
        return toDegrees(Math.atan2(y2 - y1, x2 - x1));
    }

    static int getBaseAngle(int angle) {
        int crop = angle % 45;
        return crop <= MAX_NON_ROTATE_ANGLE ? angle - crop : angle - crop + 45;
    }
}
