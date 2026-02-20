package com.betsoft.casino.mp.transport;

import com.betsoft.casino.utils.TInboundObject;

import java.util.List;

public class PrivateRoomInvite extends TInboundObject{
    private List<String> nicknames;

    public PrivateRoomInvite(long date, int rid, List<String> nicknames) {
        super(date, rid);
        this.nicknames = nicknames;
    }

    public List<String> getNicknames() {
        return nicknames;
    }

    public void setNicknames(List<String> nicknames) {
        this.nicknames = nicknames;
    }

    @Override
    public String toString() {
        return "PrivateRoomInvite{" +
                "nicknamese='" + nicknames + '\'' +
                ", date=" + date +
                ", rid=" + rid +
                '}';
    }
}
