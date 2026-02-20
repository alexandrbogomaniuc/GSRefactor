package com.betsoft.casino.mp.model.privateroom;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.List;

public class PrivateRoom implements KryoSerializable {

    private static final byte VERSION = 1;
    private long roomId;
    private String privateRoomId;
    private long ownerAccountId;
    private String ownerNickname;
    private String ownerExternalId;
    private List<Player> players;
    private long updateTime;

    public PrivateRoom() {}

    public PrivateRoom(long roomId, String privateRoomId, long ownerAccountId, String ownerNickname,
                       String ownerExternalId, List<Player> players, long updateTime) {
        this.roomId = roomId;
        this.privateRoomId = privateRoomId;
        this.ownerAccountId = ownerAccountId;
        this.ownerNickname = ownerNickname;
        this.ownerExternalId = ownerExternalId;
        this.players = players;
        this.updateTime = updateTime;
    }

    public long getRoomId() {
        return roomId;
    }

    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }

    public String getPrivateRoomId() {
        return privateRoomId;
    }

    public void setPrivateRoomId(String privateRoomId) {
        this.privateRoomId = privateRoomId;
    }

    public String getOwnerNickname() {
        return ownerNickname;
    }

    public void setOwnerNickname(String ownerNickname) {
        this.ownerNickname = ownerNickname;
    }

    public String getOwnerExternalId() {
        return ownerExternalId;
    }

    public void setOwnerExternalId(String ownerExternalId) {
        this.ownerExternalId = ownerExternalId;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public long getOwnerAccountId() {
        return ownerAccountId;
    }

    public void setOwnerAccountId(long ownerAccountId) {
        this.ownerAccountId = ownerAccountId;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeLong(roomId, true);
        output.writeString(privateRoomId);
        output.writeLong(ownerAccountId, true);
        output.writeString(ownerNickname);
        output.writeString(ownerExternalId);
        kryo.writeClassAndObject(output, players);
        if(VERSION == 1) {
            output.writeLong(updateTime, true);
        }
    }

    @Override
    public void read(Kryo kryo, Input input) {
        input.readByte();
        roomId = input.readLong(true);
        privateRoomId = input.readString();
        ownerAccountId = input.readLong(true);
        ownerNickname = input.readString();
        ownerExternalId = input.readString();
        //noinspection unchecked
        players = (List<Player>) kryo.readClassAndObject(input);
        if(VERSION == 1) {
            updateTime = input.readLong(true);
        }
    }

    @Override
    public String toString() {
        return "PrivateRoom{" +
                "roomId=" + roomId +
                ", privateRoomId='" + privateRoomId + '\'' +
                ", ownerAccountId=" + ownerAccountId +
                ", ownerNickname='" + ownerNickname + '\'' +
                ", ownerExternalId='" + ownerExternalId + '\'' +
                ", players=" + players +
                ", updateTime=" + updateTime +
                '}';
    }
}