package com.abs.casino.common.cache.data.bonus;

import com.dgphoenix.casino.common.cache.data.bonus.BaseBonus;
import com.dgphoenix.casino.common.cache.data.bonus.Bonus;
import com.dgphoenix.casino.common.cache.data.bonus.BonusGameMode;
import com.dgphoenix.casino.common.cache.data.bonus.BonusStatus;
import com.dgphoenix.casino.common.cache.data.bonus.BonusType;
import com.abs.casino.common.cache.data.bonus.MassAwardBonusTemplate;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.List;

/**
 * User: flsh
 * Date: 11.07.13
 */
public class BonusMassAwardBonusTemplate extends MassAwardBonusTemplate {
    private static final byte VERSION = 1;
    private BonusType type;
    private long amount;
    private double rolloverMultiplier;
    private boolean autoReleased;

    private BonusGameMode bonusGameMode;

    private long balance;

    public BonusMassAwardBonusTemplate() {}

    public BonusMassAwardBonusTemplate(
            Long startDate, Long expirationDate, String description, String comment,
            List<Long> gameIds, long timeAwarded, BonusType type, long amount,
            double rolloverMultiplier, BonusGameMode bonusGameMode, long balance, boolean autoReleased
    ) {
        super(startDate, expirationDate, description, comment, gameIds, timeAwarded);
        this.type = type;
        this.amount = amount;
        this.rolloverMultiplier = rolloverMultiplier;
        this.bonusGameMode = bonusGameMode;
        this.balance = balance;
        this.autoReleased = autoReleased;
    }

    @Override
    public BaseBonus createBonus(long bonusId, long accountId, long bankId, long massAwardId, Long maxWinLimit,
                                 Double maxWinMultiplier) {
        Long bonusMaxWinLimit = null;
        if (maxWinMultiplier != null && maxWinMultiplier > 0) {
            bonusMaxWinLimit = Math.round(maxWinMultiplier * amount);
        }
        return new Bonus(bonusId, accountId, bankId, type, amount, rolloverMultiplier, null, gameIds, description,
                comment, expirationDate, timeAwarded, null, balance, 0, BonusStatus.ACTIVE,
                bonusGameMode, massAwardId, autoReleased, startDate, bonusMaxWinLimit);
    }

    public BonusType getType() {
        return type;
    }

    public synchronized void setType(BonusType type) {
        this.type = type;
    }

    public long getAmount() {
        return amount;
    }

    public synchronized void setAmount(long amount) {
        this.amount = amount;
    }

    public double getRolloverMultiplier() {
        return rolloverMultiplier;
    }

    public synchronized void setRolloverMultiplier(double rolloverMultiplier) {
        this.rolloverMultiplier = rolloverMultiplier;
    }

    public BonusGameMode getBonusGameMode() {
        return bonusGameMode;
    }

    public synchronized void setBonusGameMode(BonusGameMode bonusGameMode) {
        this.bonusGameMode = bonusGameMode;
    }

    public long getBalance() {
        return balance;
    }

    public synchronized void setBalance(long balance) {
        this.balance = balance;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        super.write(kryo, output);
        output.writeString(type == null ? null : type.name());
        output.writeLong(amount, true);
        output.writeDouble(rolloverMultiplier);
        output.writeString(bonusGameMode == null ? null : bonusGameMode.name());
        output.writeLong(balance, true);
        output.writeBoolean(autoReleased);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte ver = input.readByte();
        super.read(kryo, input);
        String s = input.readString();
        type = StringUtils.isTrimmedEmpty(s) ? null : BonusType.valueOf(s);
        amount = input.readLong(true);
        rolloverMultiplier = input.readDouble();
        s = input.readString();
        bonusGameMode = StringUtils.isTrimmedEmpty(s) ? null : BonusGameMode.valueOf(s);
        balance = input.readLong(true);
        autoReleased = ver >= 1 ? input.readBoolean() : true;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BonusMassAwardBonusTemplate ");
        sb.append("[super=").append(super.toString());
        sb.append("type=").append(type);
        sb.append(", amount=").append(amount);
        sb.append(", rolloverMultiplier=").append(rolloverMultiplier);
        sb.append(", bonusGameMode=").append(bonusGameMode);
        sb.append(", balance=").append(balance);
        sb.append(", autoReleased=").append(autoReleased);
        sb.append(']');
        return sb.toString();
    }
}
