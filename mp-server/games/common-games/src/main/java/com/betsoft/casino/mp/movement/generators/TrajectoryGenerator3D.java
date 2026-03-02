package com.betsoft.casino.mp.movement.generators;

import com.betsoft.casino.mp.common.TrajectoryGenerator;
import com.betsoft.casino.mp.model.IGameMapShape;
import com.betsoft.casino.mp.model.movement.Point;
import com.betsoft.casino.mp.model.movement.Trajectory;
import com.betsoft.casino.mp.movement.common.Step;
import com.abs.casino.common.util.RNG;
import org.kynosarges.tektosyne.geometry.PointI;

import java.util.ArrayList;
import java.util.List;

import static com.betsoft.casino.mp.model.movement.MathUtils.*;

public class TrajectoryGenerator3D extends TrajectoryGenerator {

    private int minStep = 1;
    private int maxStep = 8;
    private int maxDeltaAngle = 45;
    private int angleStep = 15;
    protected long animationDelay = 0;
    protected int currentAngle;
    protected double currentX;
    protected double currentY;

    public TrajectoryGenerator3D(IGameMapShape map, PointI source, double speed) {
        super(map, source, speed);
        this.currentAngle = 0;
        this.currentX = source.x;
        this.currentY = source.y;
    }

    public TrajectoryGenerator3D setMinStep(int minStep) {
        this.minStep = minStep;
        return this;
    }

    public TrajectoryGenerator3D setMaxStep(int maxStep) {
        this.maxStep = maxStep;
        return this;
    }

    public TrajectoryGenerator3D setMaxDeltaAngle(int maxDeltaAngle) {
        this.maxDeltaAngle = maxDeltaAngle;
        return this;
    }

    public TrajectoryGenerator3D setAngleStep(int angleStep) {
        this.angleStep = angleStep;
        return this;
    }

    public TrajectoryGenerator3D setAnimationDelay(long animationDelay) {
        this.animationDelay = animationDelay;
        return this;
    }

    @Override
    public Trajectory generate(long startTime, int minSteps, boolean needFinalSteps) {
        Trajectory trajectory = super.generate(startTime, minSteps, needFinalSteps);
        if (trajectory.getPoints().size() < minSteps) {
            return new Trajectory(speed);
        }
        return trajectory;
    }

    public Trajectory generateUpdateTrajectory(Trajectory original, long currentTime, int steps, boolean needFinalSteps) {
        trajectory = new Trajectory(speed);

        List<Point> points = original.getPoints();
        if (points.size() > 2) {
            for (int i = original.getIndexOfFirstPassedPoint(currentTime); i < points.size(); i++) {
                trajectory.addPoint(points.get(i));
            }
            generateAdditionalPoints(points, steps, needFinalSteps);
        }

        return trajectory;
    }

    public Trajectory generateUpdateTrajectoryWithoutOldPoints(Trajectory original, int steps, boolean needFinalSteps) {
        trajectory = new Trajectory(speed);
        List<Point> points = original.getPoints();
        if (points.size() > 2) {
            generateAdditionalPoints(points, steps, needFinalSteps);
        }
        return trajectory;
    }

    protected void generateAdditionalPoints(List<Point> points, int steps, boolean needFinalSteps) {
        Point prevPoint = points.get(points.size() - 2);
        Point lastPoint = points.get(points.size() - 1);
        time = lastPoint.getTime();
        currentX = lastPoint.getX();
        currentY = lastPoint.getY();
        currentAngle = atan2(lastPoint.getX() - prevPoint.getX(), lastPoint.getY() - prevPoint.getY());

        for (int i = 0; i < steps; i++) {
            randomStep();
        }

        if (needFinalSteps) {
            finalSteps();
        }
    }

    @Override
    protected boolean firstStep() {
        Step step = new Step(0, 0);
        for (int angle = 0; angle < 360; angle += 45) {
            double distance = calculateDistanceToWall(angle);
            if (distance > step.getDistance()) {
                step = new Step(distance, angle);
            }
        }
        if (step.getDistance() > 0) {
            addPoint(0f, 0);
            addPoint(step);
            return true;
        }
        return false;
    }

    private double getRandomDistance(double maxDistance) {
        if (maxDistance < minStep) {
            return maxDistance;
        } else if (maxDistance < maxStep) {
            return rand(minStep, maxDistance);
        } else {
            return rand(minStep, maxStep);
        }
    }

    private double rand(double min, double max) {
        return min + (max - min) * RNG.rand();
    }

    private void addPoint(Step step) {
        addPoint(getRandomDistance(step.getDistance()), step.getAngle());
    }

    protected void addPoint(double distance, int angle) {
        currentAngle = angle;
        currentX += distance * cos(angle);
        currentY += distance * sin(angle);
        time += distance / (speed / 1000) + animationDelay;
        trajectory.addPoint(currentX, currentY, time);
    }

    @Override
    protected void randomStep() {
        List<Step> steps = getAvailableSteps(maxDeltaAngle);
        Step step = steps.isEmpty() ? getMinimalStep(getAvailableSteps(180)) : getRandomElement(steps);
        if (step.getDistance() > 0) {
            addPoint(step.getDistance(), step.getAngle());
        }
    }

    private List<Step> getAvailableSteps(int deltaAngle) {
        List<Step> steps = new ArrayList<>();
        for (int angle = -deltaAngle; angle <= deltaAngle; angle += angleStep) {
            double distance = calculateDistanceToWall(currentAngle + angle);
            if (distance > minStep) {
                steps.add(new Step(Math.min(distance, maxStep), currentAngle + angle));
            }
        }
        return steps;
    }

    private Step getMinimalStep(List<Step> steps) {
        int minAngle = 360;
        Step optimalStep = new Step(0, 360);
        for (Step step : steps) {
            int normAngle = Math.abs(step.getAngle() - currentAngle) % 360;
            if (normAngle < minAngle) {
                optimalStep = step;
                minAngle = normAngle;
            }
        }
        return optimalStep;
    }

    private <T> T getRandomElement(List<T> elements) {
        return elements.get(RNG.nextInt(elements.size()));
    }

    @Override
    protected void finalSteps() {
        Step minStep = null;
        for (int i = 0; i < 15; i++) {
            for (int angle = 0; angle < 360; angle += angleStep) {
                Step step = getStepToExit(currentX, currentY, angle);
                if (minStep == null || step != null && step.getDistance() < minStep.getDistance()) {
                    minStep = step;
                }
            }
            if (minStep != null) {
                if (minStep.getDistance() > maxStep) {
                    addPoint(minStep.getDistance() / 2, minStep.getAngle());
                    addPoint(minStep.getDistance() / 2, minStep.getAngle());
                } else if (minStep.getDistance() > 0) {
                    addPoint(minStep.getDistance(), minStep.getAngle());
                }
                return;
            } else {
                randomStep();
            }
        }
    }

    protected Step getStepToExit(double sourceX, double sourceY, int angle) {
        double distance = 0;
        double dx = cos(angle) / 2;
        double dy = sin(angle) / 2;
        double x = sourceX + dx;
        double y = sourceY + dy;
        while (map.isPassable((int) Math.round(x), (int) Math.round(y)) && !map.isSpawnPoint((int) Math.round(x), (int) Math.round(y))) {
            distance += 0.5;
            x += dx;
            y += dy;
        }
        return map.isSpawnPoint((int) Math.round(x), (int) Math.round(y)) ? new Step(distance, angle) : null;
    }

    protected double calculateDistanceToWall(int angle) {
        double distance = 0;
        double dx = cos(angle) / 2;
        double dy = sin(angle) / 2;
        double x = currentX + dx;
        double y = currentY + dy;
        while (map.isPassable((int) Math.round(x), (int) Math.round(y))) {
            distance += 0.5;
            x += dx;
            y += dy;
        }
        return distance;
    }
}
