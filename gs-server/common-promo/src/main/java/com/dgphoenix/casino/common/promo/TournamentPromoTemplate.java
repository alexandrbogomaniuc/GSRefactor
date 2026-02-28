package com.dgphoenix.casino.common.promo;

import com.abs.casino.common.currency.ICurrencyRateManager;
import com.dgphoenix.casino.common.exception.CommonException;
import com.abs.casino.common.util.DatePeriod;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.abs.casino.tools.annotations.Preset;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.common.collect.Sets;

import java.io.IOException;
import java.util.*;

/**
 * User: flsh
 * Date: 21.11.16.
 */
public class TournamentPromoTemplate<IPT extends TournamentPromoTemplate> implements IPromoTemplate<TournamentPrize, IPT>, ITournamentPromoTemplate {
    private static final byte VERSION = 1;
    private static final Set<TournamentObjective> ALLOWED_OBJECTIVES = Sets.immutableEnumSet(
            TournamentObjective.MAX_PERFORMANCE, TournamentObjective.HIGHEST_WIN, TournamentObjective.CURRENT_TOURNAMENT_BALANCE,
            TournamentObjective.TOURNAMENT_MAX_BET_SUM);
    @Preset("HIGHEST_WIN")
    private TournamentObjective objective;
    private TournamentRankQualifier rankQualifier;
    private Set<TournamentPrize> prizePool;
    private Set<SignificantEventType> significantEvents;

    private TournamentPromoTemplate() {
    }

    public TournamentPromoTemplate(@Preset("HIGHEST_WIN") TournamentObjective objective,
                                   TournamentRankQualifier rankQualifier) {
        if (!getAllowedObjectives().contains(objective)) {
            throw new IllegalArgumentException("Objective not supported: " + objective);
        }
        this.objective = objective;
        this.rankQualifier = rankQualifier;
        this.significantEvents = new HashSet<SignificantEventType>(objective.getSignificantEvents());
        this.significantEvents.addAll(rankQualifier.getSignificantEvents());
    }

    @Override
    public PromoType getPromoType() {
        return PromoType.TOURNAMENT;
    }

    @Override
    public List<DesiredPrize> createDesiredPrizes(DatePeriod campaignPeriod) {
        List<DesiredPrize> result = new ArrayList<DesiredPrize>(prizePool.size());
        for (IPrize prize : prizePool) {
            result.add(new DesiredPrize(prize.getId(), campaignPeriod.getStartDate().getTime(),
                    campaignPeriod.getEndDate().getTime()));
        }
        return result;
    }

    @Override
    public boolean qualifyPrize(IPrize prize, PromoCampaignMember member, DesiredPrize desiredPrize,
                                ICurrencyRateManager currencyRateManager, String baseCurrency,
                                String playerCurrency) throws CommonException {
        return prize.qualifyPrize(this, member, desiredPrize, currencyRateManager, baseCurrency, playerCurrency);
    }

    @Override
    public void updateMemberBetInfo(PromoCampaignMember member, PlayerBetEvent event, IPromoCampaign promoCampaign,
                                    ICurrencyRateManager currencyRateManager) throws CommonException {
        member.updateBets(1, event.getBetAmount());
    }

    @Override
    public boolean checkIfDesiredPrizeActive(DesiredPrize desiredPrize, IPromoCampaign promoCampaign) {
        boolean active = true;
        if (!desiredPrize.isInPeriod()) {
            desiredPrize.setStatus(PrizeStatus.LOST);
            active = false;
        }
        return active;
    }

    @Override
    public DesiredPrize createNewDesiredPrize(DesiredPrize desiredPrize, IPromoCampaign promoCampaign, IPrize prize) {
        return null;
    }

    @Override
    public void processWonPrize(DesiredPrize desiredPrize) {
    }

    @Override
    public TournamentObjective getObjective() {
        return objective;
    }

    @Override
    public TournamentRankQualifier getRankQualifier() {
        return rankQualifier;
    }

    @Override
    public Set<TournamentObjective> getAllowedObjectives() {
        return ALLOWED_OBJECTIVES;
    }

    @Override
    public Set<SignificantEventType> getSignificantEvents() {
        return Collections.unmodifiableSet(significantEvents);
    }

    @Override
    public Set<TournamentPrize> getPrizePool() {
        return prizePool;
    }

    public void addPrize(TournamentPrize prize) {
        if (prizePool == null) {
            prizePool = new HashSet<TournamentPrize>(1);
        }
        prizePool.add(prize);
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeString(objective.name());
        output.writeString(rankQualifier == null ? null : rankQualifier.name());
        kryo.writeClassAndObject(output, prizePool);
        kryo.writeClassAndObject(output, significantEvents);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte ver = input.readByte();
        String s = input.readString();
        objective = TournamentObjective.valueOf(s);
        s = input.readString();
        rankQualifier = StringUtils.isTrimmedEmpty(s) ? null : TournamentRankQualifier.valueOf(s);
        //noinspection unchecked
        prizePool = (Set<TournamentPrize>) kryo.readClassAndObject(input);
        if (ver < 1) {
            significantEvents = objective.getSignificantEvents();
        } else {
            significantEvents = (Set<SignificantEventType>) kryo.readClassAndObject(input);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" +
                "objective=" + objective +
                ", rankQualifier=" + rankQualifier +
                ", prizePool=" + prizePool +
                ", significantEvents=" + significantEvents +
                ']';
    }

    @Override
    public void serializeObject(JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
        gen.writeStringField("objective", objective.name());
        gen.writeStringField("rankQualifier", rankQualifier == null ? null : rankQualifier.name());
        serializeSetField(gen, "prizePool", prizePool, new TypeReference<Set<TournamentPrize>>() {});
        serializeSetField(gen, "significantEvents", significantEvents, new TypeReference<Set<SignificantEventType>>() {});
    }

    @Override
    public IPT deserializeObject(JsonParser p, DeserializationContext ctxt)
            throws IOException {
        ObjectMapper om = (ObjectMapper) p.getCodec();
        JsonNode node = om.readTree(p);

        objective = TournamentObjective.valueOf(readNullableText(node, "objective"));
        String rankName = readNullableText(node, "rankQualifier");
        rankQualifier = StringUtils.isTrimmedEmpty(rankName) ? null : TournamentRankQualifier.valueOf(rankName);

        prizePool = om.treeToValue(node.get("prizePool"), new TypeReference<Set<TournamentPrize>>() {});
        significantEvents = om.treeToValue(node.get("significantEvents"), new TypeReference<Set<SignificantEventType>>() {});
        return (IPT) this;
    }
}
