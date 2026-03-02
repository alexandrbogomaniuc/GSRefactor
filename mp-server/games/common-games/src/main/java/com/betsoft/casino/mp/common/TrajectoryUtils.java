package com.betsoft.casino.mp.common;

import com.betsoft.casino.mp.model.movement.MathUtils;
import com.betsoft.casino.mp.model.movement.Point;
import com.betsoft.casino.mp.model.movement.Trajectory;
import com.abs.casino.common.util.RNG;
import com.abs.casino.common.util.Triple;
import org.kynosarges.tektosyne.geometry.PointD;
import org.kynosarges.tektosyne.geometry.PointI;

import java.util.ArrayList;
import java.util.List;

public class TrajectoryUtils {

    public static Trajectory generateSimilarTrajectory(Trajectory baseTrajectory,
                                                       double offsetX, double offsetY, int deltaX, int deltaY,
                                                       double baseSpeed, double deltaSpeed,
                                                       long spawnTime, long groupStartTime) {
        List<Point> points = new ArrayList<>();
        for (Point point : baseTrajectory.getPoints()) {
            double x = point.getX() + offsetX + RNG.rand() * (deltaX + 1);
            double y = point.getY() + offsetY + RNG.rand() * (deltaY + 1);
            points.add(new Point(x, y, 0));
        }
        double[] distances = calculateDistances(points);
        double speed = baseSpeed + RNG.rand() * deltaSpeed;
        long time = spawnTime + groupStartTime;
        points.get(0).setTime(time);
        for (int j = 1; j < points.size(); j++) {
            time += distances[j - 1] * 1000 / speed;
            points.get(j).setTime(time);
        }
        return new Trajectory(speed, points);
    }

    public static Trajectory generateSimilarTrajectory(Trajectory baseTrajectory,
                                                       double offsetX, double offsetY, int deltaX, int deltaY,
                                                       double baseSpeed, double deltaSpeed,
                                                       long spawnTime, long groupStartTime, boolean needRandomShift) {
        List<Point> points = new ArrayList<>();
        for (Point point : baseTrajectory.getPoints()) {
            double x = point.getX() + offsetX + (needRandomShift ? RNG.rand() * (deltaX + 1) : 0);
            double y = point.getY() + offsetY + (needRandomShift ? RNG.rand() * (deltaY + 1) : 0);
            points.add(new Point(x, y, 0));
        }
        double[] distances = calculateDistances(points);
        double speed = baseSpeed + RNG.rand() * deltaSpeed;
        long time = spawnTime + groupStartTime;
        points.get(0).setTime(time);
        for (int j = 1; j < points.size(); j++) {
            time += distances[j - 1] * 1000 / speed;
            points.get(j).setTime(time);
        }
        return new Trajectory(speed, points);
    }

    public static Trajectory generateSimilarTrajectoryWithCircle(Trajectory baseTrajectory,
                                                       Triple<PointD, Double, Integer> data,
                                                       double baseSpeed, long spawnTime, long groupStartTime) {
        List<Point> points = new ArrayList<>();
        int cnt = 0;
        List<Point> baseTrajectoryPoints = baseTrajectory.getPoints();
        for (Point point : baseTrajectoryPoints) {
            double radius = data.second();
            int angle = (data.third() + 10 * cnt) % 360;
            PointD pointNew = new PointD(radius * MathUtils.cos(angle), - radius * MathUtils.sin(angle));
            double x = point.getX() + pointNew.x;
            double y = point.getY() + pointNew.y;
            points.add(new Point(x, y, 0));
            cnt++;
        }

        long time = spawnTime + groupStartTime;
        points.get(0).setTime(time);
        for (int j = 1; j < points.size(); j++) {
            points.get(j).setTime(baseTrajectoryPoints.get(j).getTime());
        }
        return new Trajectory(baseSpeed, points);
    }

    public static Trajectory generateRetinueTrajectory(List<Point> basePoints,
                                                       int offsetX, int offsetY, int deltaX, int deltaY,
                                                       double baseSpeed, long spawnTime,
                                                       long groupStartTime, int idxRetinue) {
        List<Point> points = new ArrayList<>();

        double angle = 0;

        switch (idxRetinue) {
            case 0:
                angle = Math.PI / 3;
                break;
            case 1:
                angle = 0;
                break;
            case 2:
                angle = 5 * Math.PI / 3;
                break;
            case 3:
                angle = 4 * Math.PI / 3;
                break;
            case 4:
                angle = Math.PI;
                break;
            case 5:
                angle = 2 * Math.PI / 3;
                break;
            default:
                break;
        }
        int shiftX = 0;
        int shiftY = 0;

        for (Point point : basePoints) {
            PointI  pointNew = PointI.fromPolar(5, angle)
                    .add(new PointI((int) point.getX() + shiftX,(int) point.getY() + shiftY));
            double x = pointNew.x;
            double y = pointNew.y;
            points.add(new Point(x, y, 0));
        }

        double[] distances = calculateDistances(points);
        double speed = baseSpeed;
        long time = spawnTime + groupStartTime;
        points.get(0).setTime(time);
        for (int j = 1; j < points.size(); j++) {
            time += distances[j - 1] * 1000 / speed;
            points.get(j).setTime(time);
        }
        return new Trajectory(speed, points);
    }

    public static double calculateTotalDistance(Trajectory trajectory) {
        return calculateTotalDistance(calculateDistances(trajectory.getPoints()));
    }

    private static double[] calculateDistances(List<Point> points) {
        double[] distances = new double[points.size() - 1];
        for (int j = 0; j < points.size() - 1; j++) {
            distances[j] = distance(points.get(j), points.get(j + 1));
        }
        return distances;
    }

    private static double calculateTotalDistance(double[] distances) {
        long totalDistance = 0;
        for (double distance : distances) {
            totalDistance += distance;
        }
        return totalDistance;
    }

    public static double distance(PointI a, PointI b) {
        double dx = Math.abs(b.x - a.x);
        double dy = Math.abs(b.y - a.y);
        return Math.sqrt(dx * dx + dy * dy);
    }

    public static double distance(Point a, Point b) {
        double dx = Math.abs(b.getX() - a.getX());
        double dy = Math.abs(b.getY() - a.getY());
        return Math.sqrt(dx * dx + dy * dy);
    }

    public static PointD getNormal(Point a, Point b) {
        double dx = b.getX() - a.getX();
        double dy = b.getY() - a.getY();
        double dist = Math.sqrt(dx * dx + dy * dy);
        return new PointD(dx / dist, dy / dist);
    }
}
