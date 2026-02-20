package com.dgphoenix.casino.common.promo;

import com.dgphoenix.casino.common.currency.ICurrencyRateManager;
import com.dgphoenix.casino.common.exception.CommonException;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.validation.constraints.Min;


/**
 * User: flsh
 * Date: 22.11.16.
 */
public abstract class AbstractPrize implements IMaterialPrize {
    private static final Logger LOG = LogManager.getLogger(AbstractPrize.class);

    protected long id;
    @Min(0)
    protected Integer limitPerPlayerOnPeriod;
    @Min(0)
    protected Integer limitTotalCountPerPlayer;
    @Min(0)
    protected Long limitPeriodInSeconds;
    protected int totalAwardedCountForAllPlayers;
    protected IPrizeQualifier prizeQualifier;
    protected IParticipantEventQualifier eventQualifier;

    protected AbstractPrize() {
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void setId(long id) {
        this.id = id;
    }

    @Override
    public Integer getLimitPerPlayerOnPeriod() {
        return limitPerPlayerOnPeriod;
    }

    public void setLimitPerPlayerOnPeriod(Integer limitPerPlayerOnPeriod) {
        this.limitPerPlayerOnPeriod = limitPerPlayerOnPeriod;
    }

    public void setLimitTotalCountPerPlayer(Integer limitTotalCountPerPlayer) {
        this.limitTotalCountPerPlayer = limitTotalCountPerPlayer;
    }

    @Override
    public Long getLimitPeriodInSeconds() {
        return limitPeriodInSeconds;
    }

    public void setLimitPeriodInSeconds(Long limitPeriodInSeconds) {
        this.limitPeriodInSeconds = limitPeriodInSeconds;
    }

    @Override
    public Integer getLimitTotalCountPerPlayer() {
        return limitTotalCountPerPlayer;
    }

    @Override
    public int getTotalAwardedCountForAllPlayers() {
        return totalAwardedCountForAllPlayers;
    }

    @Override
    public void incrementTotalAwardedCount() {
        totalAwardedCountForAllPlayers++;
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
                                ICurrencyRateManager currencyRateManager, String baseCurrency,
                                String playerCurrency) throws CommonException {
        if (limitTotalCountPerPlayer != null) {
            long totalAwardedPrizesForPlayer = 0;
            for (DesiredPrize prize : member.getDesiredPrizes()) {
                if (prize.getPromoPrizeId() == desiredPrize.getPromoPrizeId()) {
                    totalAwardedPrizesForPlayer += prize.getReceivedPrizesCount();
                }
            }
            if (totalAwardedPrizesForPlayer >= limitTotalCountPerPlayer) {
                LOG.info("Prize cannot be won, exceeded by total per player limit: " + toString());
                return false;
            }
        }
        if (limitPerPlayerOnPeriod != null && desiredPrize.getReceivedPrizesCount() >= limitPerPlayerOnPeriod) {
            LOG.info("Prize cannot be won, exceeded by per player and period limit: " + toString());
            return false;
        }
        boolean prizeQualified = prizeQualifier.qualifyPrize(template, member, desiredPrize, currencyRateManager, baseCurrency,
                playerCurrency);
        if (prizeQualified) {
            int qualifiedPrizesAtOnce = prizeQualifier.getQualifiedPrizesAtOnce(member, desiredPrize,
                    currencyRateManager, baseCurrency, playerCurrency);
            if (qualifiedPrizesAtOnce == 1) {
                incrementTotalAwardedCount();
                desiredPrize.incrementReceivedPrizesCount();
            } else {
                qualifiedPrizesAtOnce = limitQualifiedPrizes(desiredPrize, qualifiedPrizesAtOnce);
                totalAwardedCountForAllPlayers += qualifiedPrizesAtOnce;
                desiredPrize.incrementReceivedPrizesCount(qualifiedPrizesAtOnce);
            }
            prizeQualifier.resetCurrentProgress(desiredPrize, currencyRateManager, baseCurrency, playerCurrency);
        } else if (prizeQualifier instanceof BetAmountPrizeQualifier) {
            BetAmountPrizeQualifier betAmountPrizeQualifier = (BetAmountPrizeQualifier) prizeQualifier;
            if (betAmountPrizeQualifier.isMultiplePrizesAtOnce()) {
                desiredPrize.resetCurrentProgress(0, 0, 0);
            }
        }
        return prizeQualified;
    }

    private int limitQualifiedPrizes(DesiredPrize desiredPrize, int qualifiedPrizesAtOnce) {
        int receivedPrizesCount = desiredPrize.getReceivedPrizesCount();
        int totalPrizesCount = qualifiedPrizesAtOnce + receivedPrizesCount;
        int limitedQualifiedPrizesCount = qualifiedPrizesAtOnce;
        if (limitTotalCountPerPlayer != null && totalPrizesCount > limitTotalCountPerPlayer) {
            LOG.info("Decreasing qualified prizes count. Max prize count by total per player exceeded. " +
                    "limitTotalCountPerPlayer:" + limitTotalCountPerPlayer +
                    ", qualified prizes count:" + totalPrizesCount);
            limitedQualifiedPrizesCount = limitTotalCountPerPlayer - receivedPrizesCount;
            totalPrizesCount = limitedQualifiedPrizesCount + receivedPrizesCount;
        }
        if (limitPerPlayerOnPeriod != null && totalPrizesCount > limitPerPlayerOnPeriod) {
            LOG.info("Decreasing received prizes count. Max prize count by period per player exceeded. " +
                    "limitPerPlayerOnPeriod:" + limitPerPlayerOnPeriod +
                    ", qualified prizes count:" + totalPrizesCount);
            limitedQualifiedPrizesCount = limitPerPlayerOnPeriod - receivedPrizesCount;
        }
        return limitedQualifiedPrizesCount;
    }

    public IPrizeQualifier getPrizeQualifier() {
        return prizeQualifier;
    }

    public void setPrizeQualifier(IPrizeQualifier prizeQualifier) {
        this.prizeQualifier = prizeQualifier;
    }

    public IParticipantEventQualifier getEventQualifier() {
        return eventQualifier;
    }

    public void setEventQualifier(IParticipantEventQualifier eventQualifier) {
        this.eventQualifier = eventQualifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractPrize that = (AbstractPrize) o;

        return id == that.id;

    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    protected void baseWrite(Kryo kryo, Output output) {
        output.writeLong(id, true);
        output.writeInt(limitPerPlayerOnPeriod == null ? -1 : limitPerPlayerOnPeriod);
        output.writeInt(limitTotalCountPerPlayer == null ? -1 : limitTotalCountPerPlayer);
        output.writeLong(limitPeriodInSeconds == null ? -1 : limitPeriodInSeconds);
        output.writeInt(totalAwardedCountForAllPlayers);
        kryo.writeClassAndObject(output, prizeQualifier);
        kryo.writeClassAndObject(output, eventQualifier);
    }

    protected void baseRead(Kryo kryo, Input input) {
        id = input.readLong(true);
        int l = input.readInt();
        limitPerPlayerOnPeriod = l < 0 ? null : l;
        l = input.readInt();
        limitTotalCountPerPlayer = l < 0 ? null : l;
        long l1 = input.readLong();
        limitPeriodInSeconds = l1 < 0 ? null : l1;
        totalAwardedCountForAllPlayers = input.readInt();
        prizeQualifier = (IPrizeQualifier) kryo.readClassAndObject(input);
        eventQualifier = (IParticipantEventQualifier) kryo.readClassAndObject(input);
    }

    @Override
    public String toString() {
        return "AbstractPrize[" +
                "id=" + id +
                ", limitPerPlayerOnPeriod=" + limitPerPlayerOnPeriod +
                ", limitTotalCountPerPlayer=" + limitTotalCountPerPlayer +
                ", limitPeriodInSeconds=" + limitPeriodInSeconds +
                ", totalAwardedCountForAllPlayers=" + totalAwardedCountForAllPlayers +
                ", prizeQualifier=" + prizeQualifier +
                ", eventQualifier=" + eventQualifier +
                ']';
    }
}
