package com.dgphoenix.casino.common.promo;

import com.abs.casino.common.promo.messages.server.notifications.prizes.FRBWon;
import com.abs.casino.common.promo.messages.server.notifications.prizes.PrizeWonNotification;
import com.dgphoenix.casino.common.util.CollectionUtils;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.List;

/**
 * User: flsh
 * Date: 21.11.16.
 */
public class FRBonusPrize extends AbstractPrize {
    private static final byte VERSION = 0;

    private int rounds;
    private Long freeRoundValidity;
    private long frbTableRoundChips;
    private Long startDate = null;
    private Long expirationDate = null;
    private List<Long> gameIds;

    private FRBonusPrize() {
    }

    public FRBonusPrize(int rounds, Long freeRoundValidity, long frbTableRoundChips, Long startDate,
                        Long expirationDate, List<Long> gameIds) {
        this.rounds = rounds;
        this.freeRoundValidity = freeRoundValidity;
        this.frbTableRoundChips = frbTableRoundChips;
        this.startDate = startDate;
        this.expirationDate = expirationDate;
        this.gameIds = gameIds;
    }

    @Override
    public PrizeWonNotification getWonMessage() {
        return new FRBWon();
    }

    public int getRounds() {
        return rounds;
    }

    public void setRounds(int rounds) {
        this.rounds = rounds;
    }

    public Long getFreeRoundValidity() {
        return freeRoundValidity;
    }

    public void setFreeRoundValidity(Long freeRoundValidity) {
        this.freeRoundValidity = freeRoundValidity;
    }

    public long getFrbTableRoundChips() {
        return frbTableRoundChips;
    }

    public void setFrbTableRoundChips(long frbTableRoundChips) {
        this.frbTableRoundChips = frbTableRoundChips;
    }

    public Long getStartDate() {
        return startDate;
    }

    public void setStartDate(Long startDate) {
        this.startDate = startDate;
    }

    public Long getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Long expirationDate) {
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
        output.writeInt(rounds, true);
        kryo.writeObjectOrNull(output, freeRoundValidity, Long.class);
        output.writeLong(frbTableRoundChips, true);
        kryo.writeObjectOrNull(output, startDate, Long.class);
        kryo.writeObjectOrNull(output, expirationDate, Long.class);
        output.writeString(CollectionUtils.listOfLongsToString(gameIds));
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte ver = input.readByte();
        super.baseRead(kryo, input);
        rounds = input.readInt(true);
        freeRoundValidity = kryo.readObjectOrNull(input, Long.class);
        frbTableRoundChips = input.readLong(true);
        startDate = kryo.readObjectOrNull(input, Long.class);
        expirationDate = kryo.readObjectOrNull(input, Long.class);
        gameIds = CollectionUtils.stringToListOfLongs(input.readString());
    }

    @Override
    public String toString() {
        return "FRBonusPrize[" +
                "id=" + id +
                ", limitPerPlayerOnPeriod=" + limitPerPlayerOnPeriod +
                ", limitTotalCountPerPlayer=" + limitTotalCountPerPlayer +
                ", limitPeriodInSeconds=" + limitPeriodInSeconds +
                ", totalAwardedCountForAllPlayers=" + totalAwardedCountForAllPlayers +
                ", prizeQualifier=" + prizeQualifier +
                ", eventQualifier=" + eventQualifier +
                ", rounds=" + rounds +
                ", freeRoundValidity=" + freeRoundValidity +
                ", frbTableRoundChips=" + frbTableRoundChips +
                ", startDate=" + startDate +
                ", expirationDate=" + expirationDate +
                ", gameIds=" + CollectionUtils.listOfLongsToString(gameIds) +
                ']';
    }
}
