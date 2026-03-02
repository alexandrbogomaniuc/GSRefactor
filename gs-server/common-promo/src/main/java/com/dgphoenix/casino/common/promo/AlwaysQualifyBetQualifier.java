package com.abs.casino.common.promo;
import com.abs.casino.common.promo.*;

import com.abs.casino.common.currency.ICurrencyRateManager;
import com.abs.casino.common.exception.CommonException;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * Created by vladislav on 11/14/17.
 */
public class AlwaysQualifyBetQualifier implements IPlayerBetQualifier {
    @Override
    public boolean qualifyBet(PromoCampaignMember member, DesiredPrize prize, PlayerBetEvent event,
                              ICurrencyRateManager currencyRateManager, String baseCurrency) throws CommonException {
        prize.updateBets(1, event.getBetAmount());
        return true;
    }

    @Override
    public void write(Kryo kryo, Output output) {
    }

    @Override
    public void read(Kryo kryo, Input input) {
    }
}
