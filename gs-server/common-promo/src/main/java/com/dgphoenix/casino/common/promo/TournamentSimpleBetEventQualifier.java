package com.abs.casino.common.promo;
import com.abs.casino.common.promo.*;

import com.abs.casino.common.currency.ICurrencyRateManager;
import com.abs.casino.common.exception.CommonException;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import javax.validation.constraints.Min;

/**
 * User: flsh
 * Date: 22.11.16.
 */
public class TournamentSimpleBetEventQualifier implements IPlayerBetQualifier {
    private static final byte VERSION = 0;

    @Min(0)
    private Integer minBetAmount;

    private TournamentSimpleBetEventQualifier() {
    }

    public TournamentSimpleBetEventQualifier(Integer minBetAmount) {
        this.minBetAmount = minBetAmount;
    }

    @Override
    public boolean qualifyBet(PromoCampaignMember member, DesiredPrize prize, PlayerBetEvent event,
                              ICurrencyRateManager currencyRatesManager, String baseCurrency) throws CommonException {
        if (minBetAmount == null) {
            return true;
        }

        double minBetAmountInCurrentCurrency = currencyRatesManager.convert(minBetAmount, baseCurrency,
                event.getCurrency());
        if (event.getBetAmount() >= minBetAmountInCurrentCurrency) {
            prize.updateBets(1, event.getBetAmount());
            return true;
        }
        return false;
    }

    public Integer getMinBetAmount() {
        return minBetAmount;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeInt(minBetAmount == null ? -1 : minBetAmount);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte ver = input.readByte();
        int l = input.readInt();
        minBetAmount = l < 0 ? null : l;
    }

    @Override
    public String toString() {
        return "TournamentSimpleBetPrizeQualifier[" +
                "minBetAmount=" + minBetAmount +
                ']';
    }
}
