package com.betsoft.casino.mp.model.friends;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.*;

public class Friends implements KryoSerializable {

    private static final byte VERSION = 0;
    private String nickname;
    private String externalId;
    private Map<String, Friend> friends = new HashMap();
    private long updateTime;

    public Friends() {}

    public Friends(String nickname, String externalId, Map<String, Friend> friends) {
        this(nickname, externalId, friends, System.currentTimeMillis());
    }

    public Friends(String nickname, String externalId, Map<String, Friend> friends, long updateTime) {
        this.nickname = nickname;
        this.externalId = externalId;
        this.setFriends(friends);
        this.updateTime = updateTime;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public Map<String, Friend> getFriends() {
        return friends;
    }

    public void setFriends(Map<String, Friend> friends) {
        if(friends == null) {
            this.friends = new HashMap<>();
        } else {
            this.friends = friends;
        }
        this.updateTime = System.currentTimeMillis();
    }

    public Friend getFriend(String externalId) {
        return this.getFriends().get(externalId);
    }

    public void setFriend(Friend friend) {
        if(friend != null) {
            this.getFriends().put(friend.getExternalId(), friend);
        }
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
        output.writeString(nickname);
        output.writeString(externalId);
        kryo.writeClassAndObject(output, friends);
        output.writeLong(updateTime, true);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        input.readByte();
        //noinspection unchecked
        nickname = input.readString();
        externalId = input.readString();
        friends = (Map<String, Friend>) kryo.readClassAndObject(input);
        updateTime = input.readLong(true);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Friends)) return false;
        Friends friends1 = (Friends) o;
        return Objects.equals(nickname, friends1.nickname)
                && Objects.equals(externalId, friends1.externalId)
                && Objects.equals(friends, friends1.friends);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nickname, externalId, friends);
    }

    @Override
    public String toString() {
        return "Friends{" +
                "nickname='" + nickname + '\'' +
                ", externalId='" + externalId + '\'' +
                ", friends=" + friends +
                ", updateTime=" + updateTime +
                '}';
    }
}
