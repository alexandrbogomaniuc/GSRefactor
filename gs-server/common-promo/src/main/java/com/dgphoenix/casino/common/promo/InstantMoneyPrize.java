package com.abs.casino.common.promo;

import com.abs.casino.common.promo.IMoneyPrize;
import com.abs.casino.common.promo.messages.server.notifications.prizes.InstantMoneyWon;
import com.abs.casino.common.promo.messages.server.notifications.prizes.PrizeWonNotification;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * User: flsh
 * Date: 21.11.16.
 */
public class InstantMoneyPrize extends AbstractPrize implements IMoneyPrize {
    private static final byte VERSION = 0;

    protected long amount;

    private InstantMoneyPrize() {
    }

    public InstantMoneyPrize(long amount) {
        this.amount = amount;
    }

    @Override
    public PrizeWonNotification getWonMessage() {
        return new InstantMoneyWon(amount);
    }

    @Override
    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        super.baseWrite(kryo, output);
        output.writeLong(amount, true);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte ver = input.readByte();
        super.baseRead(kryo, input);
        amount = input.readLong(true);
    }

    @Override
    public String toString() {
        return "InstantMoneyPrize[" +
                "id=" + id +
                ", limitPerPlayerOnPeriod=" + limitPerPlayerOnPeriod +
                ", limitTotalCountPerPlayer=" + limitTotalCountPerPlayer +
                ", limitPeriodInSeconds=" + limitPeriodInSeconds +
                ", totalAwardedCountForAllPlayers=" + totalAwardedCountForAllPlayers +
                ", prizeQualifier=" + prizeQualifier +
                ", eventQualifier=" + eventQualifier +
                ", amount=" + amount +
                ']';
    }
}
