package com.dgphoenix.casino.common.promo;

import com.dgphoenix.casino.common.currency.ICurrencyRateManager;
import com.dgphoenix.casino.common.exception.CommonException;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * User: flsh
 * Date: 20.09.2019.
 */
public class MaxPerformanceEventQualifier implements IParticipantEventQualifier {
    private static final byte VERSION = 0;
    private int minBetAmount;
    private boolean notCountWeaponBoxPurchases;

    @Override
    public boolean qualifyBet(PromoCampaignMember member, DesiredPrize prize, PlayerBetEvent event,
                              ICurrencyRateManager currencyRateManager, String baseCurrency) throws CommonException {
        double minBetAmountInCurrentCurrency = currencyRateManager.convert(minBetAmount, baseCurrency,
                event.getCurrency());
        if (event.getBetAmount() >= minBetAmountInCurrentCurrency) {
            prize.updateBets(1, event.getBetAmount());
            return true;
        }
        return false;
    }

    @Override
    public boolean qualifyBonus(IPromoCampaign campaign, PromoCampaignMember member, DesiredPrize prize,
                                PlayerBonusEvent event) {
        return false;
    }

    @Override
    public boolean qualifyWin(IPromoCampaign campaign, PromoCampaignMember member, DesiredPrize prize,
                              PlayerWinEvent event, ICurrencyRateManager currencyRateManager, String baseCurrencys) {
        Long winAmount = event.getWinAmount();
        if (winAmount != null && winAmount > 0) {
            prize.updateWins(winAmount);
        }
        return true;
    }

    public int getMinBetAmount() {
        return minBetAmount;
    }

    public boolean isNotCountWeaponBoxPurchases() {
        return notCountWeaponBoxPurchases;
    }

    public void setMinBetAmount(int minBetAmount) {
        this.minBetAmount = minBetAmount;
    }

    public void setNotCountWeaponBoxPurchases(boolean notCountWeaponBoxPurchases) {
        this.notCountWeaponBoxPurchases = notCountWeaponBoxPurchases;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeInt(minBetAmount, true);
        output.writeBoolean(notCountWeaponBoxPurchases);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte ver = input.readByte();
        minBetAmount = input.readInt(true);
        notCountWeaponBoxPurchases = input.readBoolean();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MaxPerformanceEventQualifier [");
        sb.append("minBetAmount=").append(minBetAmount);
        sb.append(", notCountWeaponBoxPurchases=").append(notCountWeaponBoxPurchases);
        sb.append(']');
        return sb.toString();
    }
}
