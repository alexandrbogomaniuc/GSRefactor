package com.abs.casino.common.promo;

import java.io.IOException;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * User: flsh
 * Date: 23.11.16.
 */
public class MqEndRoundEvent extends AbstractParticipantEvent<MqEndRoundEvent> implements ICampaignStatisticsProvider {
    private static final byte VERSION = 4;
    //all amounts in cents
    private long betAmount;
    private long winAmount;
    private long purchaseSpecialWeaponAmount;
    private String currencyCode;
    private double averageBet;
    private long minBet;
    private long maxBet;
    private int maxExposure;
    private double highestWinPerSingleBet;
    private long betSum;
    private long winSum;

    private MqEndRoundEvent() {
    }

    public MqEndRoundEvent(long gameId, long eventDate, long accountId, String accountExternalId,
                           long betAmount, long winAmount, long purchaseSpecialWeaponAmount,
                           String currencyCode, double averageBet, long minBet, long maxBet, int maxExposure,
                           double highestWinPerSingleBet, long betSum, long winSum) {
        super(gameId, eventDate, accountId, accountExternalId);
        this.betAmount = betAmount;
        this.winAmount = winAmount;
        this.purchaseSpecialWeaponAmount = purchaseSpecialWeaponAmount;
        this.currencyCode = currencyCode;
        this.averageBet = averageBet;
        this.minBet = minBet;
        this.maxBet = maxBet;
        this.maxExposure = maxExposure;
        this.highestWinPerSingleBet = highestWinPerSingleBet;
        this.betSum = betSum;
        this.winSum = winSum;
    }

    @Override
    public SignificantEventType getType() {
        return SignificantEventType.MQ_END_ROUND;
    }

    public long getBetAmount() {
        return betAmount;
    }

    @Override
    public double getAverageBet() {
        return averageBet;
    }

    public long getMinBet() {
        return minBet;
    }

    public long getMaxBet() {
        return maxBet;
    }

    public Long getWinAmount() {
        return winAmount;
    }

    public long getPurchaseSpecialWeaponAmount() {
        return purchaseSpecialWeaponAmount;
    }

    public String getCurrency() {
        return currencyCode;
    }

    @Override
    public int getMaxExposure() {
        return maxExposure;
    }

    @Override
    public double getHighestWinPerSingleBet() {
        return highestWinPerSingleBet;
    }

    @Override
    public long getBetSum() {
        return betSum;
    }

    @Override
    public long getWinSum() {
        return winSum;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        baseWrite(kryo, output);
        output.writeLong(betAmount, true);
        output.writeLong(winAmount, true);
        output.writeLong(purchaseSpecialWeaponAmount, true);
        output.writeString(currencyCode);
        output.writeDouble(averageBet);
        output.writeLong(minBet, true);
        output.writeLong(maxBet, true);
        output.writeInt(maxExposure, true);
        output.writeDouble(highestWinPerSingleBet);
        output.writeLong(betSum, true);
        output.writeLong(winSum, true);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte ver = input.readByte();
        baseRead(kryo, input);
        if (ver > 1) {
            baseReadV1(kryo, input);
        }
        if (ver > 2) {
            baseReadV2(kryo, input);
        }
        betAmount = input.readLong(true);
        winAmount = input.readLong(true);
        purchaseSpecialWeaponAmount = input.readLong(true);
        currencyCode = input.readString();
        averageBet = input.readDouble();
        minBet = input.readLong(true);
        maxBet = input.readLong(true);
        maxExposure = input.readInt(true);
        if (ver > 0) {
            highestWinPerSingleBet = input.readDouble();
        }
        if (ver > 3) {
            betSum = input.readLong(true);
            winSum = input.readLong(true);
        }
    }

    @Override
    public void serializeObject(JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
        baseSerialize(gen, serializers);

        gen.writeNumberField("betAmount", betAmount);
        gen.writeNumberField("winAmount", winAmount);
        gen.writeNumberField("purchaseSpecialWeaponAmount", purchaseSpecialWeaponAmount);
        gen.writeStringField("currencyCode", currencyCode);
        gen.writeNumberField("averageBet", averageBet);
        gen.writeNumberField("minBet", minBet);
        gen.writeNumberField("maxBet", maxBet);
        gen.writeNumberField("maxExposure", maxExposure);
        gen.writeNumberField("highestWinPerSingleBet", highestWinPerSingleBet);
        gen.writeNumberField("betSum", betSum);
        gen.writeNumberField("winSum", winSum);
    }

    @Override
    public MqEndRoundEvent deserializeObject(JsonParser p, DeserializationContext ctxt)
            throws IOException {
        JsonNode node = p.getCodec().readTree(p);

        baseDeserialize(p, node, ctxt);
        betAmount = node.get("betAmount").longValue();
        winAmount = node.get("winAmount").longValue();
        purchaseSpecialWeaponAmount = node.get("purchaseSpecialWeaponAmount").asLong();
        currencyCode = readNullableText(node, "currencyCode");
        averageBet = node.get("averageBet").doubleValue();
        minBet = node.get("minBet").longValue();
        maxBet = node.get("maxBet").longValue();
        maxExposure = node.get("maxExposure").intValue();
        highestWinPerSingleBet = node.get("highestWinPerSingleBet").doubleValue();
        betSum = node.get("betSum").longValue();
        winSum = node.get("winSum").longValue();
        return this;
    }

    @Override
    public String toString() {
        return "PlayerWinEvent[" +
                "gameId=" + gameId +
                ", eventDate=" + eventDate +
                ", accountId=" + accountId +
                ", accountExternalId='" + accountExternalId + '\'' +
                ", currencyCode=" + currencyCode +
                ", betAmount=" + betAmount +
                ", winAmount=" + winAmount +
                ", purchaseSpecialWeaponAmount=" + purchaseSpecialWeaponAmount +
                ", averageBet=" + averageBet +
                ", minBet=" + minBet +
                ", maxBet=" + maxBet +
                ", maxExposure=" + maxExposure +
                ", highestWinPerSingleBet=" + highestWinPerSingleBet +
                ", betSum=" + betSum +
                ", winSum=" + winSum +
                ']';
    }
}
