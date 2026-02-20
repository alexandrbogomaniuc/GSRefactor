package com.betsoft.casino.mp.model;

import java.io.IOException;
import java.io.Serializable;

import com.dgphoenix.casino.common.cache.JsonSelfSerializable;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * User: flsh
 * Date: 14.07.2020.
 */
public class ActiveCashBonusSession implements IActiveCashBonusSession, 
        KryoSerializable, JsonSelfSerializable<ActiveCashBonusSession>, Serializable {
    private static final byte VERSION = 0;

    private long id;
    private long accountId;
    private long awardDate;
    private long expirationDate;
    private long balance;
    private long amount;
    private long betSum;
    private double rolloverMultiplier;
    private String status;
    private long maxWinLimit;

    public ActiveCashBonusSession() {}

    public ActiveCashBonusSession(long id, long accountId, long awardDate, long expirationDate, long balance,
                                  long amount, long betSum, double rolloverMultiplier, String status,
                                  long maxWinLimit) {
        this.id = id;
        this.accountId = accountId;
        this.awardDate = awardDate;
        this.expirationDate = expirationDate;
        this.balance = balance;
        this.amount = amount;
        this.betSum = betSum;
        this.rolloverMultiplier = rolloverMultiplier;
        this.status = status;
        this.maxWinLimit = maxWinLimit;
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    @Override
    public long getAwardDate() {
        return awardDate;
    }

    public void setAwardDate(long awardDate) {
        this.awardDate = awardDate;
    }

    @Override
    public long getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(long expirationDate) {
        this.expirationDate = expirationDate;
    }

    @Override
    public long getBalance() {
        return balance;
    }

    @Override
    public void setBalance(long balance) {
        this.balance = balance;
    }

    @Override
    public long getAmount() {
        return amount;
    }

    @Override
    public void setAmount(long amount) {
        this.amount = amount;
    }

    @Override
    public long getAmountToRelease() {
        return Math.round(amount * rolloverMultiplier - betSum);
    }

    @Override
    public long getBetSum() {
        return betSum;
    }

    @Override
    public void setBetSum(long betSum) {
        this.betSum = betSum;
    }

    @Override
    public void incrementBetSum(long bet) {
        this.betSum += bet;
    }

    @Override
    public double getRolloverMultiplier() {
        return rolloverMultiplier;
    }

    public void setRolloverMultiplier(double rolloverMultiplier) {
        this.rolloverMultiplier = rolloverMultiplier;
    }

    @Override
    public String getStatus() {
        return status;
    }

    @Override
    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(getStatus());
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public long getMaxWinLimit() {
        return maxWinLimit;
    }

    public void setMaxWinLimit(long maxWinLimit) {
        this.maxWinLimit = maxWinLimit;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeLong(id, true);
        output.writeLong(accountId, true);
        output.writeLong(awardDate, true);
        output.writeLong(expirationDate, true);
        output.writeLong(balance, true);
        output.writeLong(amount, true);
        output.writeLong(betSum, true);
        output.writeDouble(rolloverMultiplier);
        output.writeString(status);
        output.writeLong(maxWinLimit, true);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte version = input.readByte();
        id = input.readLong(true);
        accountId = input.readLong(true);
        awardDate = input.readLong(true);
        expirationDate = input.readLong(true);
        balance = input.readLong(true);
        amount = input.readLong(true);
        betSum = input.readLong(true);
        rolloverMultiplier = input.readDouble();
        status = input.readString();
        maxWinLimit = input.readLong(true);
    }

    @Override
    public void serializeObject(JsonGenerator gen, SerializerProvider serializers) throws IOException {
        

        gen.writeNumberField("id", id);
        gen.writeNumberField("accountId", accountId);
        gen.writeNumberField("awardDate", awardDate);
        gen.writeNumberField("expirationDate", expirationDate);
        gen.writeNumberField("balance", balance);
        gen.writeNumberField("amount", amount);
        gen.writeNumberField("betSum", betSum);
        gen.writeNumberField("rolloverMultiplier", rolloverMultiplier);
        gen.writeStringField("status", status);
        gen.writeNumberField("maxWinLimit", maxWinLimit);


    }

    @Override
    public ActiveCashBonusSession deserializeObject(JsonParser p, DeserializationContext ctxt)
            throws IOException {
        JsonNode n = p.getCodec().readTree(p);

        id = n.get("id").asLong();
        accountId = n.get("accountId").longValue();
        awardDate = n.get("awardDate").longValue();
        expirationDate = n.get("expirationDate").asLong();
        balance = n.get("balance").longValue();
        amount = n.get("amount").longValue();
        betSum = n.get("betSum").longValue();
        rolloverMultiplier = n.get("rolloverMultiplier").doubleValue();
        status = readNullableText(n, "status");
        maxWinLimit = n.get("maxWinLimit").longValue();

        return this;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ActiveCashBonusSession [");
        sb.append("id=").append(id);
        sb.append(", accountId=").append(accountId);
        sb.append(", awardDate=").append(awardDate);
        sb.append(", expirationDate=").append(expirationDate);
        sb.append(", balance=").append(balance);
        sb.append(", amount=").append(amount);
        sb.append(", betSum=").append(betSum);
        sb.append(", rolloverMultiplier=").append(rolloverMultiplier);
        sb.append(", status=").append(status);
        sb.append(", maxWinLimit=").append(maxWinLimit);
        sb.append(", amountToRelease=").append(getAmountToRelease());
        sb.append(']');
        return sb.toString();
    }
}
