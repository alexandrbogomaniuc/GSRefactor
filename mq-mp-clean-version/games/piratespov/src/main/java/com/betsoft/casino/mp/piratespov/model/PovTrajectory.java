package com.betsoft.casino.mp.piratespov.model;

import com.betsoft.casino.mp.model.movement.Trajectory;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.concurrent.TimeUnit;

public class PovTrajectory extends Trajectory {
    private static final long LEAVE_DELAY = TimeUnit.SECONDS.toMillis(15);

    private long leaveTime;

    public PovTrajectory() {}

    public PovTrajectory(double speed) {
        super(speed);
    }

    public PovTrajectory(Trajectory trajectory) {
        super(trajectory.getSpeed(), trajectory.getPoints());
        this.leaveTime = trajectory.getLastPoint().getTime() + LEAVE_DELAY;
    }

    @Override
    public long getLeaveTime() {
        return leaveTime;
    }

    public void setLeaveTime(long leaveTime) {
        this.leaveTime = leaveTime;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        super.write(kryo, output);
        output.writeLong(leaveTime, true);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        super.read(kryo, input);
        leaveTime = input.readLong(true);
    }

    @Override
    public String toString() {
        return "PovTrajectory{" +
                "speed=" + getSpeed() +
                ", points=" + getPoints() +
                ", leaveTime=" + leaveTime +
                '}';
    }
}
