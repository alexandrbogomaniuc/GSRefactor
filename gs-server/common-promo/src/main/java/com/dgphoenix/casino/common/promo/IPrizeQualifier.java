package com.abs.casino.common.promo;

import com.abs.casino.common.currency.ICurrencyRateManager;
import com.abs.casino.common.exception.CommonException;
import com.esotericsoftware.kryo.KryoSerializable;

/**
 * User: flsh
 * Date: 22.11.16.
 */
public interface IPrizeQualifier extends KryoSerializable {
    boolean qualifyPrize(IPromoTemplate template, PromoCampaignMember member, DesiredPrize prize, ICurrencyRateManager currencyRateManager,
                         String baseCurrency, String playerCurrency) throws CommonException;

    void resetCurrentProgress(DesiredPrize desiredPrize, ICurrencyRateManager currencyRateManager, String baseCurrency,
                              String playerCurrency) throws CommonException;

    int getQualifiedPrizesAtOnce(PromoCampaignMember member, DesiredPrize prize, ICurrencyRateManager currencyRateManager,
                                 String baseCurrency, String playerCurrency) throws CommonException;

    boolean isMultiplePrizesAtOnce();
}
