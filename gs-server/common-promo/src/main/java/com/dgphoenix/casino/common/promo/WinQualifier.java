package com.abs.casino.common.promo;

import com.dgphoenix.casino.common.promo.*;
import com.dgphoenix.casino.common.currency.ICurrencyRateManager;
import com.dgphoenix.casino.common.exception.CommonException;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import javax.validation.constraints.Min;

/**
 * Created by vladislav on 4/19/17.
 */
public class WinQualifier implements IPlayerWinQualifier {
    private static final byte VERSION = 0;

    @Min(0)
    private Integer minWinAmount;

    private WinQualifier() {
    }

    public WinQualifier(Integer minWinAmount) {
        this.minWinAmount = minWinAmount;
    }

    @Override
    public boolean qualifyWin(IPromoCampaign campaign, PromoCampaignMember member, DesiredPrize prize,
                              PlayerWinEvent event, ICurrencyRateManager currencyRateManager, String baseCurrency) throws CommonException {
        Long winAmount = event.getWinAmount();
        if (winAmount == null) {
            return false;
        }
        if (minWinAmount == null) {
            return true;
        }

        double minWinAmountInCurrentCurrency = currencyRateManager.convert(minWinAmount, baseCurrency,
                event.getCurrency());
        if (winAmount >= minWinAmountInCurrentCurrency) {
            prize.updateWins(winAmount);
            return true;
        }
        return false;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeInt(minWinAmount == null ? -1 : minWinAmount);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte ver = input.readByte();
        int l = input.readInt();
        minWinAmount = l < 0 ? null : l;
    }

    @Override
    public String toString() {
        return "WinQualifier{" +
                "minWinAmount=" + minWinAmount +
                '}';
    }
}
