package com.dgphoenix.casino.common.promo;

import com.dgphoenix.casino.common.currency.ICurrencyRateManager;
import com.dgphoenix.casino.common.exception.CommonException;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * User: flsh
 * Date: 20.09.2019.
 */
public class HighestWinEventQualifier implements IParticipantEventQualifier {
    private static final Logger LOG = LogManager.getLogger(HighestWinEventQualifier.class);
    private static final byte VERSION = 0;
    private int minBetAmount;

    @Override
    public boolean qualifyBet(PromoCampaignMember member, DesiredPrize prize, PlayerBetEvent event,
                              ICurrencyRateManager currencyRateManager, String baseCurrency) throws CommonException {
        double minBetAmountInCurrentCurrency = currencyRateManager.convert(minBetAmount, baseCurrency,
                event.getCurrency());
        long betAmount = event.getBetAmount();
        if (betAmount >= minBetAmountInCurrentCurrency) {
            prize.updateBets(1, betAmount);
            TournamentMemberRank tournamentMemberRank = event.getTournamentMemberRank();
            LOG.debug("Event: " + event);
            if (tournamentMemberRank == null) {
                tournamentMemberRank = new TournamentMemberRank(member.getCampaignId(), 0, member.getAccountId(),
                        member.getBankId(), event.getAccountExternalId(), member.getDisplayName(),
                        prize.getQualifiedBetsCount(), prize.getQualifiedBetSum(), prize.getTotalQualifiedWinSum(),
                        0, System.currentTimeMillis(), member.getEnterTime());
                event.setTournamentMemberRank(tournamentMemberRank);
            }
            Long roundId = event.getRoundId();
            if (roundId != null) {
                RoundStat roundStat = new RoundStat(roundId, betAmount);
                tournamentMemberRank.addRoundStat(event.getGameId(), roundStat);
            }
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
                              PlayerWinEvent event, ICurrencyRateManager currencyRateManager, String baseCurrency) {
        Long winAmount = event.getWinAmount();
        if (winAmount == null || winAmount <= 0) {
            return false;
        }
        TournamentMemberRank tournamentMemberRank = event.getTournamentMemberRank();
        LOG.debug("Event: " + event);
        if (tournamentMemberRank != null) {
            RoundStat roundStat = tournamentMemberRank.getRoundStat(event.getGameId());
            if (roundStat == null) {
                LOG.warn("RoundStat is null [may be if bet not qualified] memberRank=" + tournamentMemberRank);
            } else {
                roundStat.incrementRoundSummaryWin(event.getWinAmount());
            }
        }
        prize.updateWins(winAmount);
        return true;
    }

    public int getMinBetAmount() {
        return minBetAmount;
    }

    public void setMinBetAmount(int minBetAmount) {
        this.minBetAmount = minBetAmount;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeInt(minBetAmount, true);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte ver = input.readByte();
        minBetAmount = input.readInt(true);
    }

    @Override
    public String toString() {
        return "HighestWinEventQualifier [" + "minBetAmount=" + minBetAmount +
                ']';
    }
}
