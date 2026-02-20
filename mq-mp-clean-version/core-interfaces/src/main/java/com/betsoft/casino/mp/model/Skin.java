package com.betsoft.casino.mp.model;

public class Skin implements ISkin {
    private float speed;
    private float speedDeltaPositive;
    private float speedDeltaNegative;


    public Skin(float speed, float speedDeltaNegative, float speedDeltaPositive) {
        this.speed = speed;
        this.speedDeltaPositive = speedDeltaPositive;
        this.speedDeltaNegative = speedDeltaNegative;
    }

    @Override
    public float getSpeed() {
        return speed;
    }

    @Override
    public void setSpeed(float speed) {
        this.speed = speed;
    }

    @Override
    public float getSpeedDeltaPositive() {
        return speedDeltaPositive;
    }

    @Override
    public void setSpeedDeltaPositive(float speedDeltaPositive) {
        this.speedDeltaPositive = speedDeltaPositive;
    }

    @Override
    public float getSpeedDeltaNegative() {
        return speedDeltaNegative;
    }

    @Override
    public void setSpeedDeltaNegative(float speedDeltaNegative) {
        this.speedDeltaNegative = speedDeltaNegative;
    }

    @Override
    public String toString() {
        return "Skin{" +
                "speed=" + speed +
                ", speedDeltaPositive=" + speedDeltaPositive +
                ", speedDeltaNegative=" + speedDeltaNegative +
                '}';
    }
}
