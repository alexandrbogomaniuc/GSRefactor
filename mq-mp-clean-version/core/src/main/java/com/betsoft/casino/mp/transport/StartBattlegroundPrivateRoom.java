package com.betsoft.casino.mp.transport;

import com.betsoft.casino.utils.TInboundObject;

public class StartBattlegroundPrivateRoom extends TInboundObject {
    public StartBattlegroundPrivateRoom(long date, int rid) {
        super(date, rid);
    }

    @Override
    public String toString() {
        return "StartBattlegroundPrivateRoom{" +
                "date=" + date +
                ", rid=" + rid +
                '}';
    }
}
