package com.abs.casino.common.promo;
import com.dgphoenix.casino.common.promo.*;

import com.abs.casino.common.currency.ICurrencyRateManager;
import com.abs.casino.common.exception.CommonException;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * User: flsh
 * Date: 25.01.17.
 */
public class DelegatedEventQualifier implements IParticipantEventQualifier {
    private IPlayerBetQualifier betQualifier;
    private IPlayerWinQualifier winQualifier;
    private IPlayerBonusQualifier bonusQualifier;

    private DelegatedEventQualifier() {
    }

    public DelegatedEventQualifier(IPlayerBetQualifier betQualifier,
                                   IPlayerWinQualifier winQualifier,
                                   IPlayerBonusQualifier bonusQualifier) {
        this.betQualifier = betQualifier;
        this.winQualifier = winQualifier;
        this.bonusQualifier = bonusQualifier;
    }

    @Override
    public boolean qualifyBet(PromoCampaignMember member, DesiredPrize prize, PlayerBetEvent event,
                              ICurrencyRateManager currencyRateManager, String baseCurrency) throws CommonException {
        return betQualifier != null &&
                betQualifier.qualifyBet(member, prize, event, currencyRateManager, baseCurrency);
    }

    @Override
    public boolean qualifyBonus(IPromoCampaign campaign, PromoCampaignMember member, DesiredPrize prize,
                                PlayerBonusEvent event) throws CommonException {
        return bonusQualifier != null && bonusQualifier.qualifyBonus(campaign, member, prize, event);
    }

    @Override
    public boolean qualifyWin(IPromoCampaign campaign, PromoCampaignMember member, DesiredPrize prize,
                              PlayerWinEvent event, ICurrencyRateManager currencyRateManager, String baseCurrency) throws CommonException {
        return winQualifier != null &&
                winQualifier.qualifyWin(campaign, member, prize, event, currencyRateManager, baseCurrency);
    }

    @Override
    public void write(Kryo kryo, Output output) {
        kryo.writeClassAndObject(output, betQualifier);
        kryo.writeClassAndObject(output, winQualifier);
        kryo.writeClassAndObject(output, bonusQualifier);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        betQualifier = (IPlayerBetQualifier) kryo.readClassAndObject(input);
        winQualifier = (IPlayerWinQualifier) kryo.readClassAndObject(input);
        bonusQualifier = (IPlayerBonusQualifier) kryo.readClassAndObject(input);
    }

    public IPlayerBetQualifier getBetQualifier() {
        return betQualifier;
    }

    public IPlayerWinQualifier getWinQualifier() {
        return winQualifier;
    }

    public IPlayerBonusQualifier getBonusQualifier() {
        return bonusQualifier;
    }

    @Override
    public String toString() {
        return "DelegatedEventQualifier[" +
                "betQualifier=" + betQualifier +
                ", winQualifier=" + winQualifier +
                ", bonusQualifier=" + bonusQualifier +
                ']';
    }
}
