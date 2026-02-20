package com.betsoft.casino.mp.common;

import com.dgphoenix.casino.common.util.Pair;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.Objects;


public class RealWeaponCounts implements KryoSerializable {
    private static final byte VERSION = 0;
    private int numberOfPaidShots;
    private int numberOfUsualShots;
    double betsOfPaidShots;
    double betsOfUsualShots;


    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeInt(numberOfPaidShots, true);
        output.writeInt(numberOfUsualShots, true);
        output.writeDouble(betsOfPaidShots);
        output.writeDouble(betsOfUsualShots);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void read(Kryo kryo, Input input) {
        byte version = input.readByte();
        numberOfPaidShots = input.readInt(true);
        numberOfUsualShots = input.readInt(true);
        betsOfPaidShots = input.readDouble();
        betsOfUsualShots = input.readDouble();
    }

    public void updateData(boolean isPaidShot, double bet) {
        if (isPaidShot) {
            numberOfPaidShots++;
            betsOfPaidShots += bet;
        } else {
            numberOfUsualShots++;
            betsOfUsualShots += bet;
        }
    }

    public int getNumberOfPaidShots() {
        return numberOfPaidShots;
    }

    public void setNumberOfPaidShots(int numberOfPaidShots) {
        this.numberOfPaidShots = numberOfPaidShots;
    }

    public int getNumberOfUsualShots() {
        return numberOfUsualShots;
    }

    public void setNumberOfUsualShots(int numberOfUsualShots) {
        this.numberOfUsualShots = numberOfUsualShots;
    }

    public double getBetsOfPaidShots() {
        return betsOfPaidShots;
    }

    public void setBetsOfPaidShots(double betsOfPaidShots) {
        this.betsOfPaidShots = betsOfPaidShots;
    }

    public double getBetsOfUsualShots() {
        return betsOfUsualShots;
    }

    public void setBetsOfUsualShots(double betsOfUsualShots) {
        this.betsOfUsualShots = betsOfUsualShots;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RealWeaponCounts that = (RealWeaponCounts) o;
        return numberOfPaidShots == that.numberOfPaidShots &&
                numberOfUsualShots == that.numberOfUsualShots &&
                Double.compare(that.betsOfPaidShots, betsOfPaidShots) == 0 &&
                Double.compare(that.betsOfUsualShots, betsOfUsualShots) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(numberOfPaidShots, numberOfUsualShots, betsOfPaidShots, betsOfUsualShots);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RealWeaponCounts{");
        sb.append("numberOfPaidShots=").append(numberOfPaidShots);
        sb.append(", numberOfUsualShots=").append(numberOfUsualShots);
        sb.append(", betsOfPaidShots=").append(betsOfPaidShots);
        sb.append(", getBetsOfUsualShots=").append(betsOfUsualShots);
        sb.append('}');
        return sb.toString();
    }

    public Pair<String, Double> getData() {
        StringBuilder sb = new StringBuilder()
                .append(numberOfPaidShots).append(",")
                .append(betsOfPaidShots).append(",")
                .append(numberOfUsualShots).append(",")
                .append(betsOfUsualShots);
        return new Pair<>(sb.toString(), betsOfUsualShots + betsOfPaidShots);
    }
}
