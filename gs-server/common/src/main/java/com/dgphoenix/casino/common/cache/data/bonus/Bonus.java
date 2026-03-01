package com.abs.casino.common.cache.data.bonus;

import com.abs.casino.common.cache.data.game.BaseGameConstants;
import com.abs.casino.common.cache.BaseGameInfoTemplateCache;
import com.abs.casino.common.exception.CommonException;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.*;

public class Bonus extends BaseBonus<Bonus> {
    private static final byte VERSION = 6;
    private static final Logger LOG = LogManager.getLogger(Bonus.class);
    private BonusType type;
    private long amount;
    private double rolloverMultiplier;
    // Fractional part of super.betSum. BetSum is a rollover and now it may be incremented not only by an entire bet
    // but also by some percent( gameInfo.getProperty(BaseGameConstants.KEY_ROLLOVER_PERCENT) ) of a bet.
    private double rolloverFractionalPart;
    private boolean autoReleased;

    private BonusGameMode bonusGameMode;

    private long expirationDate;
    private long balance;

    private Long lastGameSessionId;

    private long startDate;

    private Long maxWinLimit;
    private Long bonusEndBalance;

    public Bonus() {
    }

    public Bonus(long id, long accountId, long bankId, BonusType type, long amount, double rolloverMultiplier,
                 String extId, List<Long> gameIds, String description, String comment, long expirationDate,
                 long timeAwarded, Long endTime, long balance, long betSum, BonusStatus status,
                 BonusGameMode bonusGameMode, boolean autoReleased, Long startDate, Long maxWinLimit) {
        this(id, accountId, bankId, type, amount, rolloverMultiplier, extId, gameIds, description,
                comment, expirationDate, timeAwarded, endTime, balance,
                betSum, status, bonusGameMode, null, autoReleased, startDate, maxWinLimit);
    }

    public Bonus(long id, long accountId, long bankId, BonusType type, long amount, double rolloverMultiplier,
                 String extId, List<Long> gameIds, String description, String comment, long expirationDate,
                 long timeAwarded, Long endTime, long balance, long betSum, BonusStatus status,
                 BonusGameMode bonusGameMode, Long massAwardId, boolean autoReleased, Long startDate,
                 Long maxWinLimit) {
        super();
        this.id = id;
        this.accountId = accountId;
        this.bankId = bankId;
        this.type = type;
        this.amount = amount;
        this.rolloverMultiplier = rolloverMultiplier;
        this.extId = extId;
        this.gameIds = (gameIds != null) ? new ArrayList(gameIds) : null;
        this.description = description;
        this.comment = comment;
        this.expirationDate = expirationDate;
        this.timeAwarded = timeAwarded;
        this.endTime = endTime;
        this.balance = balance;
        this.betSum = betSum;
        this.status = status;
        this.bonusGameMode = bonusGameMode;
        this.massAwardId = massAwardId;
        this.autoReleased = autoReleased;
        this.startDate = startDate != null ? startDate : System.currentTimeMillis();
        this.maxWinLimit = maxWinLimit;
    }

    //copy constructor
    private Bonus(long id, long accountId, long bankId, BonusType type, long amount, double rolloverMultiplier,
                  String extId, List<Long> gameIds, String description, String comment, long expirationDate,
                  long timeAwarded, Long endTime, long balance, long betSum, BonusStatus status,
                  BonusGameMode bonusGameMode, boolean internal, long version, double rolloverFractionalPart,
                  boolean autoReleased, long startDate, Long maxWinLimit) {
        super();
        this.id = id;
        this.accountId = accountId;
        this.bankId = bankId;
        this.type = type;
        this.amount = amount;
        this.rolloverMultiplier = rolloverMultiplier;
        this.extId = extId;
        this.gameIds = (gameIds != null) ? new ArrayList(gameIds) : null;
        this.description = description;
        this.comment = comment;
        this.expirationDate = expirationDate;
        this.timeAwarded = timeAwarded;
        this.endTime = endTime;
        this.balance = balance;
        this.betSum = betSum;
        this.status = status;
        this.bonusGameMode = bonusGameMode;
        this.internal = internal;
        this.version = version;
        this.rolloverFractionalPart = rolloverFractionalPart;
        this.autoReleased = autoReleased;
        this.startDate = startDate;
        this.maxWinLimit = maxWinLimit;
    }

    public Bonus copy() {
        return new Bonus(id, accountId, bankId, type, amount, rolloverMultiplier, extId, gameIds, description,
                comment, expirationDate, timeAwarded, endTime, balance, betSum, status, bonusGameMode,
                internal, version, rolloverFractionalPart, autoReleased, startDate, maxWinLimit);
    }

    public boolean isGameIdIncluded(Long gameId, Collection<Long> fullList) {
        return getValidGameIds(fullList).contains(gameId);
    }

    public boolean isExpired() {
        return expirationDate < System.currentTimeMillis();
    }

    public boolean isReadyToRelease() {
        return amount * rolloverMultiplier <= betSum + rolloverFractionalPart;
    }

    public long getAmountToRelease() {
        return Math.round(amount * rolloverMultiplier - (betSum + rolloverFractionalPart));
    }

    public boolean isInternal() {
        return internal;
    }

    public void setInternal(boolean internal) {
        this.internal = internal;
    }

    public long getId() {
        return id;
    }

    public void incrementVersion() {
        this.version++;
    }

    public long getAccountId() {
        return accountId;
    }

    public long getBankId() {
        return bankId;
    }

    public BonusType getType() {
        return type;
    }

    public long getAmount() {
        return amount;
    }

    public double getRolloverMultiplier() {
        return rolloverMultiplier;
    }

    public double getRolloverFractionalPart() {
        return rolloverFractionalPart;
    }

    public void setRolloverFractionalPart(double rolloverFractionalPart) {
        this.rolloverFractionalPart = rolloverFractionalPart;
    }

    public String getExtId() {
        return extId;
    }

    public List<Long> getGameIds() {
        return gameIds;
    }

    @Override
    public Collection<Long> getValidGameIds(Collection<Long> fullList) {
        List<Long> validGameIds = new ArrayList<>();
        Set<Long> actionGames = BaseGameInfoTemplateCache.getInstance().getMultiplayerGames();

        if (gameIds != null && !gameIds.isEmpty()) {
            validGameIds.addAll(fullList);
            if (bonusGameMode.equals(BonusGameMode.ALL) || bonusGameMode.equals(BonusGameMode.ONLY)) {
                validGameIds.retainAll(gameIds);
            } else if (bonusGameMode.equals(BonusGameMode.EXCEPT)) {
                validGameIds.removeAll(gameIds);
            }

            boolean isActionMode = gameIds.stream().anyMatch(actionGames::contains);
            if (isActionMode) {
                validGameIds.retainAll(actionGames);
            } else {
                validGameIds.removeAll(actionGames);
            }
        } else {
            if (bonusGameMode.equals(BonusGameMode.ALL)) {
                validGameIds.addAll(fullList);
                validGameIds.removeAll(actionGames);
            }
        }

        return validGameIds;
    }

    public boolean addGameId(Long gameId) {
        if (bonusGameMode.equals(BonusGameMode.ONLY)) {
            if (!gameIds.contains(gameId)) {
                gameIds.add(gameId);
                return true;
            }
        } else if (bonusGameMode.equals(BonusGameMode.EXCEPT) && gameIds != null && gameIds.contains(gameId)) {
            gameIds.remove(gameId);
            return true;
        }
        return false;
    }

    public String getDescription() {
        return description;
    }

    public String getComment() {
        return comment;
    }

    public long getExpirationDate() {
        return expirationDate;
    }

    public long getTimeAwarded() {
        return timeAwarded;
    }

    public Long getEndTime() {
        return endTime;
    }

    public long getBalance() {
        return balance;
    }

    public long getBetSum() {
        return betSum;
    }

    public BonusStatus getStatus() {
        return status;
    }

    public BonusGameMode getBonusGameMode() {
        return bonusGameMode;
    }

    public void setBonusGameMode(BonusGameMode bonusGameMode) {
        this.bonusGameMode = bonusGameMode;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public void setBankId(long bankId) {
        this.bankId = bankId;
    }

    public void setType(BonusType type) {
        this.type = type;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public void setRolloverMultiplier(double rolloverMultiplier) {
        this.rolloverMultiplier = rolloverMultiplier;
    }

    public void setExtId(String extId) {
        this.extId = extId;
    }

    public void setGameIds(List<Long> gameIds) {
        this.gameIds = gameIds;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setExpirationDate(long expirationDate) {
        this.expirationDate = expirationDate;
    }

    public void setTimeAwarded(long timeAwarded) {
        this.timeAwarded = timeAwarded;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    @Override
    public void setStatusAndEndTime(BonusStatus status, Long endTime) {
        super.setStatusAndEndTime(status, status == BonusStatus.EXPIRED ? expirationDate : endTime);
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public void setBetSum(long betSum) {
        this.betSum = betSum;
    }

    public void setStatus(BonusStatus status) {
        this.status = status;
    }

    public Long getLastGameSessionId() {
        return lastGameSessionId;
    }

    public void setLastGameSessionId(Long lastGameSessionId) {
        this.lastGameSessionId = lastGameSessionId;
    }

    public void incrementBalance(long delta, boolean silently) throws CommonException {
        LOG.debug("incrementBalance bonusId:" + id + " currentBalance:" +
                this.balance + " delta:" + delta + " thread:" + Thread.currentThread().getId());
        if (!silently && balance + delta < 0) {
            throw new CommonException("Balance cannot be negative, current=" + balance + ", delta=" + delta);
        }
        this.balance += delta;
    }

    public void incrementBetSum(long delta) throws CommonException {
        LOG.debug("Bonus::incrementBetSum bonusId:" + id + " currentBalance:" +
                this.betSum + " delta:" + delta + " thread:" + Thread.currentThread().getId());
        this.betSum += delta;
    }

    public boolean isAutoReleased() {
        return autoReleased;
    }

    public boolean isReady() {
        return startDate < System.currentTimeMillis();
    }

    public long getStartDate() {
        return startDate;
    }

    public Long getMaxWinLimit() {
        return maxWinLimit;
    }

    public void setMaxWinLimit(Long maxWinLimit) {
        this.maxWinLimit = maxWinLimit;
    }

    public Long getBonusEndBalance() {
        return bonusEndBalance;
    }

    public void setBonusEndBalance(Long bonusEndBalance) {
        this.bonusEndBalance = bonusEndBalance;
    }

    @Override
    public String toString() {

        final StringBuilder sb = new StringBuilder();
        sb.append("Bonus");
        sb.append("[id=").append(id);
        sb.append(", version=").append(getVersion());
        sb.append(", accountId='").append(accountId).append('\'');
        sb.append(", bankId='").append(bankId).append('\'');
        sb.append(", type='").append(type).append('\'');
        sb.append(", amount=").append(amount);
        sb.append(", rolloverMultiplier=").append(rolloverMultiplier);
        sb.append(", extId=").append(extId);
        sb.append(", gameIds=").append(gameIds);
        sb.append(", description=").append(description);
        sb.append(", comment=").append(comment);
        sb.append(", expirationDate=").append(new Date(expirationDate));
        sb.append(", timeAwarded=").append(new Date(timeAwarded));
        sb.append(", endTime=").append((getEndTime() != null) ? new Date(endTime) : "");
        sb.append(", balance=").append(balance);
        sb.append(", betSum=").append(betSum);
        sb.append(", rolloverFractionalPart=").append(rolloverFractionalPart);
        sb.append(", status=").append(status);
        sb.append(", bonusGameMode=").append(bonusGameMode);
        sb.append(", internal=").append(internal);
        sb.append(", autoReleased=").append(autoReleased);
        sb.append(", startDate=").append(new Date(startDate));
        sb.append(", maxWinLimit=").append(maxWinLimit);
        sb.append(", bonusEndBalance=").append(bonusEndBalance);
        sb.append(']');
        return sb.toString();
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        super.write(kryo, output);
        output.writeString(type.name());
        output.writeLong(amount, true);
        output.writeDouble(rolloverMultiplier);
        output.writeString(bonusGameMode.name());
        output.writeLong(expirationDate, true);
        output.writeLong(balance, true);
        output.writeLong(getVersion(), true);
        kryo.writeObjectOrNull(output, lastGameSessionId, Long.class);
        output.writeDouble(rolloverFractionalPart);
        output.writeBoolean(autoReleased);
        output.writeLong(startDate, true);
        kryo.writeObjectOrNull(output, maxWinLimit, Long.class);
        kryo.writeObjectOrNull(output, bonusEndBalance, Long.class);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte ver = input.readByte();
        super.read(kryo, input);
        type = BonusType.valueOf(input.readString());
        amount = input.readLong(true);
        rolloverMultiplier = input.readDouble();
        bonusGameMode = BonusGameMode.valueOf(input.readString());
        expirationDate = input.readLong(true);
        balance = input.readLong(true);
        if (ver >= 1) {
            setVersion(input.readLong(true));
        }
        if (ver >= 2) {
            setLastGameSessionId(kryo.readObjectOrNull(input, Long.class));
        }
        if (ver >= 3) {
            rolloverFractionalPart = input.readDouble();
        }
        autoReleased = ver < 4 || input.readBoolean();
        if (ver >= 5) {
            startDate = input.readLong(true);
        }
        if (ver >= 6) {
            maxWinLimit = kryo.readObjectOrNull(input, Long.class);
            bonusEndBalance = kryo.readObjectOrNull(input, Long.class);
        }
    }

    @Override
    protected void serializeAdditional(JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
        gen.writeStringField("type", type.name());
        gen.writeNumberField("amount", amount);
        gen.writeNumberField("rolloverMultiplier", rolloverMultiplier);
        gen.writeStringField("bonusGameMode", bonusGameMode.name());
        gen.writeNumberField("expirationDate", expirationDate);
        gen.writeNumberField("balance", balance);
        gen.writeNumberField("version", getVersion());
        serializeNumberOrNull(gen, "lastGameSessionId", lastGameSessionId);
        gen.writeNumberField("rolloverFractionalPart", rolloverFractionalPart);
        gen.writeBooleanField("autoReleased", autoReleased);
        gen.writeNumberField("startDate", startDate);
        serializeNumberOrNull(gen, "maxWinLimit", maxWinLimit);
        serializeNumberOrNull(gen, "bonusEndBalance", bonusEndBalance);
    }

    @Override
    protected void deserializeAdditional(JsonParser p, JsonNode node, DeserializationContext ctxt)
            throws IOException {
        type = BonusType.valueOf(node.get("type").textValue());
        amount = node.get("amount").longValue();
        rolloverMultiplier = node.get("rolloverMultiplier").doubleValue();
        bonusGameMode = BonusGameMode.valueOf(node.get("bonusGameMode").textValue());
        expirationDate = node.get("expirationDate").longValue();
        balance = node.get("balance").longValue();
        setVersion(node.get("version").longValue());
        setLastGameSessionId(deserializeOrNull((ObjectMapper)p.getCodec(), node.get("lastGameSessionId"), Long.class));
        rolloverFractionalPart = node.get("rolloverFractionalPart").longValue();
        autoReleased = node.get("autoReleased").booleanValue();
        startDate = node.get("startDate").longValue();
        maxWinLimit = deserializeOrNull((ObjectMapper)p.getCodec(), node.get("maxWinLimit"), Long.class);
        bonusEndBalance = deserializeOrNull((ObjectMapper)p.getCodec(), node.get("bonusEndBalance"), Long.class);
    }

    @Override
    protected Bonus getDeserialize() {
        return this;
    }
}
