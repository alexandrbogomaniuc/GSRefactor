package com.abs.casino.common.promo;
import com.abs.casino.common.promo.*;

import com.abs.casino.common.currency.ICurrencyRateManager;
import com.abs.casino.common.exception.CommonException;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * User: flsh
 * Date: 22.11.16.
 */
public class SpinCountPrizeQualifier implements IPrizeQualifier {
    private static final byte VERSION = 0;

    private int betsCountToQualify;

    private SpinCountPrizeQualifier() {
    }

    public SpinCountPrizeQualifier(int betsCountToQualify) {
        this.betsCountToQualify = betsCountToQualify;
    }

    @Override
    public boolean qualifyPrize(IPromoTemplate template, PromoCampaignMember member, DesiredPrize prize, ICurrencyRateManager currencyRateManager,
                                String baseCurrency, String playerCurrency) {
        return prize.getQualifiedBetsCount() >= betsCountToQualify;
    }

    @Override
    public void resetCurrentProgress(DesiredPrize desiredPrize, ICurrencyRateManager currencyRateManager,
                                     String baseCurrency, String playerCurrency) {
        int qualifiedBetsCount = desiredPrize.getQualifiedBetsCount();
        desiredPrize.resetCurrentProgress(qualifiedBetsCount - betsCountToQualify, 0, 0);
    }

    @Override
    public int getQualifiedPrizesAtOnce(PromoCampaignMember member, DesiredPrize prize,
                                        ICurrencyRateManager currencyRateManager, String baseCurrency,
                                        String playerCurrency) throws CommonException {
        return 1;
    }

    @Override
    public boolean isMultiplePrizesAtOnce() {
        return false;
    }

    public int getBetsCountToQualify() {
        return betsCountToQualify;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeInt(betsCountToQualify, true);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte ver = input.readByte();
        betsCountToQualify = input.readInt(true);
    }

    @Override
    public String toString() {
        return "SpinCountPrizeQualifier[" +
                "betsCountToQualify=" + betsCountToQualify +
                ']';
    }
}
