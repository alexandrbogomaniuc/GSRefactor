package com.dgphoenix.casino.gs.socket.mq;

import com.dgphoenix.casino.account.AccountManager;
import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.cassandra.persist.*;
import com.abs.casino.cassandra.persist.mp.*;
import com.abs.casino.common.GameSessionExtendedProperties;
import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.BaseGameCache;
import com.dgphoenix.casino.common.cache.BaseGameInfoTemplateCache;
import com.dgphoenix.casino.common.cache.CurrencyCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.bank.Coin;
import com.abs.casino.common.cache.data.bank.ILimit;
import com.dgphoenix.casino.common.cache.data.bet.PlayerBet;
import com.dgphoenix.casino.common.cache.data.bonus.Bonus;
import com.dgphoenix.casino.common.cache.data.bonus.BonusStatus;
import com.dgphoenix.casino.common.cache.data.bonus.FRBonus;
import com.dgphoenix.casino.common.cache.data.currency.Currency;
import com.abs.casino.common.cache.data.game.BaseGameConstants;
import com.dgphoenix.casino.common.cache.data.game.BaseGameInfoTemplate;
import com.dgphoenix.casino.common.cache.data.game.GameType;
import com.dgphoenix.casino.common.cache.data.game.IBaseGameInfo;
import com.dgphoenix.casino.common.cache.data.payment.IWallet;
import com.dgphoenix.casino.common.cache.data.payment.PaymentMode;
import com.dgphoenix.casino.common.cache.data.payment.WalletOperationStatus;
import com.dgphoenix.casino.common.cache.data.payment.WalletOperationType;
import com.abs.casino.common.cache.data.payment.transfer.ExternalPaymentTransaction;
import com.abs.casino.common.cache.data.payment.transfer.TransactionStatus;
import com.abs.casino.common.cache.data.payment.transfer.TransactionType;
import com.dgphoenix.casino.common.cache.data.session.ClientType;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.abs.casino.common.client.canex.request.friends.Friend;
import com.abs.casino.common.client.canex.request.friends.GetFriendsResponse;
import com.abs.casino.common.client.canex.request.onlineplayer.GetOnlinePlayersResponse;
import com.abs.casino.common.client.canex.request.onlineplayer.OnlinePlayer;
import com.abs.casino.common.client.canex.request.onlinerooms.Player;
import com.abs.casino.common.client.canex.request.onlinerooms.Room;
import com.abs.casino.common.currency.CurrencyRate;
import com.abs.casino.common.currency.ICurrencyRateManager;
import com.abs.casino.common.exception.AccountException;
import com.abs.casino.common.exception.WalletException;
import com.abs.casino.common.kpi.RoundKPIInfo;
import com.abs.casino.common.mp.*;
import com.dgphoenix.casino.common.promo.*;
import com.dgphoenix.casino.common.transactiondata.ITransactionData;
import com.dgphoenix.casino.common.transport.TObject;
import com.dgphoenix.casino.common.util.*;
import com.abs.casino.common.util.DigitFormatter;
import com.abs.casino.common.util.KryoHelper;
import com.abs.casino.common.util.property.PropertyUtils;
import com.abs.casino.common.util.string.StringIdGenerator;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.abs.casino.common.util.web.HttpRequestContextHolder;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import com.dgphoenix.casino.gs.GameServer;
import com.abs.casino.gs.managers.dblink.DBLinkCache;
import com.abs.casino.gs.managers.dblink.IDBLink;
import com.abs.casino.gs.managers.game.session.GameSessionManager;
import com.dgphoenix.casino.gs.managers.payment.bonus.BonusManager;
import com.dgphoenix.casino.gs.managers.payment.bonus.FRBonusManager;
import com.dgphoenix.casino.gs.managers.payment.bonus.FRBonusWinRequestFactory;
import com.dgphoenix.casino.gs.managers.payment.currency.CurrencyRatesManager;
import com.dgphoenix.casino.gs.managers.payment.wallet.*;
import com.abs.casino.gs.managers.payment.wallet.CommonWalletErrors;
import com.dgphoenix.casino.gs.managers.payment.wallet.tracker.WalletTracker;
import com.dgphoenix.casino.gs.managers.payment.wallet.v2.ICommonWalletClient;
import com.dgphoenix.casino.gs.persistance.LasthandPersister;
import com.dgphoenix.casino.gs.persistance.bet.PlayerBetPersistenceManager;
import com.abs.casino.gs.singlegames.tools.cbservtools.ArchiveBetTools;
import com.abs.casino.gs.singlegames.tools.cbservtools.IGameController;
import com.abs.casino.gs.singlegames.tools.util.LasthandHelper;
import com.dgphoenix.casino.gs.socket.MQDataConverter;
import com.abs.casino.gs.socket.NotCriticalWalletException;
import com.abs.casino.gs.socket.mq.DeactivatedRoomNotificationTask;
import com.abs.casino.kafka.dto.AddWinRequestDto;
import com.abs.casino.kafka.dto.AddWinResultDto;
import com.abs.casino.kafka.dto.BGFStatus;
import com.abs.casino.kafka.dto.BGFriendDto;
import com.abs.casino.kafka.dto.BGOStatus;
import com.abs.casino.kafka.dto.BGOnlinePlayerDto;
import com.abs.casino.kafka.dto.BGPlayerDto;
import com.abs.casino.kafka.dto.BGUpdatePrivateRoomRequest;
import com.abs.casino.kafka.dto.BGUpdateRoomResultDto;
import com.abs.casino.kafka.dto.BattlegroundInfoDto;
import com.abs.casino.kafka.dto.BattlegroundRoundInfoDto;
import com.abs.casino.kafka.dto.BuyInResultDto;
import com.abs.casino.kafka.dto.CashBonusDto;
import com.abs.casino.kafka.dto.CloseFRBonusResultDto;
import com.abs.casino.kafka.dto.CrashGameSettingDto;
import com.abs.casino.kafka.dto.CurrencyRateDto;
import com.abs.casino.kafka.dto.DetailedPlayerInfo2Dto;
import com.abs.casino.kafka.dto.FRBonusDto;
import com.dgphoenix.casino.kafka.dto.KafkaHandlerException;
import com.dgphoenix.casino.kafka.dto.MQDataDto;
import com.dgphoenix.casino.kafka.dto.MQDataWrapperDto;
import com.abs.casino.kafka.dto.PlaceDto;
import com.abs.casino.kafka.dto.RMSPlayerDto;
import com.abs.casino.kafka.dto.RMSRoomDto;
import com.abs.casino.kafka.dto.RoundInfoResultDto;
import com.abs.casino.kafka.dto.RoundPlayerDto;
import com.abs.casino.kafka.dto.SitInResponseDto;
import com.abs.casino.kafka.dto.SitOutCashBonusSessionResultDto;
import com.abs.casino.kafka.dto.SitOutResultDto;
import com.abs.casino.kafka.dto.SitOutTournamentSessionResultDto;
import com.abs.casino.kafka.dto.StartNewRoundResponseDto;
import com.abs.casino.kafka.dto.TournamentInfoDto;
import com.dgphoenix.casino.kafka.dto.VoidKafkaResponse;
import com.abs.casino.leaderboard.LeaderboardWinTracker;
import com.abs.casino.leaderboard.LeaderboardWinUploader;
import com.abs.casino.promo.PromoCampaignManager;
import com.abs.casino.promo.events.process.ParticipantEventProcessor;
import com.abs.casino.promo.events.process.PromoGameEventProcessor;
import com.abs.casino.promo.persisters.CassandraMaxBalanceTournamentPersister;
import com.abs.casino.promo.tournaments.messages.BalanceUpdated;
import com.abs.casino.promo.tournaments.messages.BattlegroundInfo;
import com.abs.casino.promo.tournaments.messages.PlayerTournamentStateChanged;
import com.abs.casino.services.mp.MPGameSessionService;
import com.abs.casino.sm.PlayerSessionFactory;
import com.dgphoenix.casino.support.ErrorPersisterHelper;
import com.abs.casino.websocket.tournaments.TournamentWebSocketMessageListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static com.dgphoenix.casino.common.cache.data.game.GameMode.*;
import static com.dgphoenix.casino.common.util.string.StringUtils.isTrimmedEmpty;

/**
 * Service for handling thrift requests from MQ servers
 */
@Service
public class MQServiceHandler {
    private static final Logger LOG = LogManager.getLogger(MQServiceHandler.class);
    private static final long CRASH_PVP_ID = 864L;
    private static final long CRASH_MAX_DEFAULT_PROFIT = 20000000;
    private static final long CRASH_DEFAULT_MIN_STAKE = 1;
    private static final long CRASH_DEFAULT_MAX_STAKE = 500;
    private static final long UPDATE_ACTIVITY_PERIOD = 180000;
    private final CassandraDepositsPersister depositsPersister;
    private final PromoCampaignManager campaignManager;
    private final CassandraMaxBalanceTournamentPersister maxBalanceTournamentPersister;
    private final TournamentBuyInHelper tournamentBuyInHelper;
    private final ICurrencyRateManager currencyRateManager;
    private final ErrorPersisterHelper errorPersisterHelper;
    private final HttpRequestContextHolder httpRequestContext;
    private final CassandraBatchOperationStatusPersister batchStatusPersister;
    private final CassandraGameSessionPersister gameSessionPersister;
    private final BattlegroundHistoryPersister battlegroundHistoryPersister;
    private final ScheduledExecutorService executorService;
    private final CassandraExternalTransactionPersister externalTransactionPersister;
    private final CassandraGameSessionExtendedPropertiesPersister extendedPropertiesPersister;
    private final RoundKPIInfoPersister roundKPIInfoPersister;
    private final PlayerBetPersistenceManager betPersistenceManager;
    private final CassandraLasthandPersister lasthandPersister;
    private final MQDataPersister mqDataPersister;
    private final List<TournamentWebSocketMessageListener> webSocketMessageListeners = new CopyOnWriteArrayList<>();
    private final MQReservedNicknamePersister mqReservedNicknamePersister;
    private final CassandraAccountInfoPersister cassandraAccountInfoPersister;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final LeaderboardResultPersister leaderboardResultPersister;
    private final AccountManager accountManager;
    private final BankInfoCache bankInfoCache;
    private MPGameSessionService mpGameSessionService = null;

    /**
     * Initializes a newly created {@code MQServiceHandler}
     *
     * @param  persistenceManager Cassandra persisters manager
     * @param  promoCampaignManager promo campaigns manager
     * @param  tournamentBuyInHelper tournament helper
     * @param  currencyRateManager Currency rates manager
     * @param  errorPersisterHelper helper for persist errors
     * @param  betPersistenceManager manager for save and load bet history
     * @param  accountManager accountInfo manager
     */
    public MQServiceHandler(CassandraPersistenceManager persistenceManager, IPromoCampaignManager promoCampaignManager,
                            TournamentBuyInHelper tournamentBuyInHelper, ICurrencyRateManager currencyRateManager,
                            ErrorPersisterHelper errorPersisterHelper, PlayerBetPersistenceManager betPersistenceManager,
                            AccountManager accountManager, CommonExecutorService executorService) {
        depositsPersister = persistenceManager.getPersister(CassandraDepositsPersister.class);
        campaignManager = (PromoCampaignManager) promoCampaignManager; // ??
        maxBalanceTournamentPersister = persistenceManager.getPersister(CassandraMaxBalanceTournamentPersister.class);
        this.tournamentBuyInHelper = tournamentBuyInHelper;
        this.currencyRateManager = currencyRateManager;
        this.errorPersisterHelper = errorPersisterHelper;
        this.betPersistenceManager = betPersistenceManager;
        this.executorService = executorService;
        httpRequestContext = HttpRequestContextHolder.getRequestContext();
        batchStatusPersister = persistenceManager.getPersister(CassandraBatchOperationStatusPersister.class);
        gameSessionPersister = persistenceManager.getPersister(CassandraGameSessionPersister.class);
        battlegroundHistoryPersister = persistenceManager.getPersister(BattlegroundHistoryPersister.class);
        externalTransactionPersister = persistenceManager.getPersister(CassandraExternalTransactionPersister.class);
        extendedPropertiesPersister = persistenceManager.getPersister(CassandraGameSessionExtendedPropertiesPersister.class);
        roundKPIInfoPersister = persistenceManager.getPersister(RoundKPIInfoPersister.class);
        lasthandPersister = persistenceManager.getPersister(CassandraLasthandPersister.class);
        mqDataPersister = persistenceManager.getPersister(MQDataPersister.class);
        mqReservedNicknamePersister = persistenceManager.getPersister(MQReservedNicknamePersister.class);
        cassandraAccountInfoPersister = persistenceManager.getPersister(CassandraAccountInfoPersister.class);
        leaderboardResultPersister = persistenceManager.getPersister(LeaderboardResultPersister.class);
        this.accountManager = accountManager;
        this.bankInfoCache = BankInfoCache.getInstance();
    }

    /**
     * Method used for shutdown service
     */
    @PreDestroy
    private void destroy() {
        ExecutorUtils.shutdownService(this.getClass().getSimpleName(), executorService, 5000L);
        LOG.info("shutdown completed");
    }

    public boolean touchSession(String sessionId) {
        LOG.debug("Touching session, SID = {}", sessionId);
        try {
            SessionHelper.getInstance().lock(sessionId);
            try {
                SessionHelper.getInstance().openSession();
                SessionInfo sessionInfo = SessionHelper.getInstance().getTransactionData().getPlayerSession();
                long now = System.currentTimeMillis();
                if (sessionInfo == null) {
                    LOG.warn("Unable to touch session - SessionInfo not found: {}", sessionId);
                    return false;
                } else if (now - sessionInfo.getLastActivityTime() >= UPDATE_ACTIVITY_PERIOD) {
                    sessionInfo.updateActivity();
                    SessionHelper.getInstance().commitTransaction();
                }
                SessionHelper.getInstance().markTransactionCompleted();
            } finally {
                SessionHelper.getInstance().clearWithUnlock();
            }
        } catch (Exception e) {
            LOG.error("Unable to touch session, SID = " + sessionId, e);
            return false;
        }
        return true;
    }

    /**
     * Return player info by sessionId
     *
     * @param sessionId player session identifier
     * @param gameId game identifier
     * @param mode  gameplay mode, free|real|frb|tournament
     * @param bonusId bonus identifier for bonus session
     * @param tournamentId tournament identifier for tournament mode
     * @return {@code TTDetailedPlayerInfo2} detailed information obout logged player
     */
    public DetailedPlayerInfo2Dto getDetailedPlayerInfo2(String sessionId, long gameId, String mode, long bonusId,
                                                         long tournamentId) {
        LOG.debug("getDetailedPlayerInfo: sessionId={}, gameId={}, mode={}, bonusId={}, tournamentId={}", sessionId,
                gameId, mode, bonusId, tournamentId);
        boolean shouldPersistError = false;
        try {
            SessionHelper.getInstance().lock(sessionId);
            try {
                SessionHelper.getInstance().openSession();
                SessionInfo sessionInfo = SessionHelper.getInstance().getTransactionData().getPlayerSession();
                if (sessionInfo == null) {
                    LOG.warn("Unable to get player info - SessionInfo not found: {}", sessionId);
                    return null;
                }
                AccountInfo accountInfo = accountManager.getAccountInfo(sessionInfo.getAccountId());
                boolean isReal = "real".equalsIgnoreCase(mode) || "frb".equalsIgnoreCase(mode);
                BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(accountInfo.getBankId());
                if (isReal) {
                    FRBonusManager.getInstance().checkMassAwardsForAccount(accountInfo);
                }
                shouldPersistError = isSessionForRealAccountAndMode(accountInfo, mode);
                Long activeFRBonusId = gameId < 0 || accountInfo.isGuest() || !isReal ? null :
                        FRBonusManager.getInstance().getEarlestActiveFRBonusId(accountInfo.getId(), gameId);
                LOG.debug("getDetailedPlayerInfo: sessionId={}, activeFRBonusId={}", sessionId, activeFRBonusId);
                CashBonusDto cashBonus = getBonus(bonusId, gameId, accountInfo);
                FRBonusDto tFRBonus = null;
                //It is necessary to check that if it is a multiplayer game, then in the bonus there are only
                // multiplayer games
                if (activeFRBonusId != null && cashBonus == null) {
                    FRBonus bonus = FRBonusManager.getInstance().getById(activeFRBonusId);
                    if (bonus != null) {
                        BaseGameInfoTemplate template = BaseGameInfoTemplateCache.getInstance().
                                getBaseGameInfoTemplateById(gameId);
                        if (template.getGameType() == GameType.MP) {
                            List<Long> gameIds = bonus.getGameIds();
                            for (Long currentGameId : gameIds) {
                                BaseGameInfoTemplate currentTemplate = BaseGameInfoTemplateCache.getInstance().
                                        getBaseGameInfoTemplateById(currentGameId);
                                GameType currentTemplateGameType = currentTemplate.getGameType();
                                if (currentTemplateGameType != null && currentTemplateGameType != GameType.MP) {
                                    LOG.error("getDetailedPlayerInfo: Found FRBonus with Multiplayer and SinglePlayer games, " +
                                            "this is very dangerous, bonus={}", bonus);
                                    bonus = null;
                                    break;
                                }
                            }
                            if (bonus != null) {
                                tFRBonus = new com.abs.casino.kafka.dto.FRBonusDto(bonus.getId(), bonus.getTimeAwarded(),
                                        bonus.getStartDate() != null ? bonus.getStartDate() : 0,
                                        bonus.getExpirationDate() != null ? bonus.getExpirationDate() : -1,
                                        bonus.getRounds(), bonus.getRoundsLeft(), bonus.getWinSum(),
                                        bonus.getFrbTableRoundChips(), bonus.getMaxWinLimit() == null ? -1 : bonus.getMaxWinLimit());
                            }
                        }
                    } else {
                        LOG.error("getDetailedPlayerInfo: Strange error, FRBonus not found for active FRBonus.id={}",
                                activeFRBonusId);
                    }
                }
                boolean freeMode = accountInfo.isGuest() || !isReal;
                long balance = freeMode ? accountInfo.getFreeBalance() : accountInfo.getBalance();
                Currency playerCurrency = CurrencyCache.getInstance().get(accountInfo.getCurrency().getCode());
                Pair<String, String> pair = new Pair<>(playerCurrency.getCode(), ICurrencyRateManager.DEFAULT_CURRENCY);
                CurrencyRate currencyRate = CurrencyRatesManager.getInstance().get(pair);
                LOG.debug("getDetailedPlayerInfo: currencyRate: {}", currencyRate);
                if (currencyRate == null) {
                    throw new KafkaHandlerException(3, "Unknown currency rate");
                }
                Map<String, String> gameSettings = new HashMap<>();
                IBaseGameInfo gameInfo = BaseGameCache.getInstance().getGameInfoById(bankInfo.getId(), gameId, playerCurrency);
                String stakesReserve = gameInfo.getProperty(BaseGameConstants.KEY_MQ_STAKES_RESERVE);
                String stakesLimit = gameInfo.getProperty(BaseGameConstants.KEY_MQ_STAKES_LIMIT);
                if (!StringUtils.isTrimmedEmpty(stakesReserve)) {
                    gameSettings.put(BaseGameConstants.KEY_MQ_STAKES_RESERVE, stakesReserve);
                    gameSettings.put(BaseGameConstants.KEY_ABS_STAKES_RESERVE, stakesReserve);
                }
                if (!StringUtils.isTrimmedEmpty(stakesLimit)) {
                    gameSettings.put(BaseGameConstants.KEY_MQ_STAKES_LIMIT, stakesLimit);
                    gameSettings.put(BaseGameConstants.KEY_ABS_STAKES_LIMIT, stakesLimit);
                }
                boolean mqStartBonusDisabled = bankInfo.isMqStartBonusDisabled();
                if (!mqStartBonusDisabled) {
                    gameSettings.put(BaseGameConstants.KEY_MQ_AWARD_PLAYER_START_BONUS, Boolean.TRUE.toString());
                    gameSettings.put(BaseGameConstants.KEY_ABS_AWARD_PLAYER_START_BONUS, Boolean.TRUE.toString());
                }
                String nickName = accountInfo.getNickName();
                if (!StringUtils.isTrimmedEmpty(nickName) && bankInfo.isMpUseProvidedNickname()) {
                    gameSettings.put(BankInfo.KEY_MP_USE_NICKNAME_IF_PROVIDED, nickName);
                }
                String allowedSymbols = bankInfo.getMpNicknameAllowedSymbols();
                if (!StringUtils.isTrimmedEmpty(allowedSymbols)) {
                    gameSettings.put(BankInfo.KEY_MP_NICKNAME_ALLOWED_SYMBOLS, allowedSymbols);
                }
                if (tournamentId > 0) {
                    IPromoCampaign promoCampaign = campaignManager.getPromoCampaign(tournamentId);
                    if (promoCampaign instanceof NetworkPromoEvent) {
                        MaxBalanceTournamentPlayerDetails details =
                                maxBalanceTournamentPersister.getForAccount(accountInfo.getId(), tournamentId);
                        gameSettings.put(BaseGameConstants.KEY_TOURNAMENT_PLAYER_ALIAS, details.getNickname());
                    }
                }
                gameSettings.put(BankInfo.KEY_CW_SEND_REAL_BET_WIN, Boolean.toString(bankInfo.isCWSendRealBetWin()));
                String weaponsMode = bankInfo.getMaxQuestWeaponMode().name();
                gameSettings.put(BankInfo.KEY_MQ_WEAPONS_MODE, weaponsMode);
                gameSettings.put("ABS_WEAPONS_MODE", weaponsMode);
                String value = bankInfo.isRoundWinsWithoutBetsAllowed() ? Boolean.TRUE.toString() : Boolean.FALSE.toString();
                gameSettings.put(BankInfo.KEY_ROUND_WINS_WITHOUT_BETS_ALLOWED, value);
                List<Long> coins = getCoins(bankInfo, gameId, playerCurrency);
                boolean battleGroundsMultiplayerGame = BaseGameInfoTemplateCache.getInstance().getBaseGameInfoTemplateById(gameId).isBattleGroundsMultiplayerGame();
                LOG.debug("getDetailedPlayerInfo: battleGroundsMultiplayerGame: {}", battleGroundsMultiplayerGame);
                List<BattlegroundInfoDto> battlegroundInfo = battleGroundsMultiplayerGame ? getBattlegroundInfo(sessionId, bankInfo, playerCurrency) : null;
                return new DetailedPlayerInfo2Dto(
                        accountInfo.getBankId(),
                        sessionInfo.getAccountId(),
                        accountInfo.getExternalId(),
                        nickName,
                        balance,
                        playerCurrency.getCode(),
                        playerCurrency.getSymbol(),
                        currencyRate.getRate(),
                        accountInfo.isGuest(),
                        !bankInfo.isNotShowUpdateBalanceButtonForMultiplayerGames(),
                        tFRBonus,
                        coins,
                        gameSettings,
                        true,
                        0,
                        "",
                        getMQLbContributionPercent(gameInfo),
                        cashBonus,
                        getTournamentInfo(tournamentId, accountInfo),
                        battlegroundInfo);
            } finally {
                SessionHelper.getInstance().clearWithUnlock();
            }
        } catch (Exception e) {
            LOG.error("getDetailedPlayerInfo: Unable to get player info for multi player lobby", e);
            if (shouldPersistError) {
                errorPersisterHelper.persistGetDetailedPlayerInfoError(sessionId, gameId, mode, e);
            }
            DetailedPlayerInfo2Dto errorResult = new DetailedPlayerInfo2Dto();
            errorResult.setSuccess(false);
            errorResult.setReasonPhrases(e.getMessage());
            return errorResult;
        } finally {
            httpRequestContext.clear();
        }
    }

    /**
     * Start gameSession and close old if required
     *
     * @param sessionId player session identifier
     * @param gameId game identifier
     * @param mode game play mode, free|real|frb|toournament
     * @param lang game client language
     * @param bonusId bonus identifier for bonus session
     * @param oldGameSessionId old gameSessionId if player restart game
     * @param oldRoundId old roundId if player restart game
     * @param roomId MQ room identifier
     * @param betNumber MQ room bet serial number
     * @param tournamentId tournament identifier for tournament mode
     * @param nickname MQ side player nickname
     * @return {@code TSitInResult} call result
     * @throws TException if any unexpected error occur
     */
    public SitInResponseDto sitIn(String sessionId, long gameId, String mode, String lang, long bonusId,
                                  long oldGameSessionId, long oldRoundId, long roomId, int betNumber, long tournamentId,
                                  String nickname) {
        boolean shouldPersistError = false;
        long transactionId = -1L;
        try {
            LOG.debug("sitIn: {}, mode={}, lang={}, bonusId={}, oldGameSessionId={}, oldRoundId={}, " +
                            "roomId={}, betNumber={}, tournamentId: {}", sessionId, mode, lang, bonusId, oldGameSessionId,
                    oldRoundId, roomId, betNumber, tournamentId);
            SessionHelper.getInstance().lock(sessionId);
            try {
                SessionHelper.getInstance().openSession();
                ITransactionData transactionData = SessionHelper.getInstance().getTransactionData();
                SessionInfo sessionInfo = transactionData.getPlayerSession();
                if (sessionInfo == null) {
                    LOG.warn("sitIn: Unable to open room - SessionInfo not found: {}", sessionId);
                    throw new KafkaHandlerException(-1, "Session not found");
                }
                AccountInfo accountInfo = transactionData.getAccount();
                shouldPersistError = isSessionForRealAccountAndMode(accountInfo, mode);
                boolean isRealMode = "real".equalsIgnoreCase(mode);
                boolean isFRB = "frb".equalsIgnoreCase(mode);
                if ((isRealMode || isFRB || "cashbonus".equalsIgnoreCase(mode))
                        && accountInfo.isGuest()) {
                    throw new KafkaHandlerException(-1, "Guest account wrong game mode: " + mode);
                }
                com.dgphoenix.casino.common.cache.data.game.GameMode gameMode = resolveMQMode(mode);
                IBaseGameInfo gameInfo = BaseGameCache.getInstance()
                        .getGameInfoById(accountInfo.getBankId(), gameId, accountInfo.getCurrency());
                transactionId = getTransactionId((int) gameId, accountInfo.getBankId(), transactionData);
                GameSession oldGameSession = transactionData.getGameSession();
                boolean reuseGameSession = false;
                Long gameSessionId = null;
                if (oldGameSession != null) {
                    LOG.debug("sitIn: found unclosed GameSession: {}", oldGameSession);
                    if (oldGameSession.getId() == oldGameSessionId) {
                        LOG.debug("sitIn: unclosed GameSession equals oldGameSessionId, close not required");
                        gameSessionId = oldGameSessionId;
                        reuseGameSession = true;
                    } else {
                        LOG.debug("sitIn: unclosed GameSession not equals oldGameSessionId, need close, gameSessionId: {}", oldGameSession.getId());
                        GameServer.getInstance().closeOnlineGame(accountInfo, sessionInfo, oldGameSession, false,
                                true);
                    }
                }
                SitInResponseDto sitInResult;
                if (oldGameSessionId > 0 && reuseGameSession) {
                    LOG.debug("Found sitIn for oldGameSession, new gameSession not started, need sitOut first, " +
                            "isFRB: {}", isFRB);
                    long balance = (isRealMode || isFRB) && !accountInfo.isGuest() ? accountInfo.getBalance() :
                            accountInfo.getFreeBalance();
                    sitInResult = new SitInResponseDto(oldGameSessionId, 0, balance, oldRoundId, true, 0, "");
                } else {
                    boolean walletBank = WalletProtocolFactory.getInstance().isWalletBank(accountInfo.getBankId());
                    if (gameSessionId == null) {
                        GameServer.getInstance().startGame(sessionInfo, gameInfo, gameSessionId,
                                gameMode, bonusId > 0 ? bonusId : null, lang, accountInfo);
                    }
                    GameSession gameSession = transactionData.getGameSession();
                    if (tournamentId != -1) {
                        gameSession.setTournamentId(tournamentId);
                        MaxBalanceTournamentPlayerDetails tournamentPlayerDetails =
                                maxBalanceTournamentPersister.getForAccount(accountInfo.getId(), tournamentId);
                        IPromoCampaign promoCampaign = campaignManager.getPromoCampaign(tournamentId);
                        if (needUpdateNickname(tournamentPlayerDetails, promoCampaign)) {
                            if (!StringUtils.isTrimmedEmpty(nickname) &&
                                    !nickname.equals(tournamentPlayerDetails.getNickname())) {
                                LOG.debug("sitIn: found nickName change, new nickName={}, tournamentPlayerDetails={}",
                                        nickname, tournamentPlayerDetails);
                                tournamentPlayerDetails.setNickname(nickname);
                                maxBalanceTournamentPersister.persist(tournamentPlayerDetails);
                            }
                        } else {
                            LOG.error("SitIn for tournament error, tournamentPlayerDetails is null for accountid={}",
                                    accountInfo.getId());
                        }
                    }
                    gameSessionId = gameSession.getId();
                    if (walletBank && isRealMode && bonusId < 0) {
                        ICommonWalletClient client = WalletProtocolFactory.getInstance().
                                getClient(accountInfo.getBankId());
                        //this hack need for prevent very rare case: ISoftBet offline wallet operation processing
                        if (client.getClass().getCanonicalName().contains("ISoftBetAPIClient") &&
                                !StringUtils.isTrimmedEmpty(sessionInfo.getSecretKey())) {
                            WalletProtocolFactory.getInstance().interceptCreateWallet(accountInfo,
                                    accountInfo.getBankId(), gameSessionId, (int) gameId, REAL,
                                    gameSession.getClientType());
                            CommonGameWallet gameWallet = transactionData.getWallet().getGameWallet((int) gameId);
                            if (gameWallet != null && StringUtils.isTrimmedEmpty(gameWallet.getAdditionalRoundInfo())) {
                                LOG.debug("Found condition for ISoftBet gameWallet.setAdditionalRoundInfo " +
                                        "from sessionInfo.secretKey");
                                gameWallet.setAdditionalRoundInfo(sessionInfo.getSecretKey());
                            }
                        }
                    }
                    IDBLink dbLink = getDBLink(sessionInfo, gameSession);
                    Long roundIdFromDbLink = dbLink.getRoundId();
                    long roundId;
                    if (reuseGameSession && oldRoundId > 0) {
                        if (oldRoundId != roundIdFromDbLink) {
                            LOG.warn("sitIn: found roundId mismatch for reused GameSession, oldRoundId={}, " +
                                    "roundIdFromDbLink={}", oldRoundId, roundIdFromDbLink);
                            if (walletBank) { //for wallet need always use roundId from CommonGameWallet
                                roundId = roundIdFromDbLink;
                            } else {
                                roundId = oldRoundId;
                            }
                        } else {
                            roundId = oldRoundId;
                        }
                    } else {
                        roundId = roundIdFromDbLink;
                    }
                    long balance = gameSession.isRealMoney() && !accountInfo.isGuest() ? accountInfo.getBalance() :
                            accountInfo.getFreeBalance();
                    sitInResult = new SitInResponseDto(gameSessionId, 0, balance,
                            roundId, true, 0, "");
                    LOG.debug("sitIn: sid={}, sitInResult={}", sessionId, sitInResult);
                    checkAvailablePromos(gameSession, accountInfo);
                }
                sessionInfo.updateActivity();
                SessionHelper.getInstance().commitTransaction();
                SessionHelper.getInstance().markTransactionCompleted();
                return sitInResult;
            } finally {
                SessionHelper.getInstance().clearWithUnlock();
            }
        } catch (Exception e) {
            LOG.error("sitIn: Unable to create game session for multi player room", e);
            if (shouldPersistError) {
                errorPersisterHelper.persistSitInError(sessionId, gameId, mode, lang, 0, bonusId, oldGameSessionId,
                        oldRoundId, roomId, betNumber, transactionId, e);
            }
            if (e instanceof KafkaHandlerException) {
                throw (KafkaHandlerException) e;
            }
            throw new KafkaHandlerException(-1, e.getMessage());
        } finally {
            httpRequestContext.clear();
        }
    }

    /**
     * Return status batch send win operation
     * @param roomId MQ room identifier
     * @param roundId MQ round identifier
     * @return {@code String} operation status
     * @throws TException if any unexpected error occur
     */
    public String getBatchAddWinStatus(long roomId, long roundId) {
        LOG.debug("getBatchAddWinStatus: roomId={}, roundId={}", roomId, roundId);
        Pair<CassandraBatchOperationStatusPersister.Status, Long> win = batchStatusPersister.getStatus(roomId, roundId, "win");
        return win != null ? win.getKey().name() : null;
    }

    /**
     * Add batch win operations for processing
     * @param roomId MQ room identifier
     * @param roundId MQ round identifier
     * @param gameId game identifier
     * @param addWinRequest set with player win requests
     * @param timeoutInMillis max time for wait result. If timeout reached returned only finished resuslts
     * @return {@code Map} with call results
     * @throws TException if any unexpected error occur
     */
    public Map<Long, AddWinResultDto> addBatchWin(long roomId, long roundId, long gameId, Set<AddWinRequestDto> addWinRequest, long timeoutInMillis) {

        LOG.debug("addBatchWin: roomId={}, roundId={}, gameId={}, addWinRequest={}",
                roomId, roundId, gameId, addWinRequest);

        batchStatusPersister
                .persist(roomId, roundId, "win", CassandraBatchOperationStatusPersister.Status.STARTED);

        LOG.debug("addBatchWin: roomId={}, roundId={}, gameId={}, addWinRequest={}", roomId, roundId, gameId, addWinRequest);
        batchStatusPersister.persist(roomId, roundId, "win", CassandraBatchOperationStatusPersister.Status.STARTED);
        if (CollectionUtils.isEmpty(addWinRequest)) {
            batchStatusPersister.persist(roomId, roundId, "win", CassandraBatchOperationStatusPersister.Status.FINISHED);
            return Collections.emptyMap();
        }
        BaseGameInfoTemplate gameTemplate = BaseGameInfoTemplateCache.getInstance().getBaseGameInfoTemplateById(gameId);
        boolean isBattlegroundGame = gameTemplate != null && gameTemplate.isBattleGroundsMultiplayerGame();
        if (isBattlegroundGame) {
            persistRoundParticipants(addWinRequest);
        }
        CountDownLatch latch = new CountDownLatch(addWinRequest.size());
        Map<Long, AddWinResultDto> results = new ConcurrentHashMap<>(addWinRequest.size());
        for (AddWinRequestDto winRequest : addWinRequest) {
            executorService.execute(() -> {
                try {
                    AddWinResultDto winResult;
                    if (winRequest.isSitOut()) {
                        winResult = addWinWithSitOut(winRequest.getSessionId(), winRequest.getGameSessionId(), winRequest.getCents(),
                                winRequest.getReturnedBet(), winRequest.getGsRoundId(), roundId, roomId, winRequest.getAccountId(), winRequest.getRoundInfo(),
                                winRequest.getContributions(), true);
                        LOG.debug("addBatchWin: accountId={} sitOut winResult={}", winRequest.getAccountId(), winResult);
                    } else {
                        winResult = addWin(winRequest.getSessionId(), winRequest.getGameSessionId(), winRequest.getCents(),
                                winRequest.getReturnedBet(), winRequest.getGsRoundId(), roundId, roomId, winRequest.getAccountId(), winRequest.getRoundInfo(),
                                winRequest.getContributions());
                        LOG.debug("addBatchWin: accountId={} non-sitOut winResult={}", winRequest.getAccountId(), winResult);
                    }
                    results.put(winRequest.getAccountId(), winResult);
                } catch (KafkaHandlerException e) {
                    LOG.error("addBatchWin: process failed, addWinRequest={}", addWinRequest, e);
                } finally {
                    latch.countDown();
                }
            });
        }
        try {
            boolean completed = latch.await(timeoutInMillis, TimeUnit.MILLISECONDS);
            if (!completed) {
                LOG.warn("addBatchWin: CountDownLatch timeout, latch count={}, timeoutInMillis={}, seats={}", latch.getCount(),
                        timeoutInMillis, addWinRequest.size());
            } else {
                LOG.info("addBatchWin: success roomId={}, roundId={}, gameId={}", roomId, roundId, gameId);
            }
        } catch (InterruptedException e) {
            LOG.error("addBatchWin: interrupted");
        } finally {
            batchStatusPersister.persist(roomId, roundId, "win", CassandraBatchOperationStatusPersister.Status.FINISHED);
        }
        return results;
    }

    /**
     * Return status for bet/win operation
     * @param accountId account identifier
     * @param roomId MQ room identifier
     * @param roundId MQ round identifier
     * @param sessionId GS side session identifier
     * @param gameSessionId GS side game session identifier
     * @param gameId game identifier
     * @param bankId bank identifier
     * @param isBet true for bet, false for win
     * @param betNumber MQ side bet serial number
     * @return {@code TransactionStatus} operation status
     */
    public String getPaymentOperationStatus(long accountId, long roomId, long roundId, String sessionId, long gameSessionId,
                                            long gameId, long bankId, Boolean isBet, int betNumber) {
        LOG.debug("getPaymentOperationStatus: roomId={}, roundId={}. accountId={}, sessionId={}, gameSessionId={}, gameId={}, " +
                "bankId={}", roomId, roundId, accountId, sessionId, gameSessionId, gameId, bankId);
        if (WalletProtocolFactory.getInstance().isWalletBank(bankId) && isBet == null) {
            return getStatusByWalletOperation(accountId, gameId);
        } else if (roomId <= 0 || roundId <= 0) {
            if (!WalletProtocolFactory.getInstance().isWalletBank(bankId)) {
                LOG.warn("getPaymentOperationStatus, found request for CommonTransfer with not defined roomId or roundId, return APPROVED, " +
                        "but it may be wrong");
                return TransactionStatus.APPROVED.name();
            } else {
                LOG.warn("getPaymentOperationStatus, found request for wallet bank with not defined roomId or roundId and isBet != null, return null, ");
                return null;
            }
        }
        String extTransactionId = getTransactionId(accountId, roomId, roundId, isBet, betNumber, gameSessionId);
        ExternalPaymentTransaction transaction = externalTransactionPersister.get(bankId, extTransactionId);
        LOG.debug("getPaymentOperationStatus: transaction={}", transaction);
        return transaction == null ? "" : transaction.getStatus().name();
    }

    private String getTransactionId(long accountId, long roomId, long roundId, Boolean isBet, int betNumber, long gameSessionId) {
        if (isBet == Boolean.TRUE) {
            return getDebitExternalTransactionIdForMpGame(accountId, gameSessionId, roomId, betNumber);
        }
        return getExternalTransactionIdForMpGame(accountId, roomId, roundId);
    }

    private String getStatusByWalletOperation(long accountId, long gameId) {
        boolean locked = false;
        String status;
        try {
            SessionHelper.getInstance().lock(accountId, 2000);
            locked = true;
            SessionHelper.getInstance().openSession();

            IWallet wallet = SessionHelper.getInstance().getTransactionData().getWallet();
            if (wallet != null) {

                IWalletOperation walletOperation = wallet.getCurrentWalletOperation((int) gameId);

                if (walletOperation == null) {
                    status = TransactionStatus.APPROVED.name();
                } else {
                    WalletOperationStatus internalStatus = walletOperation.getInternalStatus();
                    if (WalletOperationStatus.COMPLETED == internalStatus) {
                        status = TransactionStatus.APPROVED.name();
                    } else if (WalletOperationStatus.STARTED == internalStatus) {
                        status = TransactionStatus.STARTED.name();
                    } else if (WalletOperationStatus.PENDING == internalStatus || WalletOperationStatus.PEENDING_SEND_ALERT == internalStatus) {
                        status = TransactionStatus.PENDING.name();
                    } else {
                        status = TransactionStatus.FAILED.name();
                    }
                }

                LOG.debug("getStatusByWalletOperation: status={}, walletOperation={}", status, walletOperation);

            } else {
                LOG.debug("getStatusByWalletOperation: wallet is null, transaction completed");
                status = TransactionStatus.APPROVED.name();
            }
        } catch (Exception e) {
            LOG.error("getStatusByWalletOperation: load operation status from wallet failed, accountId={}", accountId, e);
            status = TransactionStatus.PENDING.name();
        } finally {
            if (locked) {
                SessionHelper.getInstance().clearWithUnlock();
            }
        }
        return status;
    }

    /**
     * Process win operation and sitOut player
     * @param sessionId GS side session identifier
     * @param gameSessionId GS side game session identifier
     * @param cents win amount in cents
     * @param returnedBet not used (returned) bet in cents
     * @param gsRoundId GS side round identifier
     * @param mpRoundId MQ side round identifier
     * @param roomId MQ side room identifier
     * @param accountId GS side account identifier
     * @param roundInfoResult MQ round details, round history
     * @param contributions leaderboard contributions
     * @param sitOut true if required sitOut
     * @return {@code TAddWinResult} operation result
     * @throws TException if any unexpected error occur
     */
    public AddWinResultDto addWinWithSitOut(String sessionId, long gameSessionId, long cents, long returnedBet, long gsRoundId, long mpRoundId,
                                            long roomId, long accountId, RoundInfoResultDto roundInfoResult,
                                            Map<Long, Double> contributions, boolean sitOut) {
        LOG.debug("addWinWithSitOut");
        boolean shouldPersistError = false;
        boolean onlineMode;
        NotCriticalWalletException notCriticalWalletException = null;
        int gameId;
        long transactionId = -1L;
        try {
            LOG.debug("sitOut: adding win to sid={}, gameSessionId={}, cents={}, returnedBet={}, roundId={}, " +
                            "roomId={}, accountId={}, roundInfoResult={}, sitOut={}", sessionId, gameSessionId, cents,
                    returnedBet, gsRoundId, roomId, accountId, roundInfoResult, sitOut);
            if (gameSessionId <= 0) {
                LOG.error("sitOut: found empty sitOut for sessionId={}. Please fix.", sessionId);
                return new AddWinResultDto(true, 0, true, TransactionErrorCodes.OK, "Empty sitOut request");
            }
            Pair<Integer, String> pair = StringIdGenerator.extractBankAndExternalUserId(sessionId);
            int bankId = pair.getKey();
            String extUserId = pair.getValue();
            SessionHelper.getInstance().lock(bankId, extUserId, 3000);
            try {
                SessionHelper.getInstance().openSession();
                ITransactionData td = SessionHelper.getInstance().getTransactionData();
                SessionInfo sessionInfo = td.getPlayerSession();
                GameSession gameSession = td.getGameSession();
                onlineMode = isOnlineMode(gameSessionId, sessionInfo, gameSession);
                storeLeaderboardContributions(gameSessionId, gsRoundId, contributions);
                AccountInfo account = accountManager.getByCompositeKey(bankId, extUserId);
                if (account == null && accountId > 0) {
                    LOG.warn("sitOut: cannot find account by extUserId (this may be ok for guest mode). bankId={}, " +
                            "extUserId={}, accountId={}", bankId, extUserId, accountId);
                    account = accountManager.getAccountInfo(accountId);
                }
                if (account == null) {
                    LOG.warn("sitOut: cannot find account by accountId (this may be ok for guest mode), " +
                            "just return. bankId={}, extUserId={}, accountId={}", bankId, extUserId, accountId);
                    return new AddWinResultDto(true, 0, true, TransactionErrorCodes.OK, "Account not found");
                }
                boolean walletBank = WalletProtocolFactory.getInstance().isWalletBank(account.getBankId());
                boolean isBattleGroundMode = roundInfoResult != null && roundInfoResult.getBattlegroundRoundInfo() != null;
                if (gameSession == null) {
                    gameSession = GameSessionManager.getInstance().getGameSessionById(gameSessionId);
                }
                gameId = (int) getGameId(gameSession, gameSessionId);
                transactionId = getTransactionId(gameId, bankId, td);
                shouldPersistError = isSessionForRealAccountAndMode(account, gameSession);
                BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
                //dirty hack. remove after good fix. roundId set to null after refund
                if (walletBank && !account.isGuest() && gameSession != null && gameSession.isRealMoney()
                        && gameSession.getId() == gameSessionId) {
                    IWallet wallet = SessionHelper.getInstance().getTransactionData().getWallet();
                    if (wallet == null) {
                        wallet = WalletProtocolFactory.getInstance().interceptCreateWallet(account, account.getBankId(), gameSession.getId(),
                                (int) gameSession.getGameId(), com.dgphoenix.casino.common.cache.data.game.GameMode.REAL,
                                gameSession.getClientType());
                    }
                    CommonGameWallet gameWallet = wallet.getGameWallet((int) gameSession.getGameId());
                    if (gameWallet != null && (gameWallet.getRoundId() == null || gameWallet.getRoundId() != gsRoundId)
                            && (cents > 0 || returnedBet > 0)) {
                        LOG.warn("sitOut: (dirty hack) found empty roundId, fix it, set roundId to {}, old roundId={}",
                                gsRoundId, gameWallet.getRoundId());
                        gameWallet.setRoundId(gsRoundId);
                        gameWallet.setRoundFinished(false);
                    }
                }
                if (!walletBank) {
                    //need correct CT stored/future deposit amount
                    Long storedDeposit = depositsPersister.getDeposit(sessionId);
                    if (storedDeposit == null || storedDeposit < 0) {
                        //nop, not process allBalance deposit and DirectCT (vietbet/ptpt)
                    } else {
                        long correctedDeposit = account.getBalance() + cents + returnedBet;
                        LOG.debug("sitOut: correcting storedDeposit. sid={}, storedDeposit={}, correctedDeposit={}",
                                sessionId, storedDeposit, correctedDeposit);
                        if (correctedDeposit >= 0) {
                            depositsPersister.persist(sessionId, correctedDeposit);
                        }
                    }
                }
                String extTransactionId = getExternalTransactionIdForMpGame(account.getId(), roomId, gsRoundId);
                ExternalPaymentTransaction transaction = externalTransactionPersister.get(bankId, extTransactionId);
                boolean needProcessWin = true;
                if (transaction != null) {
                    LOG.debug("sitOut: Transaction already exists: transaction = {}", transaction);
                    if (transaction.getStatus() == TransactionStatus.APPROVED) {
                        needProcessWin = false;
                    } else if ((transaction.getStatus() == TransactionStatus.STARTED && transaction.getAmount() == 0)) {
                        transaction.setStatus(TransactionStatus.APPROVED);
                        externalTransactionPersister.persist(transaction);
                        needProcessWin = false;
                    } else {
                        return new AddWinResultDto(
                                !onlineMode,
                                account.getBalance(),
                                false,
                                TransactionErrorCodes.FOUND_PENDING_TRANSACTION,
                                ""
                        );
                    }
                } else {
                    LOG.debug("sitOut: start processing, external transaction not found for extTransactionId='{}'",
                            extTransactionId);
                }
                long balance = account.getBalance();
                if (isBattleGroundMode && roundInfoResult.getBattlegroundRoundInfo() != null) {
                    persistBattlegroundHistory(roundInfoResult, gameId, roomId, gameSessionId);
                }
                if (onlineMode) {
                    boolean realMoney = gameSession.isRealMoney();
                    if (realMoney || gameSession.isBonusGameSession() || gameSession.isFRBonusGameSession()) {
                        persistRoundKPIInfo(roundInfoResult, gameSessionId);
                    }
                    IDBLink dbLink = getDBLink(sessionInfo, gameSession);
                    if (needProcessWin) {
                        if (realMoney) {
                            long balanceVBA = account.getBalance() + cents + returnedBet;
                            LOG.debug("balanceVBA={}", balanceVBA);
                            savePlayerBetForOnline(sessionInfo, roundInfoResult, gameSession, bankInfo, dbLink,
                                    gsRoundId, balanceVBA);
                        }
                        try {
                            balance = processOnlineWin(sessionInfo, account, gameSession, dbLink, cents, returnedBet,
                                    gsRoundId, mpRoundId, roomId, true, walletBank, roundInfoResult);
                        } catch (NotCriticalWalletException e) {
                            notCriticalWalletException = e;
                        } finally {
                            processPromoCampaign(bankInfo, sessionInfo, gameSession, account, roundInfoResult, onlineMode);
                        }
                    } else {
                        ITransactionData transactionData = SessionHelper.getInstance().getTransactionData();
                        LOG.info("sitOut: deleting online lastHand={}", transactionData.getLasthand());
                        transactionData.setLasthand(null);
                        lasthandPersister.delete(account.getId(), gameSession.getGameId(), null, null);
                    }
                    if (sitOut) {
                        long freeBalance = account.getFreeBalance();
                        GameServer.getInstance().closeOnlineGame(gameSession.getId(), false,
                                GameServer.getInstance().getServerId(), sessionInfo, true);
                        //CTCloseGameProcessor reset freeBalance, need restore for fix
                        // https://jira.dgphoenix.com/browse/MQ-971
                        if (!realMoney && !walletBank) {
                            account.setFreeBalance(freeBalance);
                        }
                        onlineMode = false;
                    }
                } else {
                    if (walletBank) {
                        if (needProcessWin) {
                            try {
                                balance = processOfflineWin(gameSessionId, cents, returnedBet, gsRoundId, roomId,
                                        true, account, roundInfoResult, bankInfo);
                            } catch (NotCriticalWalletException e) {
                                notCriticalWalletException = e;
                            } finally {
                                processPromoCampaign(bankInfo, sessionInfo, gameSession, account, roundInfoResult, onlineMode);
                            }
                        }
                    } else {
                        if (gameSession != null && gameSession.getId() == gameSessionId) {
                            String lasthandFromCassandra = lasthandPersister.get(account.getId(),
                                    gameSession.getGameId(), null, null);
                            LOG.info("Deleting CT offline lasthandFromCassandra={}", lasthandFromCassandra);
                            lasthandPersister.delete(account.getId(), gameSession.getGameId(), null, null);
                        }
                        if (cents + returnedBet == 0) {
                            //nop, process not required
                        } else {
                            LOG.error("Cannot process offline sitOut for CommonTransfer, sessionId={}, " +
                                    "gameSessionId={}", sessionId, gameSessionId);
                            //throw new Exception("Cannot process offline sitOut for CommonTransfer");
                        }
                        balance = account.getBalance();
                    }
                }
                SessionHelper.getInstance().commitTransaction();
                SessionHelper.getInstance().markTransactionCompleted();
                if (roundInfoResult != null) {
                    persistRoundKPIInfo(roundInfoResult, gameSessionId);
                }
                if (notCriticalWalletException != null) {
                    return new AddWinResultDto(
                            !onlineMode,
                            balance,
                            false,
                            TransactionErrorCodes.FOUND_PENDING_TRANSACTION,
                            notCriticalWalletException.getMessage()
                    );

                } else {

                    return new AddWinResultDto(
                            !onlineMode,
                            balance,
                            true,
                            TransactionErrorCodes.OK,
                            ""
                    );
                }
            } finally {
                SessionHelper.getInstance().clearWithUnlock();
            }
        } catch (Throwable e) {
            LOG.error("Unable to close game session for multi player room", e);
            if (shouldPersistError) {
                errorPersisterHelper.persistSitOutError(sessionId, gameSessionId, cents, returnedBet, gsRoundId, roomId,
                        accountId, transactionId, roundInfoResult, (Exception) e);
            }

            return new AddWinResultDto(
                    true,
                    0,
                    false,
                    TransactionErrorCodes.FOUND_PENDING_TRANSACTION,
                    e.getMessage()
            );

        } finally {
            httpRequestContext.clear();
        }
    }

    /**
     * Process win and close game session
     * @param sessionId GS side session identifier
     * @param gameSessionId GS side game session identifier
     * @param cents win amount in cents
     * @param returnedBet not used bet in cents
     * @param roundId MQ side round identifier
     * @param roomId MQ side room identifier
     * @param accountId account identifier
     * @param roundInfoResult detailed round info, round history
     * @param contributions leaderboard contributions
     * @return {@code TSitOutResult} call result
     * @throws TException if any unexpected error occur
     */
    public SitOutResultDto sitOut(String sessionId, long gameSessionId, long cents, long returnedBet, long roundId, long roomId,
                                  long accountId, RoundInfoResultDto roundInfoResult, Map<Long, Double> contributions) {
        boolean shouldPersistError = false;
        int gameId;
        long transactionId = -1L;
        try {
            LOG.debug("sitOut: adding win to sid={}, gameSessionId={}, cents={}, returnedBet={}, roundId={}, " +
                            "roomId={}, accountId={}, roundInfoResult={}",
                    sessionId, gameSessionId, cents, returnedBet, roundId, roomId, accountId, roundInfoResult);
            if (gameSessionId <= 0) {
                LOG.error("sitOut: found empty sitOut for sessionId={}. Please fix.", sessionId);
                return new SitOutResultDto(true, TransactionErrorCodes.OK, "Empty sitOut request");
            }
            Pair<Integer, String> pair = StringIdGenerator.extractBankAndExternalUserId(sessionId);
            int bankId = pair.getKey();
            String extUserId = pair.getValue();
            SessionHelper.getInstance().lock(bankId, extUserId);
            try {
                SessionHelper.getInstance().openSession();
                ITransactionData td = SessionHelper.getInstance().getTransactionData();
                SessionInfo sessionInfo = td.getPlayerSession();
                GameSession gameSession = td.getGameSession();
                boolean onlineMode = isOnlineMode(gameSessionId, sessionInfo, gameSession);
                storeLeaderboardContributions(gameSessionId, roundId, contributions);
                AccountInfo account = accountManager.getByCompositeKey(bankId, extUserId);
                if (account == null && accountId > 0) {
                    LOG.warn("sitOut: cannot find account by extUserId (this may be ok for guest mode). bankId={}, " +
                            "extUserId={}, accountId={}", bankId, extUserId, accountId);
                    account = accountManager.getAccountInfo(accountId);
                }
                if (account == null) {
                    LOG.warn("sitOut: cannot find account by accountId (this may be ok for guest mode), " +
                            "just return. bankId={}, extUserId={}, accountId={}", bankId, extUserId, accountId);
                    return new SitOutResultDto(true, TransactionErrorCodes.OK, "Account not found");
                }
                boolean walletBank = WalletProtocolFactory.getInstance().isWalletBank(account.getBankId());
                if (gameSession == null) {
                    gameSession = GameSessionManager.getInstance().getGameSessionById(gameSessionId);
                }
                shouldPersistError = isSessionForRealAccountAndMode(account, gameSession);
                gameId = (int) getGameId(gameSession, gameSessionId);
                transactionId = getTransactionId(gameId, bankId, td);
                BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
                //dirty hack. remove after good fix. roundId set to null after refund
                if (walletBank && !account.isGuest() && gameSession != null && gameSession.isRealMoney()
                        && gameSession.getId() == gameSessionId) {
                    IWallet wallet = SessionHelper.getInstance().getTransactionData().getWallet();
                    if (wallet == null) {
                        wallet = WalletProtocolFactory.getInstance().interceptCreateWallet(account, account.getBankId(), gameSession.getId(),
                                (int) gameSession.getGameId(), com.dgphoenix.casino.common.cache.data.game.GameMode.REAL,
                                gameSession.getClientType());
                    }
                    CommonGameWallet gameWallet = wallet.getGameWallet((int) gameSession.getGameId());
                    if (gameWallet != null && (gameWallet.getRoundId() == null || gameWallet.getRoundId() != roundId)
                            && (cents > 0 || returnedBet > 0)) {
                        LOG.warn("sitOut: (dirty hack) found empty roundId, fix it, set roundId to {}, old roundId={}",
                                roundId, gameWallet.getRoundId());
                        gameWallet.setRoundId(roundId);
                        gameWallet.setRoundFinished(false);
                    }
                }
                if (!walletBank) {
                    //need correct CT stored/future deposit amount
                    Long storedDeposit = depositsPersister.getDeposit(sessionId);
                    if (storedDeposit == null || storedDeposit < 0) {
                        //nop, not process allBalance deposit and DirectCT (vietbet/ptpt)
                    } else {
                        long correctedDeposit = account.getBalance() + cents + returnedBet;
                        LOG.debug("sitOut: correcting storedDeposit. sid={}, storedDeposit={}, correctedDeposit={}",
                                sessionId, storedDeposit, correctedDeposit);
                        if (correctedDeposit >= 0) {
                            depositsPersister.persist(sessionId, correctedDeposit);
                        }
                    }
                }
                String extTransactionId = getExternalTransactionIdForMpGame(account.getId(), roomId, roundId);
                ExternalPaymentTransaction transaction = externalTransactionPersister.get(bankId, extTransactionId);
                boolean needProcessWin = true;
                if (transaction != null) {
                    LOG.debug("sitOut: Transaction already exists: transaction = {}", transaction);
                    if (transaction.getStatus() == TransactionStatus.APPROVED) {
                        needProcessWin = false;
                    } else if ((transaction.getStatus() == TransactionStatus.STARTED && transaction.getAmount() == 0)) {
                        transaction.setStatus(TransactionStatus.APPROVED);
                        externalTransactionPersister.persist(transaction);
                        needProcessWin = false;
                    } else {
                        return new SitOutResultDto(false, TransactionErrorCodes.FOUND_PENDING_TRANSACTION, "");
                    }
                } else {
                    LOG.debug("sitOut: start processing, external transaction not found for extTransactionId='{}'",
                            extTransactionId);
                }
                if (onlineMode) {
                    boolean realMoney = gameSession.isRealMoney();
                    if (realMoney || gameSession.isBonusGameSession() || gameSession.isFRBonusGameSession()) {
                        persistRoundKPIInfo(roundInfoResult, gameSessionId);
                    }
                    IDBLink dbLink = getDBLink(sessionInfo, gameSession);
                    if (needProcessWin) {
                        if (realMoney) {
                            long balanceVBA = account.getBalance() + cents + returnedBet;
                            LOG.debug("sitOut: balanceVBA={}", balanceVBA);
                            savePlayerBetForOnline(sessionInfo, roundInfoResult, gameSession, bankInfo, dbLink,
                                    roundId, balanceVBA);
                        }
                        try {
                            processOnlineWin(sessionInfo, account, gameSession, dbLink, cents, returnedBet,
                                    roundId, roundId, roomId, true, walletBank, roundInfoResult);
                        } catch (NotCriticalWalletException e) {
                            //nop, credit operation created, need just close current GameSession
                        } finally {
                            processPromoCampaign(bankInfo, sessionInfo, gameSession, account, roundInfoResult,
                                    onlineMode);
                        }
                    } else {
                        ITransactionData transactionData = SessionHelper.getInstance().getTransactionData();
                        LOG.info("sitOut: deleting online lastHand={}", transactionData.getLasthand());
                        transactionData.setLasthand(null);
                        lasthandPersister.delete(account.getId(), gameSession.getGameId(), null, null);
                    }
                    try {
                        long freeBalance = account.getFreeBalance();
                        GameServer.getInstance().closeOnlineGame(gameSession.getId(), false,
                                GameServer.getInstance().getServerId(), sessionInfo, true);
                        //CTCloseGameProcessor reset freeBalance, need restore for fix
                        // https://jira.dgphoenix.com/browse/MQ-971
                        if (!realMoney && !walletBank) {
                            account.setFreeBalance(freeBalance);
                        }
                    } catch (Exception e) {
                        LOG.error("Cannot close game", e);
                        throw new KafkaHandlerException(-1, "Cannot close game: " + e.getMessage());
                    }
                } else {
                    if (walletBank) {
                        if (needProcessWin) {
                            try {
                                processOfflineWin(gameSessionId, cents, returnedBet, roundId, roomId,
                                        true, account, roundInfoResult, bankInfo);
                            } finally {
                                processPromoCampaign(bankInfo, sessionInfo, gameSession, account, roundInfoResult,
                                        onlineMode);
                            }
                        }
                    } else {
                        if (gameSession != null && gameSession.getId() == gameSessionId) {
                            String lasthandFromCassandra = lasthandPersister.get(account.getId(),
                                    gameSession.getGameId(), null, null);
                            LOG.info("sitOut: deleting CT offline lasthandFromCassandra={}", lasthandFromCassandra);
                            lasthandPersister.delete(account.getId(), gameSession.getGameId(), null, null);
                        }
                        if (cents + returnedBet == 0) {
                            //nop, process not required
                        } else {
                            LOG.error("sitOut: Cannot process offline sitOut for CommonTransfer, sessionId={}, " +
                                    "gameSessionId={}", sessionId, gameSessionId);
                            throw new Exception("Cannot process offline sitOut for CommonTransfer");
                        }
                    }
                }
                SessionHelper.getInstance().commitTransaction();
                SessionHelper.getInstance().markTransactionCompleted();
            } finally {
                SessionHelper.getInstance().clearWithUnlock();
            }
        } catch (Throwable e) {
            LOG.error("sitOut: Unable to close game session for multi player room", e);
            if (shouldPersistError) {
                errorPersisterHelper.persistSitOutError(sessionId, gameSessionId, cents, returnedBet, roundId, roomId,
                        accountId, transactionId, roundInfoResult, (Exception) e);
            }
            if (e instanceof KafkaHandlerException) {
                throw (KafkaHandlerException) e;
            }
            throw new KafkaHandlerException(-1, e.getMessage());
        } finally {
            httpRequestContext.clear();
        }
        return new SitOutResultDto(true, TransactionErrorCodes.OK, "");
    }


    /**
     * Process win and prepare for next round
     * @param sessionId GS side session identifier
     * @param gameSessionId GS side game session identifier
     * @param cents win amount in cents
     * @param returnedBet not used bet in cents
     * @param gsRoundId GS side round identifier
     * @param mpRoundId MQ side round identifier
     * @param roomId MQ side room identifier
     * @param accountId account identifier
     * @param roundInfoResult detailed round info, round history
     * @param contributions leaderboard contributions
     * @return {@code TAddWinResult} call result
     * @throws TException if any unexpected error occur
     */
    public AddWinResultDto addWin(String sessionId, long gameSessionId, long cents, long returnedBet, long gsRoundId, long mpRoundId,
                                  long roomId, long accountId, RoundInfoResultDto roundInfoResult,
                                  Map<Long, Double> contributions) {
        boolean shouldPersistError = false;
        NotCriticalWalletException notCriticalWalletException = null;
        int gameId;
        long transactionId = -1L;
        try {
            LOG.debug("addWin: adding win to sid={}, gameSessionId={}, cents={}, returnedBet={}, " +
                            "roundInfoResult={}, accountId={}, roomId={}", sessionId, gameSessionId,
                    cents, returnedBet, roundInfoResult, accountId, roomId);
            Pair<Integer, String> pair = StringIdGenerator.extractBankAndExternalUserId(sessionId);
            int bankId = pair.getKey();
            String extUserId = pair.getValue();
            //if try lock by sessionId may be MismatchSessionException if other session started, need prevent this
            //SessionHelper.getInstance().lock(sessionId);
            SessionHelper.getInstance().lock(bankId, extUserId);
            try {
                SessionHelper.getInstance().openSession();
                AccountInfo account = accountManager.getByCompositeKey(bankId, extUserId);
                if (account == null && accountId > 0) {
                    LOG.warn("addWin: cannot find account by extUserId (this may be ok for guest mode). bankId={}, " +
                            "extUserId={}, accountId={}", bankId, extUserId, accountId);
                    account = accountManager.getAccountInfo(accountId);
                }
                if (account == null) {
                    LOG.warn("addWin: cannot find account by accountId (this may be ok for guest mode), " +
                            "just return. bankId={}, extUserId={}, accountId={}", bankId, extUserId, accountId);
                    return new AddWinResultDto(true, 0, true, TransactionErrorCodes.OK, "Account not found");
                }
                if (gameSessionId == 0 && cents == 0 && returnedBet == 0 && roundInfoResult != null &&
                        roundInfoResult.getArchiveData() != null && roundInfoResult.getArchiveData().contains("Refund")) {
                    LOG.info("Found empty refund, just return");
                    long actualBalance = account.isGuest() ? account.getFreeBalance() : account.getBalance();
                    return new AddWinResultDto(true, actualBalance, true, TransactionErrorCodes.OK, "Found empty refund, " +
                            "nothing to do");
                }
                BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
                ITransactionData td = SessionHelper.getInstance().getTransactionData();
                SessionInfo sessionInfo = td.getPlayerSession();
                GameSession gameSession = td.getGameSession();
                boolean onlineMode = isOnlineMode(gameSessionId, sessionInfo, gameSession);
                storeLeaderboardContributions(gameSessionId, gsRoundId, contributions);
                long actualBalance = getActualBalance(gameSession, account);
                String extTransactionId = getExternalTransactionIdForMpGame(account.getId(), roomId, gsRoundId);
                boolean walletBank = WalletProtocolFactory.getInstance().isWalletBank(account.getBankId());
                ExternalPaymentTransaction transaction = externalTransactionPersister.get(bankId, extTransactionId);
                if (transaction != null) {

                    LOG.debug("addWin: Transaction already exists: transaction={}", transaction);

                    if (transaction.getStatus() == TransactionStatus.APPROVED || transaction.getStatus() == TransactionStatus.STARTED) {

                        AddWinResultDto tAddWinResult = new AddWinResultDto(!onlineMode, actualBalance, true, TransactionErrorCodes.OK, "");

                        LOG.debug("addWin: transaction.getStatus()={}, transaction={}, tAddWinResult={}",
                                transaction.getStatus(), transaction, tAddWinResult);

                        return tAddWinResult;
                    } else {
                        if (walletBank) {

                            AddWinResultDto tAddWinResult = new AddWinResultDto(!onlineMode, actualBalance, false,
                                    TransactionErrorCodes.FOUND_PENDING_TRANSACTION, "");

                            LOG.debug("addWin: transaction.getStatus()={}, walletBank={}, tAddWinResult={}",
                                    transaction.getStatus(), walletBank, tAddWinResult);

                            return tAddWinResult;

                        } else { //for CT need implement tracking
                            LOG.error("addWin: Found uncompleted payment transaction={}", transaction);
                            throw new KafkaHandlerException(-1, "Found uncompleted payment transaction");
                        }
                    }
                } else {
                    LOG.debug("addWin: start processing, external transaction not found for extTransactionId='{}'",
                            extTransactionId);
                }
                if ((roundInfoResult == null || roundInfoResult.getArchiveData() == null || roundInfoResult.getArchiveData().isEmpty()) &&
                        cents == 0 && returnedBet == 0) {
                    LOG.debug("addWin: No activity in round, update status of player, onlineMode: {}", onlineMode);
                    return new AddWinResultDto(!onlineMode, actualBalance, true, TransactionErrorCodes.OK,
                            "No activity in round, update status of player, onlineMode");
                }
                boolean isBattleGroundMode = roundInfoResult != null && roundInfoResult.getBattlegroundRoundInfo() != null;
                if (gameSession == null) { //only for offline
                    gameSession = GameSessionManager.getInstance().getGameSessionById(gameSessionId);
                }
                shouldPersistError = isSessionForRealAccountAndMode(account, gameSession);
                gameId = (int) getGameId(gameSession, gameSessionId);
                transactionId = getTransactionId(gameId, bankInfo.getId(), td);
                long balance = account.getBalance();
                //dirty hack. remove after good fix. roundId set to null after refund
                if (walletBank && !account.isGuest() && gameSession != null && gameSession.isRealMoney()
                        && gameSession.getId() == gameSessionId) {
                    IWallet wallet = td.getWallet();
                    CommonGameWallet gameWallet = wallet == null ? null : wallet.getGameWallet((int) gameSession.getGameId());
                    if (gameWallet != null && (gameWallet.getRoundId() == null || gameWallet.getRoundId() != gsRoundId)) {
                        LOG.warn("addWin: (dirty hack) found empty roundId, fix it, set roundId to {} old roundId={}",
                                gsRoundId, gameWallet.getRoundId());
                        gameWallet.setRoundId(gsRoundId);
                        gameWallet.setRoundFinished(false);
                    }
                }
                if (isBattleGroundMode && roundInfoResult.getBattlegroundRoundInfo() != null) {
                    if (!isCrashPVPGame(gameId)) {
                        persistRoundParticipants(sessionId, gameSessionId, roundInfoResult);
                    }

                    persistBattlegroundHistory(roundInfoResult, gameId, roomId, gameSessionId);
                }

                if (onlineMode) {
                    IDBLink dbLink = getDBLink(sessionInfo, gameSession);
                    if (gameSession.isRealMoney()) {
                        long balanceVBA = account.getBalance() + cents + returnedBet;
                        LOG.debug("addWin: balanceVBA={}", balanceVBA);
                        //noinspection ConstantConditions
                        savePlayerBetForOnline(sessionInfo, roundInfoResult, gameSession, bankInfo, dbLink,
                                gsRoundId, balanceVBA);
                    }
                    try {
                        balance = processOnlineWin(sessionInfo, account, gameSession, dbLink, cents, returnedBet,
                                gsRoundId, mpRoundId, roomId, true, walletBank, roundInfoResult);
                    } catch (NotCriticalWalletException e) {
                        notCriticalWalletException = e;
                    } finally {
                        processPromoCampaign(bankInfo, sessionInfo, gameSession, account, roundInfoResult, onlineMode);
                    }
                } else {
                    if (walletBank) {
                        try {
                            balance = processOfflineWin(gameSessionId, cents, returnedBet, gsRoundId, roomId, true,
                                    account, roundInfoResult, bankInfo);
                        } catch (NotCriticalWalletException e) {
                            notCriticalWalletException = e;
                        } catch (Exception e) {
                            LOG.error("Cannot process offline win", e);
                            throw new KafkaHandlerException(-1, "Cannot process offline win: " + e.getMessage());
                        } finally {
                            processPromoCampaign(bankInfo, sessionInfo, gameSession, account, roundInfoResult, onlineMode);
                        }
                    } else {
                        if (gameId > 0) {
                            String lasthandFromCassandra = lasthandPersister.get(account.getId(), gameId,
                                    null, null);
                            LOG.info("addWin: Deleting CT offline lasthandFromCassandra={}", lasthandFromCassandra);
                            lasthandPersister.delete(account.getId(), gameId, null, null);
                        }
                        if (cents + returnedBet == 0) {
                            //nop, may be not processed
                        } else {
                            LOG.error("addWin: cannot process offline win for CommonTransfer, sessionId={}, " +
                                    "gameSessionId={}. Need manual return money", sessionId, gameSessionId);
                            //throw new Exception("Cannot process offline win for CommonTransfer");
                        }
                        balance = account.getBalance();
                    }
                }
                if (roundInfoResult != null) {
                    persistRoundKPIInfo(roundInfoResult, gameSessionId);
                }
                SessionHelper.getInstance().commitTransaction();
                SessionHelper.getInstance().markTransactionCompleted();
                if (notCriticalWalletException != null) {
                    return new AddWinResultDto(
                            !onlineMode,
                            balance,
                            false,
                            TransactionErrorCodes.FOUND_PENDING_TRANSACTION,
                            notCriticalWalletException.getMessage()
                    );

                } else {

                    return new AddWinResultDto(
                            !onlineMode,
                            balance,
                            true,
                            TransactionErrorCodes.OK,
                            ""
                    );
                }
            } finally {
                SessionHelper.getInstance().clearWithUnlock();
            }
        } catch (Exception e) {
            LOG.error("Unable to add win for multi player game", e);
            if (shouldPersistError) {
                errorPersisterHelper.persistAddWinError(sessionId, gameSessionId, cents, returnedBet, gsRoundId, roomId,
                        accountId, transactionId, roundInfoResult, e);
            }

            return new AddWinResultDto(
                    true,
                    0,
                    false,
                    TransactionErrorCodes.FOUND_PENDING_TRANSACTION,
                    e.getMessage()
            );

        } finally {
            httpRequestContext.clear();
        }
    }

    /**
     * Make bet
     * @param sessionId GS side session identifier
     * @param cents bet amount in cents
     * @param gameSessionId GS side session identifier
     * @param roomId MQ side room identifier
     * @param betNumber MQ side bet number
     * @param tournamentId tournament identifier
     * @param currentBalance current player balance
     * @return {@code TBuyInResult} buyIn result
     * @throws TException if any unexpected error occur
     */
    public BuyInResultDto buyIn3(String sessionId, long cents, long gameSessionId, long roomId, int betNumber,
                               long tournamentId, long currentBalance, long roundId) {
        LOG.debug("buyIn: sessionId={}, cents={}, gameSessionId={}, roomId={}, betNumber={}, tournamentId={}",
                sessionId, cents, gameSessionId, roomId, betNumber, tournamentId);
        if (tournamentId != -1) {
            return performTournamentReBuy(sessionId, tournamentId, currentBalance);
        } else {
            return performRegularBuyIn(sessionId, cents, gameSessionId, roomId, betNumber, roundId);
        }
    }

    /**
     * Check buyIn result, this method called only if buyIn failed by unknown reason
     * @param sessionId GS side session identifier
     * @param cents bet amount in cents
     * @param accountId account identifier
     * @param gameSessionId GS side session identifier
     * @param roomId MQ side room identifier
     * @param betNumber MQ side bet number
     * @return {@code TBuyInResult} buyIn result
     * @throws TException if any unexpected error occur
     */
    public BuyInResultDto checkBuyIn(String sessionId, long cents, long accountId,
                                   long gameSessionId, long roomId, int betNumber) {
        LOG.debug("checkBuyIn: sessionId={}, cents={}, accountId={}, gameSessionId={}, roomId={}, betNumber={}",
                sessionId, cents, accountId, gameSessionId, roomId, betNumber);
        BuyInResultDto result;
        try {
            SessionHelper.getInstance().lock(accountId);
            try {
                SessionHelper.getInstance().openSession();
                AccountInfo account = accountManager.getAccountInfo(accountId);
                if (account == null) {
                    result = new BuyInResultDto(0, 0, 0, gameSessionId, false,
                            BuyInResultErrors.ACCOUNT_NOT_FOUND.getCode(),
                            BuyInResultErrors.ACCOUNT_NOT_FOUND.getDescription());
                } else {
                    String debitExternalTransactionIdForMpGame = getDebitExternalTransactionIdForMpGame(accountId,
                            gameSessionId, roomId, betNumber);
                    ExternalPaymentTransaction transaction = externalTransactionPersister.get(account.getBankId(),
                            debitExternalTransactionIdForMpGame);
                    boolean walletBank = WalletProtocolFactory.getInstance().isWalletBank(account.getBankId());
                    long gameId = 0;
                    if (transaction != null) {
                        if (transaction.getStatus() == TransactionStatus.APPROVED) {
                            return new BuyInResultDto(cents, account.getBalance(), transaction.getRoundId(), gameSessionId, true, 0, "");
                        } else {
                            gameId = transaction.getGameId();
                            LOG.warn("checkBuyIn: transaction in progress, gameSessionId={}", gameSessionId);
                        }
                    }
                    if (walletBank) {
                        ITransactionData transactionData = SessionHelper.getInstance().getTransactionData();
                        if (gameId == 0) {
                            GameSession gameSession = transactionData.getGameSession();
                            if (gameSession != null) {
                                gameId = gameSession.getGameId();
                            }
                        }
                        if (gameId == 0) {
                            result = new BuyInResultDto(0, 0, 0, gameSessionId, false,
                                    BuyInResultErrors.GAME_SESSION_NOT_FOUND.getCode(),
                                    BuyInResultErrors.GAME_SESSION_NOT_FOUND.getDescription());
                        } else {
                            CommonWallet wallet = (CommonWallet) transactionData.getWallet();
                            CommonWalletOperation operation = wallet == null ? null : wallet.getCurrentWalletOperation((int) gameId);
                            if (operation != null) {
                                if (WalletOperationStatus.COMPLETED.equals(operation.getInternalStatus())) {
                                    result = new BuyInResultDto(cents, account.getBalance(), transaction.getRoundId(),
                                            gameSessionId, true, 0, "");
                                } else {
                                    result = new BuyInResultDto(0, 0, 0, gameSessionId, false,
                                            BuyInResultErrors.PREV_OPERATION_NOT_COMPLETED.getCode(),
                                            BuyInResultErrors.PREV_OPERATION_NOT_COMPLETED.getDescription());
                                }
                            } else {
                                result = new BuyInResultDto(0, 0, 0, gameSessionId, false,
                                        BuyInResultErrors.TRANSACTION_NOT_FOUND.getCode(),
                                        BuyInResultErrors.TRANSACTION_NOT_FOUND.getDescription());
                            }
                        }
                    } else if (transaction != null) {
                        result = new BuyInResultDto(cents, account.getBalance(), transaction.getRoundId(),
                                gameSessionId, false, BuyInResultErrors.TRANSACTION_IN_PROGRESS.getCode(),
                                BuyInResultErrors.TRANSACTION_IN_PROGRESS.getDescription());
                    } else {
                        result = new BuyInResultDto(0, 0, 0, gameSessionId, false,
                                BuyInResultErrors.TRANSACTION_NOT_FOUND.getCode(),
                                BuyInResultErrors.TRANSACTION_NOT_FOUND.getDescription());
                    }
                }
            } finally {
                SessionHelper.getInstance().clearWithUnlock();
            }
        } catch (AccountException e) {
            LOG.error("Account not found", e);
            result = new BuyInResultDto(0, 0, 0, gameSessionId, false, 1, "Account not found");
        } catch (Exception e) {
            LOG.error("Failed to check buy in", e);
            if (e instanceof KafkaHandlerException) {
                throw (KafkaHandlerException) e;
            }
            throw new KafkaHandlerException(-1, e.getMessage());
        }
        LOG.debug("checkBuyIn: sessionId={}, result={}", sessionId, result);
        return result;
    }

    /**
     * Return buyIn if player for some reason did not participate in the round
     *
     * @param sessionId GS side session identifier
     * @param cents return bet in cents
     * @param accountId player account identifier
     * @param gameSessionId GS side game session identifier
     * @param roomId MQ side room identifier
     * @param betNumber MQ side bet serial number
     * @return {@code TRefundResult} operation result
     * @throws TException if any unexpected error occur
     */
    public VoidKafkaResponse refundBuyIn(String sessionId, long cents, long accountId, long gameSessionId, long roomId, int betNumber) {
        LOG.debug("refundBuyIn: sessionId={}, cents={}, accountId={}, gameSessionId={}, roomId={}, betNumber={}",
                sessionId, cents, accountId, gameSessionId, roomId, betNumber);
        VoidKafkaResponse result;
        try {
            SessionHelper.getInstance().lock(accountId);
            try {
                SessionHelper.getInstance().openSession();
                AccountInfo account = accountManager.getAccountInfo(accountId);
                String debitExternalTransactionIdForMpGame = getDebitExternalTransactionIdForMpGame(accountId,
                        gameSessionId, roomId, betNumber);
                ExternalPaymentTransaction transaction = externalTransactionPersister.get(account.getBankId(),
                        debitExternalTransactionIdForMpGame);
                if (transaction == null || transaction.getStatus() == TransactionStatus.FAILED) {
                    result = new VoidKafkaResponse(true, 0, "");
                } else if (transaction.getStatus() == TransactionStatus.APPROVED) {
                    boolean walletBank = WalletProtocolFactory.getInstance().isWalletBank(account.getBankId());
                    if (walletBank) {
                        CommonWalletOperation operationFromExt = transaction.getOperation();
                        int gameId = transaction.getGameId().intValue();
                        if (operationFromExt != null) {
                            CommonWallet cWallet = (CommonWallet) WalletProtocolFactory.getInstance().interceptCreateWallet(account,
                                    account.getBankId(), gameSessionId, gameId, com.dgphoenix.casino.common.cache.data.game.GameMode.REAL,
                                    ClientType.FLASH);
                            CommonWalletOperation operation = cWallet.getGameWalletBetOperation(gameId);
                            LOG.debug("refundBuyIn: restore CommonWalletOperation, operationFromExt={}, from wallet operation={}",
                                    operationFromExt, operation);
                            if (operation == null) {
                                CommonGameWallet gameWallet = cWallet.getGameWallet(gameId);
                                gameWallet.setBetOperation(operationFromExt);
                            }
                        } else {
                            LOG.warn("refundBuyIn: operationFromExt not found, transaction={}", transaction);
                        }
                        WalletProtocolFactory.getInstance().getWalletProtocolManager(account.getBankId()).handleFailure(account, gameId);
                    } else {
                        account.incrementBalance(cents, true);
                    }
                    result = new VoidKafkaResponse(true, 0, "");
                } else {
                    result = new VoidKafkaResponse(false, BuyInResultErrors.TRANSACTION_IN_PROGRESS.getCode(),
                            BuyInResultErrors.TRANSACTION_IN_PROGRESS.getDescription());
                }
            } finally {
                SessionHelper.getInstance().clearWithUnlock();
            }
        } catch (Exception e) {
            LOG.error("Failed refund, accountId={}", accountId, e);
            if (e instanceof KafkaHandlerException) {
                throw (KafkaHandlerException) e;
            }
            throw new KafkaHandlerException(-1, e.getMessage());
        }
        return result;
    }

    /**
     * updates Players Status In PrivateRoom
     * @param request Update Room Request
     * @return TBGUpdateRoomResult if success or null
     * @throws TException if any unexpected error occur
     */
    public BGUpdateRoomResultDto updatePlayersStatusInPrivateRoom(BGUpdatePrivateRoomRequest request) {

        if(request == null) {
            LOG.error("updatePlayersStatusInPrivateRoom: request is null");
            return new BGUpdateRoomResultDto(400, "Error: request is null", null, null);
        }

        if(request.getPlayers() == null || request.getPlayers().size() == 0) {
            LOG.error("updatePlayersStatusInPrivateRoom: request.getPlayers() is empty, request:{}", request);
            return new BGUpdateRoomResultDto(400, "Error: request.getPlayers() is empty",
                    request.getPrivateRoomId(), request.getPlayers());
        }

        int bankId = request.getBankId();
        if(bankId == 0) {
            LOG.error("updatePlayersStatusInPrivateRoom: bankId is 0");
            return new BGUpdateRoomResultDto(400, "Error: bankId is 0", request.getPrivateRoomId(), request.getPlayers());
        }

        try {
            ICommonWalletClient cw2Client = WalletProtocolFactory.getInstance().getClient(bankId);

            if (cw2Client == null) {
                LOG.error("updatePlayersStatusInPrivateRoom: error to get cw2Client for bankId:{}", bankId);
                return new BGUpdateRoomResultDto(500, "Error: to get cw2Client for bankId " + bankId,
                        request.getPrivateRoomId(), request.getPlayers());
            }

            if (!(cw2Client instanceof com.dgphoenix.casino.payment.wallet.client.v4.CanexCWClient)) {
                LOG.error("updatePlayersStatusInPrivateRoom: CWClient does not support " +
                        "updatePlayerStatusInPrivateRoom bankId:{}", bankId);
                return new BGUpdateRoomResultDto(500, "Error: CWClient does not support " +
                        "updatePlayerStatusInPrivateRoom bankId:" + bankId, request.getPrivateRoomId(), request.getPlayers());
            }

            com.dgphoenix.casino.payment.wallet.client.v4.CanexCWClient canexCWClient =
                    (com.dgphoenix.casino.payment.wallet.client.v4.CanexCWClient) cw2Client;

            StringBuilder sb = new StringBuilder();
            for(BGPlayerDto tbgPlayer : request.getPlayers()) {

                boolean updateSuccess = canexCWClient.updatePlayerStatusInPrivateRoom(
                        request.getPrivateRoomId(),
                        tbgPlayer.getNickname(),
                        tbgPlayer.getExternalId(),
                        BattlegroundService.fromTBGStatus(tbgPlayer.getStatus()),
                        bankId
                );

                if(!updateSuccess) {
                    sb.append("Fail to update:").append(tbgPlayer);
                }
            }

            return new BGUpdateRoomResultDto(200, sb.toString(), request.getPrivateRoomId(),
                    request.getPlayers());

        } catch (Exception e) {
            LOG.error("updatePlayersStatusInPrivateRoom: exception for bankId:{}, {}", bankId, e.getMessage(), e);
            return new BGUpdateRoomResultDto(500, "Exception: " + e.getMessage() , request.getPrivateRoomId(),
                    request.getPlayers());
        }
    }

    /**
     * Invite Players To PrivateRoom
     * @param players list of players, externalIds will be used in JSON body to submit to Canex
     * @return true if success or false
     * @throws TException if any unexpected error occur
     */
    public boolean invitePlayersToPrivateRoom(List<BGPlayerDto> players, String privateRoomId) {
        LOG.debug("invitePlayersToPrivateRoom: players:{}, privateRoomId:{}", players, privateRoomId);
        return invitePlayersToPrivateRoom(players, privateRoomId, null);
    }

    /**
     * Invite Players To PrivateRoom
     * @param players list of players, externalIds will be used in JSON body to submit to Canex
     * @param bankIdAsLong can be null, if null Default BankInfo is used
     * @return true if success or false
     * @throws TException if any unexpected error occur
     */
    public boolean invitePlayersToPrivateRoom(List<BGPlayerDto> players, String privateRoomId, Long bankIdAsLong) {

        LOG.debug("invitePlayersToPrivateRoom: players:{}, privateRoomId:{}, bankIdAsLong:{}",
                players, privateRoomId, bankIdAsLong);

        if(players == null || players.isEmpty()) {
            LOG.error("invitePlayersToPrivateRoom: players is empty");
            return false;
        }

        if(StringUtils.isTrimmedEmpty(privateRoomId)) {
            LOG.error("invitePlayersToPrivateRoom: privateRoomId is empty");
            return false;
        }

        BankInfo bankInfo = null;
        if(bankIdAsLong != null && bankIdAsLong.longValue() != 0) {
            bankInfo = BankInfoCache.getInstance().getBankInfo(bankIdAsLong);
        } else {
            for(Long bId : BankInfoCache.getInstance().getBankIds()) {
                if(bId != null && bId != 0) {
                    bankInfo = BankInfoCache.getInstance().getBankInfo(bId);
                    break;
                }
            }
        }

        if(bankInfo == null) {
            LOG.error("invitePlayersToPrivateRoom: bankInfo is null for {}", players);
            return false;
        }

        if(!bankInfo.isAllowUpdatePlayersStatusInPrivateRoom()) {
            LOG.debug("invitePlayersToPrivateRoom: Update Players Status In PrivateRoom is not allowed skip " +
                    "invitePlayersToPrivateRoom, check BankInfo configuration: {}", bankInfo);
            return false;
        }

        long bankId = bankInfo.getId();
        if(bankId == 0) {
            LOG.error("invitePlayersToPrivateRoom: bankId is 0 for {}", players);
            return false;
        }

        try {
            ICommonWalletClient cw2Client = WalletProtocolFactory.getInstance().getClient(bankId);

            if (cw2Client == null) {
                LOG.error("invitePlayersToPrivateRoom: error to get cw2Client for bankId:{}", bankId);
                return false;
            }

            if (!(cw2Client instanceof com.dgphoenix.casino.payment.wallet.client.v4.CanexCWClient)) {
                LOG.error("invitePlayersToPrivateRoom: CWClient does not support " +
                        "invitePlayersToPrivateRoom bankId:{}", bankId);
                return false;
            }

            com.dgphoenix.casino.payment.wallet.client.v4.CanexCWClient canexCWClient =
                    (com.dgphoenix.casino.payment.wallet.client.v4.CanexCWClient) cw2Client;

            List<String> externalIds = new ArrayList<>();

            for(BGPlayerDto tbgPlayer : players) {

                if (StringUtils.isTrimmedEmpty(tbgPlayer.getExternalId())) {
                    LOG.error("invitePlayersToPrivateRoom: externalId is empty for player:{}", tbgPlayer);
                    continue;
                }

                externalIds.add(tbgPlayer.getExternalId());
            }

            LOG.debug("invitePlayersToPrivateRoom: externalIds: {}", externalIds);

            return canexCWClient.invitePlayersToPrivateRoom(externalIds, privateRoomId);

        } catch (Exception e) {
            LOG.error("invitePlayersToPrivateRoom: exception for bankId:{}, players:{}, {}",
                    bankId, players, e.getMessage(), e);
            return false;
        }
    }

    protected static BGFStatus convertStatusToTBGStatus(com.abs.casino.common.client.canex.request.friends.Status status) {
        if(status == null) {
            return null;
        }

        switch (status) {
            case sent:
                return BGFStatus.sent;
            case received:
                return BGFStatus.received;
            case rejected:
                return BGFStatus.rejected;
            case blocked:
                return BGFStatus.blocked;
            default:
                return BGFStatus.friend;
        }
    }

    protected BGFriendDto convertFriendToTBGFriend(Friend friend) {

        LOG.debug("convertFriendToTBGFriend: friend:{}", friend);

        if(friend == null) {
            LOG.error("convertFriendToTBGFriend: friend is null");
            return null;
        }

        String externalId = friend.getExternalId();
        if(StringUtils.isTrimmedEmpty(externalId)) {
            LOG.error("convertFriendToTBGFriend: externalId is empty:{}", friend);
            return null;
        }

        String nickname = friend.getNickname();
        if(StringUtils.isTrimmedEmpty(nickname)) {
            LOG.error("convertFriendToTBGFriend: nickname is empty:{}", friend);
            return null;
        }

        com.abs.casino.common.client.canex.request.friends.Status status = friend.getStatus() == null ?
                com.abs.casino.common.client.canex.request.friends.Status.friend :
                friend.getStatus();

        BGFriendDto tbgFriend = new BGFriendDto(nickname, externalId, convertStatusToTBGStatus(status));

        LOG.debug("convertFriendToTBGFriend: tbgFriend:{}", tbgFriend);

        return tbgFriend;

    }

    protected List<BGFriendDto> convertFriendsToTBGFriends(List<Friend> friends) {
        LOG.debug("convertFriendsToTBGFriends: friends: {}", friends);

        if(friends == null) {
            LOG.error("convertFriendsToTBGFriends: friends is null");
            return null;
        }

        List<BGFriendDto> tbgFriends = new ArrayList<>();

        if(!friends.isEmpty()) {
            for (Friend friend: friends) {
                BGFriendDto tbgFriend = convertFriendToTBGFriend(friend);

                if(tbgFriend == null) {
                    LOG.error("convertFriendsToTBGFriends: tbgFriend is null");
                    continue;
                }

                tbgFriends.add(tbgFriend);
            }
        }

        LOG.debug("convertFriendsToTBGFriends: tbgFriends: {}", tbgFriends);

        return tbgFriends;
    }

    /**
     * get List of Friends for a specified player-friend
     * @param friend specified, externalIds will be used to submit to Canex
     * @return List of friends  if success or null
     * @throws TException if any unexpected error occur
     */
    public List<BGFriendDto> getFriends(BGFriendDto friend) {
        LOG.debug("getFriends: TBGFriend:{}", friend);
        return getFriends(friend, null);
    }

    /**
     * get List of Friends for a specified player-tbgFriend
     * @param tbgFriend specified, externalIds will be used to submit to Canex
     * @param bankIdAsLong can be null, if null Default BankInfo is used
     * @return List of friends  if success or null
     * @throws TException if any unexpected error occur
     */
    public List<BGFriendDto> getFriends(BGFriendDto tbgFriend, Long bankIdAsLong) {
        LOG.debug("getFriends: TBGFriend:{}, bankIdAsLong:{}", tbgFriend, bankIdAsLong);

        List<BGFriendDto> tbgFriends = new ArrayList<>();

        if(tbgFriend == null) {
            LOG.error("getFriends: tbgFriend is null");
            return tbgFriends;
        }

        String externalId = tbgFriend.getExternalId();

        if (StringUtils.isTrimmedEmpty(externalId)) {
            LOG.error("getFriends: externalId is empty for player:{}", tbgFriend);
            return tbgFriends;
        }

        BankInfo bankInfo = null;
        if(bankIdAsLong != null && bankIdAsLong.longValue() != 0) {
            bankInfo = BankInfoCache.getInstance().getBankInfo(bankIdAsLong);
        } else {
            for(Long bId : BankInfoCache.getInstance().getBankIds()) {
                if(bId != null && bId != 0) {
                    bankInfo = BankInfoCache.getInstance().getBankInfo(bId);
                    break;
                }
            }
        }

        if(bankInfo == null) {
            LOG.error("getFriends: bankInfo is null for {}", tbgFriend);
            return tbgFriends;
        }

        if(!bankInfo.isAllowUpdatePlayersStatusInPrivateRoom()) {
            LOG.debug("getFriends: Update Players Status In PrivateRoom is not allowed skip " +
                    "getFriends, check BankInfo configuration: {}", bankInfo);
            return tbgFriends;
        }

        long bankId = bankInfo.getId();
        if(bankId == 0) {
            LOG.error("getFriends: bankId is 0 for {}", tbgFriend);
            return tbgFriends;
        }

        try {
            ICommonWalletClient cw2Client = WalletProtocolFactory.getInstance().getClient(bankId);

            if (cw2Client == null) {
                LOG.error("getFriends: error to get cw2Client for bankId:{}", bankId);
                return tbgFriends;
            }

            if (!(cw2Client instanceof com.dgphoenix.casino.payment.wallet.client.v4.CanexCWClient)) {
                LOG.error("getFriends: CWClient does not support " +
                        "getFriends bankId:{}", bankId);
                return tbgFriends;
            }

            com.dgphoenix.casino.payment.wallet.client.v4.CanexCWClient canexCWClient =
                    (com.dgphoenix.casino.payment.wallet.client.v4.CanexCWClient) cw2Client;

            LOG.debug("getFriends: externalId: {}", externalId, tbgFriend.getNickname());

            GetFriendsResponse getFriendsResponse = canexCWClient.getFriends(externalId, tbgFriend.getNickname());

            if(getFriendsResponse == null) {
                LOG.error("getFriends: getFriendsResponse is null:{}", bankId);
                return tbgFriends;
            }

            tbgFriends = convertFriendsToTBGFriends(getFriendsResponse.getResult());

        } catch (Exception e) {
            LOG.error("getFriends: exception for bankId:{}, tbgFriend:{}, {}",
                    bankId, tbgFriend, e.getMessage(), e);
        }

        return tbgFriends;
    }

    protected static BGOStatus convertStatusToTBOStatus(com.abs.casino.common.client.canex.request.onlineplayer.Status status) {
        if(status == null) {
            return null;
        }

        switch (status) {
            case offline:
                return BGOStatus.offline;
            default:
                return BGOStatus.online;
        }
    }

    protected BGOnlinePlayerDto convertOnlinePlayerToTBGOnlinePlayer(OnlinePlayer onlinePlayer) {

        LOG.debug("convertOnlinePlayerToTBGOnlinePlayer: onlinePlayer:{}", onlinePlayer);

        if(onlinePlayer == null) {
            LOG.error("convertOnlinePlayerToTBGOnlinePlayer: onlinePlayer is null");
            return null;
        }

        String externalId = onlinePlayer.getExternalId();
        if(StringUtils.isTrimmedEmpty(externalId)) {
            LOG.error("convertOnlinePlayerToTBGOnlinePlayer: externalId is empty:{}", onlinePlayer);
            return null;
        }

        String nickname = onlinePlayer.getNickname();
        if(StringUtils.isTrimmedEmpty(nickname)) {
            LOG.error("convertOnlinePlayerToTBGOnlinePlayer: nickname is empty:{}", onlinePlayer);
            return null;
        }

        com.abs.casino.common.client.canex.request.onlineplayer.Status status = onlinePlayer.isOnline() ?
                com.abs.casino.common.client.canex.request.onlineplayer.Status.online :
                com.abs.casino.common.client.canex.request.onlineplayer.Status.offline;

        BGOnlinePlayerDto tbgOnlinePlayer = new BGOnlinePlayerDto(nickname, externalId, convertStatusToTBOStatus(status));

        LOG.debug("convertOnlinePlayerToTBGOnlinePlayer: tbgOnlinePlayer:{}", tbgOnlinePlayer);

        return tbgOnlinePlayer;

    }

    protected List<BGOnlinePlayerDto> convertOnlinePlayersToTBGOnlinePlayers(List<OnlinePlayer> onlinePlayers) {
        LOG.debug("convertOnlinePlayersToTBGOnlinePlayers: onlinePlayers: {}", onlinePlayers);

        if(onlinePlayers == null) {
            LOG.error("convertOnlinePlayersToTBGOnlinePlayers: onlinePlayers is null");
            return null;
        }

        List<BGOnlinePlayerDto> tbgOnlinePlayers = new ArrayList<>();

        if(!onlinePlayers.isEmpty()) {
            for (OnlinePlayer onlinePlayer: onlinePlayers) {
                BGOnlinePlayerDto tbgOnlinePlayer = convertOnlinePlayerToTBGOnlinePlayer(onlinePlayer);

                if(tbgOnlinePlayer == null) {
                    LOG.error("convertOnlinePlayersToTBGOnlinePlayers: tbgOnlinePlayer is null");
                    continue;
                }

                tbgOnlinePlayers.add(tbgOnlinePlayer);
            }
        }

        LOG.debug("convertOnlinePlayersToTBGOnlinePlayers: tbgOnlinePlayers: {}", tbgOnlinePlayers);

        return tbgOnlinePlayers;
    }

    /**
     * get Online Status for a players
     * @param onlinePlayers
     * @return List of players if success or null
     * @throws TException if any unexpected error occur
     */
    public List<BGOnlinePlayerDto> getOnlineStatus(List<BGOnlinePlayerDto> onlinePlayers) {
        LOG.debug("getOnlineStatus: onlinePlayers:{}", onlinePlayers);
        return getOnlineStatus(onlinePlayers, null);
    }

    /**
     * get Online Status for a players
     * @param onlinePlayers
     * @param bankIdAsLong can be null, if null Default BankInfo is used
     * @return List of players if success or null
     * @throws TException if any unexpected error occur
     */
    public List<BGOnlinePlayerDto> getOnlineStatus(List<BGOnlinePlayerDto> onlinePlayers, Long bankIdAsLong) {
        LOG.debug("getOnlineStatus: onlinePlayers:{}, bankIdAsLong:{}", onlinePlayers, bankIdAsLong);

        if(onlinePlayers == null || onlinePlayers.isEmpty()) {
            LOG.error("getOnlineStatus: onlinePlayers is empty");
            return null;
        }

        BankInfo bankInfo = null;
        if(bankIdAsLong != null && bankIdAsLong.longValue() != 0) {
            bankInfo = BankInfoCache.getInstance().getBankInfo(bankIdAsLong);
        } else {
            for(Long bId : BankInfoCache.getInstance().getBankIds()) {
                if(bId != null && bId != 0) {
                    bankInfo = BankInfoCache.getInstance().getBankInfo(bId);
                    break;
                }
            }
        }

        if(bankInfo == null) {
            LOG.error("getOnlineStatus: bankInfo is null for {}", onlinePlayers);
            return null;
        }

        if(!bankInfo.isAllowUpdatePlayersStatusInPrivateRoom()) {
            LOG.debug("getOnlineStatus: Update Players Status In PrivateRoom is not allowed skip " +
                    "getOnlineStatus, check BankInfo configuration: {}", bankInfo);
            return null;
        }

        long bankId = bankInfo.getId();
        if(bankId == 0) {
            LOG.error("getOnlineStatus: bankId is 0 for {}", onlinePlayers);
            return null;
        }

        try {
            ICommonWalletClient cw2Client = WalletProtocolFactory.getInstance().getClient(bankId);

            if (cw2Client == null) {
                LOG.error("getOnlineStatus: error to get cw2Client for bankId:{}", bankId);
                return null;
            }

            if (!(cw2Client instanceof com.dgphoenix.casino.payment.wallet.client.v4.CanexCWClient)) {
                LOG.error("getOnlineStatus: CWClient does not support " +
                        "getOnlineStatus bankId:{}", bankId);
                return null;
            }

            com.dgphoenix.casino.payment.wallet.client.v4.CanexCWClient canexCWClient =
                    (com.dgphoenix.casino.payment.wallet.client.v4.CanexCWClient) cw2Client;

            List<String> externalIds = new ArrayList<>();

            for(BGOnlinePlayerDto tbgOnlinePlayer : onlinePlayers) {

                if (StringUtils.isTrimmedEmpty(tbgOnlinePlayer.getExternalId())) {
                    LOG.error("getOnlineStatus: externalId is empty for tbgOnlinePlayer:{}", tbgOnlinePlayer);
                    continue;
                }

                externalIds.add(tbgOnlinePlayer.getExternalId());
            }

            LOG.debug("getOnlineStatus: externalIds: {}", externalIds);

            GetOnlinePlayersResponse getOnlinePlayersResponse = canexCWClient.getOnlineStatus(externalIds);

            if(getOnlinePlayersResponse == null) {
                LOG.error("getOnlineStatus: getOnlinePlayersResponse is null:{}", bankId);
                return null;
            }

            List<BGOnlinePlayerDto> tbgOnlinePlayers = convertOnlinePlayersToTBGOnlinePlayers(getOnlinePlayersResponse.getResult());

            return tbgOnlinePlayers;
        } catch (Exception e) {
            LOG.error("getOnlineStatus: exception for bankId:{}, onlinePlayers:{}, {}",
                    bankId, onlinePlayers, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Finish Game Session And Make SitOut using MPGameSessionService
     * @param sid player's SID
     * @param privateRoomId private room Id if exists
     * @return true if success or false
     * @throws TException if any unexpected error occur
     */
    public boolean finishGameSessionAndMakeSitOut(String sid, String privateRoomId) {

        LOG.debug("finishGameSessionAndMakeSitOut: sid:{}, privateRoomId:{}", sid, privateRoomId);

        try {
            if(mpGameSessionService == null) {

                LOG.debug("finishGameSession: sid={}, privateRoomId={}, mpGameSessionService is null, " +
                        "try to instantiate it", sid, privateRoomId);

                mpGameSessionService = ApplicationContextHelper.getBean(MPGameSessionService.class);

                if (mpGameSessionService == null) {
                    LOG.error("finishGameSession: sid={}, privateRoomId={}, " +
                            "Can't instantiate the bean mpGameSessionService", sid, privateRoomId);
                    return false;
                } else {
                    LOG.debug("finishGameSession: sid={}, privateRoomId={}, mpGameSessionService instantiated " +
                            "successfully, use it for finishGameSessionAndMakeSitOut call", sid, privateRoomId);
                }
            } else {
                LOG.debug("finishGameSession: sid={}, privateRoomId={}, mpGameSessionService is not null, " +
                        "use it for finishGameSessionAndMakeSitOut call", sid, privateRoomId);
            }

            Pair<GameSession, Boolean> finishSessionResult = mpGameSessionService
                    .finishGameSessionAndMakeSitOut(sid, privateRoomId);
            LOG.debug("finishGameSession: sid={}, finishSessionResult={}", sid, finishSessionResult);

        } catch (Exception e) {
            LOG.error("finishGameSession: Exception for sid={}, privateRoomId={}, message:{}",
                    sid, privateRoomId, e.getMessage(), e);
            return false;
        }

        return true;
    }


    /**
     * Just close gameSession without any payment operation
     * @param sessionId GS session identifier
     * @param accountId player account identifier
     * @param gameSessionId GS side game session identifier
     * @param buyIn room buyIn amount
     * @return true if success
     * @throws TException if any unexpected error occur
     */
    public boolean closeGameSession(String sessionId, long accountId, long gameSessionId, long buyIn) {
        LOG.debug("closeGameSession: sessionId={}, accountId={}, gameSessionId={}, buyIn={}", sessionId, accountId, gameSessionId, buyIn);
        try {
            Pair<Integer, String> pair = StringIdGenerator.extractBankAndExternalUserId(sessionId);
            int bankId = pair.getKey();
            String extUserId = pair.getValue();
            //if try lock by sessionId may be MismatchSessionException if other session started, need prevent this
            //SessionHelper.getInstance().lock(sessionId);
            SessionHelper.getInstance().lock(bankId, extUserId, 2000);
            try {
                SessionHelper.getInstance().openSession();
                ITransactionData td = SessionHelper.getInstance().getTransactionData();
                SessionInfo sessionInfo = td.getPlayerSession();
                GameSession gameSession = td.getGameSession();
                boolean online = isOnlineMode(gameSessionId, sessionInfo, gameSession);
                if (online) {
                    BaseGameInfoTemplate gameTemplate = BaseGameInfoTemplateCache.getInstance().getBaseGameInfoTemplateById(gameSession.getGameId());
                    boolean isBattlegroundGame = gameTemplate != null && gameTemplate.isBattleGroundsMultiplayerGame();
                    GameServer.getInstance().closeOnlineGame(gameSession.getId(), false, GameServer.getInstance().getServerId(), sessionInfo, true);
                    SessionHelper.getInstance().commitTransaction();
                    SessionHelper.getInstance().markTransactionCompleted();
                }
                return true;
            } finally {
                SessionHelper.getInstance().clearWithUnlock();
            }
        } catch (Exception e) {
            LOG.error("Cannot close gameSession, gameSessionId={}", gameSessionId, e);
        }
        return false;
    }

    /**
     * Prepare for start new round
     * @param sessionId GS session identifier
     * @param accountId player account identifier
     * @param gameSessionId game session identifier
     * @param roomId MQ side room identifier
     * @param roomRoundId MQ side round identifier
     * @param roundStartDate round start date
     * @param battlegroundRoom true if room is battleground
     * @param stakeOrBuyInAmount battleground buyIn amount in cents
     * @return {@code TStartNewRoundResult} operation result
     * @throws TException if any unexpected error occur
     */
    public StartNewRoundResponseDto startNewRound2(String sessionId, long accountId, long gameSessionId,
                                               long roomId, long roomRoundId, long roundStartDate,
                                               boolean battlegroundRoom, long stakeOrBuyInAmount) {
        LOG.debug("startNewRound: sessionId={}, accountId={}, gameSessionId={}, roomId={}, roomRoundId={}, " +
                        "roundStartDate={}, battlegroundRoom={}, stakeOrBuyInAmount={}",
                sessionId, accountId, gameSessionId, roomId, roomRoundId, roundStartDate, battlegroundRoom,
                stakeOrBuyInAmount);
        StartNewRoundResponseDto result;
        try {
            SessionHelper.getInstance().lock(accountId);
            try {
                SessionHelper.getInstance().openSession();
                SessionInfo sessionInfo = SessionHelper.getInstance().getTransactionData().getPlayerSession();
                if (sessionInfo == null) {
                    LOG.warn("Unable to startNewRound - SessionInfo not found {}", sessionId);
                    throw new KafkaHandlerException(-1, "Session not found");
                }
                GameSession gameSession = SessionHelper.getInstance().getTransactionData().getGameSession();
                if (gameSession == null) {
                    LOG.warn("Unable to startNewRound - GameSession not found {}", sessionId);
                    throw new KafkaHandlerException(-1, "GameSession not found");
                }
                if (battlegroundRoom && roomRoundId > 0) {
                    int gameId = (int) gameSession.getGameId();
                    String gameName = BaseGameInfoTemplateCache.getInstance().getGameNameById(gameId);
                    battlegroundHistoryPersister.create(accountId, new BattlegroundRound(gameId,
                            gameName, stakeOrBuyInAmount, roomRoundId, roomId, roundStartDate,
                            "STARTED", 0, 1, 1, "", 0L, gameSessionId));
                    result = new StartNewRoundResponseDto(0, gameSession.getId(), true, 0, "");
                } else {
                    IDBLink dbLink = getDBLink(sessionInfo, gameSession);
                    Long roundId = dbLink.getRoundId();
                    result = new StartNewRoundResponseDto(roundId, gameSession.getId(), true, 0, "");
                    SessionHelper.getInstance().commitTransaction();
                    SessionHelper.getInstance().markTransactionCompleted();
                }
            } finally {
                SessionHelper.getInstance().clearWithUnlock();
            }
        } catch (AccountException e) {
            LOG.warn("startNewRound error: {}", e.getMessage());
            result = new StartNewRoundResponseDto(0, 0, false, BuyInResultErrors.ACCOUNT_NOT_FOUND.getCode(),
                    e.getMessage());
        } catch (Exception e) {
            LOG.error("Failed to startNewRound", e);
            if (e instanceof KafkaHandlerException) {
                throw (KafkaHandlerException) e;
            }
            throw new KafkaHandlerException(-1, e.getMessage());
        }
        return result;
    }

    /**
     * Batch prepare start new round for many players
     * @param roundPlayers list round players
     * @param roomId MQ side room identifier
     * @param roomRoundId MQ side round identifier
     * @param roundStartDate round start date
     * @param battlegroundRoom true if battleground room
     * @param stakeOrBuyInAmount battleground buyIn amount in cents
     * @return {@code java.util.Map<Long, TStartNewRoundResult>} map with player results
     */
    public Map<Long, StartNewRoundResponseDto> startNewRoundForManyPlayers(List<RoundPlayerDto> roundPlayers, long roomId,
                                                                                 long roomRoundId, long roundStartDate, boolean battlegroundRoom,
                                                                                 long stakeOrBuyInAmount) {
        LOG.debug("startNewRoundForManyPlayers roundPlayers count: {}", roundPlayers.size());
        long now = System.currentTimeMillis();
        Map<Long, StartNewRoundResponseDto> result = new HashMap<>();
        Map<Long, Future<StartNewRoundResponseDto>> futuresMap = new HashMap<>();
        for (RoundPlayerDto roundPlayer : roundPlayers) {

            Future<StartNewRoundResponseDto> future =
                    executorService.submit(() ->
                            startNewRound2(
                                    roundPlayer.getSessionId(),
                                    roundPlayer.getAccountId(),
                                    roundPlayer.getGameSessionId(),
                                    roomId,
                                    roomRoundId,
                                    roundStartDate,
                                    battlegroundRoom,
                                    stakeOrBuyInAmount
                            )
                    );

            futuresMap.put(roundPlayer.getAccountId(), future);
        }

        for (Map.Entry<Long, Future<StartNewRoundResponseDto>> futureEntry : futuresMap.entrySet()) {
            try {
                result.put(futureEntry.getKey(), futureEntry.getValue().get());
            } catch (ExecutionException e) {
                LOG.error("unable to start new round for: {}, error: {}", futureEntry.getKey(), e.getMessage());
            } catch (InterruptedException e) {
                LOG.error("unable to start new round for account: {}", futureEntry.getKey(), e);
                Thread.currentThread().interrupt();
            }
        }
        StatisticsManager.getInstance().updateRequestStatistics("ServiceHandler startNewRoundForManyPlayers",
                System.currentTimeMillis() - now, "roomId: " + roomId + ", roomRoundId: " + roomRoundId + ", size: " + roundPlayers.size());
        LOG.debug("startNewRoundForManyPlayers finished, roomId:{}, roomRoundId:{}, result.size(): {}", roomId, roomRoundId, result.size());
        return result;
    }

    /**
     * Save round history for FRB mode
     * @param sessionId GS session identifier
     * @param gameSessionId GS game session identifier
     * @param roundId MQ side round identifier
     * @param accountId player account identifier
     * @param roundInfoResult round history
     * @return true if success
     * @throws TException if any unexpected error occur
     */
    public boolean savePlayerBetForFRB(String sessionId, long gameSessionId, long roundId, long accountId,
                                       RoundInfoResultDto roundInfoResult) {
        try {
            LOG.debug("savePlayerBetForFRB: sessionId {}, gameSessionId={}, roundInfoResult: {}, accountId={}, roundId={}",
                    sessionId, gameSessionId, roundInfoResult, accountId, roundId);
            Pair<Integer, String> pair = StringIdGenerator.extractBankAndExternalUserId(sessionId);
            int bankId = pair.getKey();
            String extUserId = pair.getValue();
            SessionHelper.getInstance().lock(bankId, extUserId);
            try {
                if (roundInfoResult == null) {
                    return false;
                }
                SessionHelper.getInstance().openSession();
                AccountInfo account = accountManager.getByCompositeKey(bankId, extUserId);
                if (account == null && accountId > 0) {
                    LOG.warn("savePlayerBetForFRB: cannot find account by extUserId (this may be ok for guest mode). bankId={}, " +
                            "extUserId={}, accountId={}", bankId, extUserId, accountId);
                    account = accountManager.getAccountInfo(accountId);
                }
                if (account == null) {
                    LOG.warn("savePlayerBetForFRB: cannot find account by accountId (this may be ok for guest mode), " +
                            "just return. bankId={}, extUserId={}, accountId={}", bankId, extUserId, accountId);
                    return false;
                }
                if (gameSessionId == 0 && roundInfoResult.getArchiveData() != null
                        && roundInfoResult.getArchiveData().contains("Refund")) {
                    LOG.info("Found empty refund, just return");
                    return false;
                }
                BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
                SessionInfo sessionInfo = SessionHelper.getInstance().getTransactionData().getPlayerSession();
                GameSession gameSession = SessionHelper.getInstance().getTransactionData().getGameSession();
                if (gameSession == null) {
                    LOG.debug("savePlayerBetForFRB: session is not found ");
                    return false;
                }
                boolean onlineMode = isOnlineMode(gameSessionId, sessionInfo, gameSession);
                if ((roundInfoResult.getArchiveData() == null || roundInfoResult.getArchiveData().isEmpty())) {
                    LOG.debug("savePlayerBetForFRB: No activity in round, update status of player, onlineMode: {}", onlineMode);
                    return false;
                }
                boolean addBet;
                if (onlineMode) {
                    long balanceVBA = account.getBalance();
                    LOG.debug("account balance: {}", balanceVBA);
                    IDBLink dbLink = getDBLink(sessionInfo, gameSession);
                    addBet = savePlayerBetForOnline(sessionInfo, roundInfoResult, gameSession, bankInfo, dbLink,
                            roundId, balanceVBA);
                } else {
                    addBet = savePlayerBetForOffline(roundId, roundInfoResult, gameSession, bankInfo, account,
                            account.getBalance());
                }
                if (addBet) {
                    FRBonus frBonus = FRBonusManager.getInstance().getById(gameSession.getFrbonusId());
                    if (frBonus != null) {
                        long betRound = Math.round(roundInfoResult.getBet() * 100);
                        long frbTableRoundChips = frBonus.getFrbTableRoundChips();
                        long realShots = betRound / frbTableRoundChips;
                        LOG.debug("savePlayerBetForFRB update betSum of FRB, frBonus.getBetSum() old: {}, add sum: {}, " +
                                        "frbTableRoundChips: {}, realShots: {}",
                                frBonus.getBetSum(), betRound, frbTableRoundChips, realShots);
                        frBonus.incrementBetSum(betRound);
                        frBonus.setRoundsLeft(frBonus.getRoundsLeft() - realShots);
                        LOG.debug("updated FRB bonus: {}", frBonus);
                        FRBonusManager.getInstance().flush(frBonus);
                        gameSession.incrementBetsCount((int) realShots);
                    }
                    gameSession.incrementRoundsCount(1);
                    gameSession.setCreateNewBet(true, false);
                    long bet = Math.round(roundInfoResult.getBet() * 100);
                    long win = Math.round(roundInfoResult.getPayout() * 100);
                    if (frBonus != null && frBonus.getMaxWinLimit() != null) {
                        if (win >= frBonus.getMaxWinLimit()) {
                            LOG.debug("FRB maxWinLimit: {} reached. Truncating FRB win., received win: {}",
                                    frBonus.getMaxWinLimit(), win);
                            win = frBonus.getMaxWinLimit();
                        }
                    }
                    gameSession.update(bet, win, 0, 0, null, null, null);
                    if (bet > 0) {
                        gameSession.incrementBetsCount(-1);
                    }
                }
                SessionHelper.getInstance().commitTransaction();
                SessionHelper.getInstance().markTransactionCompleted();
            } finally {
                SessionHelper.getInstance().clearWithUnlock();
            }
        } catch (Throwable e) {
            LOG.error("savePlayerBetForFRB error", e);
            if (e instanceof KafkaHandlerException) {
                throw (KafkaHandlerException) e;
            }
            throw new KafkaHandlerException(-1, e.getMessage());
        }
        return true;
    }

    /**
     * Logout player
     *
     * @param sessionId GS player session identifier
     * @throws TException if any unexpected error occur
     */
    public void leaveMultiPlayerLobby(String sessionId) {
        try {
            SessionHelper.getInstance().lock(sessionId);
            try {
                SessionHelper.getInstance().openSession();
                SessionInfo sessionInfo = SessionHelper.getInstance().getTransactionData().getPlayerSession();
                if (sessionInfo == null) {
                    LOG.error("Unable to leave MP lobby - SessionInfo not found {}", sessionId);
                    throw new KafkaHandlerException(-1, "Session not found");
                }
                long bankId = SessionHelper.getInstance().getTransactionData().getBankId();
                PlayerSessionFactory.getInstance().getPlayerSessionManager(bankId).logout(sessionId);
                SessionHelper.getInstance().commitTransaction();
                SessionHelper.getInstance().markTransactionCompleted();
            } finally {
                SessionHelper.getInstance().clearWithUnlock();
            }
        } catch (Throwable e) {
            LOG.error("Failed to leave multi player Lobby", e);
            throw new KafkaHandlerException(-1, e.getMessage());
        }
    }

    /**
     * Return current player balance
     *
     * @param sessionId GS player session identifier
     * @param mode game mode free|real|frb
     * @return current balance in cents
     * @throws TException if any unexpected error occur
     */
    public long getBalance(String sessionId, String mode) {
        try {
            Pair<Integer, String> pair = StringIdGenerator.extractBankAndExternalUserId(sessionId);
            int bankId = pair.getKey();
            String extUserId = pair.getValue();
            SessionHelper.getInstance().lock(bankId, extUserId);
            try {
                SessionHelper.getInstance().openSession();
                ITransactionData td = SessionHelper.getInstance().getTransactionData();
                SessionInfo sessionInfo = td.getPlayerSession();
                if (sessionInfo == null) {
                    LOG.error("Unable to get balance - SessionInfo not found {}", sessionId);
                    throw new KafkaHandlerException(-1, "Session not found");
                }
                AccountInfo account = td.getAccount();
                boolean isReal = "REAL".equalsIgnoreCase(mode) || "FRB".equalsIgnoreCase(mode);
                if (account.isGuest() || !isReal) {
                    return account.getFreeBalance();
                }
                long balance;
                BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(account.getBankId());
                if (WalletProtocolFactory.getInstance().isWalletBankWithGetBalanceSupported(bankInfo)) {
                    com.dgphoenix.casino.gs.managers.payment.wallet.v2.ICommonWalletClient client = WalletProtocolFactory.getInstance()
                            .getWalletProtocolManager(account.getBankId())
                            .getClient();
                    try {
                        double dBalance = client.getBalance(account.getId(), account.getExternalId(),
                                account.getBankId(), account.getCurrency());
                        balance = bankInfo.isParseLong() ? (long) dBalance : DigitFormatter.getCentsFromCurrency(dBalance);
                        //always need set last known balance
                        account.setBalance(balance);
                    } catch (Exception e) {
                        LOG.error("Unable to refresh balance for sid={}", sessionId, e);
                        balance = account.getBalance();
                    }
                } else {
                    balance = account.getBalance();
                }
                sessionInfo.updateActivity();
                SessionHelper.getInstance().commitTransaction();
                SessionHelper.getInstance().markTransactionCompleted();
                return balance;
            } finally {
                SessionHelper.getInstance().clearWithUnlock();
            }
        } catch (Exception e) {
            LOG.error("Can't get balance, sessionId: {}", sessionId, e);
            if (e instanceof KafkaHandlerException) {
                throw (KafkaHandlerException) e;
            }
            throw new KafkaHandlerException(-1, e.getMessage());
        }
    }

    /**
     * Return current currency rates
     * @param uRates {@code TCurrencyRate} currencies for load
     * @return {@code TCurrencyRate} current currency rates
     * @throws TException if any unexpected error occur
     */
    public Set<CurrencyRateDto> updateCurrencyRates(Set<CurrencyRateDto> uRates) {
        CurrencyRatesManager instance = CurrencyRatesManager.getInstance();
        for (CurrencyRateDto uRate : uRates) {
            LOG.debug("getCurrencyRate:  processing uRate:  {}", uRate);
            try {
                double rate = instance.convert(1, uRate.getSourceCurrency(), uRate.getDestinationCurrency());
                LOG.debug("getCurrencyRate:  processing uRate:  {}, new rate: {}", uRate, rate);
                uRate.setRate(rate);
                uRate.setUpdateDate(System.currentTimeMillis());
            } catch (Throwable e) {
                LOG.debug("rate not found  ", e);
            }
        }
        return uRates;
    }

    /**
     * Close game session for FRB mode
     * @param accountId player account identifier
     * @param sessionId GS player session identifier
     * @param gameSessionId GS game session identifier
     * @param gameId game identifier
     * @param bonusId bonus identifier
     * @param winSum win amount in cents
     * @return {@code TCloseFRBonusResult} operation result
     * @throws TException if any unexpected error occur
     */
    public CloseFRBonusResultDto closeFRBonusAndSession(long accountId, String sessionId, long gameSessionId, long gameId,
                                                      long bonusId, long winSum) {
        LOG.debug("closeFRBonusAndSession: accountId={}, sessionId={}, gameSessionId={}, gameId={}, bonusId={}, " +
                "winSum={}", accountId, sessionId, gameSessionId, gameId, bonusId, winSum);
        boolean locked = false;
        int errorCode = 0;
        String errorDetails = "";
        long balance;
        long realWinSum = 0;
        try {
            SessionHelper.getInstance().lock(accountId);
            locked = true;
            SessionHelper.getInstance().openSession();
            AccountInfo accountInfo = accountManager.getAccountInfo(accountId);
            SessionInfo sessionInfo = SessionHelper.getInstance().getTransactionData().getPlayerSession();
            GameSession gameSession = SessionHelper.getInstance().getTransactionData().getGameSession();
            boolean offline = false;
            FRBonus frBonus = FRBonusManager.getInstance().getById(bonusId);
            if (frBonus == null) {
                LOG.debug("Online bonus not found, try load from archive");
                frBonus = FRBonusManager.getInstance().getArchivedFRBonusById(bonusId);
            }
            LOG.debug("Found frBonus={}", frBonus);
            if (sessionInfo == null || !sessionInfo.getSessionId().equals(sessionId)) {
                LOG.warn("closeFRBonusAndSession: offline mode, SessionInfo not found");
                offline = true;
            } else if (gameSession == null || gameSession.getId() != gameSessionId) {
                LOG.warn("closeFRBonusAndSession: offline mode, GameSession not found");
                offline = true;
            }
            if (frBonus != null) {
                BonusStatus frBonusStatus = frBonus.getStatus();
                if (frBonus.isExpired() && frBonus.getStatus().equals(BonusStatus.ACTIVE)) {
                    FRBonusManager.getInstance().expireBonus(frBonus);
                    frBonusStatus = BonusStatus.EXPIRED;
                }
                boolean frbHasWrongState = frBonusStatus.equals(BonusStatus.CANCELLED)
                        || frBonusStatus.equals(BonusStatus.CANCELLING) || frBonusStatus.equals(BonusStatus.EXPIRED);
                if (frbHasWrongState) {
                    errorCode = frBonusStatus.ordinal();
                    errorDetails = frBonusStatus.name();
                } else if (!frBonusStatus.equals(BonusStatus.CLOSED)) {
                    if (frBonus.getMaxWinLimit() != null) {
                        if (winSum >= frBonus.getMaxWinLimit()) {
                            LOG.debug("FRB maxWinLimit: {} reached. Truncating FRB win., received win: {}",
                                    frBonus.getMaxWinLimit(), winSum);
                            winSum = frBonus.getMaxWinLimit();
                        }
                    }
                    realWinSum = winSum;
                    frBonus.setWinSum(winSum);
                    FRBonusWinRequestFactory.getInstance().interceptCreateFRBonusWin(accountInfo, accountInfo.getBankId(),
                            gameSessionId, gameId);
                    FRBonusManager.getInstance().closeBonus(frBonus);
                    SessionHelper.getInstance().commitTransaction();
                    FRBonusWinRequestFactory.getInstance().handleMPGameCredit(accountInfo, true, gameId, gameSessionId,
                            bonusId, offline ? null : sessionInfo, winSum);
                    if (gameSession == null) {
                        gameSession = gameSessionPersister.get(gameSessionId);
                    }
                    if (offline) {
                        if (gameSession != null) {
                            gameSessionPersister.persist(gameSession);
                        }
                    } else {
                        boolean walletBank = WalletProtocolFactory.getInstance().isWalletBank(accountInfo.getBankId());
                        if (!walletBank) {
                            accountInfo.incrementBalance(winSum, false);
                        } else {
                            //nop, balance for wallet must be updated on External side
                        }
                    }
                }
            }
            if (!offline) {
                GameServer.getInstance().closeOnlineGame(gameSessionId, false, GameServer.getInstance().getServerId(),
                        sessionInfo, true);
            }
            balance = accountInfo.getBalance();
            SessionHelper.getInstance().commitTransaction();
            SessionHelper.getInstance().markTransactionCompleted();
        } catch (Exception e) {
            LOG.error("cannot closeFRBonusAndSession", e);
            if (e instanceof KafkaHandlerException) {
                throw (KafkaHandlerException) e;
            }
            throw new KafkaHandlerException(-1, e.getMessage());
        } finally {
            if (locked) {
                SessionHelper.getInstance().clearWithUnlock();
            }
        }
        Long earlestActiveFRBonusId = FRBonusManager.getInstance().getEarlestActiveFRBonusId(accountId, gameId);
        return new CloseFRBonusResultDto(earlestActiveFRBonusId == null ? -1 : earlestActiveFRBonusId,
                balance, realWinSum, errorCode == 0, errorCode, errorDetails);
    }

    /**
     * Save cash bonus mode round result
     * @param accountId player account identifier
     * @param sessionId GS player session identifier
     * @param gameSessionId GS game session identifier
     * @param bonusId GS cache bonus identifier
     * @param balance current player bonus balance
     * @param betSum round bets sum in cents
     * @param data current MQ player info and preferences
     * @param roundInfo player round info for history
     * @param roundId MQ round identifier
     * @return {@code TCashBonus} current bonus info
     * @throws TException if any unexpected error occur
     */
    public CashBonusDto saveCashBonusRoundResult(long accountId, String sessionId, long gameSessionId, long bonusId,
                                                 long balance, long betSum, MQDataDto data, RoundInfoResultDto roundInfo,
                                                 long roundId) {
        LOG.debug("saveCashBonusRoundResult: accountId={}, sessionId={}, gameSessionId={}, bonusId={}, " +
                        "balance={}, betSum={}, data={}, roundInfo={}, roundId={}", accountId, sessionId,
                gameSessionId, bonusId, balance, betSum, data, roundInfo, roundId);
        CashBonusDto result = null;
        try {
            SessionHelper.getInstance().lock(accountId);
            try {
                SessionHelper.getInstance().openSession();
                ITransactionData td = SessionHelper.getInstance().getTransactionData();
                AccountInfo account = td.getAccount();
                SessionInfo sessionInfo = td.getPlayerSession();
                BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(account.getBankId());
                GameSession gameSession = SessionHelper.getInstance().getTransactionData().getGameSession();
                if (gameSession == null) {
                    LOG.debug("saveCashBonusRoundResult: session is not found ");
                    return null;
                }
                boolean onlineMode = isOnlineMode(gameSessionId, sessionInfo, gameSession);
                if ((roundInfo.getArchiveData() == null || roundInfo.getArchiveData().isEmpty())) {
                    LOG.debug("saveCashBonusRoundResult: No activity in round, update status of player, " +
                            "onlineMode: {}", onlineMode);
                } else {
                    boolean addBet;
                    if (onlineMode) {
                        IDBLink dbLink = getDBLink(sessionInfo, gameSession);
                        addBet = savePlayerBetForOnline(sessionInfo, roundInfo, gameSession, bankInfo, dbLink,
                                roundId, balance);
                    } else {
                        addBet = savePlayerBetForOffline(roundId, roundInfo, gameSession, bankInfo, account, balance);
                    }
                    if (addBet) {
                        gameSession.incrementRoundsCount(1);
                        gameSession.setCreateNewBet(true, false);
                        gameSession.update(Math.round(roundInfo.getBet() * 100), Math.round(roundInfo.getPayout() * 100),
                                0, 0, null, null, null);
                        SessionHelper.getInstance().commitTransaction();
                    }
                }
                //not required save any progress or stats for cashBonus
                //storeMQData(data);
                Bonus bonus = BonusManager.getInstance().getById(bonusId);
                if (bonus == null) {
                    bonus = BonusManager.getInstance().getArchivedBonusById(bonusId);
                    if (!onlineMode) {
                        return new CashBonusDto(bonus.getId(), bonus.getTimeAwarded(), bonus.getExpirationDate(),
                                bonus.getBalance(), bonus.getAmount(), bonus.getAmountToRelease(),
                                bonus.getRolloverMultiplier(), bonus.getBetSum(), bonus.getStatus().name(),
                                bonus.getMaxWinLimit() == null ? -1 : bonus.getMaxWinLimit());
                    }
                }
                bonus.setBalance(balance);
                bonus.setBetSum(betSum);
                boolean isExpired = bonus.isExpired() && bonus.getStatus().equals(BonusStatus.ACTIVE);
                boolean needLost = balance == 0 && bonus.getStatus().equals(BonusStatus.ACTIVE);
                boolean releaseFailedButStarted = false;
                if (isExpired) {
                    BonusManager.getInstance().expireBonus(bonus);
                    BonusManager.getInstance().save(bonus);
                    LOG.debug("saveCashBonusRoundResult: bonus is expired");
                    GameServer.getInstance().closeOnlineGame(gameSessionId, false,
                            GameServer.getInstance().getServerId(), sessionInfo, true);
                } else if (needLost) {
                    BonusManager.getInstance().lostBonus(bonus);
                    BonusManager.getInstance().save(bonus);
                    LOG.debug("saveCashBonusRoundResult: bonus is lost");
                    GameServer.getInstance().closeOnlineGame(gameSessionId, false,
                            GameServer.getInstance().getServerId(), sessionInfo, true);
                } else if (bonus.isReadyToRelease()) {
                    try {
                        //need save bonus before release, if release failed tracking task may be
                        // run once with old amount
                        BonusManager.getInstance().save(bonus);
                        if (onlineMode) {
                            LOG.debug("saveCashBonusRoundResult: updated account balance: {}", account.getBalance());
                            GameServer.getInstance().closeOnlineGame(gameSessionId, false,
                                    GameServer.getInstance().getServerId(), sessionInfo, true);
                        } else {
                            BonusManager.getInstance().releaseBonus(bonus);
                        }
                    } catch (com.dgphoenix.casino.common.exception.CommonException e) {
                        LOG.error("Bonus release failed, but processing started, bonus={}", bonus, e);
                        if (bonus.getStatus().equals(BonusStatus.RELEASED) ||
                                bonus.getStatus().equals(BonusStatus.RELEASING)) {
                            releaseFailedButStarted = true;
                            BonusManager.getInstance().save(bonus);
                            SessionHelper.getInstance().getDomainSession().persistBonus();
                        } else {
                            throw e;
                        }
                    }
                }
                if (!releaseFailedButStarted) {
                    bonus = BonusManager.getInstance().getById(bonusId);
                    if (bonus == null) {
                        bonus = BonusManager.getInstance().getArchivedBonusById(bonusId);
                    }
                }
                LOG.debug("saveCashBonusRoundResult, bonus after processing: {} ", bonus);
                result = new CashBonusDto(bonus.getId(), bonus.getTimeAwarded(), bonus.getExpirationDate(),
                        bonus.getBalance(), bonus.getAmount(), bonus.getAmountToRelease(),
                        bonus.getRolloverMultiplier(), bonus.getBetSum(), bonus.getStatus().name(),
                        bonus.getMaxWinLimit() == null ? -1 : bonus.getMaxWinLimit());
                if (!releaseFailedButStarted) {
                    SessionHelper.getInstance().commitTransaction();
                    SessionHelper.getInstance().markTransactionCompleted();
                } else {
                    LOG.error("saveCashBonusRoundResult, skip commit transaction, see the error above. " +
                            "sessionId: {}, bonusId={}", sessionId, bonusId);
                }
            } finally {
                SessionHelper.getInstance().clearWithUnlock();
            }
        } catch (Exception e) {
            LOG.error("saveCashBonusRoundResult, sessionId: {}, bonusId={}", sessionId, bonusId, e);
            if (e instanceof KafkaHandlerException) {
                throw (KafkaHandlerException) e;
            }
        }
        return result;
    }

    /**
     * Save current round and close gameSession for cache bonus game
     * @param accountId player account identifier
     * @param sessionId GS player session identifier
     * @param gameSessionId GS game session identifier
     * @param bonusId GS cache bonus identifier
     * @param balance current player bonus balance
     * @param betSum round bets sum in cents
     * @param data current MQ player info and preferences
     * @param roundInfo player round info for history
     * @param roundId MQ round identifier
     * @return {@code TSitOutCashBonusSessionResult} operation result
     * @throws TException if any unexpected error occur
     */
    public SitOutCashBonusSessionResultDto sitOutCashBonusSession(long accountId, String sessionId, long gameSessionId,
                                                                  long bonusId, long balance, long betSum, MQDataDto data,
                                                                  RoundInfoResultDto roundInfo, long roundId) {
        LOG.debug("sitOutCashBonusSession: accountId={}, sessionId={}, gameSessionId={}, bonusId={}, " +
                        "balance={}, betSum={}, data={}, roundInfo={}, roundId={}", accountId, sessionId,
                gameSessionId, bonusId, balance, betSum, data, roundInfo, roundId);
        SitOutCashBonusSessionResultDto result = null;
        try {
            SessionHelper.getInstance().lock(accountId);
            try {
                SessionHelper.getInstance().openSession();
                ITransactionData td = SessionHelper.getInstance().getTransactionData();
                AccountInfo account = td.getAccount();
                SessionInfo sessionInfo = td.getPlayerSession();
                GameSession gameSession = SessionHelper.getInstance().getTransactionData().getGameSession();
                Long activeFRBonusId = FRBonusManager.getInstance().getEarlestActiveFRBonusId(account.getId(),
                        data.getGameId());
                if (activeFRBonusId == null) {
                    activeFRBonusId = -1L;
                }
                LOG.debug("sitOutCashBonusSession: sessionId={}, activeFRBonusId={}", sessionId, activeFRBonusId);
                if (gameSession == null) {
                    LOG.debug("sitOutCashBonusSession: session is not found ");
                    Bonus bonus = BonusManager.getInstance().getById(bonusId);
                    if (bonus == null) {
                        bonus = BonusManager.getInstance().getArchivedBonusById(bonusId);
                    }
                    LOG.debug("bonus from archive: {}", bonus);
                    CashBonusDto tCashBonus = new CashBonusDto(bonus.getId(), bonus.getTimeAwarded(), bonus.getExpirationDate(),
                            bonus.getBalance(), bonus.getAmount(), bonus.getAmountToRelease(),
                            bonus.getRolloverMultiplier(), bonus.getBetSum(), bonus.getStatus().name(),
                            bonus.getMaxWinLimit() == null ? -1 : bonus.getMaxWinLimit());
                    LOG.debug("sitOutCashBonusSession: tCashBonus: {}", tCashBonus);
                    result = new SitOutCashBonusSessionResultDto(tCashBonus, activeFRBonusId, true, TransactionErrorCodes.OK, "");
                    LOG.debug("sitOutCashBonusSession: bonus has already been released, result: {}", result);
                    return result;
                }
                BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(account.getBankId());
                boolean onlineMode = isOnlineMode(gameSessionId, sessionInfo, gameSession);
                if ((roundInfo.getArchiveData() == null || roundInfo.getArchiveData().isEmpty())) {
                    LOG.debug("sitOutCashBonusSession: No activity in round, update status of player, " +
                            "onlineMode: {}", onlineMode);
                } else {
                    boolean addBet;
                    if (onlineMode) {
                        IDBLink dbLink = getDBLink(sessionInfo, gameSession);
                        addBet = savePlayerBetForOnline(sessionInfo, roundInfo, gameSession, bankInfo, dbLink,
                                roundId, balance);
                    } else {
                        addBet = savePlayerBetForOffline(roundId, roundInfo, gameSession, bankInfo, account, balance);
                    }
                    if (addBet) {
                        SessionHelper.getInstance().getDomainSession().persistPlayerBet();
                        gameSession.incrementRoundsCount(1);
                        gameSession.setCreateNewBet(true, false);
                        gameSession.update(Math.round(roundInfo.getBet() * 100), Math.round(roundInfo.getPayout() * 100),
                                0, 0, null, null, null);
                    }
                }
                //not required save any progress or stats for cashBonus
                //storeMQData(data);
                Bonus bonus = BonusManager.getInstance().getById(bonusId);
                if (bonus == null) {
                    bonus = BonusManager.getInstance().getArchivedBonusById(bonusId);
                    LOG.debug("bonus from archive: {}", bonus);
                } else {
                    bonus.setBalance(balance);
                    bonus.setBetSum(betSum);
                    if (bonus.isExpired() && bonus.getStatus().equals(BonusStatus.ACTIVE)) {
                        BonusManager.getInstance().expireBonus(bonus);
                        bonus = BonusManager.getInstance().getById(bonusId);
                        LOG.debug("sitOutCashBonusSession: bonus is expired account balance: {}", bonus);
                    }
                }
                boolean releaseFailedButStarted = false;
                boolean readyToRelease = bonus.getStatus().equals(BonusStatus.ACTIVE) && bonus.isReadyToRelease();
                try {
                    if (onlineMode) {
                        LOG.debug("sitOutCashBonusSession: updated account balance: {}, readyToRelease={}",
                                account.getBalance(), readyToRelease);
                        GameServer.getInstance().closeOnlineGame(gameSessionId, false,
                                GameServer.getInstance().getServerId(), sessionInfo, true);
                    } else {
                        if (readyToRelease) {
                            BonusManager.getInstance().releaseBonus(bonus);
                        }
                    }
                } catch (com.dgphoenix.casino.common.exception.CommonException e) {
                    LOG.error("sitOutCashBonusSession: bonus release failed, but processing started, bonus={}, " +
                            "readyToRelease={}", bonus, readyToRelease, e);
                    if (bonus.getStatus().equals(BonusStatus.RELEASED) ||
                            bonus.getStatus().equals(BonusStatus.RELEASING)) {
                        releaseFailedButStarted = true;
                        SessionHelper.getInstance().getDomainSession().persistBonus();
                    } else {
                        throw e;
                    }
                }
                CashBonusDto tCashBonus = new CashBonusDto(bonus.getId(), bonus.getTimeAwarded(), bonus.getExpirationDate(),
                        bonus.getBalance(), bonus.getAmount(), bonus.getAmountToRelease(),
                        bonus.getRolloverMultiplier(), bonus.getBetSum(), bonus.getStatus().name(),
                        bonus.getMaxWinLimit() == null ? -1 : bonus.getMaxWinLimit());
                result = new SitOutCashBonusSessionResultDto(tCashBonus, activeFRBonusId, true, TransactionErrorCodes.OK, "");
                if (!releaseFailedButStarted) {
                    SessionHelper.getInstance().commitTransaction();
                    SessionHelper.getInstance().markTransactionCompleted();
                } else {
                    LOG.error("sitOutCashBonusSession, skip commit transaction, see the error above. " +
                            "sessionId: {}, bonusId={}", sessionId, bonusId);
                }
            } finally {
                SessionHelper.getInstance().clearWithUnlock();
            }
        } catch (Exception e) {
            LOG.error("sitOutCashBonusSession, sessionId: {}", sessionId, e);
            if (e instanceof KafkaHandlerException) {
                throw (KafkaHandlerException) e;
            }
        }
        return result;
    }

    /**
     * Store current round result for tournament mode and start new round
     *
     * @param accountId player account identifier
     * @param sessionId GS player session identifier
     * @param gameSessionId GS game session identifier
     * @param tournamentId GS side tournament identifier
     * @param balance current balance
     * @param data current MQ player info and preferences
     * @param roundInfo player round info for history
     * @param roundId MQ round identifier
     * @return {@code TTournamentInfo} actual tournament info
     * @throws TException if any unexpected error occur
     */
    public TournamentInfoDto saveTournamentRoundResult(long accountId, String sessionId, long gameSessionId,
                                                       long tournamentId, long balance, MQDataDto data,
                                                       RoundInfoResultDto roundInfo, long roundId) {
        long correctedRoundId = roundId <= 0 ? IdGenerator.getInstance().getNext(IWallet.class) : roundId;
        LOG.debug("saveTournamentResult: accountId={}, sessionId={}, gameSessionId={}, tournamentId={}, " +
                        "balance={}, data={}, roundInfo={}, roundId={}, correctedRoundId={}", accountId, sessionId,
                gameSessionId, tournamentId, balance, data, roundInfo, roundId, correctedRoundId);
        if (roundId <= 0) {
            replaceEmptyRoundId(roundInfo, correctedRoundId);
        }
        TournamentInfoDto result = null;
        try {
            SessionHelper.getInstance().lock(accountId);
            try {
                SessionHelper.getInstance().openSession();
                ITransactionData td = SessionHelper.getInstance().getTransactionData();
                AccountInfo account = td.getAccount();
                SessionInfo sessionInfo = td.getPlayerSession();
                BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(account.getBankId());
                GameSession gameSession = SessionHelper.getInstance().getTransactionData().getGameSession();
                if (gameSession == null) {
                    LOG.debug("saveTournamentRoundResult: gameSession is not found ");
                    return null;
                }
                boolean onlineMode = isOnlineMode(gameSessionId, sessionInfo, gameSession);
                if ((roundInfo.getArchiveData() == null || roundInfo.getArchiveData().isEmpty())) {
                    LOG.debug("saveTournamentRoundResult: No activity in round, update status of player, " +
                            "onlineMode: {}", onlineMode);
                } else {
                    boolean addBet;
                    if (onlineMode) {
                        IDBLink dbLink = getDBLink(sessionInfo, gameSession);
                        addBet = savePlayerBetForOnline(sessionInfo, roundInfo, gameSession, bankInfo, dbLink,
                                correctedRoundId, balance);
                    } else {
                        addBet = savePlayerBetForOffline(correctedRoundId, roundInfo, gameSession, bankInfo,
                                account, balance);
                    }
                    long betAmount = Math.round(roundInfo.getBet() * 100);
                    if (addBet) {
                        gameSession.incrementRoundsCount(1);
                        gameSession.setCreateNewBet(true, false);
                        gameSession.update(betAmount, Math.round(roundInfo.getPayout() * 100),
                                0, 0, null, null, null);
                    }
                    //storeMQData(data);
                    createOrUpdateMaxBalance(balance, gameSession, account, tournamentId, data.getNickname(),
                            sessionId, betAmount);
                    SessionHelper.getInstance().commitTransaction();
                    SessionHelper.getInstance().markTransactionCompleted();
                    result = getTournamentInfo(tournamentId, account);
                }
            } finally {
                SessionHelper.getInstance().clearWithUnlock();
            }
        } catch (Exception e) {
            LOG.error("saveTournamentRoundResult failed, sessionId: {}", sessionId, e);
            if (e instanceof KafkaHandlerException) {
                throw (KafkaHandlerException) e;
            }
            throw new KafkaHandlerException(-1, e.getMessage());
        }
        return result;
    }

    /**
     * Save round result and close current tournament session
     *
     * @param accountId player account identifier
     * @param sessionId GS player session identifier
     * @param gameSessionId GS game session identifier
     * @param tournamentId GS side tournament identifier
     * @param balance current balance
     * @param data current MQ player info and preferences
     * @param roundInfo player round info for history
     * @param roundId MQ round identifier
     * @return {@code TSitOutTournamentSessionResult} operation result
     * @throws TException if any unexpected error occur
     */
    public SitOutTournamentSessionResultDto sitOutTournamentSession(long accountId, String sessionId, long gameSessionId,
                                                                    long tournamentId, long balance, MQDataDto data,
                                                                    RoundInfoResultDto roundInfo, long roundId) {
        long correctedRoundId = roundId <= 0 ? IdGenerator.getInstance().getNext(IWallet.class) : roundId;
        LOG.debug("sitOutTournamentSession: accountId={}, sessionId={}, gameSessionId={}, tournamentId={}, " +
                        "balance={}, data={}, roundInfo={}, roundId={}, correctedRoundId={}", accountId, sessionId,
                gameSessionId, tournamentId, balance, data, roundInfo, roundId, correctedRoundId);
        if (roundId <= 0) {
            replaceEmptyRoundId(roundInfo, correctedRoundId);
        }
        SitOutTournamentSessionResultDto result = null;
        try {
            SessionHelper.getInstance().lock(accountId);
            try {
                SessionHelper.getInstance().openSession();
                ITransactionData td = SessionHelper.getInstance().getTransactionData();
                AccountInfo account = td.getAccount();
                SessionInfo sessionInfo = td.getPlayerSession();
                Long activeFRBonusId = FRBonusManager.getInstance().getEarlestActiveFRBonusId(account.getId(),
                        data.getGameId());
                if (activeFRBonusId == null) {
                    activeFRBonusId = -1L;
                }
                GameSession gameSession = SessionHelper.getInstance().getTransactionData().getGameSession();
                if (gameSession == null) {
                    LOG.warn("sitOutTournamentSession: gameSession is not found, gameSessionId={}", gameSessionId);
                    TournamentInfoDto tournament = getTournamentInfo(tournamentId, account);
                    result = new SitOutTournamentSessionResultDto(tournament, activeFRBonusId, true, TransactionErrorCodes.OK, "");
                    LOG.debug("sitOutTournamentSession: result: {}", result);
                    return result;
                }
                BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(account.getBankId());
                boolean onlineMode = isOnlineMode(gameSessionId, sessionInfo, gameSession);
                long betAmount = Math.round(roundInfo.getBet() * 100);
                if ((roundInfo.getArchiveData() == null || roundInfo.getArchiveData().isEmpty())) {
                    LOG.debug("sitOutTournamentSession: No activity in round, update status of player, " +
                            "onlineMode: {}", onlineMode);
                } else {
                    boolean addBet;
                    if (onlineMode) {
                        IDBLink dbLink = getDBLink(sessionInfo, gameSession);
                        addBet = savePlayerBetForOnline(sessionInfo, roundInfo, gameSession, bankInfo, dbLink,
                                correctedRoundId, balance);
                    } else {
                        addBet = savePlayerBetForOffline(correctedRoundId, roundInfo, gameSession, bankInfo, account,
                                balance);
                    }
                    if (addBet) {
                        SessionHelper.getInstance().getDomainSession().persistPlayerBet();
                        gameSession.incrementRoundsCount(1);
                        gameSession.setCreateNewBet(true, false);
                        gameSession.update(betAmount, Math.round(roundInfo.getPayout() * 100),
                                0, 0, null, null, null);
                    }
                }
                //storeMQData(data);
                createOrUpdateMaxBalance(balance, gameSession, account, tournamentId, data.getNickname(), sessionId, betAmount);
                if (onlineMode) {
                    GameServer.getInstance().closeOnlineGame(gameSessionId, false,
                            GameServer.getInstance().getServerId(), sessionInfo, true);
                }
                TournamentInfoDto tournament = getTournamentInfo(tournamentId, account);
                result = new SitOutTournamentSessionResultDto(tournament, activeFRBonusId, true, TransactionErrorCodes.OK, "");
                SessionHelper.getInstance().commitTransaction();
                SessionHelper.getInstance().markTransactionCompleted();
            } finally {
                SessionHelper.getInstance().clearWithUnlock();
            }
        } catch (Exception e) {
            LOG.error("sitOutTournamentSession, sessionId: {}", sessionId, e);
            if (e instanceof KafkaHandlerException) {
                throw (KafkaHandlerException) e;
            }
        }
        return result;
    }

    /**
     * Return player currency code
     *
     * @param accountId GS side account identifier
     * @return currency code
     * @throws TException if any unexpected error occur
     */
    public String getPlayerCurrency(long accountId) {
        LOG.debug("getPlayerCurrency: accountId={}", accountId);
        try {
            SessionHelper.getInstance().lock(accountId);
            try {
                SessionHelper.getInstance().openSession();
                AccountInfo accountInfo = accountManager.getAccountInfo(accountId);
                if (accountInfo == null) {
                    throw new KafkaHandlerException(-1, "Account not found");
                }
                return accountInfo.getCurrency().getCode();
            } finally {
                SessionHelper.getInstance().clearWithUnlock();
            }
        } catch (Exception e) {
            LOG.warn("getPlayerCurrency: Unable to get player currency for multi player lobby (this is may be ok " +
                    "for guest account)", e);
            throw new KafkaHandlerException(-1, e.getMessage());
        }
    }

    /**
     * Return external account identifiers
     *
     * @param accountIds account identifiers set
     * @return {@code Map<Long, String>} external account identifiers
     */
    public Map<Long, String> getExternalAccountIds(List<Long> accountIds) {
        Map<Long, String> result = new HashMap<>();
        for (long accountId : accountIds) {
            AccountInfo accountInfo = cassandraAccountInfoPersister.get(accountId);
            if (accountInfo != null) {
                result.put(accountId, accountInfo.getExternalId());
            }
        }
        return result;
    }

    /**
     * Save current MQ game specific data/preferences
     *
     * @param data {@code TMQData} actual game preferences
     * @throws TException if any unexpected error occur
     */
    public void storeMQData(MQDataDto data) throws KafkaHandlerException {
        try {
            mqDataPersister.persist(MQDataConverter.convert(data));
        } catch (Exception e) {
            LOG.error("Failed to store MQ Data", e);
            throw new KafkaHandlerException(-1, e.getMessage());
        }
    }

    /**
     * Load actual MQ game specific data/preferences
     *
     * @param accountId account identifier
     * @param gameId game identifier for load
     * @return {@code TMQData} actual game preferences
     * @throws TException if any unexpected error occur
     */
    public MQDataWrapperDto getMQData(long accountId, long gameId) {
        try {
            MQData data = mqDataPersister.load(accountId, gameId);
            LOG.info("Loaded MQ Data: {}", data);
            MQDataWrapperDto wrapper = new MQDataWrapperDto();
            if (data != null) {
                wrapper.setData(MQDataConverter.convert(data));
            }
            return wrapper;
        } catch (Exception e) {
            LOG.error("Failed to retrieve MQ Data", e);
            throw new KafkaHandlerException(-1, e.getMessage());
        }
    }

    /**
     * Send balance updated message
     *
     * @param sessionId player session identifier
     * @param balance current balance
     */
    public void sendBalanceUpdated(String sessionId, long balance) {
        BalanceUpdated message = new BalanceUpdated(balance);
        sendWebSocketMessage(sessionId, message);
    }

    /**
     * Add reserved or censored nickname
     *
     * @param region region/country identifier
     * @param owner owner identifier
     * @param nicknames reserved nickname
     */
    public void addMQReservedNicknames(String region, long owner, Set<String> nicknames) {
        LOG.debug("addMQReservedNicknames: region={}, owner={}, nicknames={}", region, owner, nicknames);
        for (String nickname : nicknames) {
            mqReservedNicknamePersister.persist(region, nickname, owner);
        }
    }

    /**
     * Remove reserved or censored nickname
     *
     * @param region region/country identifier
     * @param owner owner identifier
     * @param nicknames reserved nickname
     */
    public void removeMQReservedNicknames(String region, long owner, Set<String> nicknames) {
        LOG.debug("removeMQReservedNicknames: region={}, owner={}, nicknames={}", region, owner, nicknames);
        for (String nickname : nicknames) {
            mqReservedNicknamePersister.remove(region, nickname);
        }
    }

    /**
     * Return Crash game settings, this method used for periodicaly refresh cache values
     *
     * @param bankIds bank identifiers
     * @param gameId game identifier
     * @return {@code Set<TCrashGameSetting>} crash game settings
     * @throws TException if any unexpected error occur
     */
    public Set<CrashGameSettingDto> getCrashGameSettings(Set<Long> bankIds, int gameId) {

        LOG.debug("getCrashGameSettings: bankIds={}, gameId={}", bankIds, gameId);

        Set<CrashGameSettingDto> result = new HashSet<>(bankIds.size());

        for (Long bankId : bankIds) {

            BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);

            if (bankInfo == null) {
                continue;
            }

            Currency currency = bankInfo.getDefaultCurrency();

            IBaseGameInfo gameInfo = BaseGameCache.getInstance().getGameInfoById(bankId, gameId, currency);

            if (gameInfo != null) {
                @SuppressWarnings("unchecked")
                Map<String, String> properties = gameInfo.getPropertiesMap();
                int maxRoomPlayers = PropertyUtils.getIntProperty(properties, BaseGameConstants.KEY_MULTIPLAYER_MAX_ROOM_PLAYERS, Integer.MAX_VALUE);
                int maxMultiplier = PropertyUtils.getIntProperty(properties, BaseGameConstants.KEY_CRASHGAME_MAX_MULTIPLIER, Integer.MAX_VALUE);
                long maxPlayerProfitInRound = getMaxPlayerProfitInRound(bankInfo, properties, BaseGameConstants.KEY_CRASHGAME_MAX_PLAYER_PROFIT);
                long totalPlayersProfitInRound = getMaxPlayerProfitInRound(bankInfo, properties, BaseGameConstants.KEY_CRASHGAME_MAX_ALL_PLAYERS_PROFIT);
                ILimit limit = gameInfo.getLimit();
                long minLimit = getMinLimit(limit, bankInfo);
                long maxLimit = getMaxLimit(limit, bankInfo);

                CrashGameSettingDto gameSetting = new CrashGameSettingDto(
                        bankId,
                        currency.getCode(),
                        maxRoomPlayers,
                        maxMultiplier,
                        maxPlayerProfitInRound,
                        totalPlayersProfitInRound,
                        minLimit,
                        maxLimit,
                        bankInfo.isCWSendRealBetWin()
                );

                result.add(gameSetting);
                LOG.debug("getCrashGameSettings: add gameSetting={}", gameSetting);
            }
        }

        return result;
    }

    private BuyInResultDto performRegularBuyIn(String sessionId, long cents, long gameSessionId, long roomId,
                                             int betNumber, long mpRoundId) throws KafkaHandlerException {
        BuyInResultDto result;
        boolean shouldPersistError = false;
        try {
            SessionHelper.getInstance().lock(sessionId);
            try {
                SessionHelper.getInstance().openSession();
                SessionInfo sessionInfo = SessionHelper.getInstance().getTransactionData().getPlayerSession();
                if (sessionInfo == null) {
                    LOG.warn("Unable to perform buy in - SessionInfo not found {}", sessionId);
                    throw new KafkaHandlerException(-1, "Session not found");
                }
                AccountInfo account = accountManager.getAccountInfo(sessionInfo.getAccountId());
                GameSession gameSession = SessionHelper.getInstance().getTransactionData().getGameSession();
                if (gameSession == null) {
                    LOG.warn("Unable to perform buy in - GameSession not found {}", sessionId);
                    throw new KafkaHandlerException(-1, "GameSession not found");
                }
                shouldPersistError = isSessionForRealAccountAndMode(account, gameSession);
                LOG.debug("buyIn: sessionId={} balance={}, freeBalance={}", sessionId, account.getBalance(),
                        account.getFreeBalance());
                boolean walletBank = WalletProtocolFactory.getInstance().isWalletBank(account.getBankId());
                IDBLink dbLink = getDBLink(sessionInfo, gameSession);
                Long roundId = dbLink.getRoundId();
                result = performBuyIn(account, sessionInfo, gameSession, cents, walletBank, dbLink, roundId, mpRoundId,
                        roomId, betNumber);
                SessionHelper.getInstance().commitTransaction();
                SessionHelper.getInstance().markTransactionCompleted();
            } finally {
                SessionHelper.getInstance().clearWithUnlock();
            }
        } catch (Throwable e) {
            LOG.error("Failed to perform buy in", e);
            if (shouldPersistError) {
                errorPersisterHelper.persistBuyInError(sessionId, cents, gameSessionId, roomId, betNumber, (Exception) e);
            }
            if (e instanceof KafkaHandlerException && ((KafkaHandlerException) e).getCode() > 0) {
                KafkaHandlerException ce = (KafkaHandlerException) e;
                result = new BuyInResultDto(0, 0, 0, gameSessionId, false, ce.getCode(), ce.getMessage());
            } else {
                if (e instanceof KafkaHandlerException) {
                    throw (KafkaHandlerException) e;
                }
                throw new KafkaHandlerException(-1, e.getMessage());
            }
        } finally {
            httpRequestContext.clear();
        }
        return result;
    }

    private BuyInResultDto performBuyIn(AccountInfo account, SessionInfo sessionInfo, GameSession gameSession,
                                      long cents, boolean walletBank, IDBLink dbLink, Long roundId, Long mpRoundId,
                                      long roomId, int betNumber) throws Exception {
        if (cents <= 0) {
            throw new Exception("Invalid buyIn amount: " + cents);
        }
        if (account.isGuest() || !gameSession.isRealMoney()) {
            if (account.getFreeBalance() >= cents) {
                account.setFreeBalance(account.getFreeBalance() - cents);
                LOG.debug("Buy in for guest {} success", sessionInfo.getSessionId());
                return new BuyInResultDto(cents, account.getFreeBalance(), roundId, gameSession.getId(), true, 0, "");
            } else {
                LOG.warn("Unable to perform buy in for {} - not enough money", sessionInfo.getSessionId());
                throw new KafkaHandlerException(-1, "Not enough money");
            }
        } else {
            if (walletBank) {
                makeWalletBet(sessionInfo, dbLink, gameSession, account, cents, roundId, roomId, betNumber, mpRoundId);
            } else {
                makeBet(sessionInfo, dbLink, gameSession, account, cents);
                String debitExternalTransactionIdForMpGame = getDebitExternalTransactionIdForMpGame(account.getId(),
                        gameSession.getId(), roomId, betNumber);
                ExternalPaymentTransaction transaction = new ExternalPaymentTransaction(debitExternalTransactionIdForMpGame,
                        account.getExternalId(), account.getId(), account.getBankId(), cents, gameSession.getId(),
                        gameSession.getGameId(), System.currentTimeMillis(), null,
                        walletBank ? PaymentMode.WALLET : PaymentMode.COMMON_TRANSFER, null,
                        TransactionStatus.APPROVED, TransactionType.DEPOSIT,
                        "", roundId, "", false);
                externalTransactionPersister.persist(transaction);
            }
            LOG.debug("Buy in for {} success, roundId is {}", sessionInfo.getSessionId(), roundId);
            return new BuyInResultDto(cents, account.getBalance(), roundId, gameSession.getId(), true, 0, "");
        }
    }

    private String getDebitExternalTransactionIdForMpGame(long accountId, long gameSessionId, long roomId,
                                                          int betNumber) {
        return accountId + "+" + gameSessionId + "+" + roomId + "+" + betNumber;
    }

    private void makeBet(SessionInfo sessionInfo, IDBLink dbLink, GameSession gameSession,
                         AccountInfo account, long amount) {
        LOG.debug("makeBet: before debit account={}", account);
        sessionInfo.updateActivity();
        try {
            dbLink.incrementBalance(-amount, 0);
            dbLink.interceptBet(amount, 0);
            dbLink.updateCurrentBetWin(amount, 0);
            dbLink.updateLastActivity();
            if (StringUtils.isTrimmedEmpty(dbLink.getLasthand())) {
                Map<String, String> lasthand1 = new HashMap<>();
                Map<String, String> lasthand2 = new HashMap<>();
                lasthand1.put(IGameController.PARAMROUNDID, Long.toString(dbLink.getRoundId()));
                lasthand2.put(IGameController.PARAMROUNDID, Long.toString(dbLink.getRoundId()));
                String data = LasthandHelper.pack(lasthand1, lasthand2, null, null);
                LOG.debug("MQ saveLasthand roundId: {}", data);
                LasthandPersister.getInstance().save(gameSession.getGameId(), data);
            }
        } catch (Exception e) {
            LOG.error("cannot make bet sessionInfo={}, gameSession={}", sessionInfo, gameSession, e);
            throw new KafkaHandlerException(-1, "Debit failed");
        }
        LOG.debug("makeBet: after debit account={}", account);
    }

    private void makeWalletBet(SessionInfo sessionInfo, IDBLink dbLink, GameSession gameSession,
                               AccountInfo account, long amount, long roundId, long roomId, int betNumber, long mpRoundId) throws Exception {
        LOG.debug("makeWalletBet: before debit account={}", account);
        IWallet wallet = SessionHelper.getInstance().getTransactionData().getWallet();
        checkPendingOperation(wallet, gameSession.getGameId());
        sessionInfo.updateActivity();
        String debitExternalTransactionIdForMpGame = getDebitExternalTransactionIdForMpGame(account.getId(),
                gameSession.getId(), roomId, betNumber);
        ExternalPaymentTransaction transaction = new ExternalPaymentTransaction(debitExternalTransactionIdForMpGame,
                account.getExternalId(), account.getId(), account.getBankId(), amount,
                gameSession.getId(), gameSession.getGameId(), System.currentTimeMillis(), null, PaymentMode.WALLET,
                null, TransactionStatus.STARTED, TransactionType.DEPOSIT,
                "", roundId, "", false);
        externalTransactionPersister.persist(transaction);
        MultiplayerExternalWallettransactionHandler handler = new MultiplayerExternalWallettransactionHandler(transaction.getBankId(),
                transaction.getExtId());
        try {
            WalletProtocolFactory.getInstance().interceptCreateWallet(account, account.getBankId(), gameSession.getId(),
                    (int) gameSession.getGameId(), com.dgphoenix.casino.common.cache.data.game.GameMode.REAL, sessionInfo.getClientType());
            WalletProtocolFactory.getInstance().interceptDebit(account.getId(), account.getBankId(), amount, dbLink,
                    sessionInfo, handler, mpRoundId);
            makeBet(sessionInfo, dbLink, gameSession, account, amount);
        } catch (WalletException e) {
            LOG.error("makeWalletBet: debit failed sessionInfo={}, gameSession={}, walletException={}", sessionInfo,
                    gameSession, e.toString(), e);
            externalTransactionPersister.persist(transaction);
            WalletTracker.getInstance().addTask(account.getId());
            CWError walletError = e.getWalletError();
            if (walletError != null) {
                throw new KafkaHandlerException(walletError.getCode(), walletError.getDescription());
            } else {
                Integer code = e.tryToGetNumericErrorCode();
                throw new KafkaHandlerException(code != null ? code : 0, e.getMessage());
            }
        } catch (Exception e) {
            LOG.error("makeWalletBet: unexpected error, debit failed sessionInfo={}, gameSession={}",
                    sessionInfo, gameSession, e);
            throw new KafkaHandlerException(-1, "Debit failed");
        }
        try {
            LOG.debug("makeWalletBet: old transaction={}", transaction);
            //need reload transaction before changeStatus
            transaction = externalTransactionPersister.get(account.getBankId(), debitExternalTransactionIdForMpGame);
            transaction.setStatus(TransactionStatus.APPROVED);
            transaction.setFinishDate(System.currentTimeMillis());
            externalTransactionPersister.persist(transaction);
            LOG.debug("makeWalletBet: persist transaction={}", transaction);
            WalletProtocolFactory.getInstance().interceptDebitCompleted(account.getId(), dbLink, true, null);
        } catch (WalletException e) {
            LOG.error("processing error: interceptDebitCompleted failed sessionInfo={}, gameSession={}",
                    sessionInfo, gameSession, e);
            throw new KafkaHandlerException(-1, "Debit completed failed");
        }
        LOG.debug("makeWalletBet: after debit account={}", account);
    }

    private BuyInResultDto performTournamentReBuy(String sessionId, long tournamentId,
                                                long currentBalance) throws KafkaHandlerException {
        try {
            IPromoCampaign campaign = campaignManager.getPromoCampaign(tournamentId);
            if (campaign != null && campaign.getTemplate() instanceof MaxBalanceTournamentPromoTemplate) {
                MaxBalanceTournamentPromoTemplate template = (MaxBalanceTournamentPromoTemplate) campaign.getTemplate();
                long accountId = getAccountId(sessionId);
                MaxBalanceTournamentPlayerDetails details = maxBalanceTournamentPersister
                        .getForAccount(accountId, tournamentId);
                if (details == null) {
                    throw new KafkaHandlerException(-1, "Player should join tournament prior to ReBuy");
                }
                if ((!template.isReBuyEnabled() || (template.getReBuyLimit() != -1
                        && template.getReBuyLimit() <= details.getReBuyCount())) && details.getCurrentBalance() < 1) {
                    PlayerTournamentStateChanged message = new PlayerTournamentStateChanged(tournamentId, true, false);
                    sendPlayerTournamentStateChanged(sessionId, message);
                    throw new KafkaHandlerException(-1, "ReBuy limit exceeded");
                }
                long reBuyAmount = template.getReBuyAmount();
                tournamentBuyInHelper.performBuyIn(sessionId, tournamentId, template.getReBuyPrice(),
                        campaign.getBaseCurrency(), details.getReBuyCount() + 1, reBuyAmount, false);
                details.addReBuyAmount(reBuyAmount);
                details.incrementReBuyCount();
                long fixedReBuyAmount = template.isResetBalance() ? reBuyAmount - currentBalance : reBuyAmount;
                details.setCurrentBalance(template.isResetBalance() ? reBuyAmount : reBuyAmount + currentBalance);
                maxBalanceTournamentPersister.persist(details);
                sendBalanceUpdatedToAllServers(sessionId, getBalance(sessionId, "REAL"));
                return new BuyInResultDto(fixedReBuyAmount, 0, 0, 0, true, 0, "");
            } else {
                throw new KafkaHandlerException(-1, "Tournament not found");
            }
        } catch (WalletException e) {
            LOG.error("Failed to perform reBuy", e);
            CWError walletError = e.getWalletError();
            if (walletError != null) {
                if (walletError.getCode() == CommonWalletErrors.INSUFFICIENT_FUNDS.getCode()) {
                    throw new KafkaHandlerException(1008, "Insufficient balance");
                } else {
                    return new BuyInResultDto(0, 0, 0, 0, false, walletError.getCode(), walletError.getDescription());
                }
            }
            throw new KafkaHandlerException(-1, e.getMessage());
        } catch (Throwable e) {
            LOG.error("Failed to perform reBuy", e);
            if (e instanceof KafkaHandlerException) {
                throw (KafkaHandlerException) e;
            }
            throw new KafkaHandlerException(-1, e.getMessage());
        }
    }

    private void uploadLeaderboardResult(long bankId, long leaderboardId, long endDate, String resultString) {
        boolean success = false;
        try {
            success = new LeaderboardWinUploader().upload(bankId, leaderboardId, resultString);
        } catch (Exception e) {
            LOG.error("Unexpected error during upload", e);
        }
        if (!success) {
            LeaderboardWinTracker.getInstance().addTask(bankId, leaderboardId, endDate, resultString);
        }
    }

    private Pair<Long, Long> getCrashLimit(IBaseGameInfo gameInfo, BankInfo bankInfo) {
        ILimit gameLimit = gameInfo.getLimit();
        long minLimit = getMinLimit(gameLimit, bankInfo);
        if (minLimit == Long.MAX_VALUE) {
            minLimit = CRASH_DEFAULT_MIN_STAKE;
        }
        long maxLimit = getMaxLimit(gameLimit, bankInfo);
        if (maxLimit == Long.MAX_VALUE) {
            maxLimit = CRASH_DEFAULT_MAX_STAKE;
        }
        return new Pair(minLimit, maxLimit);
    }

    private long getMinLimit(ILimit limit, BankInfo bankInfo) {
        boolean defaultLimit = isDefaultLimit(limit, bankInfo);
        if (defaultLimit) {
            return bankInfo.getLimit() != null ? bankInfo.getLimit().getMinValue() : Long.MAX_VALUE;
        } else {
            return limit.getMinValue();
        }
    }

    private long getMaxLimit(ILimit limit, BankInfo bankInfo) {
        boolean defaultLimit = isDefaultLimit(limit, bankInfo);
        if (defaultLimit) {
            return bankInfo.getLimit() != null ? bankInfo.getLimit().getMaxValue() : Long.MAX_VALUE;
        } else {
            return limit.getMaxValue();
        }
    }

    private boolean isDefaultLimit(ILimit limit, BankInfo bankInfo) {
        return limit == null || limit.equals(bankInfo.getLimit());
    }

    /**
     * Calculate and return max crash game profit
     *
     * @param gameInfo {@code IBaseGameInfo} game info
     * @param bankInfo {@code BankInfo} bank info
     * @param currency {@code Currency} currency for conversion
     * @return max possible profit
     * @throws com.dgphoenix.casino.common.exception.CommonException if cuurency not found or conversion rate not found
     */
    public double getCrashMaxProfit(IBaseGameInfo gameInfo, BankInfo bankInfo, Currency currency)
            throws com.dgphoenix.casino.common.exception.CommonException {
        @SuppressWarnings("unchecked")
        Map<String, String> properties = gameInfo.getPropertiesMap();
        long maxPlayerProfitInRound = getMaxPlayerProfitInRound(bankInfo, properties, BaseGameConstants.KEY_CRASHGAME_MAX_PLAYER_PROFIT);
        boolean isUsedDefaultProfit = false;
        if (maxPlayerProfitInRound == Long.MAX_VALUE) {
            maxPlayerProfitInRound = CRASH_MAX_DEFAULT_PROFIT;
            isUsedDefaultProfit = true;
        }
        long totalPlayersProfitInRound = getMaxPlayerProfitInRound(bankInfo, properties, BaseGameConstants.KEY_CRASHGAME_MAX_ALL_PLAYERS_PROFIT);
        if (totalPlayersProfitInRound == Long.MAX_VALUE) {
            totalPlayersProfitInRound = CRASH_MAX_DEFAULT_PROFIT;
        }
        long maxProfit = Math.min(maxPlayerProfitInRound, totalPlayersProfitInRound);
        return currencyRateManager.convert(maxProfit, isUsedDefaultProfit ? "EUR" : bankInfo.getDefaultCurrency().getCode(), currency.getCode());
    }

    private long getMaxPlayerProfitInRound(BankInfo bankInfo, Map<String, String> bgiProperties, String propertyName) {

        Long maxPlayerProfitInRound = PropertyUtils.getLongProperty(bgiProperties, propertyName);
        String currency = bankInfo.getDefaultCurrency().getCode();
        LOG.debug("getMaxPlayerProfitInRound: maxPlayerProfitInRound={}, currency={}", maxPlayerProfitInRound, currency);

        if (maxPlayerProfitInRound == null) {
            Long maxExposure = bankInfo.getMaxWin();
            if (maxExposure != null) {
                maxPlayerProfitInRound = getMaxPlayerProfitByGlExposure(bankInfo, maxExposure, currency);
            } else {
                maxPlayerProfitInRound = Long.MAX_VALUE;
            }
            LOG.debug("getMaxPlayerProfitInRound: maxExposure={}, maxPlayerProfitInRound={}", maxExposure, maxPlayerProfitInRound);
        }

        return maxPlayerProfitInRound;
    }

    private long getMaxPlayerProfitByGlExposure(BankInfo bankInfo, Long maxExposure, String defaultCurrency) {

        LOG.debug("getMaxPlayerProfitByGlExposure: maxExposure={}, defaultCurrency={}, bankInfo={}",
                maxExposure, defaultCurrency, bankInfo);

        long maxPlayerProfitInRound;
        try {
            if (bankInfo.isGLUseDefaultCurrency()) {

                double maxDefaultProfitInBankCurrency = currencyRateManager.convert(CRASH_MAX_DEFAULT_PROFIT, "EUR", defaultCurrency);

                if (maxExposure < maxDefaultProfitInBankCurrency) {
                    maxPlayerProfitInRound = maxExposure;
                } else {
                    maxPlayerProfitInRound = Long.MAX_VALUE;
                }

                LOG.debug("getMaxPlayerProfitByGlExposure: maxPlayerProfitInRound={}, maxDefaultProfitInBankCurrency={}",
                        maxPlayerProfitInRound, maxDefaultProfitInBankCurrency);

            } else {

                if(maxExposure < CRASH_MAX_DEFAULT_PROFIT) {
                    maxPlayerProfitInRound = (long) currencyRateManager.convert(maxExposure, "EUR", defaultCurrency);
                } else {
                    maxPlayerProfitInRound = Long.MAX_VALUE;
                }

                LOG.debug("getMaxPlayerProfitByGlExposure: maxPlayerProfitInRound={}", maxPlayerProfitInRound);
            }

        } catch (Exception e) {
            LOG.error("getMaxPlayerProfitByGlExposure: error", e);
            maxPlayerProfitInRound = Long.MAX_VALUE;
        }

        return maxPlayerProfitInRound;
    }

    private void createOrUpdateMaxBalance(long balance, GameSession gameSession, AccountInfo account,
                                          long tournamentId, String nickName, String sessionId, long betAmount) {
        if (gameSession != null && (gameSession.isFRBonusGameSession() || gameSession.isBonusGameSession()
                || !gameSession.isRealMoney())) {
            LOG.error("createOrUpdateMaxBalance: wrong gameSession={}", gameSession);
            return;
        }
        try {
            IPromoCampaign promoCampaign = campaignManager.getPromoCampaign(tournamentId);
            if (promoCampaign != null && promoCampaign.getTemplate() instanceof MaxBalanceTournamentPromoTemplate) {
                long accountId = account.getId();
                MaxBalanceTournamentPromoTemplate template = (MaxBalanceTournamentPromoTemplate) promoCampaign.getTemplate();
                MaxBalanceTournamentPlayerDetails details =
                        maxBalanceTournamentPersister.getForAccount(accountId, tournamentId);
                if (details != null) {
                    details.setCurrentBalance(balance);
                    details.setMaxBalance(Math.max(details.getMaxBalance(), balance));
                    details.setNickname(nickName);
                    details.addBetAmount(betAmount);
                } else {
                    details = new MaxBalanceTournamentPlayerDetails(account.getBankId(), account.getExternalId(),
                            accountId, tournamentId, nickName, balance, template.getBuyInAmount(),
                            0, 0, balance, System.currentTimeMillis(), betAmount);
                    LOG.error("MaxBalanceTournamentPlayerDetails is null, create new: " +
                            "tournamentId={}, accountId={}", tournamentId, accountId);
                }
                LOG.debug("createOrUpdateMaxBalance: details={}", details);
                if ((!template.isReBuyEnabled() || (template.getReBuyLimit() != -1
                        && template.getReBuyLimit() <= details.getReBuyCount())) && details.getCurrentBalance() < 1) {
                    sendPlayerTournamentStateChanged(sessionId, new PlayerTournamentStateChanged(tournamentId, true, false));
                }
                maxBalanceTournamentPersister.persist(details);
            }
        } catch (Exception e) {
            LOG.error("Unexpected error occurred", e);
        }
    }

    /**
     * Send tournament state change notification
     *
     * @param sessionId player session identifier
     * @param tournamentId tournament identifier
     * @param cannotJoin true if join to tournament not allowed
     * @param joined true if player joined
     */
    public void sendPlayerTournamentStateChanged(String sessionId, long tournamentId, boolean cannotJoin,
                                                 boolean joined) {
        PlayerTournamentStateChanged message = new PlayerTournamentStateChanged(tournamentId, cannotJoin, joined);
        sendWebSocketMessage(sessionId, message);
    }

    /**
     * Add tournament websocket listener
     *
     * @param listener listener for add
     */
    public void addTournamentWebSocketMessageListener(TournamentWebSocketMessageListener listener) {
        webSocketMessageListeners.add(listener);
    }

    private void sendWebSocketMessage(String sessionId, TObject message) {
        webSocketMessageListeners.forEach(listener -> listener.notify(sessionId, message));
    }

    private void sendPlayerTournamentStateChanged(String sessionId, PlayerTournamentStateChanged message) {
        webSocketMessageListeners.forEach(listener -> listener.sendPlayerTournamentStateChanged(sessionId, message));
    }

    //required for tournament mode, roundId may be unknown
    private void replaceEmptyRoundId(RoundInfoResultDto roundInfo, long roundId) {
        String corrected = roundInfo.getArchiveData().replace("playerRoundId=0", "playerRoundId=" + roundId);
        roundInfo.setArchiveData(corrected);
    }

    private void sendBalanceUpdatedToAllServers(String sessionId, long balance) {
        for (TournamentWebSocketMessageListener webSocketMessageListener : webSocketMessageListeners) {
            webSocketMessageListener.sendUpdateBalanceToAllServers(sessionId, balance);
        }
    }

    private long getAccountId(String sessionId) throws com.dgphoenix.casino.common.exception.CommonException {
        SessionHelper.getInstance().lock(sessionId);
        try {
            SessionHelper.getInstance().openSession();
            SessionInfo sessionInfo = SessionHelper.getInstance().getTransactionData().getPlayerSession();
            if (sessionInfo == null) {
                throw new KafkaHandlerException(-1, "SessionInfo not found");
            }
            return sessionInfo.getAccountId();
        } finally {
            SessionHelper.getInstance().clearWithUnlock();
        }
    }

    private void persistRoundParticipants(String sid, long gameSessionId, RoundInfoResultDto roundInfoResult) {
        try {
            Set<Long> accountIds = roundInfoResult.getBattlegroundRoundInfo().getPlaces().stream()
                    .map(PlaceDto::getAccountId)
                    .collect(Collectors.toSet());
            battlegroundHistoryPersister.addParticipants(new BattlegroundRoundParticipant(sid, roundInfoResult.getBattlegroundRoundInfo().getRoundId(),
                    gameSessionId, accountIds, roundInfoResult.getRoundStartTime(), roundInfoResult.getTime(), roundInfoResult.getBattlegroundRoundInfo().getPrivateRoomId()), accountIds);
        } catch (Exception e) {
            LOG.error("persistRoundParticipants error", e);
        }
    }

    private long getActualBalance(GameSession gameSession, AccountInfo account) {
        boolean isReal = gameSession != null && (gameSession.isRealMoney() || gameSession.isFRBonusGameSession());
        return account.isGuest() || !isReal ? account.getFreeBalance() : account.getBalance();
    }

    private String getExternalTransactionIdForMpGame(long accountId, long roomId, long roundId) {
        return accountId + "+" + roomId + "+" + roundId;
    }

    private boolean isSessionForRealAccountAndMode(AccountInfo accountInfo, String mode) {
        boolean isReal = "real".equalsIgnoreCase(mode) || "frb".equalsIgnoreCase(mode) ||
                "cashbonus".equalsIgnoreCase(mode);
        return !accountInfo.isGuest() && isReal;
    }

    private boolean isSessionForRealAccountAndMode(AccountInfo accountInfo, GameSession gameSession) {
        boolean isReal = gameSession != null && (gameSession.isRealMoney() || gameSession.isFRBonusGameSession());
        return !accountInfo.isGuest() && isReal;
    }

    private long getRebuyAmount(long tournamentId, String playerCurrency) throws com.dgphoenix.casino.common.exception.CommonException {
        long rebuyAmount = 0;
        IPromoCampaign promoCampaign = campaignManager.getPromoCampaign(tournamentId);
        if (promoCampaign != null && promoCampaign.getTemplate() instanceof MaxBalanceTournamentPromoTemplate) {
            MaxBalanceTournamentPromoTemplate template = (MaxBalanceTournamentPromoTemplate) promoCampaign.getTemplate();
            rebuyAmount = (long) currencyRateManager.convert(template.getBuyInAmount(),
                    promoCampaign.getBaseCurrency(), playerCurrency);
        }
        return rebuyAmount;
    }

    private CashBonusDto getBonus(long bonusId, long gameId, AccountInfo accountInfo)
            throws com.dgphoenix.casino.common.exception.CommonException {
        if (bonusId < 0) {
            return null;
        }
        Bonus bonus = BonusManager.getInstance().getById(bonusId);
        if (bonus == null || bonus.getStatus() != BonusStatus.ACTIVE || bonus.getAccountId() != accountInfo.getId()) {
            LOG.error("Bonus is invalid, bonusId={}, bonus={}, accountId={}", bonusId, bonus, accountInfo.getId());
            throw new com.dgphoenix.casino.common.exception.CommonException("bonus is invalid");
        }
        if (!bonus.isReady()) {
            LOG.error("Bonus not ready, bonusId={}, bonus={}", bonusId, bonus);
            throw new com.dgphoenix.casino.common.exception.CommonException("bonus is not ready yet");
        }
        if (!BonusManager.getInstance().bonusIsValidForGameId(bonus, accountInfo, gameId)) {
            LOG.error("gameId is not found for this bonus, bonusId={}, bonus={}", bonusId, bonus);
            throw new com.dgphoenix.casino.common.exception.CommonException("gameId is not found for this bonus");
        }
        return new CashBonusDto(bonus.getId(), bonus.getTimeAwarded(), bonus.getExpirationDate(), bonus.getBalance(),
                bonus.getAmount(), bonus.getAmountToRelease(), bonus.getRolloverMultiplier(), bonus.getBetSum(),
                bonus.getStatus().name(), bonus.getMaxWinLimit() == null ? -1 : bonus.getMaxWinLimit());
    }

    public List<Long> getCoins(BankInfo bankInfo, long gameId, Currency currency) {
        IBaseGameInfo info = BaseGameCache.getInstance().getGameInfoById(bankInfo.getId(), gameId, currency);
        List<Coin> coins = null;
        if (info != null) {
            coins = info.getCoins();
        }
        if (coins == null || coins.isEmpty()) {
            coins = bankInfo.getCoins();
        }
        List<Long> result = new ArrayList<>();
        for (Coin coin : coins) {
            result.add(coin.getValue());
        }
        return result;
    }

    private List<BattlegroundInfoDto> getBattlegroundInfo(String sessionId, BankInfo bankInfo, Currency currency)
            throws com.dgphoenix.casino.common.exception.CommonException {
        Set<BattlegroundInfo> battlegroundInfos = tournamentBuyInHelper.
                getBattlegroundInfos(sessionId, bankInfo.getId(), currency.getCode());

        List<BattlegroundInfoDto> result = null;
        if (!com.dgphoenix.casino.common.util.CollectionUtils.isEmpty(battlegroundInfos)) {
            result = new ArrayList<>(battlegroundInfos.size());
            for (BattlegroundInfo info : battlegroundInfos) {
                result.add(convert(info));
            }
        }

        LOG.debug("getBattlegroundInfo: bankId = {} , result.size() = {} ",
                bankInfo.getId(), result != null ? result.size() : null);

        return result;
    }

    private BattlegroundInfoDto convert(BattlegroundInfo info) {
        return new BattlegroundInfoDto(info.getGameId(), info.getIcon(), info.getRules(), info.getBuyIns(),
                info.getRake());
    }

    private double getMQLbContributionPercent(IBaseGameInfo gameInfo) {
        Double percent = gameInfo.getMQLeaderboardContributionPercent();
        return percent != null ? percent : 0.02;
    }

    private TournamentInfoDto getTournamentInfo(long tournamentId, AccountInfo accountInfo)
            throws com.dgphoenix.casino.common.exception.CommonException {
        if (tournamentId < 0) {
            return null;
        }
        IPromoCampaign promoCampaign = campaignManager.getPromoCampaign(tournamentId);
        if (promoCampaign == null) {
            throw new com.dgphoenix.casino.common.exception.CommonException("Tournament not found");
        }
        boolean reBuyAllowed = false;
        long buyInPrice = 0;
        long buyInAmount = 0;
        long reBuyPrice = 0;
        long reBuyAmount = 0;
        int reBuyLimit = 0;
        boolean resetBalanceAfterRebuy = false;
        if (promoCampaign.getTemplate() instanceof MaxBalanceTournamentPromoTemplate) {
            MaxBalanceTournamentPromoTemplate template =
                    (MaxBalanceTournamentPromoTemplate) promoCampaign.getTemplate();
            reBuyAllowed = template.isReBuyEnabled();
            buyInPrice = (long) currencyRateManager.convert(template.getBuyInPrice(),
                    promoCampaign.getBaseCurrency(), accountInfo.getCurrency().getCode());
            buyInAmount = template.getBuyInAmount();
            reBuyPrice = (long) currencyRateManager.convert(template.getReBuyPrice(),
                    promoCampaign.getBaseCurrency(), accountInfo.getCurrency().getCode());
            reBuyAmount = template.getReBuyAmount();
            reBuyLimit = template.getReBuyLimit();
            resetBalanceAfterRebuy = template.isResetBalance();
        }
        MaxBalanceTournamentPlayerDetails details =
                maxBalanceTournamentPersister.getForAccount(accountInfo.getId(), tournamentId);
        if (details == null) {
            LOG.error("getTournamentInfo, playerDetails not found, tournamentId={}, accountId={}",
                    tournamentId, accountInfo.getId());
            throw new com.dgphoenix.casino.common.exception.CommonException("Player not joined to tournament");
        }
        com.abs.casino.common.util.DatePeriod period = promoCampaign.getActionPeriod();
        return new TournamentInfoDto(
                tournamentId,
                promoCampaign.getName(),
                promoCampaign.getStatus().toString(),
                period.getStartDate().getTime(),
                period.getEndDate().getTime(),
                details.getCurrentBalance(),
                buyInPrice,
                buyInAmount,
                reBuyAllowed,
                reBuyPrice,
                reBuyAmount,
                details.getReBuyCount(),
                reBuyLimit,
                resetBalanceAfterRebuy);
    }

    private com.dgphoenix.casino.common.cache.data.game.GameMode resolveMQMode(String mode) {
        if ("real".equalsIgnoreCase(mode) || "frb".equalsIgnoreCase(mode) || "tournament".equalsIgnoreCase(mode)) {
            return REAL;
        } else if ("cashbonus".equalsIgnoreCase(mode)) {
            return BONUS;
        } else {
            return FREE;
        }
    }

    private Long getTransactionId(int gameId, long bankId, ITransactionData transactionData) {
        if (WalletProtocolFactory.getInstance().isWalletBank(bankId)) {
            IWallet wallet = transactionData.getWallet();
            if (wallet != null) {
                IWalletOperation walletOperation = wallet.getCurrentWalletOperation(gameId);
                return walletOperation != null ? walletOperation.getId() : -1L;
            }
        }
        return -1L;
    }

    private boolean needUpdateNickname(MaxBalanceTournamentPlayerDetails tournamentPlayerDetails, IPromoCampaign promoCampaign) {
        return tournamentPlayerDetails != null &&
                !promoCampaign.isNetworkPromoCampaign() &&
                !(promoCampaign instanceof NetworkPromoEvent);
    }

    private IDBLink getDBLink(SessionInfo sessionInfo, GameSession gameSession) {
        checkInvalidDBLink(sessionInfo);
        IDBLink dbLink = DBLinkCache.getInstance().get(sessionInfo.getGameSessionId());
        if (dbLink == null) {
            try {
                dbLink = GameServer.getInstance().restartGame(sessionInfo, gameSession);
            } catch (com.dgphoenix.casino.common.exception.CommonException e) {
                LOG.error("Unable to create dbLink", e);
                throw new KafkaHandlerException(-1, "Internal error, cannot create dbLink: " + e.getMessage());
            }
            LOG.debug("recreated dbLink={}", dbLink);
        }
        if (dbLink.getRoundId() == null) {
            dbLink.setRoundId(dbLink.generateRoundId());
        }
        sessionInfo.updateActivity();
        dbLink.updateLastActivity();
        return dbLink;
    }

    private void checkInvalidDBLink(SessionInfo sessionInfo) {
        Long gameSessionId = sessionInfo.getGameSessionId();
        if (gameSessionId != null) {
            ITransactionData td = SessionHelper.getInstance().getTransactionData();
            if (td.getLastLockerId() != GameServer.getInstance().getServerId()) {
                LOG.info("Remove invalid dbLink. Current locker={}; Last locker={}",
                        GameServer.getInstance().getServerId(), td.getLastLockerId());
                DBLinkCache.getInstance().remove(gameSessionId);
            }
        }
    }

    private void checkAvailablePromos(GameSession gameSession, AccountInfo accountInfo)
            throws com.dgphoenix.casino.common.exception.CommonException {
        if (!gameSession.isFRBonusGameSession() && !gameSession.isBonusGameSession() && gameSession.isRealMoney()) {
            Set<IPromoCampaign> active = campaignManager.getTournamentsForMultiplayerGames(gameSession.getBankId(),
                    gameSession.getGameId(), Status.STARTED, accountInfo);
            LOG.debug("checkAvailablePromos: active={}", active);
            if (CollectionUtils.isNotEmpty(active)) {
                List<Long> ids = new ArrayList<>(active.size());
                for (IPromoCampaign campaign : active) {
                    LOG.debug("checkAvailablePromos: found={}", campaign);
                    ids.add(campaign.getId());
                }
                gameSession.setPromoCampaignIds(ids);
                campaignManager.registerPlayerInPromos(ids, accountInfo, gameSession.getId(), gameSession.getGameId());
            }
        }
    }

    private boolean isCrashPVPGame(long gameId) {
        return gameId == CRASH_PVP_ID;
    }

    private long getGameId(GameSession gameSession, long gameSessionId) {
        long gameId = gameSession == null ? -1 : gameSession.getGameId();
        if (gameSession == null) {
            GameSession archivedGameSession = gameSessionPersister.get(gameSessionId);
            if (archivedGameSession != null) {
                gameId = archivedGameSession.getGameId();
            }
        }
        return gameId;
    }

    private void persistRoundParticipants(Set<AddWinRequestDto> addWinRequest) {
        try {
            Set<Long> accountIds = addWinRequest.stream()
                    .map(AddWinRequestDto::getAccountId)
                    .collect(Collectors.toSet());
            List<BattlegroundRoundParticipant> roundParticipants = addWinRequest.stream()
                    .filter(round -> round.getRoundInfo() != null &&
                            round.getRoundInfo().getBattlegroundRoundInfo() != null)
                    .map(round -> convert(round, accountIds))
                    .collect(Collectors.toList());
            battlegroundHistoryPersister.addParticipantsWithBatch(roundParticipants, accountIds);
        } catch (Exception e) {
            LOG.error("persistRoundParticipants in batch error", e);
        }
    }

    private BattlegroundRoundParticipant convert(AddWinRequestDto request, Set<Long> accountIds) {
        if(request.getSessionId() == null) {
            LOG.error("MQServiceHandler.convert sessionId is null");
        }
        if(request.getRoundInfo() == null || request.getRoundInfo().getBattlegroundRoundInfo() == null || request.getRoundInfo().getBattlegroundRoundInfo().getPrivateRoomId() == null)
        {
            LOG.error("MQServiceHandler.convert roundInfo = {}", request.getRoundInfo() );
            LOG.error("MQServiceHandler.convert battlegroundRoundInfo = {}", request.getRoundInfo().getBattlegroundRoundInfo());
            LOG.error("MQServiceHandler.convert privateRoomId = {}", request.getRoundInfo().getBattlegroundRoundInfo().getPrivateRoomId());
        }

        LOG.debug("MQServiceHandler.convert roundId ={}, roundStatTime ={}, roundTime ={}, privateRoomId ={}, gameSessionId ={}",request.getRoundInfo().getBattlegroundRoundInfo().getRoundId(),
                request.getRoundInfo().getRoundStartTime(),
                request.getRoundInfo().getTime(), request.getRoundInfo().getBattlegroundRoundInfo().getPrivateRoomId(), request.getGameSessionId());
        return new BattlegroundRoundParticipant(request.getSessionId(), request.getRoundInfo().getBattlegroundRoundInfo().getRoundId(), request.getGameSessionId(),
                accountIds, request.getRoundInfo().getRoundStartTime(), request.getRoundInfo().getTime(), request.getRoundInfo().getBattlegroundRoundInfo().getPrivateRoomId());
    }

    private boolean isOnlineMode(long gameSessionId, SessionInfo sessionInfo,
                                 GameSession gameSession) {
        return sessionInfo != null && gameSession != null && Objects.equals(gameSession.getId(), gameSessionId);
    }

    private void storeLeaderboardContributions(long gameSessionId, long roundId, Map<Long, Double> contributions)
            throws com.dgphoenix.casino.common.exception.CommonException {
        LOG.info("Storing leaderboard contributions for gameSessionId={}, roundId={}: {}", gameSessionId,
                roundId, contributions);
        GameSessionExtendedProperties properties = extendedPropertiesPersister.get(gameSessionId);
        properties.addLeaderboardContributions(roundId, contributions);
        extendedPropertiesPersister.persist(gameSessionId, properties);
    }

    private void persistBattlegroundHistory(RoundInfoResultDto roundInfoResult, long gameId, long roomId,
                                            long gameSessionId) {
        String gameName = BaseGameInfoTemplateCache.getInstance().getGameNameById(gameId);
        BattlegroundRoundInfoDto battlegroundRoundInfo = roundInfoResult.getBattlegroundRoundInfo();
        int playersNumber = battlegroundRoundInfo.getPlayersNumber();
        List<PlaceDto> places = battlegroundRoundInfo.getPlaces();
        places.stream()
                .filter(place -> place.getAccountId() == roundInfoResult.getAccountId())
                .findFirst()
                .ifPresent(place -> {
                    Long winnerMoney = getWinnerMoney(battlegroundRoundInfo.getStatus(), places);
                    BattlegroundRound battlegroundRound = new BattlegroundRound((int) gameId, gameName,
                            battlegroundRoundInfo.getBuyIn(), battlegroundRoundInfo.getRoundId(), roomId,
                            battlegroundRoundInfo.getRoundStartDate(), battlegroundRoundInfo.getStatus(),
                            place.getGameScore(), playersNumber, place.getRank(),
                            battlegroundRoundInfo.getWinnerName(), winnerMoney, gameSessionId);
                    battlegroundHistoryPersister.update(place.getAccountId(), battlegroundRound);
                });
    }

    private Long getWinnerMoney(String battlegroundStatus, List<PlaceDto> places) {
        if (!"CANCELLED".equalsIgnoreCase(battlegroundStatus)) {
            return places.stream()
                    .filter(place -> place.getRank() == 1)
                    .findFirst()
                    .map(PlaceDto::getWin)
                    .orElse(0L);
        }
        return 0L;
    }

    private void persistRoundKPIInfo(RoundInfoResultDto roundInfoResult, long gameSessionId) {
        String data = roundInfoResult.getArchiveData();
        String paramName = "kpiInfo=";
        if (data != null && data.contains(paramName)) {
            RoundKPIInfo roundKPIInfo = null;
            try {
                int beginIndex = data.indexOf(paramName);
                int endIndex = data.indexOf('=', beginIndex + paramName.length()); //kpiInfo is base64 encoded
                if (endIndex == -1) {
                    endIndex = data.indexOf(';', beginIndex + paramName.length());
                }
                if (endIndex == -1) {
                    endIndex = data.length();
                }
                String kpiData = data.substring(beginIndex + paramName.length(), endIndex);
                roundKPIInfo = KryoHelper.deserializeFrom(Base64.getDecoder().decode(kpiData), RoundKPIInfo.class);
                LOG.debug("persistRoundKPIInfo: kpiData={}, roundKPIInfo={}", kpiData, roundKPIInfo);
            } catch (Exception e) {
                LOG.error("Can't parse kpiData, data={}", data, e);
            }
            if (roundKPIInfo != null && roundKPIInfo.getRoundId() > 0) {
                roundKPIInfoPersister.persist(gameSessionId, roundKPIInfo);
            }
        } else {
            LOG.warn("kpiInfo not found in roundInfoResult.getArchiveData, gameSessionId={}", gameSessionId);
        }
    }

    private boolean savePlayerBetForOnline(SessionInfo sessionInfo, RoundInfoResultDto roundInfoResult,
                                           GameSession gameSession, BankInfo bankInfo, IDBLink dbLink,
                                           long roundId, long balanceLong) {
        double bet = roundInfoResult.getBet();
        double payout = roundInfoResult.getPayout();
        if (roundInfoResult.getBet() == 0 && roundInfoResult.getPayout() == 0
                && roundInfoResult.getArchiveData() != null
                && roundInfoResult.getArchiveData().contains("playerRoundId=0")) {
            LOG.debug("savePlayerBetForOnline, roundInfoResult has empty roundId, not save:");
            return false;
        }
        boolean sendVbaToExternalSystem = bankInfo.isSendVbaToExternalSystem();
        long betLong = Math.round(bet * 100);
        long winLong = Math.round(payout * 100);
        PlayerBet playerBet = new PlayerBet(gameSession.getLastPlayerBetId() + 1,
                gameSession.getRoundsCount() + 1, ArchiveBetTools.GS_ENDROUND, roundInfoResult.getArchiveData(), "",
                betLong, winLong, balanceLong, null, System.currentTimeMillis());
        LOG.debug("online,  savePlayerBet: {}", playerBet);
        PlayerBet currentBet = betPersistenceManager.getCurrentBet(gameSession);
        boolean isDuplicate = currentBet != null && currentBet.getData().contains(String.valueOf(roundId));
        boolean isDuplicateFrb = currentBet != null && currentBet.getData().equals(roundInfoResult.getArchiveData());
        LOG.debug("online, isDuplicate: {}, isDuplicateFrb: {}", isDuplicate, isDuplicateFrb);
        boolean addBet = !isDuplicate || (dbLink.isFRBGame() && !isDuplicateFrb);
        if (addBet) {
            betPersistenceManager.persist(gameSession, playerBet,
                    true, sendVbaToExternalSystem, dbLink.getRoundId(), dbLink.isSaveGameSidByRound(),
                    dbLink.isSaveShortBetInfo());
        }
        return addBet;
    }

    //return balance
    private long processOnlineWin(SessionInfo sessionInfo, AccountInfo account, GameSession gameSession, IDBLink dbLink,
                                  long winAmount, long returnedBet, long roundId, long mpRoundId, long roomId, boolean roundFinished,
                                  boolean walletBank, RoundInfoResultDto roundInfoResult)
            throws Exception {
        if (account.isGuest() || !gameSession.isRealMoney()) {
            account.setFreeBalance(account.getFreeBalance() + winAmount + returnedBet);
            gameSession.update(Math.round(roundInfoResult.getBet() * 100), Math.round(roundInfoResult.getPayout() * 100), 0, 0,
                    null, null, null);
            return account.getFreeBalance();
        } else {
            if (walletBank) {
                makeWalletWin(sessionInfo, gameSession, dbLink, account, winAmount, returnedBet, roundId, mpRoundId, roomId,
                        roundFinished, roundInfoResult);
            } else {
                ITransactionData transactionData = SessionHelper.getInstance().getTransactionData();
                LOG.info("Deleting online CT lastHand={}", transactionData.getLasthand());
                transactionData.setLasthand(null);
                String lasthandFromCassandra = lasthandPersister.get(account.getId(), gameSession.getGameId(),
                        null, null);
                LOG.info("Deleting CT offline lasthandFromCassandra={}", lasthandFromCassandra);
                lasthandPersister.delete(account.getId(), gameSession.getGameId(), null, null);
                SessionHelper.getInstance().getDomainSession().persistPlayerBet();
                makeWin(sessionInfo, gameSession, dbLink, winAmount, returnedBet, roundFinished, roundInfoResult, null);
                account.incrementBalance(winAmount + returnedBet, true);
                String extTransactionId = getExternalTransactionIdForMpGame(account.getId(), roomId, roundId);
                ExternalPaymentTransaction transaction = new ExternalPaymentTransaction(extTransactionId,
                        account.getExternalId(), account.getId(), account.getBankId(), winAmount + returnedBet,
                        gameSession.getId(), gameSession.getGameId(), System.currentTimeMillis(), null,
                        PaymentMode.COMMON_TRANSFER, null, TransactionStatus.APPROVED, TransactionType.WITHDRAWAL,
                        "", roundId, "", roundFinished);
                externalTransactionPersister.persist(transaction);
            }
        }
        return account.getBalance();
    }

    private void makeWalletWin(SessionInfo sessionInfo, GameSession gameSession, IDBLink dbLink, AccountInfo account,
                               long winAmount, long returnedBet, long roundId, long mpRoundId, long roomId, boolean roundFinished,
                               RoundInfoResultDto roundInfoResult)
            throws Exception {
        sessionInfo.updateActivity();
        ITransactionData transactionData = SessionHelper.getInstance().getTransactionData();
        IWallet wallet = transactionData.getWallet();
        checkPendingOperation(wallet, gameSession.getGameId());
        MultiplayerExternalWallettransactionHandler handler;
        boolean needSendWin = true;
        try {
            WalletProtocolFactory.getInstance().interceptCreateWallet(account, account.getBankId(), gameSession.getId(),
                    (int) gameSession.getGameId(), com.dgphoenix.casino.common.cache.data.game.GameMode.REAL,
                    gameSession.getClientType());
            dbLink.updateLastActivity();
            String extTransactionId = getExternalTransactionIdForMpGame(account.getId(), roomId, roundId);
            ExternalPaymentTransaction transaction = new ExternalPaymentTransaction(extTransactionId,
                    account.getExternalId(), account.getId(), account.getBankId(), winAmount + returnedBet,
                    gameSession.getId(), gameSession.getGameId(), System.currentTimeMillis(), null, PaymentMode.WALLET,
                    null, TransactionStatus.STARTED, TransactionType.WITHDRAWAL,
                    "", roundId, "", roundFinished);
            externalTransactionPersister.persist(transaction);
            handler = new MultiplayerExternalWallettransactionHandler(transaction.getBankId(), transaction.getExtId());
            //in common case (bet form BaseServlet increaseWinAmount called from processRealDebitCompleted)
            wallet.increaseWinAmount((int) gameSession.getGameId(), winAmount + returnedBet);
            dbLink.setWinAmount(0L);
            LOG.info("makeWalletWin: deleting online lastHand={}", transactionData.getLasthand());
            transactionData.setLasthand(null);
            lasthandPersister.delete(account.getId(), gameSession.getGameId(), null, null);
            CommonGameWallet gameWallet = wallet.getGameWallet((int) gameSession.getGameId());
            if (winAmount + returnedBet == 0) {
                needSendWin = gameWallet.getBetAmount() > 0;
            }
            CommonWalletOperation operation = null;
            if (needSendWin) {
                WalletProtocolFactory.getInstance().interceptCredit(account.getId(), dbLink, roundFinished,
                        sessionInfo, handler);
                operation = gameWallet.getWinOperation();
                if (operation != null) {
                    operation.setRealBet(Math.round(roundInfoResult.getBet() * 100));
                    operation.setRealWin(Math.round(roundInfoResult.getPayout() * 100));
                    operation.setSwBet(fetchTotalBetsSpecialWeapons(roundInfoResult.getArchiveData()));
                    operation.setSwCompensatedWin(fetchWeaponSurplusMoney(roundInfoResult.getArchiveData()));
                }
            } else {
                LOG.debug("makeWalletWin: skip interceptCredit, bet and win is zero, remove gameWallet");
                wallet.removeGameWallet((int) gameSession.getGameId());
                transaction.setStatus(TransactionStatus.APPROVED);
                transaction.setFinishDate(System.currentTimeMillis());
                externalTransactionPersister.persist(transaction);
            }
            makeWin(sessionInfo, gameSession, dbLink, winAmount, returnedBet, roundFinished, roundInfoResult,
                    operation != null ? operation.getId() : null);
            dbLink.incrementBalance(0, winAmount + returnedBet);
            gameSession.setLastPaymentOperationId(transaction.getInternalOperationId());
            SessionHelper.getInstance().commitTransaction();
        } catch (Exception e) {
            LOG.error("processing error: credit failed sessionInfo={}, gameSession={}", sessionInfo, gameSession, e);
            WalletTracker.getInstance().addTask(account.getId());
            throw new KafkaHandlerException(-1, "Credit failed");
        }
        if (needSendWin) {
            try {
                WalletProtocolFactory.getInstance().interceptCreditCompleted(account.getId(), dbLink, true, handler, mpRoundId);
            } catch (WalletException e) {
                LOG.error("processing error: interceptCreditCompleted failed, sessionInfo={}, gameSession={}",
                        sessionInfo, gameSession, e);
                WalletTracker.getInstance().addTask(account.getId());
                throw new NotCriticalWalletException("Credit completed failed");
            }
        }
    }

    private void checkPendingOperation(IWallet wallet, long gameId) {
        IWalletOperation pendingOperation = wallet == null ? null : wallet.getCurrentWalletOperation((int) gameId);
        if (pendingOperation != null) {
            LOG.error("processing error, previous operation is not completed, operation={}, gameId={}",
                    pendingOperation, gameId);
            throw new KafkaHandlerException(-1, "Previous operation not completed, operationId=" + pendingOperation.getExternalTransactionId());
        }
    }

    private void makeWin(SessionInfo sessionInfo, GameSession gameSession, IDBLink dbLink, long winAmount,
                         long returnedBet, boolean roundFinished, RoundInfoResultDto roundInfoResult, Long operationId)
            throws com.dgphoenix.casino.common.exception.CommonException {
        gameSession.update(Math.round(roundInfoResult.getBet() * 100), Math.round(roundInfoResult.getPayout() * 100), 0, 0,
                null, null, operationId);
        dbLink.interceptBet(0, winAmount + returnedBet);
        dbLink.setRoundId(null);
        if (roundFinished) {
            dbLink.setRoundFinished();
        }
        if (gameSession.getIncome() < 0) {
            LOG.warn("processOfflineWin: Found negative income, transfer to win. gameSession={}", gameSession);
            gameSession.addWin(Math.abs(gameSession.getIncome()), 0);
            gameSession.setIncome(0);
        }
        gameSession.setCreateNewBet(true, false);
        dbLink.updateCurrentBetWin(0, winAmount + returnedBet);
    }

    protected long fetchTotalBetsSpecialWeapons(String data) {
        return getMoneyFromData(data, "totalBetsSpecialWeapons");
    }

    protected long fetchWeaponSurplusMoney(String data) {
        return getMoneyFromData(data, "weaponSurplusMoney");
    }

    protected long getMoneyFromData(String data, String paramName) {
        long totalBetsSpecialWeapons = 0L;
        String swString = paramName + "=";
        if (data != null && data.contains(swString)) {
            int beginIndex = data.indexOf(swString);
            int endIndex = data.indexOf('.', beginIndex);
            String specialWeapons = data.substring(beginIndex + swString.length(), endIndex);
            try {
                totalBetsSpecialWeapons = Long.parseLong(specialWeapons);
            } catch (NumberFormatException e) {
                LOG.warn("Can't parse '{}' - return 0, data={}", paramName, data, e);
            }
        }
        return totalBetsSpecialWeapons;
    }

    private void processPromoCampaign(BankInfo bankInfo, SessionInfo sessionInfo, GameSession gameSession,
                                      AccountInfo account, RoundInfoResultDto roundInfoResult, boolean onlineMode) {
        try {
            if (gameSession != null && gameSession.isRealMoney() && !gameSession.isBonusGameSession() && !gameSession.isFRBonusGameSession()) {
                List<Long> campaignIds = gameSession.getPromoCampaignIds();
                if (campaignIds == null || campaignIds.isEmpty()) {
                    return;
                }
                Map<Long, Set<SignificantEventType>> promoEventsMap = null;
                MQServiceHandler.ParsedRoundInfoResult parsedResult = getParsedRoundInfoResult(roundInfoResult);
                long totalBetsSpecialWeapons = parsedResult.getTotalBetsSpecialWeapons();
                long realBet = Math.round(roundInfoResult.getBet() * 100);
                long realWin = Math.round(roundInfoResult.getPayout() * 100);
                LOG.debug("processPromoCampaign: campaignIds={}, parsedResult={}, " +
                                "realBet={}, realWin={}",
                        campaignIds, parsedResult, realBet, realWin);
                Set<PromoCampaignMember> processedMembers = new HashSet<>();
                IPromoCampaign mqEndRoundTournament = null;
                for (Long campaignId : campaignIds) {
                    IPromoCampaign campaign = campaignManager.getPromoCampaign(campaignId);
                    if (campaign != null) {
                        @SuppressWarnings("unchecked")
                        Set<SignificantEventType> significantEvents = campaign.getTemplate().getSignificantEvents();
                        LOG.debug("processPromoCampaign: campaign={},", campaign);
                        if (significantEvents != null && !significantEvents.isEmpty()) {
                            if (promoEventsMap == null) {
                                promoEventsMap = new HashMap<>();
                            }
                            promoEventsMap.put(campaignId, significantEvents);
                            if (mqEndRoundTournament == null &&
                                    significantEvents.contains(SignificantEventType.MQ_END_ROUND)) {
                                mqEndRoundTournament = campaign;
                            }
                            PromoCampaignMember member = SessionHelper.getInstance().getTransactionData().getPromoMember(campaign.getId());
                            if (member == null) {
                                LOG.debug("processPromoCampaign: member not found in transaction data, this is normal " +
                                        "for offline process");
                                campaignManager.registerPlayerInPromos(Collections.singleton(campaignId), account,
                                        gameSession.getId(), gameSession.getGameId());
                                member = SessionHelper.getInstance().getTransactionData().
                                        getPromoMember(campaign.getId());
                            }
                            if (member != null) {
                                processedMembers.add(member);
                            }
                            PlayerIdentificationType identificationType = campaign.getPlayerIdentificationType();
                            //MQ nickName may be changed, need check and update tournament display name
                            if (member != null && identificationType == PlayerIdentificationType.MQ_NICK_NAME) {
                                String bestNickName = account.getNickName();
                                if (StringUtils.isTrimmedEmpty(bestNickName)) {
                                    MQData mqData = mqDataPersister.load(account.getId(), gameSession.getGameId());
                                    if (mqData != null) {
                                        bestNickName = mqData.getNickname();
                                    }
                                    if (StringUtils.isTrimmedEmpty(bestNickName)) {
                                        bestNickName = identificationType.getName(account);
                                    }
                                }
                                if (!member.getDisplayName().equals(bestNickName)) {
                                    LOG.debug("Change promoCampaignMember display name from '{}' to '{}'",
                                            member.getDisplayName(), bestNickName);
                                    member.setDisplayName(bestNickName);
                                    campaignManager.savePromoCampaignMember(member);
                                }
                            }
                        }
                    } else {
                        LOG.warn("processPromoCampaign: campaign not found, id={}", campaignId);
                    }
                }
                if (promoEventsMap != null) {
                    ParticipantEventProcessor participantEventProcessor = ApplicationContextHelper.getBean(ParticipantEventProcessor.class);
                    CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getBean(CassandraPersistenceManager.class);
                    PromoGameEventProcessor promoGameEventProcessor = new PromoGameEventProcessor(null, campaignManager,
                            participantEventProcessor, promoEventsMap, persistenceManager);
                    String currencyCode = account.getCurrency().getCode();
                    if (realBet - totalBetsSpecialWeapons > 0) {
                        PlayerBetEvent betEvent = new PlayerBetEvent(gameSession.getGameId(), System.currentTimeMillis(),
                                account.getId(), account.getExternalId(), realBet - totalBetsSpecialWeapons,
                                currencyCode);
                        LOG.debug("processPromoCampaign: betEvent={}", betEvent);
                        try {
                            promoGameEventProcessor.process(betEvent, null);
                        } catch (Exception e) {
                            LOG.error("processPromoCampaign: process BetEvent error", e);
                        }
                    }
                    if (mqEndRoundTournament != null && realBet > 0) {
                        TournamentPromoTemplate<?> template = (TournamentPromoTemplate<?>) mqEndRoundTournament.getTemplate();
                        TournamentPrize prize = template.getPrizePool().iterator().next();
                        IParticipantEventQualifier eventQualifier = prize.getEventQualifier();
                        double averageBet = 1;
                        long minBet = 1;
                        long maxBet = 2;
                        //this is just optimization, not required avgBet for not MaxPerf tournaments
                        if (eventQualifier instanceof MaxPerformanceEventQualifier) {
                            MaxPerformanceEventQualifier maxQualifier = (MaxPerformanceEventQualifier) eventQualifier;
                            long betAmountForIncrement = maxQualifier.isNotCountWeaponBoxPurchases() ?
                                    realBet - totalBetsSpecialWeapons : realBet;
                            double minBetAmountInCurrentCurrency = currencyRateManager.convert(
                                    maxQualifier.getMinBetAmount(), mqEndRoundTournament.getBaseCurrency(),
                                    currencyCode);
                            LOG.debug("processPromoCampaign: minBetAmountInCurrentCurrency={}, betAmountForIncrement={}",
                                    minBetAmountInCurrentCurrency, betAmountForIncrement);
                            if (betAmountForIncrement >= minBetAmountInCurrentCurrency) {
                                campaignManager.incrementAverageBet(mqEndRoundTournament.getId(), 1,
                                        betAmountForIncrement);
                            }
                            averageBet = campaignManager.getAverageBet(mqEndRoundTournament.getId());
                            List<Long> coins = getCoins(bankInfo, gameSession.getGameId(),
                                    CurrencyCache.getInstance().get(currencyCode));
                            minBet = Collections.min(coins);
                            maxBet = Collections.max(coins);
                        }
                        double highestWinPerSingleBet = parsedResult.getMaxShotTotalWin() <= 0 ||
                                parsedResult.getRoomStake() <= 0 ? 0 :
                                ((double) parsedResult.getMaxShotTotalWin()) / (parsedResult.getRoomStake() * 100);
                        PromoCampaignMember member = SessionHelper.getInstance().getTransactionData().getPromoMember(mqEndRoundTournament.getId());
                        MqEndRoundEvent mqEndRoundEvent = new MqEndRoundEvent(gameSession.getGameId(),
                                System.currentTimeMillis(), account.getId(), account.getExternalId(), realBet, realWin,
                                totalBetsSpecialWeapons, currencyCode, averageBet, minBet, maxBet,
                                PromoCampaignManager.MAX_EXPLOSURE, highestWinPerSingleBet,
                                member == null ? 0 : member.getTotalBetSum(), member == null ? 0 : member.getTotalWinSum());
                        promoGameEventProcessor.process(mqEndRoundEvent, null);
                    }
                    if (!onlineMode) {
                        for (PromoCampaignMember member : processedMembers) {
                            LOG.debug("processPromoCampaign: save member for online: {}", member);
                            campaignManager.savePromoCampaignMember(member);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("processPromoCampaign error. Loss game event", e);
        }
    }

    class ParsedRoundInfoResult {
        private long totalBetsSpecialWeapons;
        private long maxShotTotalWin;
        private long roomStake;

        public ParsedRoundInfoResult() {
        }

        public long getTotalBetsSpecialWeapons() {
            return totalBetsSpecialWeapons;
        }

        public void setTotalBetsSpecialWeapons(long totalBetsSpecialWeapons) {
            this.totalBetsSpecialWeapons = totalBetsSpecialWeapons;
        }

        public long getMaxShotTotalWin() {
            return maxShotTotalWin;
        }

        public void setMaxShotTotalWin(long maxShotTotalWin) {
            this.maxShotTotalWin = maxShotTotalWin;
        }

        public long getRoomStake() {
            return roomStake;
        }

        public void setRoomStake(long roomStake) {
            this.roomStake = roomStake;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("ParsedRoundInfoResult [");
            sb.append("totalBetsSpecialWeapons=").append(totalBetsSpecialWeapons);
            sb.append(", maxShotTotalWin=").append(maxShotTotalWin);
            sb.append(", roomStake=").append(roomStake);
            sb.append(']');
            return sb.toString();
        }
    }

    private MQServiceHandler.ParsedRoundInfoResult getParsedRoundInfoResult(RoundInfoResultDto roundInfoResult) {
        MQServiceHandler.ParsedRoundInfoResult result = new MQServiceHandler.ParsedRoundInfoResult();
        if (roundInfoResult == null || StringUtils.isTrimmedEmpty(roundInfoResult.getArchiveData())) {
            return result;
        }
        StringTokenizer st = new StringTokenizer(roundInfoResult.getArchiveData(), ";");
        while (st.hasMoreTokens()) {
            String pair = st.nextToken();
            if (!isTrimmedEmpty(pair)) {
                StringTokenizer st2 = new StringTokenizer(pair, "=");
                int tokensCount = st2.countTokens();
                if (tokensCount == 2) {
                    String key = st2.nextToken();
                    if ("totalBetsSpecialWeapons".equalsIgnoreCase(key)) {
                        String value = st2.nextToken();
                        try {
                            double amount = Double.parseDouble(value);
                            result.setTotalBetsSpecialWeapons(Math.round(amount));
                        } catch (NumberFormatException e) {
                            LOG.error("Cannot parse totalBetsSpecialWeapons: {}", value);
                        }
                    }
                    if ("maxShotTotalWin".equalsIgnoreCase(key)) {
                        String value = st2.nextToken();
                        try {
                            long amount = Long.parseLong(value);
                            result.setMaxShotTotalWin(amount);
                        } catch (NumberFormatException e) {
                            LOG.error("Cannot parse maxShotTotalWin: {}", value);
                        }
                    }
                    if ("roomStake".equalsIgnoreCase(key)) {
                        String value = st2.nextToken();
                        try {
                            long amount = Long.parseLong(value);
                            result.setRoomStake(amount);
                        } catch (NumberFormatException e) {
                            LOG.error("Cannot parse roomStake: {}", value);
                        }
                    }
                }
            }
        }
        return result;
    }

    private long processOfflineWin(long gameSessionId, long winAmount, long returnedBet, long roundId,
                                   long roomId, boolean roundFinished, AccountInfo account,
                                   RoundInfoResultDto roundInfoResult, BankInfo bankInfo) throws Exception {
        ITransactionData transactionData = SessionHelper.getInstance().getTransactionData();
        if (account.isGuest()) {
            return account.getFreeBalance() + winAmount + returnedBet;
        }
        GameSession gameSession = GameSessionManager.getInstance().getGameSessionById(gameSessionId);
        if (gameSession == null) {
            LOG.error("processOfflineWin: error, gameSession not found in DB, gameSessionId={}", gameSessionId);
            if (transactionData.getGameSession() != null && transactionData.getGameSession().getId() == gameSessionId) {
                LOG.warn("processOfflineWin: found GameSession in transactionDate, online mode, please fix. TD={}",
                        transactionData);
                gameSession = transactionData.getGameSession();
            } else {
                //this is free session
                return account.getFreeBalance() + winAmount + returnedBet;
            }
        }
        if (!gameSession.isRealMoney()) {
            LOG.debug("processOfflineWin: not real session, return. gameSession={}", gameSession);
            return account.getFreeBalance() + winAmount + returnedBet;
        }
        int gameId = (int) gameSession.getGameId();
        CommonWallet wallet = (CommonWallet) transactionData.getWallet();
        if (wallet == null) {
            wallet = (CommonWallet) WalletProtocolFactory.getInstance().interceptCreateWallet(account,
                    account.getBankId(), gameSession.getId(), gameId,
                    com.dgphoenix.casino.common.cache.data.game.GameMode.REAL, gameSession.getClientType());
        }
        CommonWalletOperation operation = wallet.getCurrentWalletOperation(gameId);
        if (operation != null) {
            LOG.error("processOfflineWin: processing error, previous operation is not completed, operation={}, gameSession={}",
                    operation, gameSession);
            throw new Exception("previous operation is not completed");
        }
        String extTransactionId = getExternalTransactionIdForMpGame(account.getId(), roomId, roundId);
        ExternalPaymentTransaction transaction = new ExternalPaymentTransaction(extTransactionId,
                account.getExternalId(), account.getId(), account.getBankId(), winAmount + returnedBet, gameSessionId,
                gameSession.getGameId(), System.currentTimeMillis(), null, PaymentMode.WALLET, null,
                TransactionStatus.STARTED, TransactionType.WITHDRAWAL,
                "", roundId, "", roundFinished);
        externalTransactionPersister.persist(transaction);
        MultiplayerExternalWallettransactionHandler handler = new MultiplayerExternalWallettransactionHandler(
                transaction.getBankId(),
                transaction.getExtId());
        wallet.setGameWalletRoundFinished(gameId, roundFinished);
        Long sbRoundId = wallet.getGameWalletRoundId(gameId);
        if (sbRoundId == null) {
            sbRoundId = IdGenerator.getInstance().getNext(IWallet.class);
            wallet.setGameWalletRoundId(gameId, sbRoundId);
        }
        CommonGameWallet gameWallet = wallet.getGameWallet(gameId);
        //for MQ && CW2 refunded bet stored in winAmount. see MQPRT-55
        long walletWinAmount = gameWallet.getWinAmount();
        long betLong = Math.round(roundInfoResult.getBet() * 100);
        long winLong = Math.round(roundInfoResult.getPayout() * 100);
        long totalOperationWinAmount = walletWinAmount + winAmount + returnedBet;
        LOG.debug("processOfflineWin wallet, accountId={}, winAmount={}, walletWinAmount={}, roundId={}, betLong={}, " +
                        "winLong={}, totalOperationWinAmount={}, gameSession={}, before processing balance={}",
                account.getId(), winAmount, walletWinAmount, roundId, betLong, winLong, totalOperationWinAmount,
                gameSession, account.getBalance());
        IWalletProtocolManager protocolManager = WalletProtocolFactory.getInstance().getWalletProtocolManager(
                account.getBankId());
        boolean needSendWin = (totalOperationWinAmount > 0 || gameWallet.getBetAmount() > 0) &&
                protocolManager.getClient().isCreditCondition(totalOperationWinAmount, 0, true,
                        WalletProtocolFactory.EMPTY_WALLET_DB_LINK);
        if (needSendWin) {
            operation = wallet.createCommonWalletOperation(IdGenerator.getInstance().getNext(CommonWalletOperation.class),
                    account.getId(), gameSessionId, roundId, totalOperationWinAmount,
                    WalletOperationType.CREDIT, "MQ send offline win", WalletOperationStatus.STARTED,
                    WalletOperationStatus.STARTED, gameId, 0, gameSession.getExternalSessionId());
            operation.setRealBet(betLong + walletWinAmount);
            operation.setRealWin(winLong);
            operation.setSwBet(fetchTotalBetsSpecialWeapons(roundInfoResult.getArchiveData()));
            operation.setSwCompensatedWin(fetchWeaponSurplusMoney(roundInfoResult.getArchiveData()));
            handler.operationCreated(operation);
        }
        boolean correctGameId = transactionData.getLasthand() != null && transactionData.getLasthand().getId() == gameId;
        LOG.info("Deleting offline gameId = {} correctGameId: {}", gameId, correctGameId);
        if (correctGameId) {
            transactionData.setLasthand(null);
            LOG.info("Deleting offline lastHand={}", transactionData.getLasthand());
        }
        String lasthandFromCassandra = lasthandPersister.get(account.getId(), gameId, null, null);
        LOG.info("Deleting offline lasthandFromCassandra={}", lasthandFromCassandra);
        lasthandPersister.delete(account.getId(), gameId, null, null);
        SessionHelper.getInstance().commitTransaction();
        Exception creditFailedException = null;
        if (needSendWin) {
            try {
                protocolManager.credit(account, gameId, winAmount + walletWinAmount, roundFinished, wallet, operation, true, roundId);
                protocolManager.completeOperation(account, gameId, WalletOperationStatus.COMPLETED,
                        gameWallet, operation, handler);
            } catch (Exception e) {
                LOG.warn("processOfflineWin: failed wallet credit. Add to tracking. Exception saved and " +
                        "throw later. after send GameSessionUpdatedAlert", e);
                WalletTracker.getInstance().addTask(account.getId());
                creditFailedException = e;
            }
        }
        if (creditFailedException == null) {
            if (roundFinished) {
                wallet.updateGameWallet(gameId, 0L, 0L, null);
            } else {
                wallet.updateGameWallet(gameId, 0L, 0L);
            }
        }
        savePlayerBetForOffline(sbRoundId, roundInfoResult, gameSession, bankInfo, account, wallet.getServerBalance());
        if (needSendWin) {
            gameSession.update(betLong + walletWinAmount, winLong, 0, 0, null, null, operation.getId());
        } else {
            if (betLong + walletWinAmount > 0) {
                LOG.debug("processOfflineWin: found income change and no credit operation, just update gameSession");
                gameSession.update(betLong + walletWinAmount, 0, 0, 0, null, null,
                        gameSession.getLastPaymentOperationId());
            } else {
                LOG.debug("processOfflineWin: skip send credit, bet and win is zero, remove game wallet");
            }
            wallet.removeGameWallet(gameId);
        }
        gameSession.incrementRoundsCount(1);
        if (gameSession.getIncome() < 0) {
            LOG.warn("processOfflineWin: Found negative income, transfer to win. gameSession={}", gameSession);
            gameSession.addWin(Math.abs(gameSession.getIncome()), 0);
            gameSession.setIncome(0);
        }
        account.setBalance(wallet.getServerBalance());
        gameSessionPersister.persist(gameSession);
        if (creditFailedException != null) {
            throw creditFailedException;
        }
        return account.getBalance();
    }

    private String parseRoundId(PlayerBet bet) {
        String servletData = bet.getServletData();
        String data = bet.getData();
        if (!StringUtils.isTrimmedEmpty(servletData) && servletData.contains("ROUND_ID=")) {
            String source = "ROUND_ID=";
            int index = servletData.indexOf(source);
            return servletData.substring(index + source.length());
        } else if (!StringUtils.isTrimmedEmpty(data) && data.contains("playerRoundId=")) {
            String source = "playerRoundId=";
            int index = data.indexOf(source);
            return data.substring(index + source.length(), data.indexOf(';', index));
        }
        return null;
    }

    private boolean savePlayerBetForOffline(long roundId, RoundInfoResultDto roundInfoResult, GameSession gameSession,
                                            BankInfo bankInfo, AccountInfo account, long currentBalance) {
        double bet = roundInfoResult.getBet();
        double payout = roundInfoResult.getPayout();
        //IDBLink dbLink = getDBLink(sessionInfo, gameSession);
        if (roundInfoResult.getBet() == 0 && roundInfoResult.getPayout() == 0
                && roundInfoResult.getArchiveData() != null
                && roundInfoResult.getArchiveData().contains("playerRoundId=0")) {
            LOG.debug("savePlayerBetForOffline,  roundInfoResult has empty roundId, not save:");
            return false;
        }
        long balance = currentBalance;
        boolean sendVbaToExternalSystem = bankInfo.isSendVbaToExternalSystem();
        long betLong = Math.round(bet * 100);
        long winLong = Math.round(payout * 100);
        if (gameSession != null) {
            long id = gameSession.getLastPlayerBetId() + 1;
            long roundsCount = gameSession.getRoundsCount() + 1;
            PlayerBet playerBet = new PlayerBet(id, roundsCount, ArchiveBetTools.GS_ENDROUND, roundInfoResult.getArchiveData(), "",
                    betLong, winLong, balance, null, System.currentTimeMillis());
            LOG.debug("offline savePlayerBet: {}", playerBet);
            boolean isDuplicate = false;
            boolean isDuplicateFrb = false;
            List<PlayerBet> playerBets = betPersistenceManager.getBets(gameSession.getId());
            List<PlayerBet> bets = new ArrayList<>();
            for (PlayerBet pBet : playerBets) {
                if (pBet.getGameStateId() != ArchiveBetTools.GS_NOTFINISHED &&
                        pBet.getGameStateId() != ArchiveBetTools.GS_ENDGAME)
                    bets.add(pBet);
                if (pBet.getData().contains(String.valueOf(roundId)))
                    isDuplicate = true;
                if (pBet.getData().equals(roundInfoResult.getArchiveData()))
                    isDuplicateFrb = true;
            }
            LOG.debug("isDuplicate: {}, isDuplicateFrb: {} roundId: {} sessionId: {}",
                    isDuplicate, isDuplicateFrb, roundId, gameSession.getId());
            boolean addBet = !isDuplicate || (gameSession.isFRBonusGameSession() && !isDuplicateFrb);
            if (addBet) {
                bets.add(playerBet);
            }
            PlayerBet endBet = new PlayerBet(addBet ? id + 1 : id, addBet ? roundsCount + 1 : roundsCount,
                    ArchiveBetTools.GS_ENDGAME, null, null, 0, 0, balance,
                    null, System.currentTimeMillis());
            bets.add(endBet);
            betPersistenceManager.persist(gameSession.getId(), bets);
            return addBet;
        }
        return false;
    }

    private Currency getCurrencyFractionCode(Currency fraction, GameSession gameSession) {
        String restoredFractionCode = gameSession.getCurrencyFraction();
        if (!StringUtils.isTrimmedEmpty(restoredFractionCode)) {
            return CurrencyCache.getInstance().get(restoredFractionCode);
        }
        return fraction;
    }

    protected Player convertTRMSPlayerToPlayer(RMSPlayerDto trmsPlayer) {

        LOG.debug("convertTRMSPlayerToPlayer: trmsPlayer={}", trmsPlayer);

        if(trmsPlayer == null) {
            LOG.error("convertTRMSPlayerToPlayer: trmsPlayer is null");
            return null;
        }

        Player player = new Player(
                trmsPlayer.getNickname(),
                trmsPlayer.isOwner(),
                null);


        String sessionId = trmsPlayer.getSessionId();

        if (!StringUtils.isTrimmedEmpty(sessionId)) {

            Pair<Integer, String> pair = StringIdGenerator.extractBankAndExternalUserId(sessionId);
            String externalId = pair.getValue();
            player.setExternalId(externalId);
        }

        return player;
    }

    protected Room convertTRMSRoomToRoom(RMSRoomDto trmsRoom) {

        LOG.debug("convertTRMSRoomToRoom: trmsRoom={}", trmsRoom);

        if(trmsRoom == null) {
            LOG.error("convertTRMSRoomToRoom: trmsRoom is null");
            return null;
        }

        Room room = new Room(
                trmsRoom.getRoomId(),
                trmsRoom.getServerId(),
                trmsRoom.isBattleground(),
                trmsRoom.isPrivate(),
                trmsRoom.getBuyInStake(),
                trmsRoom.getCurrency(),
                trmsRoom.getGameId(),
                trmsRoom.getGameName(),
                null);

        List<RMSPlayerDto> trmsPlayers = trmsRoom.getPlayers();

        if (trmsPlayers == null) {
            LOG.error("convertTRMSRoomsToRooms: trmsPlayers list is null, trmsRoom: {}", trmsRoom);
            return room;
        }

        List<Player> players = new ArrayList<>();

        for (RMSPlayerDto trmsPlayer : trmsPlayers) {
            Player player = convertTRMSPlayerToPlayer(trmsPlayer);
            if(player == null) {
                LOG.error("convertTRMSRoomsToRooms: player is null, for trmsPlayer: {}", trmsPlayer);
            } else {
                players.add(player);
            }
        }

        room.setPlayers(players);;

        LOG.debug("convertTRMSRoomsToRooms: room={}", room);

        return room;
    }

    protected List<Room> convertTRMSRoomsToRooms(List<RMSRoomDto> trmsRooms) {

        if (trmsRooms == null) {
            LOG.error("convertTRMSRoomsToRooms: trmsRooms is null");
            return null;
        }

        LOG.debug("convertTRMSRoomsToRooms: trmsRooms.size()={}", trmsRooms.size());

        List<Room> rooms = new ArrayList<>();

        for (RMSRoomDto trmsRoom : trmsRooms) {

            Room room = convertTRMSRoomToRoom(trmsRoom);
            if(room == null) {
                LOG.error("convertTRMSRoomsToRooms: room is null, for trmsRoom: {}", trmsRoom);
            } else {
                rooms.add(room);
            }
        }

        return rooms;
    }

    public boolean pushOnlineRoomsPlayers(List<RMSRoomDto> trmsRooms) {
        return pushOnlineRoomsPlayers(trmsRooms, null);
    }

    public boolean pushOnlineRoomsPlayers(List<RMSRoomDto> trmsRooms, Long bankIdAsLong) {

        LOG.debug("pushOnlineRoomsPlayers: bankIdAsLong:{}, trmsRooms:{}", bankIdAsLong, trmsRooms);

        BankInfo bankInfo = null;
        if(bankIdAsLong != null && bankIdAsLong.longValue() != 0) {
            bankInfo = BankInfoCache.getInstance().getBankInfo(bankIdAsLong);
        } else {
            for(Long bId : BankInfoCache.getInstance().getBankIds()) {
                if(bId != null && bId != 0) {
                    bankInfo = BankInfoCache.getInstance().getBankInfo(bId);
                    break;
                }
            }
        }

        if(bankInfo == null) {
            LOG.error("pushOnlineRoomsPlayers: bankInfo is null");
            return false;
        }

        if(!bankInfo.isAllowUpdatePlayersStatusInPrivateRoom()) {
            LOG.debug("pushOnlineRoomsPlayers: Update Players Status In PrivateRoom is not allowed skip " +
                    "pushOnlineRoomsPlayers, check BankInfo configuration: {}", bankInfo);
            return false;
        }

        long bankId = bankInfo.getId();
        if(bankId == 0) {
            LOG.error("pushOnlineRoomsPlayers: bankId is 0");
            return false;
        }

        List<Room> rooms = convertTRMSRoomsToRooms(trmsRooms);

        if(rooms == null || rooms.isEmpty()) {
            LOG.error("pushOnlineRoomsPlayers: rooms is empty:{}", rooms);
            return false;
        }

        try {
            ICommonWalletClient cw2Client = WalletProtocolFactory.getInstance().getClient(bankId);

            if (cw2Client == null) {
                LOG.error("pushOnlineRoomsPlayers: error to get cw2Client for bankId:{}", bankId);
                return false;
            }

            if (!(cw2Client instanceof com.dgphoenix.casino.payment.wallet.client.v4.CanexCWClient)) {
                LOG.error("pushOnlineRoomsPlayers: CWClient does not support " +
                        "pushOnlineRoomsPlayers bankId:{}", bankId);
                return false;
            }

            com.dgphoenix.casino.payment.wallet.client.v4.CanexCWClient canexCWClient =
                    (com.dgphoenix.casino.payment.wallet.client.v4.CanexCWClient) cw2Client;

            return canexCWClient.pushRoomsPlayers(rooms);

        } catch (Exception e) {
            LOG.error("pushOnlineRoomsPlayers: exception for bankId:{}, trmsRooms:{}, {}",
                    bankId, trmsRooms, e.getMessage(), e);
            return false;
        }
    }

    public void notifyPrivateRoomWasDeactivated(String privateRoomId, String reason, long bankId) {
        BankInfo bankInfo = bankInfoCache.getBankInfo(bankId);
        executorService.schedule(new DeactivatedRoomNotificationTask(privateRoomId, reason, bankInfo), 0, TimeUnit.SECONDS);
    }
}
