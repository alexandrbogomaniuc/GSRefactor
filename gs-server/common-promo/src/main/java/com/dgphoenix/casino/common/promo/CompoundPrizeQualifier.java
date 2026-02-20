package com.dgphoenix.casino.common.promo;

import com.dgphoenix.casino.common.currency.ICurrencyRateManager;
import com.dgphoenix.casino.common.exception.CommonException;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.ArrayList;
import java.util.List;

public class CompoundPrizeQualifier implements IPrizeQualifier {
    private static final byte VERSION = 0;

    private List<IPrizeQualifier> qualifiers = new ArrayList<IPrizeQualifier>();

    @Override
    public boolean qualifyPrize(IPromoTemplate template, PromoCampaignMember member, DesiredPrize prize, ICurrencyRateManager currencyRateManager, String baseCurrency, String playerCurrency) throws CommonException {
        for (IPrizeQualifier qualifier : qualifiers) {
            if (!qualifier.qualifyPrize(template, member, prize, currencyRateManager, baseCurrency, playerCurrency)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void resetCurrentProgress(DesiredPrize desiredPrize, ICurrencyRateManager currencyRateManager, String baseCurrency, String playerCurrency) throws CommonException {
        /*for (IPrizeQualifier qualifier : qualifiers) {
            qualifier.resetCurrentProgress(desiredPrize, currencyRateManager, baseCurrency, playerCurrency);
        }*/
    }

    @Override
    public int getQualifiedPrizesAtOnce(PromoCampaignMember member, DesiredPrize prize, ICurrencyRateManager currencyRateManager, String baseCurrency, String playerCurrency) throws CommonException {
        return 1;
    }

    public void addQualifier(IPrizeQualifier qualifier) {
        qualifiers.add(qualifier);
    }

    @Override
    public boolean isMultiplePrizesAtOnce() {
        return false;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        kryo.writeClassAndObject(output, qualifiers);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte ver = input.readByte();
        qualifiers = (List<IPrizeQualifier> ) kryo.readClassAndObject(input);
    }

    @Override
    public String toString() {
        return "CompoundPrizeQualifier{" +
                "qualifiers=" + qualifiers +
                '}';
    }
}
