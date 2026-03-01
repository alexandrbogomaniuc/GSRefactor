package com.abs.casino.common.promo;
import com.dgphoenix.casino.common.promo.*;

import com.abs.casino.common.currency.ICurrencyRateManager;
import com.dgphoenix.casino.common.exception.CommonException;
import com.esotericsoftware.kryo.KryoSerializable;

/**
 * User: flsh
 * Date: 23.01.17.
 */
public interface IPlayerBetQualifier extends KryoSerializable {
    boolean qualifyBet(PromoCampaignMember member, DesiredPrize prize, PlayerBetEvent event,
                       ICurrencyRateManager currencyRateManager, String baseCurrency) throws CommonException;
}
