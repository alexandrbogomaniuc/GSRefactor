package com.abs.casino.common.promo.messages.server.notifications.prizes;


import com.abs.casino.gs.singlegames.tools.cbservtools.response.ServerMessage;

public class InstantMoneyWon extends PrizeWonNotification {
    private long amount;

    public InstantMoneyWon(long amount) {
        this.amount = amount;
    }

    public long getAmount() {
        return amount;
    }

    @Override
    public String httpFormat() {
        return ServerMessage.FIELDS_JOINER.join(super.httpFormat(), amount);
    }

    @Override
    public String toString() {
        return "InstantMoneyWon{" + super.toString() +
                ", amount=" + amount +
                "}";
    }
}
