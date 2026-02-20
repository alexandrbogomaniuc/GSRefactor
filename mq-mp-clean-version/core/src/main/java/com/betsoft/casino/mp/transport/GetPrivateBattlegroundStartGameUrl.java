package com.betsoft.casino.mp.transport;

import com.betsoft.casino.utils.TInboundObject;

import java.util.Objects;

public class GetPrivateBattlegroundStartGameUrl extends TInboundObject {
    private String privateRoomId;

    public GetPrivateBattlegroundStartGameUrl(long date, int rid, String privateRoomId) {
        super(date, rid);
        this.privateRoomId = privateRoomId;
    }

    public String getPrivateRoomId() {
        return privateRoomId;
    }

    public void setPrivateRoomId(String privateRoomId) {
        this.privateRoomId = privateRoomId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        GetPrivateBattlegroundStartGameUrl that = (GetPrivateBattlegroundStartGameUrl) o;
        return Objects.equals(privateRoomId, that.privateRoomId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), privateRoomId);
    }

    @Override
    public String toString() {
        return "GetPrivateBattlegroundStartGameUrl{" +
                "privateRoomId='" + privateRoomId + '\'' +
                ", date=" + date +
                ", rid=" + rid +
                '}';
    }
}
