package com.betsoft.casino.mp.sectorx.model;

public class ValidatedBezierPoint {
    private double x;
    private double y;
    private double percent;
    private double correctPercent;
    private double segmentLength;

    public ValidatedBezierPoint(double x, double y, double percent, double correctPercent) {
        this.x = x;
        this.y = y;
        this.percent = percent;
        this.correctPercent = correctPercent;
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

    public double getPercent() {
        return percent;
    }

    public double getSegmentLength() {
        return segmentLength;
    }

    public void setSegmentLength(double segmentLength) {
        this.segmentLength = segmentLength;
    }

    public void setPercent(double percent) {
        this.percent = percent;
    }

    public double getCorrectPercent() {
        return correctPercent;
    }

    public void setCorrectPercent(double correctPercent) {
        this.correctPercent = correctPercent;
    }
}
