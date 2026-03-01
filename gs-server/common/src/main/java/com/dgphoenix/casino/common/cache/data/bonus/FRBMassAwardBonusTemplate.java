package com.abs.casino.common.cache.data.bonus;

import com.dgphoenix.casino.common.cache.data.bonus.BaseBonus;
import com.dgphoenix.casino.common.cache.data.bonus.BonusStatus;
import com.dgphoenix.casino.common.cache.data.bonus.FRBonus;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.List;

/**
 * User: flsh
 * Date: 11.07.13
 */
public class FRBMassAwardBonusTemplate extends MassAwardBonusTemplate {
    private static final byte VERSION = 0;
    private long rounds;
    private Long freeRoundValidity;
    private Long frbTableRoundChips;

    public FRBMassAwardBonusTemplate() {}

    public FRBMassAwardBonusTemplate(Long startDate, Long expirationDate, String description, String comment,
                                     List<Long> gameIds, long timeAwarded, long rounds, Long freeRoundValidity, Long frbTableRoundChips) {
        super(startDate, expirationDate, description, comment, gameIds, timeAwarded);

        this.rounds = rounds;
        this.freeRoundValidity = freeRoundValidity;
        this.frbTableRoundChips = frbTableRoundChips;
    }

    public long getRounds() {
        return rounds;
    }

    public Long getFreeRoundValidity() {
        return freeRoundValidity;
    }

    public synchronized void setRounds(long rounds) {
        this.rounds = rounds;
    }

    public synchronized void setFreeRoundValidity(Long freeRoundValidity) {
        this.freeRoundValidity = freeRoundValidity;
    }

    @Override
    public BaseBonus createBonus(long bonusId, long accountId, long bankId, long massAwardId, Long maxWinLimit,
                                 Double maxWinMultiplier) {
        return new FRBonus(bonusId, accountId, bankId, rounds, rounds,
                null, gameIds, comment, description, 0, 0, timeAwarded, BonusStatus.ACTIVE,
                startDate, expirationDate, freeRoundValidity, massAwardId, frbTableRoundChips, null, null);
    }

    public Long getFrbTableRoundChips() {
        return frbTableRoundChips;
    }

    public synchronized void setFrbTableRoundChips(Long frbTableRoundChips) {
        this.frbTableRoundChips = frbTableRoundChips;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        super.write(kryo, output);
        output.writeLong(rounds, true);
        kryo.writeObjectOrNull(output, freeRoundValidity, Long.class);
        kryo.writeObjectOrNull(output, frbTableRoundChips, Long.class);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte ver = input.readByte();
        super.read(kryo, input);
        rounds = input.readLong(true);
        freeRoundValidity = kryo.readObjectOrNull(input, Long.class);
        frbTableRoundChips = kryo.readObjectOrNull(input, Long.class);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("FRBMassAwardBonusTemplate");
        sb.append("[super=").append(super.toString());
        sb.append(", rounds=").append(rounds);
        sb.append(", freeRoundValidity=").append(freeRoundValidity);
        sb.append(", frbTableRoundChips=").append(frbTableRoundChips);
        sb.append(']');
        return sb.toString();
    }

}

