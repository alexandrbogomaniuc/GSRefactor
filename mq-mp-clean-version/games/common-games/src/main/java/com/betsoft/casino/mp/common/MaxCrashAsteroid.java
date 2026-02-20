package com.betsoft.casino.mp.common;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.Objects;

public class MaxCrashAsteroid implements KryoSerializable {
    private static final byte VERSION = 3;

    private double mult;
    private int type;
    private Double offsetBeforeCrashMs;
    private Double speedCoefficient;
    private Double xPercent;
    private Double yPercent;
    private Double slowDistancePercent;


    public MaxCrashAsteroid() {

    }

    public MaxCrashAsteroid(double mult, int type, Double offsetBeforeCrashMs, Double speedCoefficient, Double xPercent, Double yPercent, Double slowDistancePercent) {
        this.mult = mult;
        this.type = type;
        this.offsetBeforeCrashMs = offsetBeforeCrashMs;
        this.speedCoefficient = speedCoefficient;
        this.xPercent = xPercent;
        this.yPercent = yPercent;
        this.slowDistancePercent = slowDistancePercent;
    }

    public double getMult() {
        return mult;
    }

    public void setMult(double mult) {
        this.mult = mult;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Double getOffsetBeforeCrashMs() {
        return offsetBeforeCrashMs;
    }

    public void setOffsetBeforeCrashMs(Double offsetBeforeCrashMs) {
        this.offsetBeforeCrashMs = offsetBeforeCrashMs;
    }

    public Double getSpeedCoefficient() {
        return speedCoefficient;
    }

    public void setSpeedCoefficient(Double speedCoefficient) {
        this.speedCoefficient = speedCoefficient;
    }

    public Double getXPercent() {
        return xPercent;
    }

    public void setxPercent(Double xPercent) {
        this.xPercent = xPercent;
    }

    public Double getYPercent() {
        return yPercent;
    }

    public void setyPercent(Double yPercent) {
        this.yPercent = yPercent;
    }

    public Double getSlowDistancePercent() {
        return slowDistancePercent;
    }

    public void setSlowDistancePercent(Double slowDistancePercent) {
        this.slowDistancePercent = slowDistancePercent;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeDouble(mult);
        output.writeInt(type);
        kryo.writeObjectOrNull(output, speedCoefficient, Double.class);
        kryo.writeObjectOrNull(output, offsetBeforeCrashMs, Double.class);
        kryo.writeObjectOrNull(output, xPercent, Double.class);
        kryo.writeObjectOrNull(output, yPercent, Double.class);
        kryo.writeObjectOrNull(output, slowDistancePercent, Double.class);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte version = input.readByte();
        mult = input.readDouble();
        type = input.readInt();
        if(version >= 1) {
            speedCoefficient = kryo.readObjectOrNull(input, Double.class);
        }
        if(version >= 2) {
            offsetBeforeCrashMs = kryo.readObjectOrNull(input, Double.class);
        }
        if(version >= 3) {
            xPercent = kryo.readObjectOrNull(input, Double.class);
            yPercent = kryo.readObjectOrNull(input, Double.class);
            slowDistancePercent = kryo.readObjectOrNull(input, Double.class);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MaxCrashAsteroid)) return false;
        MaxCrashAsteroid that = (MaxCrashAsteroid) o;
        return Double.compare(mult, that.mult) == 0 && type == that.type && Objects.equals(offsetBeforeCrashMs, that.offsetBeforeCrashMs) && Objects.equals(speedCoefficient, that.speedCoefficient) && Objects.equals(xPercent, that.xPercent) && Objects.equals(yPercent, that.yPercent) && Objects.equals(slowDistancePercent, that.slowDistancePercent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mult, type, offsetBeforeCrashMs, speedCoefficient, xPercent, yPercent, slowDistancePercent);
    }

    @Override
    public String toString() {
        return "MaxCrashAsteroid{" +
                "mult=" + mult +
                ", type=" + type +
                ", offsetBeforeCrashMs=" + offsetBeforeCrashMs +
                ", speedCoefficient=" + speedCoefficient +
                ", xPercent=" + xPercent +
                ", yPercent=" + yPercent +
                ", slowDistancePercent=" + slowDistancePercent +
                '}';
    }
}
