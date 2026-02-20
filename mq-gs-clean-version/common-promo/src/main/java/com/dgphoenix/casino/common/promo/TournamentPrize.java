package com.dgphoenix.casino.common.promo;

import com.dgphoenix.casino.common.currency.ICurrencyRateManager;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.promo.messages.server.notifications.prizes.PrizeWonNotification;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.Set;

public class TournamentPrize implements IVirtualPrize {
    private static final byte VERSION = 0;

    private long id;
    private IParticipantEventQualifier eventQualifier;
    private Set<RankPrize> prizesPool;

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void setId(long id) {
        this.id = id;
    }

    public IParticipantEventQualifier getEventQualifier() {
        return eventQualifier;
    }

    public void setEventQualifier(IParticipantEventQualifier eventQualifier) {
        this.eventQualifier = eventQualifier;
    }

    public Set<RankPrize> getPrizesPool() {
        return prizesPool;
    }

    public void setPrizesPool(Set<RankPrize> prizesPool) {
        this.prizesPool = prizesPool;
    }

    @Override
    public boolean qualifyBet(PromoCampaignMember member, DesiredPrize desiredPrize, PlayerBetEvent event,
                              ICurrencyRateManager currencyRateManager, String baseCurrency) throws CommonException {
        return eventQualifier.qualifyBet(member, desiredPrize, event, currencyRateManager, baseCurrency);
    }

    @Override
    public boolean qualifyWin(IPromoCampaign campaign, PromoCampaignMember member, DesiredPrize desiredPrize,
                              PlayerWinEvent event, ICurrencyRateManager currencyRateManager, String baseCurrency) throws CommonException {
        return eventQualifier.qualifyWin(campaign, member, desiredPrize, event, currencyRateManager, baseCurrency);
    }

    @Override
    public boolean qualifyBonus(IPromoCampaign campaign, PromoCampaignMember member, DesiredPrize desiredPrize,
                                PlayerBonusEvent event) throws CommonException {
        return eventQualifier.qualifyBonus(campaign, member, desiredPrize, event);
    }

    @Override
    public boolean qualifyEndRound(IPromoCampaign campaign, PromoCampaignMember member, DesiredPrize desiredPrize,
                                   EndRoundEvent event, ICurrencyRateManager currencyRateManager, String baseCurrency) throws CommonException {
        return true;
    }

    @Override
    public boolean qualifyPrize(IPromoTemplate template, PromoCampaignMember member, DesiredPrize desiredPrize,
                                ICurrencyRateManager currencyRateManager, String baseCurrency, String playerCurrency) {
        return false;
    }

    @Override
    public Integer getLimitPerPlayerOnPeriod() {
        return 1;
    }

    @Override
    public Integer getLimitTotalCountPerPlayer() {
        return 1;
    }

    @Override
    public Long getLimitPeriodInSeconds() {
        //return null, endDate is equals promo.endDate
        return null;
    }

    @Override
    public int getTotalAwardedCountForAllPlayers() {
        //cannot be awarded, prizes distributed under Qualification stage
        return 0;
    }

    @Override
    public void incrementTotalAwardedCount() {

    }

    @Override
    public PrizeWonNotification getWonMessage() {
        return null;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeLong(id, true);
        kryo.writeClassAndObject(output, eventQualifier);
        kryo.writeClassAndObject(output, prizesPool);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte ver = input.readByte();
        id = input.readLong(true);
        eventQualifier = (IParticipantEventQualifier) kryo.readClassAndObject(input);
        //noinspection unchecked
        prizesPool = (Set<RankPrize>) kryo.readClassAndObject(input);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TournamentPrize that = (TournamentPrize) o;

        return id == that.id;

    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public String toString() {
        return "TournamentPrize[" +
                "id=" + id +
                ", eventQualifier=" + eventQualifier +
                ", prizesPool=" + prizesPool +
                ']';
    }
}
