package com.betsoft.casino.mp.model;

import com.abs.casino.common.cache.Identifiable;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.Objects;

/**
 * User: flsh
 * Date: 14.04.18.
 */
public class RoundInfo implements Identifiable, KryoSerializable {
    private static final byte VERSION = 0;
    private long id;
    private long roomId;
    private long startDate;
    private long endDate;

    public RoundInfo() {
    }

    public RoundInfo(long id, long roomId, long startDate, long endDate) {
        this.id = id;
        this.roomId = roomId;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getRoomId() {
        return roomId;
    }

    public void setRoomId(long roomId) {
        this.roomId = roomId;
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

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeLong(id, true);
        output.writeLong(roomId, true);
        output.writeLong(startDate, true);
        output.writeLong(endDate, true);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        input.readByte();
        id = input.readLong(true);
        roomId = input.readLong(true);
        startDate = input.readLong(true);
        endDate = input.readLong(true);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoundInfo roundInfo = (RoundInfo) o;
        return id == roundInfo.id &&
                roomId == roundInfo.roomId;
    }

    @Override
    public int hashCode() {

        return Objects.hash(id);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RoundInfo [");
        sb.append("id=").append(id);
        sb.append(", roomId=").append(roomId);
        sb.append(", startDate=").append(startDate);
        sb.append(", endDate=").append(endDate);
        sb.append(']');
        return sb.toString();
    }
}
