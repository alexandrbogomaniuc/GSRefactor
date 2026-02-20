package com.betsoft.casino.mp.transport;

import java.io.Serializable;

/**
 * User: flsh
 * Date: 11.06.17.
 */
public class EnemyMove implements Serializable {
    private long id;
    private double x;
    private double y;
    private int angle;

    public EnemyMove(long id, double x, double y, int angle) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.angle = angle;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public int getAngle() {
        return angle;
    }

    public void setAngle(int angle) {
        this.angle = angle;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EnemyMove that = (EnemyMove) o;

        if (id != that.id) return false;
        if (Double.compare(that.x, x) != 0) return false;
        if (Double.compare(that.y, y) != 0) return false;
        return that.angle == angle;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public String toString() {
        return "EnemyMove[" +
                "id=" + id +
                ", x=" + x +
                ", y=" + y +
                ", angle=" + angle +
                ']';
    }
}
