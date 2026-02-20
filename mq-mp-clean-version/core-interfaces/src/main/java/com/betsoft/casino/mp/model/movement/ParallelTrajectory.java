package com.betsoft.casino.mp.model.movement;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.List;

public class ParallelTrajectory extends Trajectory {
    private static final byte VERSION = 0;
    private double parallelOffsetX;
    private double parallelOffsetY;

    public ParallelTrajectory() {}

    public ParallelTrajectory (double speed, List<Point> points) {
        super(speed, points);
        bezierTrajectory = true;
        paralleltrajectory = true;
    }

    public double getParallelOffsetX() {
        return parallelOffsetX;
    }

    public void setParallelOffsetX(double parallelOffsetX) {
        this.parallelOffsetX = parallelOffsetX;
    }

    public double getParallelOffsetY() {
        return parallelOffsetY;
    }

    public void setParallelOffsetY(double parallelOffsetY) {
        this.parallelOffsetY = parallelOffsetY;
    }

    public void write(Kryo kryo, Output output) {
        output.write(VERSION);
        super.write(kryo, output);
        output.writeBoolean(bezierTrajectory);
        output.writeBoolean(paralleltrajectory);
        output.writeDouble(parallelOffsetX);
        output.writeDouble(parallelOffsetY);
    }

    public void read(Kryo kryo, Input input) {
        byte version = input.readByte();
        super.read(kryo, input);
        bezierTrajectory = input.readBoolean();
        paralleltrajectory = input.readBoolean();
        parallelOffsetX = input.readDouble();
        parallelOffsetY = input.readDouble();
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder("HybridTrajectory{");
        sb.append("bezierTrajectory=").append(bezierTrajectory).append(", ");
        sb.append("points=").append(getPoints());
        sb.append("paralleltrajectory=").append(paralleltrajectory);
        sb.append("parallelOffsetX=").append(parallelOffsetX);
        sb.append("parallelOffsetY=").append(parallelOffsetY);
        sb.append('}');
        return sb.toString();
    }
}
