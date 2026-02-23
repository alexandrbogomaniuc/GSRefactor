package com.dgphoenix.casino.promo.tournaments;

import com.dgphoenix.casino.account.AccountManager;
import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.currency.ICurrencyRateManager;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.promo.*;
import com.dgphoenix.casino.common.promo.feed.tournament.MaxBalanceRecord;
import com.dgphoenix.casino.common.promo.feed.tournament.TournamentFeed;
import com.dgphoenix.casino.common.util.Pair;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.gs.singlegames.tools.util.NumberUtils;
import com.dgphoenix.casino.promo.persisters.CassandraMaxBalanceTournamentPersister;
import com.dgphoenix.casino.promo.tournaments.messages.PlaceInfo;
import com.dgphoenix.casino.promo.tournaments.messages.PrizeInfo;
import com.dgphoenix.casino.promo.tournaments.messages.PrizePlaceInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


public class TournamentLeaderboardBuilder {
    private static final Logger LOG = LogManager.getLogger(TournamentLeaderboardBuilder.class);

    private final CassandraMaxBalanceTournamentPersister maxBalanceTournamentPersister;
    private final ICurrencyRateManager currencyRatesManager;

    public TournamentLeaderboardBuilder(CassandraMaxBalanceTournamentPersister maxBalanceTournamentPersister,
                                        ICurrencyRateManager currencyRatesManager) {
        this.maxBalanceTournamentPersister = maxBalanceTournamentPersister;
        this.currencyRatesManager = currencyRatesManager;
    }

    public TournamentLeaderboard createForPlayer(IPromoCampaign tournament, String currency,
                                                 String sessionId) throws CommonException {
        Set<RankPrize> prizes = getPrizes(tournament);
        long accountId = getAccountId(sessionId);
        List<MaxBalanceTournamentPlayerDetails> players = getSortedPlayers(tournament);
        List<PrizeInfo> prizeInfos = convertPrizes(prizes, tournament.getBaseCurrency(), currency);
        Pair<PlaceInfo, List<PlaceInfo>> places = combinePlaces(players, prizeInfos, accountId, tournament);
        return new TournamentLeaderboard(prizeInfos.size(), places.getValue(), places.getKey());
    }

    public TournamentLeaderboard createForNetworkPlayer(IPromoCampaign tournament, String currency, String sessionId,
                                                        String summaryFeed, NetworkPromoEvent networkPromoEvent)
            throws IOException, CommonException {
        Set<RankPrize> prizes = getPrizes(tournament);
        long accountId = getAccountId(sessionId);
        List<PrizeInfo> prizeInfos = convertPrizes(prizes, tournament.getBaseCurrency(), currency);
        List<Player> players = getPlayers(summaryFeed);
        Pair<PlaceInfo, List<PlaceInfo>> places =
                combineNetworkPlaces(players, prizeInfos, accountId, networkPromoEvent.getId());
        return new TournamentLeaderboard(prizeInfos.size(), places.getValue(), places.getKey());
    }

    private List<Player> getPlayers(String summaryFeed) throws IOException {
        if (StringUtils.isTrimmedEmpty(summaryFeed)) {
            return Collections.emptyList();
        }
        ObjectMapper mapper = new ObjectMapper();
        SummaryFeed feed = mapper.readValue(summaryFeed, SummaryFeed.class);
        return feed.getDataset().getPlayers();
    }

    protected Pair<PlaceInfo, List<PlaceInfo>> combineNetworkPlaces(List<Player> players, List<PrizeInfo> prizes,
                                                                    long accountId, long networkPromoEventId) {
        List<PlaceInfo> places = new ArrayList<>();
        MaxBalanceTournamentPlayerDetails currentPlayerDetails =
                maxBalanceTournamentPersister.getForAccount(accountId, networkPromoEventId);
        PlaceInfo currentPlayerInfo = null;
        int lastIndex = players.size() > prizes.size() && prizes.size() > 10 ? prizes.size() : Math.min(players.size(), 10);
        for (int i = 0; i < lastIndex; i++) {
            Player player = players.get(i);
            PlaceInfo placeInfo;
            long score = NumberUtils.decimalStringToCentsHalfUp(player.getScore());
            if (i < prizes.size()) {
                placeInfo = new PrizePlaceInfo(i + 1, player.getNickName(), score, prizes.get(i).getPrize());
            } else {
                placeInfo = new PlaceInfo(i + 1, player.getNickName(), score);
            }
            places.add(placeInfo);
            if (currentPlayerDetails != null && player.getNickName().equals(currentPlayerDetails.getNickname())) {
                currentPlayerInfo = placeInfo;
            }
        }

        if (currentPlayerInfo == null && currentPlayerDetails != null) {
            currentPlayerInfo = getCurrentNetworkPlayerInfo(players, currentPlayerDetails.getNickname(), prizes);
        }
        List<PlaceInfo> limited = places.stream()
                .limit(10)
                .collect(Collectors.toList());
        return new Pair<>(currentPlayerInfo, limited);
    }

    private long getAccountId(String sessionId) throws CommonException {
        if (sessionId == null) {
            return -1;
        }
        SessionHelper.getInstance().lock(sessionId);
        try {
            SessionHelper.getInstance().openSession();
            return SessionHelper.getInstance().getTransactionData().getAccountId();
        } finally {
            SessionHelper.getInstance().clearWithUnlock();
        }
    }

    public TournamentFeed createForExport(IPromoCampaign tournament) throws CommonException {
        Set<RankPrize> prizes = getPrizes(tournament);
        List<MaxBalanceTournamentPlayerDetails> players = getSortedPlayers(tournament);
        List<PrizeInfo> prizeInfos = convertPrizes(prizes);
        return createFeed(players, prizeInfos, tournament);
    }

    protected Set<RankPrize> getPrizes(IPromoCampaign tournament) throws CommonException {
        Set<IPrize> prizePool = tournament.getPrizePool();
        if (prizePool.size() == 1) {
            IPrize prizes = prizePool.iterator().next();
            if (prizes instanceof TournamentPrize) {
                return ((TournamentPrize) prizes).getPrizesPool();
            }
        }
        LOG.error("Prizes for tournament are not configured: {}", tournament);
        throw new CommonException("Bad tournament configuration");
    }

    protected List<PrizeInfo> convertPrizes(Set<RankPrize> prizes) {
        List<PrizeInfo> result = new ArrayList<>();
        for (RankPrize rankPrize : prizes) {
            RankRange range = rankPrize.getRankRange();
            long prize = ((InstantMoneyPrize) rankPrize.getPrize()).getAmount();
            for (int i = range.getStart(); i <= range.getEnd(); i++) {
                result.add(new PrizeInfo(i, prize));
            }
        }
        result.sort(Comparator.comparingInt(PrizeInfo::getPlace));
        return result;
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

    private List<MaxBalanceTournamentPlayerDetails> getSortedPlayers(IPromoCampaign campaign) {
        Comparator<MaxBalanceTournamentPlayerDetails> comparator = getDetailsComparator(campaign);
        return maxBalanceTournamentPersister
                .getByTournament(campaign.getId())
                .stream()
                .filter(player -> (player != null && player.getNickname() != null && !player.getNickname().isEmpty()))
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    private Comparator<MaxBalanceTournamentPlayerDetails> getDetailsComparator(IPromoCampaign campaign) {
        TournamentObjective objective = ((TournamentPromoTemplate<?>) campaign.getTemplate()).getObjective();
        if (objective == TournamentObjective.TOURNAMENT_MAX_BET_SUM) {
            return Comparator
                    .comparing(MaxBalanceTournamentPlayerDetails::getBetAmount)
                    .thenComparing(MaxBalanceTournamentPlayerDetails::getCurrentBalance)
                    .reversed();
        } else {
            return Comparator
                    .comparing(MaxBalanceTournamentPlayerDetails::getCurrentBalance)
                    .thenComparing(MaxBalanceTournamentPlayerDetails::getBetAmount)
                    .reversed();
        }
    }

    protected Pair<PlaceInfo, List<PlaceInfo>> combinePlaces(List<MaxBalanceTournamentPlayerDetails> players,
                                                             List<PrizeInfo> prizes, long accountId,
                                                             IPromoCampaign tournament) {
        List<PlaceInfo> places = new ArrayList<>();
        PlaceInfo currentPlayerInfo = null;
        int lastIndex = players.size() > prizes.size() && prizes.size() > 10 ? prizes.size() : Math.min(players.size(), 10);
        for (int i = 0; i < lastIndex; i++) {
            MaxBalanceTournamentPlayerDetails player = players.get(i);
            PlaceInfo placeInfo;
            long score = getPlayerScore(tournament, player);
            if (i < prizes.size()) {
                placeInfo = new PrizePlaceInfo(i + 1, player.getNickname(), score, prizes.get(i).getPrize());
            } else {
                placeInfo = new PlaceInfo(i + 1, player.getNickname(), score);
            }
            places.add(placeInfo);
            if (player.getAccountId() == accountId) {
                currentPlayerInfo = placeInfo;
            }
        }
        if (currentPlayerInfo == null) {
            currentPlayerInfo = getCurrentPlayerInfo(players, accountId, prizes, tournament);
        }
        List<PlaceInfo> limited = places.stream()
                .limit(10)
                .collect(Collectors.toList());
        return new Pair<>(currentPlayerInfo, limited);
    }

    private long getPlayerScore(IPromoCampaign tournament, MaxBalanceTournamentPlayerDetails player) {
        TournamentObjective objective = ((TournamentPromoTemplate<?>) tournament.getTemplate()).getObjective();
        return objective == TournamentObjective.TOURNAMENT_MAX_BET_SUM ? player.getBetAmount() : player.getCurrentBalance();
    }

    private PlaceInfo getCurrentPlayerInfo(List<MaxBalanceTournamentPlayerDetails> players, long accountId,
                                           List<PrizeInfo> prizes, IPromoCampaign tournament) {
        for (int i = 0; i < players.size(); i++) {
            MaxBalanceTournamentPlayerDetails details = players.get(i);
            if (details.getAccountId() == accountId) {
                long score = getPlayerScore(tournament, details);
                if (i < prizes.size() && prizes.get(i) != null) {
                    return new PrizePlaceInfo(i + 1, details.getNickname(), score, prizes.get(i).getPrize());
                } else {
                    return new PlaceInfo(i + 1, details.getNickname(), score);
                }
            }
        }
        return null;
    }

    private PlaceInfo getCurrentNetworkPlayerInfo(List<Player> players, String nickname, List<PrizeInfo> prizes) {
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            if (player.getNickName().equals(nickname)) {
            long score = NumberUtils.decimalStringToCentsHalfUp(player.getScore());
                if (i < prizes.size() && prizes.get(i) != null) {
                    return new PrizePlaceInfo(i + 1, player.getNickName(), score, prizes.get(i).getPrize());
                } else {
                    return new PlaceInfo(i + 1, player.getNickName(), score);
                }
            }
        }
        return null;
    }

    private TournamentFeed createFeed(List<MaxBalanceTournamentPlayerDetails> players, List<PrizeInfo> prizes,
                                      IPromoCampaign tournament) {
        TournamentFeed tournamentFeed = new TournamentFeed();
        for (int i = 0; i < players.size(); i++) {
            MaxBalanceTournamentPlayerDetails player = players.get(i);
            if (player.getBankId() == 0) {
                AccountInfo accountInfo = AccountManager.getInstance().getAccountInfo(player.getAccountId());
                player.setBankId(accountInfo.getBankId());
                player.setExtAccountId(accountInfo.getExternalId());
            }
            long score = getPlayerScore(tournament, player);
            tournamentFeed.addRecord(new MaxBalanceRecord(i + 1, player.getBankId(),
                    player.getExtAccountId(), player.getNickname(),
                    String.format("%.2f", (double) score / 100),
                    i < prizes.size() ? String.format("%.2f", (double) prizes.get(i).getPrize() / 100) : ""));
        }
        return tournamentFeed;
    }

    private static class SummaryFeed {
        private Dataset dataset;

        public Dataset getDataset() {
            return dataset;
        }

        public void setDataset(Dataset dataset) {
            this.dataset = dataset;
        }

        @Override
        public String toString() {
            return "Json{" +
                    "dataset=" + dataset +
                    '}';
        }
    }

    private static class Dataset {
        private List<Player> players;

        public List<Player> getPlayers() {
            return players;
        }

        public void setPlayers(List<Player> players) {
            this.players = players;
        }

        @Override
        public String toString() {
            return "Dataset{" +
                    "players=" + players +
                    '}';
        }
    }

    protected static class Player {
        private String bankName;
        private String nickName;
        private String score;

        public String getBankName() {
            return bankName;
        }

        public void setBankName(String bankName) {
            this.bankName = bankName;
        }

        public String getNickName() {
            return nickName;
        }

        public void setNickName(String nickName) {
            this.nickName = nickName;
        }

        public String getScore() {
            return score;
        }

        public void setScore(String score) {
            this.score = score;
        }

        @Override
        public String toString() {
            return "Player{" +
                    "bankName='" + bankName + '\'' +
                    ", nickname='" + nickName + '\'' +
                    ", score='" + score + '\'' +
                    '}';
        }
    }
}
