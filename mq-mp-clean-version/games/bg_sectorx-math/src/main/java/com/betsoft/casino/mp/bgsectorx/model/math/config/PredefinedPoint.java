package com.betsoft.casino.mp.bgsectorx.model.math.config;

public class PredefinedPoint {
    protected final  double x;
    protected final double y;
    protected final long timeShift;

    public PredefinedPoint(double x, double y, long timeShift) {
        this.x = x;
        this.y = y;
        this.timeShift = timeShift;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public long getTimeShift() {
        return timeShift;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PredefinedPoint{");
        sb.append("x=").append(x);
        sb.append(", y=").append(y);
        sb.append(", timeShift=").append(timeShift);
        sb.append('}');
        return sb.toString();
    }
}
