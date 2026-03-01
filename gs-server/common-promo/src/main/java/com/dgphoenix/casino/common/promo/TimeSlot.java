package com.abs.casino.common.promo;

import java.io.IOException;

import com.abs.casino.common.cache.JsonSelfSerializable;
import com.abs.casino.common.util.ITimeProvider;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;

public class TimeSlot implements KryoSerializable, JsonSelfSerializable<TimeSlot> {
    private static final byte VERSION = 0;
    private long prizeId; //AbstractPrize.id
    private long startDate;
    private long endDate;
    private long winUnlockTime;
    private boolean prizeWon;
    private transient ITimeProvider timeProvider;

    public TimeSlot() {}

    public TimeSlot(long prizeId, long startDate, long endDate, long winUnlockTime) {
        this.prizeId = prizeId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.winUnlockTime = winUnlockTime;
    }

    public long getPrizeId() {
        return prizeId;
    }

    public void setPrizeId(long prizeId) {
        this.prizeId = prizeId;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public long getEndDate() {
        return endDate;
    }

    public void setEndDate(long endDate) {
        this.endDate = endDate;
    }

    public boolean isCanBeWon() {
        return getCurrentTime() >= winUnlockTime && !isPrizeWon();
    }

    public long getWinUnlockTime() {
        return winUnlockTime;
    }

    public void setWinUnlockTime(long winUnlockTime) {
        this.winUnlockTime = winUnlockTime;
    }

    public boolean isPrizeWon() {
        return prizeWon;
    }

    public void setPrizeWon(boolean prizeWon) {
        this.prizeWon = prizeWon;
    }

    public long getCurrentTime() {
        if (timeProvider != null) {
            return timeProvider.getTime();
        }
        return System.currentTimeMillis();
    }

    public void setTimeProvider(ITimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    public ITimeProvider getTimeProvider() {
        return timeProvider;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeLong(prizeId, true);
        output.writeLong(startDate, true);
        output.writeLong(endDate, true);
        output.writeLong(winUnlockTime, true);
        output.writeBoolean(prizeWon);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte ver = input.readByte();
        prizeId = input.readLong(true);
        startDate = input.readLong(true);
        endDate = input.readLong(true);
        winUnlockTime = input.readLong(true);
        prizeWon = input.readBoolean();
    }

    @Override
    public void serializeObject(JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
        gen.writeNumberField("prizeId", prizeId);
        gen.writeNumberField("startDate", startDate);
        gen.writeNumberField("endDate", endDate);
        gen.writeNumberField("winUnlockTime", winUnlockTime);
        gen.writeBooleanField("prizeWon", prizeWon);
    }

    @Override
    public TimeSlot deserializeObject(JsonParser p, DeserializationContext ctxt)
            throws IOException {
        JsonNode node = p.getCodec().readTree(p);

        prizeId = node.get("prizeId").longValue();
        startDate = node.get("startDate").longValue();
        endDate = node.get("endDate").longValue();
        winUnlockTime = node.get("winUnlockTime").longValue();
        prizeWon = node.get("prizeWon").booleanValue();

        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TimeSlot timeSlot = (TimeSlot) o;

        if (prizeId != timeSlot.prizeId) return false;
        if (startDate != timeSlot.startDate) return false;
        if (endDate != timeSlot.endDate) return false;
        return winUnlockTime == timeSlot.winUnlockTime;
    }

    @Override
    public int hashCode() {
        int result = (int) (prizeId ^ (prizeId >>> 32));
        result = 31 * result + (int) (startDate ^ (startDate >>> 32));
        result = 31 * result + (int) (endDate ^ (endDate >>> 32));
        result = 31 * result + (int) (winUnlockTime ^ (winUnlockTime >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "TimeSlot[" +
                "prizeId=" + prizeId +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", winUnlockTime=" + winUnlockTime +
                ", prizeWon=" + prizeWon +
                ']';
    }
}
