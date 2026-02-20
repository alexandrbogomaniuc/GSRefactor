package com.betsoft.casino.mp.model.movement;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.List;

public class HybridTrajectory extends Trajectory {
    private static final byte VERSION = 0;
    private boolean isCircularLargeRadius;
    private boolean isCircularStatic;

    public HybridTrajectory() {}

    public HybridTrajectory(double speed, List<Point> points) {
        super(speed, points);
        bezierTrajectory = true;
        isHybridTrajectory = true;
    }

    public HybridTrajectory(double speed, List<Point> points, int circularAngle) {
        super(speed, points);
        this.bezierTrajectory = true;
        this.isHybridTrajectory = true;
        this.circularAngle = circularAngle;
        this.isCircularTrajectory = true;
    }

    public HybridTrajectory(double speed, List<Point> points, int circularAngle, boolean isCircularTrajectory, boolean isCircularLargeRadius, boolean isCircularStatic) {
        super(speed, points);
        this.bezierTrajectory = true;
        this.isHybridTrajectory = true;
        this.circularAngle = circularAngle;
        this.isCircularTrajectory = isCircularTrajectory;
        this.isCircularLargeRadius = isCircularLargeRadius;
        this.isCircularStatic = isCircularStatic;
    }

    public boolean isCircularLargeRadius() {
        return isCircularLargeRadius;
    }

    public void setCircularLargeRadius(boolean circularLargeRadius) {
        isCircularLargeRadius = circularLargeRadius;
    }

    public boolean isCircularStatic() {
        return isCircularStatic;
    }

    public void setCircularStatic(boolean circularStatic) {
        isCircularStatic = circularStatic;
    }

    public void write(Kryo kryo, Output output) {
        output.write(VERSION);
        super.write(kryo, output);
        output.writeBoolean(bezierTrajectory);
        output.writeInt(getCircularAngle(), true);
        output.writeBoolean(isCircularTrajectory);
        output.writeBoolean(isCircularLargeRadius);
        output.writeBoolean(isCircularStatic);
    }

    public void read(Kryo kryo, Input input) {
        byte version = input.readByte();
        super.read(kryo, input);
        bezierTrajectory = input.readBoolean();
        circularAngle = input.readInt(true);
        isCircularTrajectory = input.readBoolean();
        isCircularLargeRadius = input.readBoolean();
        isCircularStatic = input.readBoolean();
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder("HybridTrajectory{");
        sb.append("bezierTrajectory=").append(bezierTrajectory);
        sb.append("points=").append(getPoints());
        sb.append("isCircularTrajectory=").append(isCircularTrajectory);
        sb.append("isCircularLargeRadius=").append(isCircularLargeRadius);
        sb.append("isCircularStatic=").append(isCircularStatic);
        sb.append("circularAngle=").append(getCircularAngle());
        sb.append('}');
        return sb.toString();
    }
}
