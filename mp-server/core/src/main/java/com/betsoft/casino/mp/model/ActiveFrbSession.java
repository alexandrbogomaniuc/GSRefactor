package com.betsoft.casino.mp.model;

import com.dgphoenix.casino.common.cache.JsonSelfSerializable;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.io.Serializable;

/**
 * User: flsh
 * Date: 05.08.18.
 */
public class ActiveFrbSession implements IActiveFrbSession, 
        KryoSerializable, JsonSelfSerializable<ActiveFrbSession>, Serializable {
    private static final byte VERSION = 0;

    private long bonusId;
    private long accountId;
    private long awardDate;
    private long startDate;
    private Long expirationDate;
    private int startAmmoAmount;
    private int currentAmmoAmount;
    private long winSum;
    private long stake;
    private String status;
    private long maxWinLimit;

    public ActiveFrbSession() {}

    public ActiveFrbSession(long bonusId, long accountId, long awardDate, long startDate, Long expirationDate,
                            int startAmmoAmount, int currentAmmoAmount, long winSum, long stake, String status,
                            long maxWinLimit) {
        this.bonusId = bonusId;
        this.accountId = accountId;
        this.awardDate = awardDate;
        this.startDate = startDate;
        this.expirationDate = expirationDate;
        this.startAmmoAmount = startAmmoAmount;
        this.currentAmmoAmount = currentAmmoAmount;
        this.winSum = winSum;
        this.stake = stake;
        this.status = status;
        this.maxWinLimit = maxWinLimit;
    }

    @Override
    public long getBonusId() {
        return bonusId;
    }

    @Override
    public void setBonusId(long bonusId) {
        this.bonusId = bonusId;
    }

    @Override
    public long getAccountId() {
        return accountId;
    }

    @Override
    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    @Override
    public long getAwardDate() {
        return awardDate;
    }

    @Override
    public void setAwardDate(long awardDate) {
        this.awardDate = awardDate;
    }

    @Override
    public long getStartDate() {
        return startDate;
    }

    @Override
    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    @Override
    public Long getExpirationDate() {
        return expirationDate;
    }

    @Override
    public void setExpirationDate(Long expirationDate) {
        this.expirationDate = expirationDate;
    }

    @Override
    public int getStartAmmoAmount() {
        return startAmmoAmount;
    }

    @Override
    public void setStartAmmoAmount(int startAmmoAmount) {
        this.startAmmoAmount = startAmmoAmount;
    }

    @Override
    public int getCurrentAmmoAmount() {
        return currentAmmoAmount;
    }

    @Override
    public void setCurrentAmmoAmount(int currentAmmoAmount) {
        this.currentAmmoAmount = currentAmmoAmount;
    }

    @Override
    public long getWinSum() {
        return winSum;
    }

    @Override
    public void setWinSum(long winSum) {
        this.winSum = winSum;
    }

    @Override
    public void incrementWinSum(long delta) {
        this.winSum += delta;
    }

    public long getMaxWinLimit() {
        return maxWinLimit;
    }

    public void setMaxWinLimit(long maxWinLimit) {
        this.maxWinLimit = maxWinLimit;
    }

    @Override
    public void decrementWinSum(long delta) {
        this.winSum -= delta;
    }

    @Override
    public long getStake() {
        return stake;
    }

    @Override
    public void setStake(long stake) {
        this.stake = stake;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ActiveFRBonus [");
        sb.append("bonusId=").append(bonusId);
        sb.append(", accountId=").append(accountId);
        sb.append(", awardDate=").append(awardDate);
        sb.append(", startDate=").append(startDate);
        sb.append(", expirationDate=").append(expirationDate);
        sb.append(", startAmmoAmount=").append(startAmmoAmount);
        sb.append(", currentAmmoAmount=").append(currentAmmoAmount);
        sb.append(", winSum=").append(winSum);
        sb.append(", stake=").append(stake);
        sb.append(", status=").append(status);
        sb.append(", maxWinLimit=").append(maxWinLimit);
        sb.append(']');
        return sb.toString();
    }

    @Override
    public String getStatus() {
        return status;
    }

    @Override
    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeLong(bonusId, true);
        output.writeLong(accountId, true);
        output.writeLong(awardDate, true);
        output.writeLong(startDate, true);
        kryo.writeObjectOrNull(output, expirationDate, Long.class);
        output.writeInt(startAmmoAmount, true);
        output.writeInt(currentAmmoAmount, true);
        output.writeLong(winSum, true);
        output.writeLong(stake, true);
        output.writeString(status);
        output.writeLong(maxWinLimit, true);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte version = input.readByte();
        bonusId = input.readLong(true);
        accountId = input.readLong(true);
        awardDate = input.readLong(true);
        startDate = input.readLong(true);
        expirationDate = kryo.readObjectOrNull(input, Long.class);
        startAmmoAmount = input.readInt(true);
        currentAmmoAmount = input.readInt(true);
        winSum = input.readLong(true);
        stake = input.readLong(true);
        status = input.readString();
        maxWinLimit = input.readLong(true);
    }

    @Override
    public void serializeObject(JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeNumberField("bonusId", bonusId);
        gen.writeNumberField("accountId", accountId);
        gen.writeNumberField("awardDate", awardDate);
        gen.writeNumberField("startDate", startDate);
        serializeNumberOrNull(gen, "expirationDate", expirationDate);
        gen.writeNumberField("startAmmoAmount", startAmmoAmount);
        gen.writeNumberField("currentAmmoAmount", currentAmmoAmount);
        gen.writeNumberField("winSum", winSum);
        gen.writeNumberField("stake", stake);
        gen.writeStringField("status", status);
        gen.writeNumberField("maxWinLimit", maxWinLimit);
    }

    @Override
    public ActiveFrbSession deserializeObject(JsonParser p, DeserializationContext ctxt)
            throws IOException {
        JsonNode n = p.getCodec().readTree(p);

        bonusId = n.get("bonusId").longValue();
        accountId = n.get("accountId").longValue();
        awardDate = n.get("awardDate").longValue();
        startDate = n.get("startDate").longValue();
        expirationDate = deserializeOrNull((ObjectMapper)p.getCodec(), n.get("expirationDate"), Long.class);
        startAmmoAmount = n.get("startAmmoAmount").intValue();
        currentAmmoAmount = n.get("currentAmmoAmount").intValue();
        winSum = n.get("winSum").longValue();
        stake = n.get("stake").longValue();
        status = readNullableText(n, "status");
        maxWinLimit = n.get("maxWinLimit").longValue();

        return this;
    }
}
