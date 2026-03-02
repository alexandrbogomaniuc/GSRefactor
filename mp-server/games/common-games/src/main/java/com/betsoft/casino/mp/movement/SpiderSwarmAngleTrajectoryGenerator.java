package com.betsoft.casino.mp.movement;

import com.betsoft.casino.mp.model.movement.Point;
import com.betsoft.casino.mp.model.movement.Trajectory;
import com.abs.casino.common.util.RNG;

import java.util.ArrayList;
import java.util.List;

public class SpiderSwarmAngleTrajectoryGenerator {
    private long time;

    public Trajectory generate(long time, List<Point> basePoints, double speed) {
        this.time = time;
        double x0 = basePoints.get(0).getX() + signedRand(3);
        double y0 = basePoints.get(0).getY() + signedRand(3);
        double x1 = basePoints.get(1).getX() + signedRand(5);
        double y1 = basePoints.get(1).getY() + signedRand(5);
        double x2 = basePoints.get(2).getX() + signedRand(3);
        double y2 = basePoints.get(2).getY() + signedRand(3);

        Trajectory trajectory = new Trajectory(speed)
                .addPoints(createSegments(x0, y0, x1, y1, speed, speed / 4));
        this.time += 3000L;
        return trajectory.addPoints(createSegments(x1, y1, x2, y2, speed / 4, speed));
    }

    private List<Point> createSegments(double x1, double y1, double x2, double y2, double startSpeed, double finishSpeed) {
        int segments = RNG.nextInt(7, 11);
        double dx = x2 - x1;
        double dy = y2 - y1;
        double distance = Math.sqrt(dx * dx + dy * dy);
        double segmentSize = distance / segments;
        double maxShift = segmentSize * 0.03;

        List<Point> points = new ArrayList<>();
        points.add(new Point(x1, y1, time));
        for (int i = 1; i <= segments; i++) {
            double randomShift = signedRand(maxShift);
            double speed = Math.sqrt(startSpeed * startSpeed * (segments - i) + finishSpeed * finishSpeed * i);
            time += segmentSize * 1000 / speed;
            points.add(new Point(
                    x1 + dx / segments * i + dy / segmentSize * randomShift,
                    y1 + dy / segments * i + dx / segmentSize * randomShift,
                    time));
        }
        return points;
    }

    private double signedRand(double maxAbs) {
        return RNG.rand() * maxAbs * 2 - maxAbs;
    }
}
