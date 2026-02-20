package com.betsoft.casino.mp.common;

public class GroupMember {
    private int type;
    private int skin;
    private double dx;
    private double dy;
    private long dt;

    public GroupMember(int type, int skin, double dx, double dy, long dt) {
        this.type = type;
        this.skin = skin;
        this.dx = dx;
        this.dy = dy;
        this.dt = dt;
    }

    public int getType() {
        return type;
    }

    public int getSkin() {
        return skin;
    }

    public double getDx() {
        return dx;
    }

    public double getDy() {
        return dy;
    }

    public long getDt() {
        return dt;
    }

    @Override
    public String toString() {
        return "GroupMember{" +
                "type=" + type +
                ", skin=" + skin +
                ", dx=" + dx +
                ", dy=" + dy +
                ", dt=" + dt +
                '}';
    }
}
