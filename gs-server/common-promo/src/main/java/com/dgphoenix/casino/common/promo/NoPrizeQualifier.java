package com.dgphoenix.casino.common.promo;

import com.abs.casino.common.currency.ICurrencyRateManager;
import com.dgphoenix.casino.common.exception.CommonException;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * User: flsh
 * Date: 26.01.17.
 */
public class NoPrizeQualifier implements IPrizeQualifier {
    @Override
    public boolean qualifyPrize(IPromoTemplate template, PromoCampaignMember member, DesiredPrize prize, ICurrencyRateManager currencyRateManager,
                                String baseCurrency, String playerCurrency) {
        return false;
    }

    @Override
    public void resetCurrentProgress(DesiredPrize desiredPrize, ICurrencyRateManager currencyRateManager,
                                     String baseCurrency, String playerCurrency) {
        //nop
    }

    @Override
    public int getQualifiedPrizesAtOnce(PromoCampaignMember member, DesiredPrize prize,
                                        ICurrencyRateManager currencyRateManager, String baseCurrency,
                                        String playerCurrency) throws CommonException {
        return 0;
    }

    @Override
    public boolean isMultiplePrizesAtOnce() {
        return false;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        //nop
    }

    @Override
    public void read(Kryo kryo, Input input) {
        //nop
    }
}
