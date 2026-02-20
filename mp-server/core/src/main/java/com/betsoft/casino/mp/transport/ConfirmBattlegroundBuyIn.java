package com.betsoft.casino.mp.transport;

import com.betsoft.casino.utils.TInboundObject;

public class ConfirmBattlegroundBuyIn extends TInboundObject {

    public ConfirmBattlegroundBuyIn(long date, int rid) {
        super(date, rid);
    }

    @Override
    public String toString() {
        return "ConfirmBattlegroundBuyIn{" +
                "date=" + date +
                ", rid=" + rid +
                '}';
    }
}
