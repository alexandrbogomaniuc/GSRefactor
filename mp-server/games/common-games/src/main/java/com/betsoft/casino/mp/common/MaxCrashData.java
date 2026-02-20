package com.betsoft.casino.mp.common;

import com.dgphoenix.casino.common.util.RNG;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Contains data for current round in room.  (crash multiplier, roundId of room, salt, ... )
 */
public class MaxCrashData implements KryoSerializable {
    private static final int REGULAR_ASTEROID_TYPE_BEGIN = 1;
    private static final int REGULAR_ASTEROID_TYPE_END = 4;

    private static final int CRASH_ASTEROID_TYPE_BEGIN = 10; //final asteroid
    private static final int CRASH_ASTEROID_TYPE_END = 12;

    private static final int X_SPAWN_PERCENT_FROM = 60;
    private static final int X_SPAWN_PERCENT_TO = 100;

    private static final int Y_SPAWN_PERCENT_FROM = 0;
    private static final int Y_SPAWN_PERCENT_TO = 40;

    private static final int SLOWING_DISTANCE_PERCENT_FROM = 50;
    private static final int SLOWING_DISTANCE_PERCENT_TO = 99;

    private static final byte VERSION = 5;
    private double naturalMultiplier;
    private double crashMult;
    private double currentMult;
    private long startTime;
    private long roundId;
    private String salt;
    private String token;
    private long offsetStartTime;
    private String function;
    private long lastEjectTime;
    private double timeSpeedMult;

    private boolean reachedMultiplierLimit = false;
    private double crashTime;

    private Map<Double, MaxCrashAsteroid> asteroidMults = new HashMap<Double, MaxCrashAsteroid>();

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeDouble(crashMult);
        output.writeDouble(currentMult);
        output.writeLong(startTime, true);
        output.writeLong(roundId, true);
        output.writeString(salt);
        output.writeString(token);
        output.writeLong(offsetStartTime, true);
        output.writeString(function);
        output.writeBoolean(reachedMultiplierLimit);
        output.writeLong(lastEjectTime, true);
        output.writeDouble(timeSpeedMult);
        output.writeDouble(crashTime);
        output.writeDouble(naturalMultiplier);
        kryo.writeObject(output, asteroidMults);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte version = input.readByte();
        crashMult = input.readDouble();
        currentMult = input.readDouble();
        startTime = input.readLong(true);
        roundId = input.readLong(true);
        salt = input.readString();
        token = input.readString();
        offsetStartTime = input.readLong(true);
        function = input.readString();
        reachedMultiplierLimit = input.readBoolean();
        if (version > 0) {
            lastEjectTime = input.readLong(true);
        }
        if (version > 1) {
            timeSpeedMult = input.readDouble();
        }
        if (version > 2) {
            crashTime = input.readDouble();
        }
        if (version > 3) {
            naturalMultiplier = input.readDouble();
        } else {
            naturalMultiplier = crashMult;
        }
        if(version > 4) {
            asteroidMults = kryo.readObject(input, HashMap.class);
        }
    }

    public double getNaturalMultiplier() {
        return naturalMultiplier <= 0 ? crashMult : naturalMultiplier;
    }

    public void setNaturalMultiplier(double naturalMultiplier) {
        this.naturalMultiplier = naturalMultiplier;
    }

    public double getCrashMult() {
        return crashMult;
    }

    public void setCrashMult(double crashMult) {
        this.crashMult = crashMult;
    }

    public double getCurrentMult() {
        return currentMult;
    }

    public void setCurrentMult(double currentMult) {
        this.currentMult = currentMult;
    }

    public boolean isNeedCrashInstantly() {
        return  crashMult <= 1.0;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getRoundId() {
        return roundId;
    }

    public void setRoundId(long roundId) {
        this.roundId = roundId;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getOffsetStartTime() {
        return offsetStartTime;
    }

    public void setOffsetStartTime(long offsetStartTime) {
        this.offsetStartTime = offsetStartTime;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public boolean isReachedMultiplierLimit() {
        return reachedMultiplierLimit;
    }

    public void setReachedMultiplierLimit(boolean reachedMultiplierLimit) {
        this.reachedMultiplierLimit = reachedMultiplierLimit;
    }

    public long getLastEjectTime() {
        return lastEjectTime;
    }

    public void setLastEjectTime(long lastEjectTime) {
        this.lastEjectTime = lastEjectTime;
    }

    public double getTimeSpeedMult() {
        return timeSpeedMult;
    }

    public void setTimeSpeedMult(double timeSpeedMult) {
        this.timeSpeedMult = timeSpeedMult;
    }

    public double getCrashTime() {
        return crashTime;
    }

    public void setCrashTime(double crashTime) {
        this.crashTime = crashTime;
    }

    public Map<Double, MaxCrashAsteroid> getAsteroidMults() {
        return asteroidMults;
    }

    public void setAsteroidMults(Map<Double, MaxCrashAsteroid> asteroidMults) {
        this.asteroidMults = asteroidMults;
    }

    public static double getNumberWithScale(double number, int scale, RoundingMode roundingMode) {
        return BigDecimal.valueOf(number).setScale(scale, roundingMode).doubleValue();
    }

    public void initAsteroidMults(double crashMultWithOffset, Double offsetBeforeCrashMs, Double speedCoefficient) {

        asteroidMults = new HashMap<>();

        //generate intermediate (non-crash) asteroids of the types: 1, 2, 3 or 4
        double mult = 1.05;
        for(; mult < crashMultWithOffset;) {

            double minGap = 0.1 * Math.pow(1.05, mult - 1);
            double maxGap = 0.3 * Math.pow(1.15, mult - 1);
            double gap = minGap + (maxGap - minGap) * RNG.randUniform();
            mult += gap;
            if(mult < crashMultWithOffset) {
                double deltaTillEnd = crashMultWithOffset - mult;
                if(deltaTillEnd > 0.5) { //check if "mult" is not too close to crashMultWithOffset
                    double multScaled = getNumberWithScale(mult, 2, RoundingMode.HALF_UP);
                    //generate random asteroid type from 1 till 4
                    int asteroidType = RNG.nextInt(REGULAR_ASTEROID_TYPE_BEGIN, REGULAR_ASTEROID_TYPE_END + 1);
                    MaxCrashAsteroid maxCrashAsteroid = new MaxCrashAsteroid(multScaled, asteroidType, null, null, null, null, null);//regular asteroid
                    asteroidMults.put(multScaled, maxCrashAsteroid);
                }
            }
        }

        //generate last (crash) asteroid of the type: 10, 11 or 12
        double crashMultWithOffsetRounded = getNumberWithScale(crashMultWithOffset, 2, RoundingMode.HALF_UP);

        //crash (final) asteroid type should be one of the values: 10, 11, 12
        int crashAsteroidType = RNG.nextInt(CRASH_ASTEROID_TYPE_BEGIN, CRASH_ASTEROID_TYPE_END + 1);

        double xPercent = X_SPAWN_PERCENT_TO; //value should be 100, when yPercent is changed from 0 till 40
        double yPercent = Y_SPAWN_PERCENT_FROM; //value should be 0, when xPercent is changed from 60 till 100

        if(RNG.nextBoolean()) {//change one of two parameters: xPercent or yPercent, not both at the same time
            //xPercent should have value from 60 till 100
            xPercent = RNG.nextInt(X_SPAWN_PERCENT_FROM, X_SPAWN_PERCENT_TO + 1);
        } else {
            //yPercent should have value from 0 till 40
            yPercent = RNG.nextInt(Y_SPAWN_PERCENT_FROM, Y_SPAWN_PERCENT_TO + 1);
        }

        //slowDistancePercent should have value from 50 till 99
        double slowDistancePercent = RNG.nextInt(SLOWING_DISTANCE_PERCENT_FROM, SLOWING_DISTANCE_PERCENT_TO + 1);

        MaxCrashAsteroid maxCrashAsteroid = new MaxCrashAsteroid(
                crashMultWithOffsetRounded,
                crashAsteroidType,
                offsetBeforeCrashMs,
                speedCoefficient,
                xPercent,
                yPercent,
                slowDistancePercent);

        asteroidMults.put(crashMultWithOffsetRounded, maxCrashAsteroid);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MaxCrashData that = (MaxCrashData) o;
        return Double.compare(that.crashMult, crashMult) == 0 && Double.compare(that.currentMult, currentMult) == 0 &&
                startTime == that.startTime && roundId == that.roundId && Objects.equals(salt, that.salt) && Objects.equals(token, that.token) && offsetStartTime == that.offsetStartTime && Objects.equals(function, that.function);
    }

    @Override
    public int hashCode() {
        return Objects.hash(crashMult, currentMult, startTime, roundId, salt, token, offsetStartTime, function);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", MaxCrashData.class.getSimpleName() + "[", "]")
                .add("naturalMultiplier=" + naturalMultiplier)
                .add("crashMult=" + crashMult)
                .add("currentMult=" + currentMult)
                .add("startTime=" + startTime)
                .add("roundId=" + roundId)
                .add("reachedMultiplierLimit=" + reachedMultiplierLimit)
                .add("salt='" + salt + "'")
                .add("offsetStartTime='" + offsetStartTime + "'")
                .add("function='" + function + "'")
                .add("lastEjectTime='" + lastEjectTime + "'")
                .add("timeSpeedMult='" + timeSpeedMult + "'")
                .add("crashTime='" + crashTime + "'")
                .add("token='" + token + "'")
                .add("asteroidMults='" + asteroidMults + "'")
                .toString();
    }
}
