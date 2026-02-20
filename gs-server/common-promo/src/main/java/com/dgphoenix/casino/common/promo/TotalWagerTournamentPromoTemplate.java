package com.dgphoenix.casino.common.promo;

import com.dgphoenix.casino.tools.annotations.Preset;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.common.collect.Sets;

import java.io.IOException;
import java.util.Set;

/**
 * User: flsh
 * Date: 10.12.2020.
 */
public class TotalWagerTournamentPromoTemplate extends MaxBalanceTournamentPromoTemplate {
    private static final byte VERSION = 0;
    private static final Set<TournamentObjective> ALLOWED_OBJECTIVES = Sets.immutableEnumSet(TournamentObjective.TOURNAMENT_MAX_BET_SUM);

    public TotalWagerTournamentPromoTemplate(@Preset("TOURNAMENT_MAX_BET_SUM") TournamentObjective objective, TournamentRankQualifier rankQualifier,
                                             long buyInPrice, long buyInAmount, long prize, boolean reBuyEnabled,
                                             long reBuyPrice, long reBuyAmount, int reBuyLimit, long cutOffTime,
                                             long iconId, boolean resetBalance) {
        super(objective, rankQualifier, buyInPrice, buyInAmount, prize, reBuyEnabled, reBuyPrice, reBuyAmount,
                reBuyLimit, cutOffTime, iconId, resetBalance);
    }

    @Override
    public PromoType getPromoType() {
        return PromoType.TOTAL_WAGER_TOURNAMENT;
    }

    @Override
    public Set<TournamentObjective> getAllowedObjectives() {
        return ALLOWED_OBJECTIVES;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        super.write(kryo, output);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte ver = input.readByte();
        super.read(kryo, input);
    }

    @Override
    public void serializeObject(JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
        super.serializeObject(gen, serializers);
    }

    @Override
    public TotalWagerTournamentPromoTemplate deserializeObject(JsonParser p, DeserializationContext ctxt)
            throws IOException {
        super.deserializeObject(p, ctxt);
        return this;
    }

    @Override
    public String toString() {
        return "TotalWagerTournamentPromoTemplate[" +
                "objective=" + super.getObjective() +
                ", rankQualifier=" + super.getRankQualifier() +
                ", prizePool=" + super.getPrizePool() +
                ", significantEvents=" + super.getSignificantEvents() +
                ", buyInPrice=" + getBuyInPrice() +
                ", buyInAmount=" + getBuyInAmount() +
                ", prize=" + getPrize() +
                ", reBuyEnabled=" + isReBuyEnabled() +
                ", reBuyPrice=" + getReBuyPrice() +
                ", reBuyAmount=" + getReBuyAmount() +
                ", reBuyLimit=" + getReBuyLimit() +
                ", cutOffTime=" + getCutOffTime() +
                ", iconId=" + getIconId() +
                ", resetBalance=" + isResetBalance() +
                ']';
    }
}
