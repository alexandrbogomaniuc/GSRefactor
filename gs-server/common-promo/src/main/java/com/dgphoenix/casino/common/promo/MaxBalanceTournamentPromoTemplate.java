package com.dgphoenix.casino.common.promo;

import com.abs.casino.common.currency.ICurrencyRateManager;
import com.dgphoenix.casino.common.exception.CommonException;
import com.abs.casino.tools.annotations.Preset;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.common.collect.Sets;

import java.io.IOException;
import java.util.Set;

public class MaxBalanceTournamentPromoTemplate extends TournamentPromoTemplate<MaxBalanceTournamentPromoTemplate>
        implements INetworkPromoEventTemplate<TournamentPrize, MaxBalanceTournamentPromoTemplate> {
    private static final byte VERSION = 2;
    private static final Set<TournamentObjective> ALLOWED_OBJECTIVES = Sets.immutableEnumSet(TournamentObjective.CURRENT_TOURNAMENT_BALANCE);

    private long buyInAmount;
    private long buyInPrice;
    private long prize;
    private boolean reBuyEnabled;
    private long reBuyPrice;
    private long reBuyAmount;
    private int reBuyLimit;
    private long cutOffTime;
    private long iconId;
    private boolean resetBalance;

    public MaxBalanceTournamentPromoTemplate(@Preset("CURRENT_TOURNAMENT_BALANCE") TournamentObjective objective,
                                             TournamentRankQualifier rankQualifier, long buyInPrice, long buyInAmount, long prize,
                                             boolean reBuyEnabled, long reBuyPrice, long reBuyAmount, int reBuyLimit, long cutOffTime,
                                             long iconId, boolean resetBalance) {
        super(objective, rankQualifier);
        this.buyInAmount = buyInAmount;
        this.buyInPrice = buyInPrice;
        this.prize = prize;
        this.reBuyEnabled = reBuyEnabled;
        this.reBuyPrice = reBuyPrice;
        this.reBuyAmount = reBuyAmount;
        this.reBuyLimit = reBuyLimit;
        this.cutOffTime = cutOffTime;
        this.iconId = iconId;
        this.resetBalance = resetBalance;
    }

    @Override
    public long getBuyInPrice() {
        return buyInPrice;
    }

    public void setBuyInPrice(long buyInPrice) {
        this.buyInPrice = buyInPrice;
    }

    @Override
    public long getBuyInAmount() {
        return buyInAmount;
    }

    public void setBuyInAmount(long buyInAmount) {
        this.buyInAmount = buyInAmount;
    }

    @Override
    public long getPrize() {
        return prize;
    }

    public void setPrize(long prize) {
        this.prize = prize;
    }

    @Override
    public boolean isReBuyEnabled() {
        return reBuyEnabled;
    }

    public void setReBuyEnabled(boolean rebuyEnabled) {
        this.reBuyEnabled = rebuyEnabled;
    }

    @Override
    public long getReBuyPrice() {
        return reBuyPrice;
    }

    public void setReBuyPrice(long rebuyPrice) {
        this.reBuyPrice = rebuyPrice;
    }

    @Override
    public long getReBuyAmount() {
        return reBuyAmount;
    }

    public void setReBuyAmount(long rebuyAmount) {
        this.reBuyAmount = rebuyAmount;
    }

    public int getReBuyLimit() {
        return reBuyLimit;
    }

    public void setReBuyLimit(int reBuyLimit) {
        this.reBuyLimit = reBuyLimit;
    }

    @Override
    public long getCutOffTime() {
        return cutOffTime;
    }

    public void setCutOffTime(long cutOffTime) {
        this.cutOffTime = cutOffTime;
    }

    @Override
    public long getIconId() {
        return iconId;
    }

    public void setIconId(long iconId) {
        this.iconId = iconId;
    }

    @Override
    public boolean isResetBalance() {
        return resetBalance;
    }

    public void setResetBalance(boolean resetBalance) {
        this.resetBalance = resetBalance;
    }

    @Override
    public PromoType getPromoType() {
        return PromoType.MAX_BALANCE_TOURNAMENT;
    }

    @Override
    public Set<TournamentObjective> getAllowedObjectives() {
        return ALLOWED_OBJECTIVES;
    }

    @Override
    public void updateMemberBetInfo(PromoCampaignMember member, PlayerBetEvent event, IPromoCampaign promoCampaign,
                                    ICurrencyRateManager currencyRateManager) throws CommonException {
        member.updateBets(1, event.getBetAmount());
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        super.write(kryo, output);
        output.writeLong(buyInAmount, true);
        output.writeLong(buyInPrice, true);
        output.writeLong(prize, true);
        output.writeBoolean(reBuyEnabled);
        output.writeLong(reBuyPrice, true);
        output.writeLong(reBuyAmount, true);
        output.writeInt(reBuyLimit, true);
        output.writeLong(cutOffTime, true);
        output.writeLong(iconId, true);
        output.writeBoolean(resetBalance);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte ver = input.readByte();
        super.read(kryo, input);
        this.buyInAmount = input.readLong(true);
        this.buyInPrice = input.readLong(true);
        this.prize = input.readLong(true);
        this.reBuyEnabled = input.readBoolean();
        this.reBuyPrice = input.readLong(true);
        this.reBuyAmount = input.readLong(true);
        this.reBuyLimit = input.readInt(true);
        this.cutOffTime = input.readLong(true);
        if (ver >= 1) {
            this.iconId = input.readLong(true);
        }
        if (ver >= 2) {
            this.resetBalance = input.readBoolean();
        }
    }

    @Override
    public void serializeObject(JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
        super.serializeObject(gen, serializers);

        gen.writeNumberField("buyInAmount", buyInAmount);
        gen.writeNumberField("buyInPrice", buyInPrice);
        gen.writeNumberField("prize", prize);
        gen.writeBooleanField("reBuyEnabled", reBuyEnabled);
        gen.writeNumberField("reBuyPrice", reBuyPrice);
        gen.writeNumberField("reBuyAmount", reBuyAmount);
        gen.writeNumberField("reBuyLimit", reBuyLimit);
        gen.writeNumberField("cutOffTime", cutOffTime);
        gen.writeNumberField("iconId", iconId);
        gen.writeBooleanField("resetBalance", resetBalance);
    }

    @Override
    public MaxBalanceTournamentPromoTemplate deserializeObject(JsonParser p, DeserializationContext ctxt)
            throws IOException {
        ObjectMapper om = (ObjectMapper) p.getCodec();
        JsonNode node = om.readTree(p);

        super.deserializeObject(p, ctxt);

        this.buyInAmount = node.get("buyInAmount").asLong();
        this.buyInPrice = node.get("buyInPrice").asLong();
        this.prize = node.get("prize").asLong();
        this.reBuyEnabled = node.get("reBuyEnabled").asBoolean();
        this.reBuyPrice = node.get("reBuyPrice").asLong();
        this.reBuyAmount = node.get("reBuyAmount").asLong();
        this.reBuyLimit = node.get("reBuyLimit").asInt();
        this.cutOffTime = node.get("cutOffTime").asLong();
        this.iconId = node.get("iconId").asLong();
        this.resetBalance = node.get("resetBalance").asBoolean();

        return this;
    }

    @Override
    public String toString() {
        return "MaxBalanceTournamentPromoTemplate[" +
                "objective=" + super.getObjective() +
                ", rankQualifier=" + super.getRankQualifier() +
                ", prizePool=" + super.getPrizePool() +
                ", significantEvents=" + super.getSignificantEvents() +
                ", buyInPrice=" + buyInPrice +
                ", buyInAmount=" + buyInAmount +
                ", prize=" + prize +
                ", reBuyEnabled=" + reBuyEnabled +
                ", reBuyPrice=" + reBuyPrice +
                ", reBuyAmount=" + reBuyAmount +
                ", reBuyLimit=" + reBuyLimit +
                ", cutOffTime=" + cutOffTime +
                ", iconId=" + iconId +
                ", resetBalance=" + resetBalance +
                ']';
    }
}
