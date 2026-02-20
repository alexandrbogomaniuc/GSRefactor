package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.IObserverRemoved;
import com.betsoft.casino.utils.TInboundObject;

public class ObserverRemoved extends TInboundObject implements IObserverRemoved {
    private String nickname;

    public ObserverRemoved(long date, int rid, String nickname) {
        super(date, rid);
        this.nickname = nickname;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
