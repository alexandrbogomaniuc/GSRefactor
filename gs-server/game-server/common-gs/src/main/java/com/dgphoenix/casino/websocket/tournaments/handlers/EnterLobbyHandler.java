package com.abs.casino.websocket.tournaments.handlers;

import com.dgphoenix.casino.account.AccountManager;
import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.BaseGameCache;
import com.dgphoenix.casino.common.cache.CurrencyCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.account.IAccountInfo;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.currency.Currency;
import com.dgphoenix.casino.common.cache.data.game.GameMode;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.abs.casino.common.currency.CurrencyRate;
import com.abs.casino.common.currency.ICurrencyRateManager;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.promo.*;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.abs.casino.common.util.DatePeriod;
import com.dgphoenix.casino.common.util.Pair;
import com.dgphoenix.casino.gs.managers.payment.currency.CurrencyRatesManager;
import com.dgphoenix.casino.gs.socket.mq.MQServiceHandler;
import com.dgphoenix.casino.gs.socket.mq.TournamentBuyInHelper;
import com.abs.casino.promo.PromoCampaignManager;
import com.abs.casino.promo.icon.TournamentIcon;
import com.abs.casino.promo.persisters.CassandraBattlegroundConfigPersister;
import com.abs.casino.promo.persisters.CassandraLocalizationsPersister;
import com.abs.casino.promo.persisters.CassandraMaxBalanceTournamentPersister;
import com.abs.casino.promo.persisters.CassandraTournamentIconPersister;
import com.abs.casino.promo.tournaments.ErrorCodes;
import com.abs.casino.promo.tournaments.messages.*;
import com.abs.casino.promo.tournaments.messages.*;
import com.dgphoenix.casino.support.ErrorPersisterHelper;
import com.abs.casino.websocket.tournaments.IMessageHandler;
import com.abs.casino.websocket.tournaments.ISocketClient;
import com.abs.casino.websocket.tournaments.TournamentClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class EnterLobbyHandler implements IMessageHandler<EnterLobby> {
    private static final Logger LOG = LogManager.getLogger(EnterLobbyHandler.class);
    private static final String DEFAULT_LOCALIZATION = "en";

    private final IPromoCampaignManager promoCampaignManager;
    private final ICurrencyRateManager currencyRateManager;
    private final CassandraMaxBalanceTournamentPersister maxBalanceTournamentPersister;
    private final CassandraLocalizationsPersister localizationsPersister;
    private final CassandraTournamentIconPersister iconPersister;
    private final MQServiceHandler serviceHandler;
    private final ErrorPersisterHelper errorPersisterHelper;
    private final CassandraBattlegroundConfigPersister cassandraBattlegroundConfigPersister;
    private final TournamentBuyInHelper buyInHelper;

    public EnterLobbyHandler(IPromoCampaignManager promoCampaignManager,
                             ICurrencyRateManager currencyRateManager,
                             CassandraPersistenceManager cpm,
                             ErrorPersisterHelper errorPersisterHelper, TournamentBuyInHelper buyInHelper) {
        this.promoCampaignManager = promoCampaignManager;
        this.currencyRateManager = currencyRateManager;
        this.maxBalanceTournamentPersister = cpm.getPersister(CassandraMaxBalanceTournamentPersister.class);
        this.localizationsPersister = cpm.getPersister(CassandraLocalizationsPersister.class);
        this.iconPersister = cpm.getPersister(CassandraTournamentIconPersister.class);
        this.serviceHandler = ApplicationContextHelper.getApplicationContext().getBean(MQServiceHandler.class);
        this.errorPersisterHelper = errorPersisterHelper;
        this.cassandraBattlegroundConfigPersister = cpm.getPersister(CassandraBattlegroundConfigPersister.class);
        this.buyInHelper = buyInHelper;
    }

    @Override
    public void handle(EnterLobby message, ISocketClient client) {
        String sessionId = message.getSid();
        LOG.debug("enterLobby, session id={}", sessionId);
        if (sessionId == null || !sessionId.equals(client.getSessionId())) {
            LOG.error("Session mismatch: {}, {}", sessionId, client.getSessionId());
            client.sendMessage(createErrorMessage(ErrorCodes.INVALID_SESSION, "Session mismatch", message.getRid()));
            return;
        }
        try {
            AccountInfo accountInfo;
            SessionHelper.getInstance().lock(sessionId);
            try {
                SessionHelper.getInstance().openSession();
                SessionInfo sessionInfo = SessionHelper.getInstance().getTransactionData().getPlayerSession();
                if (sessionInfo == null) {
                    LOG.warn("SessionInfo not found: {}", sessionId);
                    client.sendMessage(createErrorMessage(ErrorCodes.INVALID_SESSION, "Session not found", message.getRid(),
                            error -> errorPersisterHelper.persistTournamentError(sessionId, error, message)));
                    return;
                }
                accountInfo = AccountManager.getInstance().getAccountInfo(sessionInfo.getAccountId());
            } finally {
                SessionHelper.getInstance().clearWithUnlock();
            }

            BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(accountInfo.getBankId());
            Currency playerCurrency = CurrencyCache.getInstance().get(accountInfo.getCurrency().getCode());
            Pair<String, String> pair = new Pair<>(playerCurrency.getCode(), ICurrencyRateManager.DEFAULT_CURRENCY);
            CurrencyRate currencyRate = CurrencyRatesManager.getInstance().get(pair);
            LOG.debug("EnterLobby - currencyRate: {}", currencyRate);
            client.connect(sessionId, playerCurrency.getCode(), message.getLang(), message.getCdn());
            long balance = accountInfo.getBalance();
            try {
                balance = serviceHandler.getBalance(client.getSessionId(), GameMode.REAL.name());
                LOG.debug("getBalance: accountId={}, balance={}", accountInfo.getId(), balance);
            } catch (Exception e) {
                LOG.error("Cannot getBalance, account={}", accountInfo, e);
            }
            long gameId = message.getGameId() == null ? 1 : message.getGameId();
            NetworkPromoCampaign networkPromoCampaign = getNetworkPromoCampaign(gameId, bankInfo.getId());
            NetworkTournament networkTournament = getNetworkTournament(message, networkPromoCampaign, accountInfo);
            List<ShortTournamentInfo> tournaments =
                    getTournamentsInfo(client, accountInfo, bankInfo, gameId, networkPromoCampaign);


            Set<BattlegroundInfo> battlegroundInfos = null;
            if (client instanceof TournamentClient) {
                battlegroundInfos = buyInHelper.getBattlegroundInfos(client.getSessionId(), bankInfo.getId(),
                        accountInfo.getCurrency().getCode());
            }

            client.sendMessage(new EnterLobbyResponse(System.currentTimeMillis(),
                    message.getRid(),
                    balance > 0 ? balance : 0,
                    accountInfo.getCurrency().getSymbol(),
                    tournaments,
                    networkTournament,
                    battlegroundInfos,
                    accountInfo.getCurrency().getCode()));
        } catch (Exception e) {
            LOG.error("Unable to EnterLobby", e);
            client.sendMessage(createErrorMessage(ErrorCodes.BAD_REQUEST, "Bad request", message.getRid(),
                    error -> errorPersisterHelper.persistTournamentError(sessionId, error, message, e)));
        }
    }


    private NetworkPromoCampaign getNetworkPromoCampaign(long gameId, long bankId) {
        if (gameId > 0) {
            return null;
        }
        return ((PromoCampaignManager) promoCampaignManager).getNetworkPromoCampaignByBank(bankId);
    }

    private List<ShortTournamentInfo> getTournamentsInfo(ISocketClient client, AccountInfo accountInfo,
                                                         BankInfo bankInfo, long gameId,
                                                         NetworkPromoCampaign networkPromoCampaign) throws CommonException {
        String lang = client.getLang();
        if (gameId < 1) {
            return fetchTournaments(accountInfo, bankInfo, networkPromoCampaign, lang);
        }
        return getAvailableTournaments(bankInfo, accountInfo, lang);
    }

    private List<ShortTournamentInfo> fetchTournaments(AccountInfo accountInfo, BankInfo bankInfo,
                                                       NetworkPromoCampaign networkPromoCampaign,
                                                       String lang) throws CommonException {
        List<ShortTournamentInfo> infos = new ArrayList<>();
        if (networkPromoCampaign == null) {
            return infos;
        }
        for (NetworkPromoEvent networkPromoEvent : networkPromoCampaign.getEvents()) {
            infos.add(convert(networkPromoEvent, accountInfo, lang, getMinCoinForGames(bankInfo,
                    networkPromoEvent.getGameIds(), networkPromoEvent.getBaseCurrency())));
        }
        return infos;
    }

    private NetworkTournament getNetworkTournament(EnterLobby message, NetworkPromoCampaign networkPromoCampaign,
                                                   AccountInfo accountInfo) {
        if (networkPromoCampaign == null) {
            return null;
        }
        List<Long> events = fetchEvents(networkPromoCampaign);
        DatePeriod actionPeriod = networkPromoCampaign.getActionPeriod();
        LocalizationTitles localizationTitle = getLocalizationTitle(message, networkPromoCampaign);
        MaxBalanceTournamentPlayerDetails details =
                maxBalanceTournamentPersister.getForAccount(accountInfo.getId(), networkPromoCampaign.getId());
        boolean joined = details != null;
        List<Long> gameIds = filterGameIds(networkPromoCampaign, accountInfo);
        return new NetworkTournament(networkPromoCampaign.getId(), networkPromoCampaign.getName(),
                actionPeriod.getStartDate().getTime(), actionPeriod.getEndDate().getTime(), 0, 0, 0, 0, false, 0,
                networkPromoCampaign.getTotalPrizePool(), joined, getPromoStatus(networkPromoCampaign), null,
                gameIds, false, events,
                localizationTitle.getTournamentRules(), localizationTitle.getPrizeAllocation(),
                localizationTitle.getHowToWin());
    }

    private LocalizationTitles getLocalizationTitle(EnterLobby message, NetworkPromoCampaign networkPromoCampaign) {
        LocalizationTitles title = networkPromoCampaign.getLocalizationTitle(message.getLang());
        return title == null ? networkPromoCampaign.getLocalizationTitle(DEFAULT_LOCALIZATION) : title;
    }

    private List<Long> filterGameIds(NetworkPromoCampaign networkPromoCampaign, AccountInfo accountInfo) {
        Currency currency = CurrencyCache.getInstance().get(networkPromoCampaign.getBaseCurrency());
        Set<Long> allGamesSet = BaseGameCache.getInstance().getAllGamesSet(accountInfo.getBankId(), currency);
        return networkPromoCampaign.getGameIds().stream()
                .filter(allGamesSet::contains)
                .collect(Collectors.toList());
    }

    private List<Long> fetchEvents(NetworkPromoCampaign networkPromoCampaign) {
        return networkPromoCampaign.getEvents().stream()
                .map(PromoCampaign::getId)
                .collect(Collectors.toList());
    }

    private List<ShortTournamentInfo> getAvailableTournaments(BankInfo bankInfo, IAccountInfo accountInfo, String lang)
            throws CommonException {
        long bankId = bankInfo.getId();
        Set<IPromoCampaign> campaigns = promoCampaignManager
                .getTournamentsForMultiplayerGames(bankId, accountInfo.getCurrency().getCode())
                .stream()
                .filter(campaign -> campaign.getTemplate().getPromoType().isScoreCounting())
                .collect(Collectors.toSet());
        List<ShortTournamentInfo> tournaments = new ArrayList<>();
        for (IPromoCampaign campaign : campaigns) {
            if (campaign.getTemplate().getPromoType().isTournamentLogic() &&
                    isNotExpired(campaign, bankInfo.getTournamentExcludeTime())) {
                long minCoin = getMinCoin(bankInfo, campaign.getGameIds().iterator().next(), campaign.getBaseCurrency());
                tournaments.add(convert(campaign, accountInfo, lang, minCoin));
            }
        }
        return tournaments;
    }

    private long getMinCoin(BankInfo bankInfo, long gameId, String currency) {
        return serviceHandler.getCoins(bankInfo, gameId, CurrencyCache.getInstance().get(currency))
                .stream()
                .min(Long::compareTo)
                .orElse(1L);
    }

    private long getMinCoinForGames(BankInfo bankInfo, Set<Long> gameIds, String currency) {
        return gameIds.stream()
                .map(gameId -> serviceHandler.getCoins(bankInfo, gameId, CurrencyCache.getInstance().get(currency)))
                .flatMap(Collection::stream)
                .min(Long::compareTo)
                .orElse(1L);
    }

    private boolean isNotExpired(IPromoCampaign promoCampaign, long excludeTime) {
        return System.currentTimeMillis() - promoCampaign.getActionPeriod().getEndDate().getTime() < excludeTime;
    }

    private ShortTournamentInfo convert(IPromoCampaign campaign, IAccountInfo accountInfo, String lang,
                                        long minCoin) throws CommonException {
        MaxBalanceTournamentPromoTemplate template = (MaxBalanceTournamentPromoTemplate) campaign.getTemplate();
        MaxBalanceTournamentPlayerDetails details =
                maxBalanceTournamentPersister.getForAccount(accountInfo.getId(), campaign.getId());
        boolean joined = details != null && isPlayerCanJoin(template, details, minCoin, campaign);
        boolean cannotJoin = details != null && !isPlayerCanJoin(template, details, minCoin, campaign);
        long buyInPrice = (long) currencyRateManager
                .convert(template.getBuyInPrice(), campaign.getBaseCurrency(), accountInfo.getCurrency().getCode());
        long reBuyPrice = (long) currencyRateManager
                .convert(template.getReBuyPrice(), campaign.getBaseCurrency(), accountInfo.getCurrency().getCode());
        long prize = (long) currencyRateManager
                .convert(template.getPrize(), campaign.getBaseCurrency(), accountInfo.getCurrency().getCode());
        String title = localizationsPersister.getLocalizedPromoTitle(campaign.getId(), lang);
        Long networkTournamentId = getNetworkTournamentId(campaign);
        return new ShortTournamentInfo(campaign.getId(),
                title == null ? campaign.getName() : title,
                campaign.getActionPeriod().getStartDate().getTime(),
                campaign.getActionPeriod().getEndDate().getTime(),
                buyInPrice,
                template.getBuyInAmount(),
                reBuyPrice,
                template.getReBuyAmount(),
                template.isReBuyEnabled(),
                template.getReBuyLimit(),
                prize,
                joined,
                getPromoStatus(campaign),
                getIconUrl(template),
                new ArrayList<>(campaign.getGameIds()),
                cannotJoin,
                networkTournamentId);
    }

    private String getPromoStatus(IPromoCampaign campaign) {
        Status status = campaign.getStatus();
        if (status == Status.QUALIFICATION) {
            return Status.FINISHED.name();
        }
        return status.name();
    }

    private Long getNetworkTournamentId(IPromoCampaign campaign) {
        if (campaign instanceof NetworkPromoEvent) {
            NetworkPromoEvent event = (NetworkPromoEvent) campaign;
            return event.getParentPromoCampaignId();
        }
        return null;
    }

    private boolean isPlayerCanJoin(MaxBalanceTournamentPromoTemplate template, MaxBalanceTournamentPlayerDetails details,
                                    long minCoin, IPromoCampaign campaign) {
        if (campaign.isNetworkPromoCampaign()) {
            return true;
        } else if (campaign instanceof NetworkPromoEvent) {
            return details.getCurrentBalance() >= minCoin;
        }
        return (template.isReBuyEnabled() &&
                (template.getReBuyLimit() == -1 || details.getReBuyCount() < template.getReBuyLimit())) ||
                details.getCurrentBalance() >= minCoin;
    }

    private String getIconUrl(MaxBalanceTournamentPromoTemplate template) {
        TournamentIcon icon = iconPersister.getById(template.getIconId());
        if (icon != null) {
            return icon.getHttpAddress();
        }
        return "";
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}
