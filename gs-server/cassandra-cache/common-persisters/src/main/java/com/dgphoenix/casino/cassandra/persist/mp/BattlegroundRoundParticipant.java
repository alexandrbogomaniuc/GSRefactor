package com.dgphoenix.casino.cassandra.persist.mp;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.HashSet;
import java.util.Set;

public class BattlegroundRoundParticipant implements KryoSerializable {
    private static final byte VERSION = 1;

    private String sid;
    private long roundId;
    private long gameSessionId;
    private Set<Long> accountIds;
    private long startTime;
    private long endTime;

    private String privateRoomId;

    public BattlegroundRoundParticipant() {}

    public BattlegroundRoundParticipant(String sid, long roundId, long gameSessionId, Set<Long> accountIds, long startTime, long endTime, String privateRoomId) {
        this.sid = sid;
        this.roundId = roundId;
        this.gameSessionId = gameSessionId;
        this.accountIds = accountIds;
        this.startTime = startTime;
        this.endTime = endTime;
        this.privateRoomId = privateRoomId;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public long getRoundId() {
        return roundId;
    }

    public void setRoundId(long roundId) {
        this.roundId = roundId;
    }

    public long getGameSessionId() {
        return gameSessionId;
    }

    public void setGameSessionId(long gameSessionId) {
        this.gameSessionId = gameSessionId;
    }

    public Set<Long> getAccountIds() {
        return accountIds;
    }

    public void setAccountIds(Set<Long> accountIds) {
        this.accountIds = accountIds;
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

    public String getPrivateRoomId() {
        return privateRoomId;
    }

    public void setPrivateRoomId(String privateRoomId) {
        this.privateRoomId = privateRoomId;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeString(sid);
        output.writeLong(roundId, true);
        output.writeLong(gameSessionId, true);
        kryo.writeObjectOrNull(output, accountIds, HashSet.class);
        output.writeLong(startTime, true);
        output.writeLong(endTime, true);
        output.writeString(privateRoomId);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte version = input.readByte();
        sid = input.readString();
        roundId = input.readLong(true);
        gameSessionId = input.readLong(true);
        accountIds = kryo.readObjectOrNull(input, HashSet.class);
        startTime = input.readLong(true);
        endTime = input.readLong(true);
        if (version > 0) {
            privateRoomId = input.readString();
        }
    }

    @Override
    public String toString() {
        return "BattlegroundRoundParticipant{" +
                "sid='" + sid + '\'' +
                ", roundId=" + roundId +
                ", gameSessionId=" + gameSessionId +
                ", accountIds=" + accountIds +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", privateRoomId=" + privateRoomId +
                '}';
    }
}
