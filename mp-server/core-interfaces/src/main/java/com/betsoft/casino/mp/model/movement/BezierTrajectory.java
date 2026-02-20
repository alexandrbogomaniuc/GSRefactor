package com.betsoft.casino.mp.model.movement;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import java.util.List;

public class BezierTrajectory extends Trajectory{
    private static final byte VERSION = 1;

    public BezierTrajectory() {}

    public BezierTrajectory(double speed, List<Point> points) {
        super(speed, points);
        this.bezierTrajectory = true;
    }

    public BezierTrajectory(double speed, List<Point> points, int circularAngle) {
        super(speed, points);
        this.bezierTrajectory = true;
        this.circularAngle = circularAngle;
        this.isCircularTrajectory = true;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.write(VERSION);
        super.write(kryo, output);
        output.writeBoolean(bezierTrajectory);
        output.writeInt(getCircularAngle(), true);
        output.writeBoolean(isCircularTrajectory);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte version = input.readByte();
        super.read(kryo, input);
        bezierTrajectory = input.readBoolean();
        if (version >= 1) {
            circularAngle = input.readInt(true);
            isCircularTrajectory = input.readBoolean();
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BezierTrajectory{");
        sb.append("bezierTrajectory=").append(bezierTrajectory);
        sb.append("points=").append(getPoints());
        sb.append("isCircularTrajectory=").append(isCircularTrajectory);
        sb.append("circularAngle=").append(getCircularAngle());
        sb.append('}');
        return sb.toString();
    }
}
