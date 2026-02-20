package com.betsoft.casino.mp.transport;

import java.util.StringJoiner;

public class PointExt {
    protected double x;
    protected double y;
    protected long time;
    protected boolean invulnerable;
    protected boolean teleport;
    protected boolean freeze;

    public PointExt(double x, double y, long time, boolean invulnerable, boolean teleport, boolean freeze) {
        this.x = x;
        this.y = y;
        this.time = time;
        this.invulnerable = invulnerable;
        this.teleport = teleport;
        this.freeze = freeze;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean isInvulnerable() {
        return invulnerable;
    }

    public void setInvulnerable(boolean invulnerable) {
        this.invulnerable = invulnerable;
    }

    public boolean isTeleport() {
        return teleport;
    }

    public void setTeleport(boolean teleport) {
        this.teleport = teleport;
    }

    public boolean isFreeze() {
        return freeze;
    }

    public void setFreeze(boolean freeze) {
        this.freeze = freeze;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", PointExt.class.getSimpleName() + "[", "]")
                .add("x=" + x)
                .add("y=" + y)
                .add("time=" + time)
                .add("invulnerable=" + invulnerable)
                .add("teleport=" + teleport)
                .add("freeze=" + freeze)
                .toString();
    }
}
