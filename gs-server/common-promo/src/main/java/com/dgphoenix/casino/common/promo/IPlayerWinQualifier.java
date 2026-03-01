package com.abs.casino.common.promo;
import com.dgphoenix.casino.common.promo.*;

import com.abs.casino.common.currency.ICurrencyRateManager;
import com.dgphoenix.casino.common.exception.CommonException;
import com.esotericsoftware.kryo.KryoSerializable;

/**
 * User: flsh
 * Date: 23.01.17.
 */
public interface IPlayerWinQualifier extends KryoSerializable {
    boolean qualifyWin(IPromoCampaign campaign, PromoCampaignMember member, DesiredPrize prize, PlayerWinEvent event,
                       ICurrencyRateManager currencyRateManager, String baseCurrency) throws CommonException;
}
