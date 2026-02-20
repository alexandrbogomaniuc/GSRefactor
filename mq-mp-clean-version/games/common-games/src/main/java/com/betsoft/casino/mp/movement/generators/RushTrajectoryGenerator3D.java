package com.betsoft.casino.mp.movement.generators;

import com.betsoft.casino.mp.model.IGameMapShape;
import com.betsoft.casino.mp.model.movement.Point;
import com.betsoft.casino.mp.model.movement.Trajectory;
import com.dgphoenix.casino.common.util.RNG;
import org.kynosarges.tektosyne.geometry.PointD;
import org.kynosarges.tektosyne.geometry.PointI;

import java.util.List;

import static com.betsoft.casino.mp.model.movement.MathUtils.*;

public class RushTrajectoryGenerator3D extends TrajectoryGenerator3D {

    private static final double OUTER_RADIUS = 13;
    private static final double MIN_RUSH_DISTANCE = 7;

    protected final double rushSpeed;

    public RushTrajectoryGenerator3D(IGameMapShape map, PointI source, double speed, double rushSpeed) {
        super(map, source, speed);
        this.rushSpeed = rushSpeed;
    }

    @Override
    public Trajectory generate(long startTime, int minSteps, boolean needFinalSteps) {
        trajectory = new Trajectory(speed);
        time = startTime;
        firstStep();
        int phases = minSteps / 3;
        for (int i = 0; i < phases; i++) {
            tryRush();
            randomMovement(3);
        }
        if (needFinalSteps) {
            finalSteps();
        }
        return trajectory;
    }

    private void tryRush() {
        PointD center = map.getCenter();
        double dx = center.x - currentX;
        double dy = center.y - currentY;
        int angle = atan2(dx, dy);
        double distance = Math.sqrt(dx * dx + dy * dy) - OUTER_RADIUS;
        if (distance > 0 && calculateDistanceToWall(angle) >= distance) {
            double rx = currentX + distance * cos(angle);
            double ry = currentY + distance * sin(angle);
            int dir = RNG.nextBoolean() ? 1 : -1;
            int rushAngle = RNG.nextInt(25, 35);
            // Chord length L = 2r * sin (alpha / 2)
            double rushDistance = Math.min(
                    OUTER_RADIUS * 2 * sin(180 - 2 * rushAngle),
                    calculateRushDistanceToWall(rx, ry, angle + dir * rushAngle));
            if (rushDistance >= MIN_RUSH_DISTANCE) {
                addPoint(distance, angle);
                addRushPoint(rushDistance, angle + dir * rushAngle);
            }
        }
    }

    private void randomMovement(int steps) {
        for (int j = 0; j < steps; j++) {
            randomStep();
        }
    }

    protected void addRushPoint(double distance, int angle) {
        currentAngle = angle;
        currentX += distance * cos(angle);
        currentY += distance * sin(angle);
        time += distance / (rushSpeed / 1000) + animationDelay;
        trajectory.addPoint(currentX, currentY, time);
    }

    @Override
    protected void generateAdditionalPoints(List<Point> points, int steps, boolean needFinalSteps) {
        Point prevPoint = points.get(points.size() - 2);
        Point lastPoint = points.get(points.size() - 1);
        time = lastPoint.getTime();
        currentX = lastPoint.getX();
        currentY = lastPoint.getY();
        currentAngle = atan2(lastPoint.getX() - prevPoint.getX(), lastPoint.getY() - prevPoint.getY());

        int phases = steps / 3;
        for (int i = 0; i < phases; i++) {
            tryRush();
            randomMovement(3);
        }
        if (needFinalSteps) {
            finalSteps();
        }
    }

    protected double calculateRushDistanceToWall(double fromX, double fromY, int angle) {
        double distance = 0;
        double dx = cos(angle) / 2;
        double dy = sin(angle) / 2;
        double x = fromX + dx;
        double y = fromY + dy;
        while (map.isPassable((int) Math.round(x + dx), (int) Math.round(y + dx)) || map.isBorder((int) Math.round(x + dx), (int) Math.round(y + dx))) {
            distance += 0.5;
            x += dx;
            y += dy;
        }
        return map.isPassable((int) Math.round(x), (int) Math.round(y)) ? distance : 0;
    }
}
