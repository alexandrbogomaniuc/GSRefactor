package com.abs.casino.common.promo;
import com.dgphoenix.casino.common.promo.*;

import com.abs.casino.common.currency.ICurrencyRateManager;
import com.abs.casino.common.exception.CommonException;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * User: flsh
 * Date: 22.11.16.
 */
public class ByAmountBetEventQualifier implements IPlayerBetQualifier {
    private static final byte VERSION = 0;

    private int amount;

    private ByAmountBetEventQualifier() {
    }

    public ByAmountBetEventQualifier(int amount) {
        this.amount = amount;
    }

    @Override
    public boolean qualifyBet(PromoCampaignMember member, DesiredPrize prize, PlayerBetEvent event,
                              ICurrencyRateManager currencyRateManager, String baseCurrency) throws CommonException {
        double amountPerSpinInCurrentCurrency = currencyRateManager.convert(amount, baseCurrency, event.getCurrency());
        if (event.getBetAmount() >= amountPerSpinInCurrentCurrency) {
            prize.updateBets(1, event.getBetAmount());
            return true;
        }
        return false;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeInt(amount, true);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte ver = input.readByte();
        amount = input.readInt(true);
    }

    @Override
    public String toString() {
        return "ByAmountBetEventQualifier[" +
                "amount=" + amount +
                ']';
    }
}
