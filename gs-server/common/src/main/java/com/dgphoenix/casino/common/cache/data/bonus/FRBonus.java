package com.abs.casino.common.cache.data.bonus;

import com.abs.casino.common.cache.BankInfoCache;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class FRBonus extends BaseBonus<FRBonus> {
    private static final byte VERSION = 3;
    private static final Logger LOG = LogManager.getLogger(FRBonus.class);
    private long roundsLeft;
    private long rounds;
    private long winSum;
    private Long startDate;
    private Long expirationDate;
    private Long dateOfFirstUse = null;
    // Number of days the Free round should be available after the player have entered the Free round bonus
    private Long freeRoundValidity;
    private long frbTableRoundChips;
    private Long coinValue;
    private Long maxWinLimit;


    public FRBonus() {
    }

    public FRBonus(long id, long accountId, long bankId, String extId, String description, String comment,
                   List<Long> gameIds, long betSum, long timeAwarded, Long endTime,
                   BonusStatus status, boolean internal, Long massAwardId, long roundsLeft, long rounds, long winSum,
                   Long startDate, Long expirationDate, Long dateOfFirstUse, Long freeRoundValidity,
                   Long frbTableRoundChips) {
        super(id, accountId, bankId, extId, description, comment, gameIds, betSum, timeAwarded, endTime, status,
                internal, massAwardId);
        this.roundsLeft = roundsLeft;
        this.rounds = rounds;
        this.winSum = winSum;
        this.startDate = startDate;
        this.expirationDate = expirationDate;
        this.dateOfFirstUse = dateOfFirstUse;
        this.freeRoundValidity = freeRoundValidity;
        this.frbTableRoundChips = frbTableRoundChips == null ? 0 : frbTableRoundChips;
    }

    public FRBonus(long id, long accountId, long bankId, long rounds, long roundsLeft,
                   String extId, List<Long> gameIds, String comment, String description,
                   long betSum, long winSum, long timeAwarded, BonusStatus status,
                   Long startDate, Long expirationDate, Long freeRoundValidity, Long massAwardId,
                   Long frbTableRoundChips, Long coinValue, Long maxWinLimit) {

        this.id = id;
        this.accountId = accountId;
        this.bankId = bankId;
        this.rounds = rounds;
        this.roundsLeft = roundsLeft;
        this.extId = extId;
        this.gameIds = gameIds;
        this.betSum = betSum;
        this.winSum = winSum;
        this.timeAwarded = timeAwarded;
        this.status = status;
        this.comment = comment;
        this.description = description;

        this.startDate = startDate;
        this.expirationDate = expirationDate;
        this.freeRoundValidity = freeRoundValidity;

        this.massAwardId = massAwardId;
        this.frbTableRoundChips = frbTableRoundChips == null ? 0 : frbTableRoundChips;

        this.coinValue = coinValue;
        this.maxWinLimit = maxWinLimit;
    }

    public boolean isExpired() {

        if (expirationDate == null && freeRoundValidity == null) {
            return false;
        }

        long time = System.currentTimeMillis();

        if (expirationDate != null && time > getExpirationDate()) {
            return true;
        }

        long daysToMinMultiplier = 1;
        if (BankInfoCache.getInstance().getBankInfo(bankId).isFreeRoundValidityInMinutes()) {
            //daysToMinMultiplier = 1;
        } else if (BankInfoCache.getInstance().getBankInfo(bankId).isFreeRoundValidityInHours()) {
            daysToMinMultiplier = 60;
        } else {
            daysToMinMultiplier = 24 * 60;
        }
        return getDateOfFirstUse() != null && getFreeRoundValidity() != null &&
                (getDateOfFirstUse() + getFreeRoundValidity() * daysToMinMultiplier * 60 * 1000 < time);
    }

    public boolean isReady() {
        return startDate == null || startDate < System.currentTimeMillis();
    }

    public boolean isNewVersion() {
        return getStartDate() != null;
    }

    public Long getStartDate() {
        return startDate;
    }

    public Long getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Long expirationDate) {
        this.expirationDate = expirationDate;
    }

    public Long getDateOfFirstUse() {
        return dateOfFirstUse;
    }

    public Long getFreeRoundValidity() {
        return freeRoundValidity;
    }

    public synchronized void setDateOfFirstUse(Long dateOfFirstUse) {
        if (this.dateOfFirstUse == null) {
            this.dateOfFirstUse = dateOfFirstUse;
        }
    }

    @Override
    public void setStatusAndEndTime(BonusStatus status, Long endTime) {
        Long finalEndTime = endTime;
        if (status == BonusStatus.EXPIRED && expirationDate != null) {
            finalEndTime = expirationDate;
        }

        super.setStatusAndEndTime(status, finalEndTime);
    }

    @Override
    public Collection<Long> getValidGameIds(Collection<Long> fullList) {
        return new ArrayList<>(gameIds);
    }

    public long getRoundsLeft() {
        return roundsLeft;
    }

    public synchronized void setRoundsLeft(long roundsLeft) {
        this.roundsLeft = roundsLeft;
    }

    public long getRounds() {
        return rounds;
    }

    public synchronized void setRounds(long rounds) {
        this.rounds = rounds;
    }

    public synchronized void incrementRoundsAndRoundsLeft(long incValue) {
        this.rounds += incValue;
        this.roundsLeft += incValue;
    }

    public synchronized void incrementVersion() {
        this.version++;
    }

    public synchronized void decrementRoundsLeft() {
        if (roundsLeft > 0) --roundsLeft;
    }

    public long getWinSum() {
        return winSum;
    }

    public synchronized void setWinSum(long winSum) {
        this.winSum = winSum;
    }

    public synchronized void incrementWinSum(long delta) throws CommonException {
        LOG.debug("FRBonus::incrementWinSum frbonusId:" + getId() + " currentBalance:" +
                getWinSum() + " delta:" + delta + " thread:" + Thread.currentThread().getId());
        this.winSum += delta;
    }

    public long getFrbTableRoundChips() {
        return frbTableRoundChips;
    }

    public Long getCoinValue() {
        return coinValue;
    }

    public void setCoinValue(Long coinValue) {
        this.coinValue = coinValue;
    }

    public Long getMaxWinLimit() {
        return maxWinLimit;
    }

    public void setMaxWinLimit(Long maxWinLimit) {
        this.maxWinLimit = maxWinLimit;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("FRBonus");
        sb.append("[id=").append(getId());
        sb.append(", version=").append(getVersion());
        sb.append(", accountId='").append(getAccountId()).append('\'');
        sb.append(", bankId='").append(getBankId()).append('\'');
        sb.append(", rounds='").append(rounds).append('\'');
        sb.append(", roundsLeft='").append(roundsLeft).append('\'');
        sb.append(", extId=").append(getExtId());
        sb.append(", gameIds=").append(getGameIds());
        sb.append(", timeAwarded=").append(new Date(getTimeAwarded()));
        sb.append(", betSum=").append(getBetSum());
        sb.append(", winSum=").append(getWinSum());
        sb.append(", status=").append(getStatus());
        sb.append(", internal=").append(isInternal());
        sb.append(", description=").append(description);

        sb.append(", massAwardId=").append(massAwardId);

        sb.append(", startDate=").append(startDate != null ? new Date(startDate) : null);
        sb.append(", endTime=").append(endTime != null ? new Date(endTime) : null);
        sb.append(", expirationDate=").append(expirationDate != null ? new Date(expirationDate) : null);
        sb.append(", dateOfFirstUse=").append(dateOfFirstUse != null ? new Date(dateOfFirstUse) : null);
        sb.append(", freeRoundValidity=").append(freeRoundValidity);
        sb.append(", frbTableRoundChips=").append(frbTableRoundChips);

        sb.append(", comment=").append(comment);
        sb.append(", coinValue=").append(coinValue);
        sb.append(", maxWinLimit=").append(maxWinLimit);
        sb.append(']');
        return sb.toString();
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        super.write(kryo, output);
        output.writeLong(roundsLeft, true);
        output.writeLong(rounds, true);
        output.writeLong(winSum, true);
        kryo.writeObjectOrNull(output, startDate, Long.class);
        kryo.writeObjectOrNull(output, expirationDate, Long.class);
        kryo.writeObjectOrNull(output, dateOfFirstUse, Long.class);
        kryo.writeObjectOrNull(output, freeRoundValidity, Long.class);
        output.writeLong(frbTableRoundChips, true);
        output.writeLong(getVersion(), true);
        kryo.writeObjectOrNull(output, coinValue, Long.class);
        kryo.writeObjectOrNull(output, maxWinLimit, Long.class);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte ver = input.readByte();
        super.read(kryo, input);
        roundsLeft = input.readLong(true);
        rounds = input.readLong(true);
        winSum = input.readLong(true);
        startDate = kryo.readObjectOrNull(input, Long.class);
        expirationDate = kryo.readObjectOrNull(input, Long.class);
        dateOfFirstUse = kryo.readObjectOrNull(input, Long.class);
        freeRoundValidity = kryo.readObjectOrNull(input, Long.class);
        frbTableRoundChips = input.readLong(true);
        if (ver >= 1) {
            setVersion(input.readLong(true));
        }
        if (ver >= 2) {
            coinValue = kryo.readObjectOrNull(input, Long.class);
        }
        if (ver >= 3) {
            maxWinLimit = kryo.readObjectOrNull(input, Long.class);
        }
    }

    @Override
    protected void serializeAdditional(JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
        gen.writeNumberField("roundsLeft", roundsLeft);
        gen.writeNumberField("rounds", rounds);
        gen.writeNumberField("winSum", winSum);
        serializeNumberOrNull(gen, "startDate", startDate);
        serializeNumberOrNull(gen, "expirationDate", expirationDate);
        serializeNumberOrNull(gen, "dateOfFirstUse", dateOfFirstUse);
        serializeNumberOrNull(gen, "freeRoundValidity", freeRoundValidity);
        gen.writeNumberField("frbTableRoundChips", frbTableRoundChips);
        gen.writeNumberField("version", getVersion());
        serializeNumberOrNull(gen, "coinValue", coinValue);
        serializeNumberOrNull(gen, "maxWinLimit", maxWinLimit);
    }

    @Override
    protected void deserializeAdditional(JsonParser p, JsonNode node, DeserializationContext ctxt)
            throws IOException {
        roundsLeft = node.get("roundsLeft").longValue();
        rounds = node.get("rounds").longValue();
        winSum = node.get("winSum").longValue();
        startDate = deserializeOrNull((ObjectMapper)p.getCodec(), node.get("startDate"), Long.class);
        expirationDate = deserializeOrNull((ObjectMapper)p.getCodec(), node.get("expirationDate"), Long.class);
        dateOfFirstUse = deserializeOrNull((ObjectMapper)p.getCodec(), node.get("dateOfFirstUse"), Long.class);
        freeRoundValidity = deserializeOrNull((ObjectMapper)p.getCodec(), node.get("freeRoundValidity"), Long.class);
        frbTableRoundChips = node.get("frbTableRoundChips").longValue();
        setVersion(node.get("version").longValue());
        coinValue = deserializeOrNull((ObjectMapper)p.getCodec(), node.get("coinValue"), Long.class);
        maxWinLimit = deserializeOrNull((ObjectMapper)p.getCodec(), node.get("maxWinLimit"), Long.class);
    }

    @Override
    protected FRBonus getDeserialize() {
        return this;
    }
}
