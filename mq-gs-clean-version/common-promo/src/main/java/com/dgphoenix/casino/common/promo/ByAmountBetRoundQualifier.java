package com.dgphoenix.casino.common.promo;

import com.dgphoenix.casino.common.currency.ICurrencyRateManager;
import com.dgphoenix.casino.common.exception.CommonException;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class ByAmountBetRoundQualifier implements IParticipantEventQualifier {
    private static final byte VERSION = 0;

    private IPlayerBetQualifier betQualifier;

    public ByAmountBetRoundQualifier(IPlayerBetQualifier betQualifier) {
        this.betQualifier = betQualifier;
    }

    @Override
    public boolean qualifyBet(PromoCampaignMember member, DesiredPrize prize, PlayerBetEvent event,
                              ICurrencyRateManager currencyRateManager, String baseCurrency) throws CommonException {
        boolean qualified = betQualifier != null && betQualifier.qualifyBet(member, prize, event, currencyRateManager, baseCurrency);
        prize.setRoundBet(event.getGameId(), event.getBetAmount());
        prize.getRoundQualification().get(event.getGameId()).setBetQualified(qualified);
        return qualified;
    }

    @Override
    public boolean qualifyBonus(IPromoCampaign campaign, PromoCampaignMember member, DesiredPrize prize,
                                PlayerBonusEvent event) {
        return false;
    }

    @Override
    public boolean qualifyWin(IPromoCampaign campaign, PromoCampaignMember member, DesiredPrize prize,
                              PlayerWinEvent event, ICurrencyRateManager currencyRateManager, String baseCurrency) {
        long winAmount = event.getWinAmount() != null ? event.getWinAmount() : 0;
        boolean betQualified = prize.isBetQualified(event.getGameId());
        prize.incrementRoundWin(event.getGameId(), winAmount);
        prize.getRoundQualification().get(event.getGameId()).setWinQualified(true);
        if(betQualified) {
            prize.updateWins(winAmount);
            return true;
        }
        return false;
    }

    public boolean qualifyEndRound(IPromoCampaign campaign, PromoCampaignMember member, DesiredPrize desiredPrize,
                                   EndRoundEvent event, ICurrencyRateManager currencyRateManager, String baseCurrency) throws CommonException {
        boolean betQualified = desiredPrize.isBetQualified(event.getGameId());
        if (betQualified) {
            desiredPrize.updateMaxQualifiedRoundWin(event.getGameId());
            desiredPrize.updateMaxQualifiedRoundRtp(event.getGameId());
        }
        desiredPrize.resetRoundQualification(event.getGameId());
        return betQualified;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        kryo.writeClassAndObject(output, betQualifier);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte ver = input.readByte();
        betQualifier = (IPlayerBetQualifier) kryo.readClassAndObject(input);
    }

    @Override
    public String toString() {
        return "ByAmountBetRoundQualifier{" +
                "betQualifier=" + betQualifier +
                '}';
    }
}
