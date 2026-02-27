package com.abs.casino.common.promo.messages.server.notifications.prizes;

import com.dgphoenix.casino.gs.singlegames.tools.cbservtools.response.ServerMessage;

/**
 * Created by vladislav on 12/2/16.
 */
public class TicketsWon extends PrizeWonNotification {
    private int wonTickets;
    private int totalTickets;

    public TicketsWon(int wonTickets) {
        this.wonTickets = wonTickets;
    }

    public int getWonTickets() {
        return wonTickets;
    }

    public void setWonTickets(int wonTickets) {
        this.wonTickets = wonTickets;
    }

    public int getTotalTickets() {
        return totalTickets;
    }

    public void setTotalTickets(int totalTickets) {
        this.totalTickets = totalTickets;
    }

    @Override
    public String httpFormat() {
        return ServerMessage.FIELDS_JOINER.join(super.httpFormat(), wonTickets, totalTickets);
    }

    @Override
    public String oldHttpFormat() {
        return ServerMessage.FIELDS_JOINER.join(super.oldHttpFormat(), wonTickets, totalTickets);
    }

    @Override
    public String toString() {
        return "TicketsWon{" +
                super.toString() +
                ", wonTickets=" + wonTickets +
                ", totalTickets=" + totalTickets +
                "}";
    }
}
