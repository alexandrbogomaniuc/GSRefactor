package com.abs.casino.websocket.tournaments.handlers;

import com.abs.casino.common.currency.ICurrencyRateManager;
import com.abs.casino.common.exception.CommonException;
import com.abs.casino.common.promo.*;
import com.abs.casino.common.transport.TObject;
import com.abs.casino.promo.tournaments.messages.PrizeInfo;
import com.abs.casino.websocket.tournaments.IMessageHandler;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public abstract class AbstractLobbyHandler<MESSAGE extends TObject> implements IMessageHandler<MESSAGE> {

    protected final ICurrencyRateManager currencyRatesManager;

    public AbstractLobbyHandler(ICurrencyRateManager currencyRatesManager) {
        this.currencyRatesManager = currencyRatesManager;
    }

    protected Set<RankPrize> getPrizes(IPromoCampaign campaign) {
        Set<IPrize> prizePool = campaign.getPrizePool();
        if (prizePool.size() == 1) {
            IPrize prizes = prizePool.iterator().next();
            if (prizes instanceof TournamentPrize) {
                return ((TournamentPrize) prizes).getPrizesPool();
            }
        }
        getLog().error("Tournament has bad configuration: {}", campaign);
        return null;
    }

    protected List<PrizeInfo> convertPrizes(Set<RankPrize> prizes, String tournamentCurrency, String playerCurrency) throws CommonException {
        List<PrizeInfo> result = new ArrayList<>();
        for (RankPrize rankPrize : prizes) {
            RankRange range = rankPrize.getRankRange();
            long prize = convertPrize(rankPrize.getPrize(), tournamentCurrency, playerCurrency);
            for (int i = range.getStart(); i <= range.getEnd(); i++) {
                result.add(new PrizeInfo(i, prize));
            }
        }
        result.sort(Comparator.comparingInt(PrizeInfo::getPlace));
        return result;
    }

    private long convertPrize(IMaterialPrize prize, String tournamentCurrency, String playerCurrency) throws CommonException {
        if (prize instanceof InstantMoneyPrize) {
            return (long) currencyRatesManager.convert(((InstantMoneyPrize) prize).getAmount(),
                    tournamentCurrency, playerCurrency);
        } else {
            return 0;
        }
    }
}
