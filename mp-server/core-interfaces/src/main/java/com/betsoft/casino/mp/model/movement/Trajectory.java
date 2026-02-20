package com.betsoft.casino.mp.model.movement;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kynosarges.tektosyne.geometry.PointD;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Trajectory implements KryoSerializable, Serializable {
    private static final Logger LOG = LogManager.getLogger(Trajectory.class);

    private static final byte VERSION = 0;
    private static final double EPSILON = 0.01;

    private Long id;
    private double speed;
    private List<Point> points;
    private int maxSize;

    protected Boolean bezierTrajectory = null;
    protected Boolean paralleltrajectory = null;
    protected Boolean isCircularTrajectory = null;
    protected Boolean isHybridTrajectory = null;

    protected int circularAngle = -1;

    public Trajectory() {}

    public Trajectory(double speed) {
        this.speed = speed;
        this.points = new ArrayList<>(8);
    }

    public Trajectory(Long id, double speed) {
        this.id = id;
        this.speed = speed;
        this.points = new ArrayList<>(8);
    }

    public Trajectory(double speed, List<Point> points) {
        this.speed = speed;
        if (points instanceof ArrayList) {
            this.points = points;
        } else {
            this.points = points == null ? new ArrayList<>() : new ArrayList<>(points);
        }
    }

    public Trajectory(double speed, List<Point> points, int maxSize) {
        this.speed = speed;
        if (points instanceof ArrayList) {
            this.points = points;
        } else {
            this.points = points == null ? new ArrayList<>() : new ArrayList<>(points);
        }
        this.maxSize = maxSize;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Trajectory addPoint(Point point) {
        assertTrajectorySize();
        points.add(point);
        return this;
    }

    public Trajectory addPoints(List<Point> points) {
        this.points.addAll(points);
        return this;
    }

    public void setPoint(int index, Point point) {
        this.points.set(index, point);
    }

    private void assertTrajectorySize() {
        int maxSize = getMaxSize();
        if (points.size() > maxSize) {
            List<PointD> pointsWithoutDuplicates = points.stream()
                    .map(point -> new PointD(point.getX(), point.getY()))
                    .distinct()
                    .collect(Collectors.toList());
            if (pointsWithoutDuplicates.size() > 1) {
                LOG.error("Bad trajectory, maxSize: {}, points.size: {}, points: {} ", maxSize, points.size(), points);
            } else if (pointsWithoutDuplicates.size() == 1) {
                LOG.error("Bad trajectory, maxSize: {}, wrong point: {} ", maxSize, pointsWithoutDuplicates.get(0));
            }
            throw new RuntimeException("Too many points..." + points.size());
        }
    }

    public Trajectory addPoint(double x, double y, long time) {
        assertTrajectorySize();
        points.add(new Point(x, y, time));
        return this;
    }

    public Trajectory addPointWithIndex(double x, double y, long time, int index) {
        assertTrajectorySize();
        points.add(index, new Point(x, y, time));
        return this;
    }

    public void removeLastPoint() {
        if (!points.isEmpty()) {
            points.remove(points.size() - 1);
        }
    }

    public Point getLastPoint() {
        return points.get(points.size() - 1);
    }

    public long getLeaveTime() {
        return points.get(points.size() - 1).getTime();
    }

    public int getIndexOfFirstPassedPoint(long time) {
        int i = 1;
        while (i < points.size() && points.get(i).getTime() < time) {
            i++;
        }
        return i - 1;
    }

    public boolean intersects(Trajectory trajectory, long deltaTime) {
        DualTrajectoryIterator iterator = new DualTrajectoryIterator(this, trajectory);
        while (iterator.hasNext()) {
            SegmentPair segment = iterator.next();
            if (intersects(segment.getA1(), segment.getA2(), segment.getB1(), segment.getB2(), deltaTime)) {
                return true;
            }
        }
        return false;
    }

    private boolean intersects(Point a1, Point a2, Point b1, Point b2, long deltaTime) {
        double dx1 = a2.getX() - a1.getX();
        double dy1 = a2.getY() - a1.getY();
        double dx2 = b2.getX() - b1.getX();
        double dy2 = b2.getY() - b1.getY();
        double dx = a1.getX() - b1.getX();
        double dy = a1.getY() - b1.getY();

        double d = dy2 * dx1 - dx2 * dy1;
        double na = dx2 * dy - dy2 * dx;
        double nb = dx1 * dy - dy1 * dx;

        if (Math.abs(d) > EPSILON) {
            double ua = na / d;
            double ub = nb / d;
            if (ua >= -EPSILON && ua <= 1.0 + EPSILON && ub >= -EPSILON && ub <= 1.0 + EPSILON) {
                long t1 = a1.getTime() + (long) ((a2.getTime() - a1.getTime()) * ua);
                long t2 = b1.getTime() + (long) ((b2.getTime() - b1.getTime()) * ub);
                return Math.abs(t2 - t1) < deltaTime;
            }
        } else {
            if (Math.abs(na) < EPSILON && Math.abs(nb) < EPSILON) {
                return Math.abs(dx1) > EPSILON
                        ? MathUtils.isOverlaps(a1.getX(), a2.getX(), b1.getX(), b2.getX())
                        : MathUtils.isOverlaps(a1.getY(), a2.getY(), b1.getY(), b2.getY());
            }
        }
        return false;
    }

    public double getSpeed() {
        return speed;
    }

    public List<Point> getPoints() {
        return Collections.unmodifiableList(points);
    }

    public Trajectory copyWithPassedPoints(long time) {
        Trajectory trajectory = new Trajectory(speed);
        int passedIndex = getIndexOfFirstPassedPoint(time);
        for (int i = 0; i <= passedIndex; i++) {
            trajectory.addPoint(points.get(i));
        }
        return trajectory;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeDouble(speed);
        kryo.writeObject(output, points);
        kryo.writeObjectOrNull(output, id, Long.class);
        output.writeInt(getMaxSize(), true);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void read(Kryo kryo, Input input) {
        byte version = input.readByte();
        speed = input.readDouble();
        points = kryo.readObject(input, ArrayList.class);
        id = kryo.readObjectOrNull(input, Long.class);
        maxSize = input.readInt(true);
    }

    public int getMaxSize() {
        if (maxSize == 0) {
            maxSize = 300;
        }
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public boolean isEmpty() {
        return points.isEmpty();
    }

    public Boolean isBezierTrajectory() {
        return bezierTrajectory;
    }

    public void setBezierTrajectory(Boolean bezierTrajectory) {
        this.bezierTrajectory = bezierTrajectory;
    }

    public Boolean isParalleltrajectory() {
        return paralleltrajectory;
    }

    public void setParalleltrajectory(Boolean paralleltrajectory) {
        this.paralleltrajectory = paralleltrajectory;
    }

    public Boolean isCircularTrajectory() {
        return isCircularTrajectory;
    }

    public void setCircularTrajectory(Boolean circularTrajectory) {
        isCircularTrajectory = circularTrajectory;
    }

    public Boolean isHybridTrajectory() {
        return isHybridTrajectory;
    }

    public void setHybridTrajectory(Boolean isHybridTrajectory) {
        isHybridTrajectory = isHybridTrajectory;
    }

    public int getCircularAngle() {
        return circularAngle;
    }

    public void setCircularAngle(int circularAngle) {
        this.circularAngle = circularAngle;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Trajectory [");
        sb.append("id=").append(id);
        sb.append(" bezierTrajectory=").append(bezierTrajectory);
        sb.append(" paralleltrajectory=").append(paralleltrajectory);
        sb.append(" isCircularTrajectory=").append(isCircularTrajectory);
        sb.append(" isHybridTrajectory=").append(isHybridTrajectory);
        sb.append(" circularAngle=").append(circularAngle);
        sb.append(" points=").append(points);
        sb.append(" points.size()=").append(points.size());
        sb.append(" speed=").append(speed);
        sb.append(" maxSize=").append(getMaxSize());
        sb.append(']');
        return sb.toString();
    }
}
