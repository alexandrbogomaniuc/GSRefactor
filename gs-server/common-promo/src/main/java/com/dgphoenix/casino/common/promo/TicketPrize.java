package com.abs.casino.common.promo;

import com.abs.casino.common.promo.IHighFrequencyPrize;
import com.dgphoenix.casino.common.promo.messages.server.notifications.prizes.PrizeWonNotification;
import com.dgphoenix.casino.common.promo.messages.server.notifications.prizes.TicketsWon;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * User: flsh
 * Date: 21.11.16.
 */
public class TicketPrize extends AbstractPrize implements IHighFrequencyPrize {
    private static final byte VERSION = 0;

    public TicketPrize() {
        super();
    }

    @Override
    public PrizeWonNotification getWonMessage() {
        return new TicketsWon(1);
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        super.baseWrite(kryo, output);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte ver = input.readByte();
        super.baseRead(kryo, input);
    }

    @Override
    public String toString() {
        return "TicketPrize[" +
                "id=" + id +
                ", limitPerPlayerOnPeriod=" + limitPerPlayerOnPeriod +
                ", limitTotalCountPerPlayer=" + limitTotalCountPerPlayer +
                ", limitPeriodInSeconds=" + limitPeriodInSeconds +
                ", totalAwardedCountForAllPlayers=" + totalAwardedCountForAllPlayers +
                ", prizeQualifier=" + prizeQualifier +
                ", eventQualifier=" + eventQualifier+
                ']';
    }
}
