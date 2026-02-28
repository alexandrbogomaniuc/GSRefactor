package com.dgphoenix.casino.common.promo;

import com.dgphoenix.casino.common.cache.JsonSelfSerializable;
import com.dgphoenix.casino.common.currency.ICurrencyRateManager;
import com.dgphoenix.casino.common.exception.CommonException;
import com.abs.casino.common.util.DatePeriod;
import com.esotericsoftware.kryo.KryoSerializable;

import java.util.List;
import java.util.Set;

/**
 * User: flsh
 * Date: 15.11.16.
 */
public interface IPromoTemplate<P extends IPrize, IPT extends IPromoTemplate> extends KryoSerializable, JsonSelfSerializable<IPT> {
    PromoType getPromoType();

    Set<SignificantEventType> getSignificantEvents();

    Set<P> getPrizePool();

    boolean checkIfDesiredPrizeActive(DesiredPrize desiredPrize, IPromoCampaign promoCampaign);

    DesiredPrize createNewDesiredPrize(DesiredPrize desiredPrize, IPromoCampaign promoCampaign, IPrize prize);

    void processWonPrize(DesiredPrize desiredPrize);

    List<DesiredPrize> createDesiredPrizes(DatePeriod campaignPeriod);

    boolean qualifyPrize(IPrize prize, PromoCampaignMember member, DesiredPrize desiredPrize,
                         ICurrencyRateManager currencyRateManager, String baseCurrency,
                         String playerCurrency) throws CommonException;

    void updateMemberBetInfo(PromoCampaignMember member, PlayerBetEvent event, IPromoCampaign promoCampaign,
                                    ICurrencyRateManager currencyRateManager) throws CommonException;
}
