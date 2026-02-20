package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.IAvatar;
import com.betsoft.casino.mp.model.ITransportAsteroid;
import com.betsoft.casino.mp.model.ITransportSeat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * User: flsh
 * Date: 09.06.17.
 */
public class Asteroid implements ITransportAsteroid, Serializable {
    private int type;
    private Double speed;
    private Double x;
    private Double y;
    private Double slow;

    public Asteroid() {
    }

    public Asteroid(int type, Double speed, Double x, Double y, Double slow) {
        this.type = type;
        this.speed = speed;
        this.x = x;
        this.y = y;
        this.slow = slow;
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public void setType(int type) {
        this.type = type;
    }

    @Override
    public Double getSpeed() {
        return speed;
    }

    @Override
    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    @Override
    public Double getX() {
        return x;
    }

    @Override
    public void setX(Double x) {
        this.x = x;
    }

    @Override
    public Double getY() {
        return y;
    }

    @Override
    public void setY(Double y) {
        this.y = y;
    }

    @Override
    public Double getSlow() {
        return slow;
    }

    @Override
    public void setSlow(Double slow) {
        this.slow = slow;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Asteroid)) return false;
        Asteroid asteroid = (Asteroid) o;
        return Objects.equals(getType(), asteroid.getType()) && Objects.equals(getSpeed(), asteroid.getSpeed()) && Objects.equals(getX(), asteroid.getX()) && Objects.equals(getY(), asteroid.getY()) && Objects.equals(getSlow(), asteroid.getSlow());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), getSpeed(), getX(), getY(), getSlow());
    }

    @Override
    public String toString() {
        return "Asteroid{" +
                "type=" + type +
                ", speed=" + speed +
                ", x=" + x +
                ", y=" + y +
                ", slow=" + slow +
                '}';
    }
}
