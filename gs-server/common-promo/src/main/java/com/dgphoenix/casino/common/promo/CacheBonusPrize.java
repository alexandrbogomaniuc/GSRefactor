package com.abs.casino.common.promo;

import com.abs.casino.common.promo.messages.server.notifications.prizes.CacheBonusWon;
import com.abs.casino.common.promo.messages.server.notifications.prizes.PrizeWonNotification;
import com.dgphoenix.casino.common.promo.AbstractPrize;
import com.dgphoenix.casino.common.util.CollectionUtils;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.List;

/**
 * User: flsh
 * Date: 15.02.17.
 * com.dgphoenix.casino.common.cache.data.bonus.Bonus created with: BonusType.PROMO, BonusGameMode.ONLY
 */
public class CacheBonusPrize extends AbstractPrize {
    private static final byte VERSION = 0;
    private long amount;
    private double rolloverMultiplier;
    private long expirationDate;
    private List<Long> gameIds;

    private CacheBonusPrize() {
    }

    public CacheBonusPrize(long amount, double rolloverMultiplier, long expirationDate,
                           List<Long> gameIds) {
        this.amount = amount;
        this.rolloverMultiplier = rolloverMultiplier;
        this.expirationDate = expirationDate;
        this.gameIds = gameIds;
    }

    @Override
    public PrizeWonNotification getWonMessage() {
        return new CacheBonusWon();
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public double getRolloverMultiplier() {
        return rolloverMultiplier;
    }

    public void setRolloverMultiplier(double rolloverMultiplier) {
        this.rolloverMultiplier = rolloverMultiplier;
    }

    public long getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(long expirationDate) {
        this.expirationDate = expirationDate;
    }

    public List<Long> getGameIds() {
        return gameIds;
    }

    public void setGameIds(List<Long> gameIds) {
        this.gameIds = gameIds;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        super.baseWrite(kryo, output);
        output.writeLong(amount, true);
        output.writeDouble(rolloverMultiplier);
        output.writeLong(expirationDate);
        output.writeString(CollectionUtils.listOfLongsToString(gameIds));
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte ver = input.readByte();
        super.baseRead(kryo, input);
        amount = input.readLong(true);
        rolloverMultiplier = input.readDouble();
        expirationDate = input.readLong();
        gameIds = CollectionUtils.stringToListOfLongs(input.readString());
    }

    @Override
    public String toString() {
        return "CacheBonusPrize[" +
                "id=" + id +
                ", limitPerPlayerOnPeriod=" + limitPerPlayerOnPeriod +
                ", limitTotalCountPerPlayer=" + limitTotalCountPerPlayer +
                ", limitPeriodInSeconds=" + limitPeriodInSeconds +
                ", totalAwardedCountForAllPlayers=" + totalAwardedCountForAllPlayers +
                ", prizeQualifier=" + prizeQualifier +
                ", eventQualifier=" + eventQualifier +
                ", amount=" + amount +
                ", rolloverMultiplier=" + rolloverMultiplier +
                ", expirationDate=" + expirationDate +
                ", gameIds=" + gameIds +
                ']';
    }
}
