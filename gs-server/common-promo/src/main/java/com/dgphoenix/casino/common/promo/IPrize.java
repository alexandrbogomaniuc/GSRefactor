package com.dgphoenix.casino.common.promo;

import com.dgphoenix.casino.common.cache.Identifiable;
import com.dgphoenix.casino.common.currency.ICurrencyRateManager;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.promo.messages.server.notifications.prizes.PrizeWonNotification;
import com.esotericsoftware.kryo.KryoSerializable;

/**
 * User: flsh
 * Date: 17.11.16.
 */
public interface IPrize extends Identifiable, KryoSerializable {
    long getId();

    void setId(long id);

    boolean qualifyBet(PromoCampaignMember member, DesiredPrize desiredPrize, PlayerBetEvent event,
                       ICurrencyRateManager currencyRateManager, String baseCurrency) throws CommonException;

    boolean qualifyWin(IPromoCampaign campaign, PromoCampaignMember member, DesiredPrize desiredPrize,
                       PlayerWinEvent event, ICurrencyRateManager currencyRateManager, String baseCurrency) throws CommonException;

    boolean qualifyBonus(IPromoCampaign campaign, PromoCampaignMember member, DesiredPrize desiredPrize,
                         PlayerBonusEvent event) throws CommonException;

    boolean qualifyEndRound(IPromoCampaign campaign, PromoCampaignMember member, DesiredPrize desiredPrize,
                            EndRoundEvent event, ICurrencyRateManager currencyRateManager, String baseCurrency) throws CommonException;

    boolean qualifyPrize(IPromoTemplate template, PromoCampaignMember member, DesiredPrize desiredPrize, ICurrencyRateManager currencyRateManager,
                         String baseCurrency, String playerCurrency) throws CommonException;

    //total prize count may be awarded by same player in period, null is unlimited
    Integer getLimitPerPlayerOnPeriod();

    //total prize count may be awarded by same player, null is unlimited
    Integer getLimitTotalCountPerPlayer();

    Long getLimitPeriodInSeconds();

    int getTotalAwardedCountForAllPlayers();

    void incrementTotalAwardedCount();

    PrizeWonNotification getWonMessage();
}
