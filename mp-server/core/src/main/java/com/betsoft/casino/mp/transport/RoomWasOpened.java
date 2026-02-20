package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.room.IRoomWasOpened;
import com.betsoft.casino.utils.TInboundObject;

public class RoomWasOpened extends TInboundObject implements IRoomWasOpened {
    private String nickname;
    private boolean isKicked;

    public RoomWasOpened(long date, int rid, String nickname, boolean isKicked) {
        super(date, rid);
        this.nickname = nickname;
        this.isKicked = isKicked;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public boolean isKicked() {
        return isKicked;
    }

    public void setKicked(boolean kicked) {
        isKicked = kicked;
    }
}
