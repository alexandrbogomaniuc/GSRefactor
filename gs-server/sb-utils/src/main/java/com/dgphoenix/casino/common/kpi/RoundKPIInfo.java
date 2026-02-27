package com.abs.casino.common.kpi;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.Map;

/**
 * User: flsh
 * Date: 19.11.2020.
 */
public class RoundKPIInfo implements KryoSerializable {
    private static final byte VERSION = 1;
    private long roundId;
    private int swFreeShotsCount;
    private long realBet;
    private long realWin;
    private long swCompensation;
    private long startTime;
    private long endTime;

    //key price, value is count
    private Map<Long, Integer> paidRegularShotsStat;
    private Map<Long, Integer> swShotsStat;
    private Map<Long, Integer> lootboxStat;
    private long roomCoinInCents;
    //key betLevel, value is Map (key weaponId, value is count)
    private Map<Integer, Map<Integer, Integer>> swShotsByBetLevel;
    private Map<Integer, Map<Integer, Integer>> swFreeShotsByBetLevel;

    public RoundKPIInfo() {}


    public RoundKPIInfo(long roundId, int swFreeShotsCount, long realBet, long realWin, long swCompensation,
                        long startTime, long endTime, Map<Long, Integer> paidRegularShotsStat,
                        Map<Long, Integer> swShotsStat, Map<Long, Integer> lootboxStat,
                        Map<Integer, Map<Integer, Integer>> swShotsByBetLevel,
                        Map<Integer, Map<Integer, Integer>> swFreeShotsByBetLevel,
                        long roomCoinInCents
    ) {
        this.roundId = roundId;
        this.swFreeShotsCount = swFreeShotsCount;
        this.realBet = realBet;
        this.realWin = realWin;
        this.swCompensation = swCompensation;
        this.startTime = startTime;
        this.endTime = endTime;
        this.paidRegularShotsStat = paidRegularShotsStat;
        this.swShotsStat = swShotsStat;
        this.lootboxStat = lootboxStat;
        this.swShotsByBetLevel = swShotsByBetLevel;
        this.swFreeShotsByBetLevel = swFreeShotsByBetLevel;
        this.roomCoinInCents = roomCoinInCents;
    }

    public Map<Integer, Map<Integer, Integer>> getSwShotsByBetLevel() {
        return swShotsByBetLevel;
    }

    public void setSwShotsByBetLevel(Map<Integer, Map<Integer, Integer>> swShotsByBetLevel) {
        this.swShotsByBetLevel = swShotsByBetLevel;
    }

    public Map<Integer, Map<Integer, Integer>> getSwFreeShotsByBetLevel() {
        return swFreeShotsByBetLevel;
    }

    public void setSwFreeShotsByBetLevel(Map<Integer, Map<Integer, Integer>> swFreeShotsByBetLevel) {
        this.swFreeShotsByBetLevel = swFreeShotsByBetLevel;
    }

    public long getRoomCoinInCents() {
        return roomCoinInCents;
    }

    public void setRoomCoinInCents(long roomCoinInCents) {
        this.roomCoinInCents = roomCoinInCents;
    }

    public long getRoundId() {
        return roundId;
    }

    public void setRoundId(long roundId) {
        this.roundId = roundId;
    }

    public int getSwFreeShotsCount() {
        return swFreeShotsCount;
    }

    public void setSwFreeShotsCount(int swFreeShotsCount) {
        this.swFreeShotsCount = swFreeShotsCount;
    }

    public long getRealBet() {
        return realBet;
    }

    public void setRealBet(long realBet) {
        this.realBet = realBet;
    }

    public long getRealWin() {
        return realWin;
    }

    public void setRealWin(long realWin) {
        this.realWin = realWin;
    }

    public long getSwCompensation() {
        return swCompensation;
    }

    public void setSwCompensation(long swCompensation) {
        this.swCompensation = swCompensation;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public Map<Long, Integer> getPaidRegularShotsStat() {
        return paidRegularShotsStat;
    }

    public void setPaidRegularShotsStat(Map<Long, Integer> paidRegularShotsStat) {
        this.paidRegularShotsStat = paidRegularShotsStat;
    }

    public Map<Long, Integer> getSwShotsStat() {
        return swShotsStat;
    }

    public void setSwShotsStat(Map<Long, Integer> swShotsStat) {
        this.swShotsStat = swShotsStat;
    }

    public Map<Long, Integer> getLootboxStat() {
        return lootboxStat;
    }

    public void setLootboxStat(Map<Long, Integer> lootboxStat) {
        this.lootboxStat = lootboxStat;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RoundKPIInfo [");
        sb.append("roundId=").append(roundId);
        sb.append(", swFreeShotsCount=").append(swFreeShotsCount);
        sb.append(", realBet=").append(realBet);
        sb.append(", realWin=").append(realWin);
        sb.append(", swCompensation=").append(swCompensation);
        sb.append(", startTime=").append(startTime);
        sb.append(", endTime=").append(endTime);
        sb.append(", paidRegularShotsStat=").append(paidRegularShotsStat);
        sb.append(", swShotsStat=").append(swShotsStat);
        sb.append(", lootboxStat=").append(lootboxStat);
        sb.append(", roomCoinInCents=").append(roomCoinInCents);
        sb.append(", swShotsByBetLevel=").append(swShotsByBetLevel);
        sb.append(", swFreeShotsByBetLevel=").append(swFreeShotsByBetLevel);
        sb.append(']');
        return sb.toString();
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeLong(roundId, true);
        output.writeInt(swFreeShotsCount, true);
        output.writeLong(realBet, true);
        output.writeLong(realWin, true);
        output.writeLong(swCompensation, true);
        output.writeLong(startTime, true);
        output.writeLong(endTime, true);
        kryo.writeClassAndObject(output, paidRegularShotsStat);
        kryo.writeClassAndObject(output, swShotsStat);
        kryo.writeClassAndObject(output, lootboxStat);
        output.writeLong(roomCoinInCents, true);
        kryo.writeClassAndObject(output, swShotsByBetLevel);
        kryo.writeClassAndObject(output, swFreeShotsByBetLevel);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void read(Kryo kryo, Input input) {
        byte ver = input.readByte();
        roundId = input.readLong(true);
        swFreeShotsCount = input.readInt(true);
        realBet = input.readLong(true);
        realWin = input.readLong(true);
        swCompensation = input.readLong(true);
        startTime = input.readLong(true);
        endTime = input.readLong(true);
        paidRegularShotsStat = (Map<Long, Integer>) kryo.readClassAndObject(input);
        swShotsStat = (Map<Long, Integer>) kryo.readClassAndObject(input);
        lootboxStat = (Map<Long, Integer>) kryo.readClassAndObject(input);
        if(ver >= 1){
            roomCoinInCents = input.readLong(true);
            swShotsByBetLevel = (Map<Integer, Map<Integer, Integer>>) kryo.readClassAndObject(input);
            swFreeShotsByBetLevel = (Map<Integer, Map<Integer, Integer>>) kryo.readClassAndObject(input);
        }
    }
}
