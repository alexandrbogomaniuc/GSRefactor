package com.abs.casino.common.promo;

import com.dgphoenix.casino.common.promo.*;
import java.io.IOException;

import com.abs.casino.common.cache.JsonSelfSerializable;
import com.dgphoenix.casino.common.util.ITimeProvider;
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

public class PromoPeriod implements KryoSerializable, JsonSelfSerializable<PromoPeriod> {
    private static final byte VERSION = 0;
    private long startDate;
    private long endDate;
    private int slotCount;
    private TimeSlot currentTimeSlot;
    private transient ITimeProvider timeProvider;

    public PromoPeriod() {}

    public PromoPeriod(long startDate, long endDate, int slotCount) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.slotCount = slotCount;
    }

    public long getSlotDuration() {
        return (endDate - startDate) / slotCount;
    }

    public TimeSlot getCurrentTimeSlot() {
        return currentTimeSlot;
    }

    public void setCurrentTimeSlot(TimeSlot currentTimeSlot) {
        this.currentTimeSlot = currentTimeSlot;
    }

    public long getStartDate() {
        return startDate;
    }

    public long getEndDate() {
        return endDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(long endDate) {
        this.endDate = endDate;
    }

    public int getSlotCount() {
        return slotCount;
    }

    public void setSlotCount(int slotCount) {
        this.slotCount = slotCount;
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
        output.writeLong(startDate, true);
        output.writeLong(endDate, true);
        output.writeInt(slotCount, true);
        kryo.writeClassAndObject(output, currentTimeSlot);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte ver = input.readByte();
        startDate = input.readLong(true);
        endDate = input.readLong(true);
        slotCount = input.readInt(true);
        currentTimeSlot = (TimeSlot) kryo.readClassAndObject(input);
    }

    @Override
    public void serializeObject(JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
        gen.writeNumberField("startDate", startDate);
        gen.writeNumberField("endDate", endDate);
        gen.writeNumberField("slotCount", slotCount);
        gen.writeObjectField("currentTimeSlot", currentTimeSlot);
    }

    @Override
    public PromoPeriod deserializeObject(JsonParser p, DeserializationContext ctxt)
            throws IOException {
        ObjectMapper om = (ObjectMapper) p.getCodec();
        JsonNode node = om.readTree(p);

        startDate = node.get("startDate").asLong();
        endDate = node.get("endDate").asLong();
        slotCount = node.get("slotCount").asInt();
        currentTimeSlot = om.treeToValue(node.get("currentTimeSlot"), TimeSlot.class);

        return this;
    }

    @Override
    public String toString() {
        return "PromoPeriod[" +
                "startDate=" + startDate +
                ", endDate=" + endDate +
                ", slotCount=" + slotCount +
                ", currentTimeSlot=" + currentTimeSlot +
                ']';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PromoPeriod that = (PromoPeriod) o;

        if (startDate != that.startDate) return false;
        if (endDate != that.endDate) return false;
        return slotCount == that.slotCount;
    }

    @Override
    public int hashCode() {
        int result = (int) (startDate ^ (startDate >>> 32));
        result = 31 * result + (int) (endDate ^ (endDate >>> 32));
        result = 31 * result + slotCount;
        return result;
    }

}
