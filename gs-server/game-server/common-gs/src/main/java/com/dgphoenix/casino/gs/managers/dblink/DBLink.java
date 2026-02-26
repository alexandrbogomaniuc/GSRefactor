package com.dgphoenix.casino.gs.managers.dblink;

import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.cassandra.persist.CassandraPlayerGameSettingsPersister;
import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.BaseGameCache;
import com.dgphoenix.casino.common.cache.BaseGameInfoTemplateCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.account.LasthandInfo;
import com.dgphoenix.casino.common.cache.data.account.PlayerGameSettings;
import com.dgphoenix.casino.common.cache.data.bank.*;
import com.dgphoenix.casino.common.cache.data.bet.PlayerBet;
import com.dgphoenix.casino.common.cache.data.currency.Currency;
import com.dgphoenix.casino.common.cache.data.game.BaseGameConstants;
import com.dgphoenix.casino.common.cache.data.game.GameMode;
import com.dgphoenix.casino.common.cache.data.game.IBaseGameInfo;
import com.dgphoenix.casino.common.cache.data.payment.IWallet;
import com.abs.casino.common.cache.data.payment.frb.IFRBonusWin;
import com.dgphoenix.casino.common.cache.data.session.ClientType;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.dgphoenix.casino.common.config.UtilsApplicationContextHelper;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.exception.DBException;
import com.dgphoenix.casino.common.exception.GameException;
import com.dgphoenix.casino.common.games.IStartGameHelper;
import com.dgphoenix.casino.common.games.StartGameHelpers;
import com.dgphoenix.casino.common.jackpot.IJPWinQualifier;
import com.dgphoenix.casino.common.promo.IPromoCampaign;
import com.dgphoenix.casino.common.promo.IPromoCampaignManager;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.dgphoenix.casino.common.util.DigitFormatter;
import com.dgphoenix.casino.common.util.IdGenerator;
import com.dgphoenix.casino.common.util.NtpTimeProvider;
import com.dgphoenix.casino.common.util.logkit.GameLog;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import com.dgphoenix.casino.gs.ISendBetInterceptor;
import com.dgphoenix.casino.gs.LocalSessionTracker;
import com.dgphoenix.casino.gs.managers.game.settings.GameSettingsManager;
import com.dgphoenix.casino.gs.managers.payment.currency.CurrencyRatesManager;
import com.dgphoenix.casino.gs.managers.payment.wallet.CommonGameWallet;
import com.dgphoenix.casino.gs.managers.payment.wallet.IWalletOperation;
import com.dgphoenix.casino.gs.managers.payment.wallet.WalletProtocolFactory;
import com.dgphoenix.casino.gs.persistance.GameSessionPersister;
import com.dgphoenix.casino.gs.persistance.LasthandPersister;
import com.dgphoenix.casino.gs.persistance.bet.PlayerBetPersistenceManager;
import com.dgphoenix.casino.gs.singlegames.tools.cbservtools.ArchiveBetTools;
import com.dgphoenix.casino.gs.singlegames.tools.cbservtools.CBGameException;
import com.dgphoenix.casino.gs.singlegames.tools.cbservtools.IGameController;
import com.dgphoenix.casino.gs.singlegames.tools.util.LasthandHelper;
import com.dgphoenix.casino.unj.api.AbstractSharedGameState;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.context.ApplicationContext;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static org.apache.commons.lang3.StringUtils.EMPTY;

public class DBLink implements IDBLink {
    private static final org.apache.logging.log4j.Logger LOG = LogManager.getLogger(DBLink.class);
    private static final double ZERO_MONEY = toMoney(0);
    private static final AtomicLong recreateCount = new AtomicLong(0);
    private static final long UPDATE_ACTIVITY_PERIOD = 60000;
    protected final long accountId;
    protected final String nickName;
    private final long bankId;
    protected final long gameId;
    protected final String gameName;
    protected final long gameSessionId;
    private Double minBet = null;
    private Double maxBet = null;
    private double[] COINSEQ = null;
    private boolean limitsChanged = false;
    private final Map<String, String> lastHandLimits = new HashMap<>(4);
    private Long roundId;
    private long winAmount;
    private final Currency currency;
    private IBaseGameInfo<?, ?> gameInfo;
    private final double payoutPercent;
    private PlayerBet lastSavedBet;
    private Logger logger;
    private final String logPrefix;
    private volatile long lastActivity = System.currentTimeMillis();
    private long currentBet;
    private long currentWin;
    private boolean authState = false;
    private boolean usePlayerGameSettings = false;
    private PlayerGameSettings playerGameSettings;
    private final boolean walletBank;
    private ISendBetInterceptor sendBetInterceptor;
    private final boolean sendVbaToExternalSystem;
    private final boolean sendRoundId;
    private final boolean sendExternalWalletMessages;
    private Long lastPaymentOperationId;
    private boolean closeOldGameAfterRoundFinished = false;
    private TimeZone timeZone;
    private final boolean saveGameSidByRound;
    private final boolean saveShortBetInfo;
    private long maxBetInCredits;
    private Map<String, String[]> requestParameters = new HashMap<>();
    private Long lastCheckTime = null;
    private final IPromoCampaignManager promoCampaignManager;
    private final GameSettingsManager gameSettingsManager;
    private final PlayerBetPersistenceManager playerBetPersistenceManager;
    protected CurrencyRatesManager currencyConverter;
    private final CassandraPlayerGameSettingsPersister playerGameSettingsPersister;
    protected boolean logoutOnError;

    //recreate constructor
    public DBLink(long accountId, String nickName, long bankId, long gameId, String gameName, boolean isJackpotGame,
                  GameSession gameSession, Currency currency) {
        this.accountId = accountId;
        this.nickName = nickName;
        this.bankId = bankId;
        this.gameId = gameId;
        this.gameName = gameName;
        this.gameSessionId = gameSession.getId();
        this.currency = currency;
        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
        ApplicationContext applicationContext = ApplicationContextHelper.getApplicationContext();
        this.currencyConverter = applicationContext.getBean("currencyRatesManager", CurrencyRatesManager.class);
        this.gameSettingsManager = applicationContext.getBean("gameSettingsManager", GameSettingsManager.class);
        this.playerBetPersistenceManager = applicationContext.getBean("playerBetPersistenceManager", PlayerBetPersistenceManager.class);
        this.promoCampaignManager = applicationContext.getBean(IPromoCampaignManager.class);
        refreshGameSettings();
        this.payoutPercent = gameInfo.getPayoutPercent();
        this.walletBank = WalletProtocolFactory.getInstance().isWalletBank(getBankId());
        recreateCount.incrementAndGet();
        CassandraPersistenceManager persistenceManager = applicationContext.getBean("persistenceManager", CassandraPersistenceManager.class);
        playerGameSettingsPersister = persistenceManager.getPersister(CassandraPlayerGameSettingsPersister.class);
        this.sendVbaToExternalSystem = bankInfo.isSendVbaToExternalSystem();
        this.sendRoundId = bankInfo.isSendRoundId();
        this.sendExternalWalletMessages = bankInfo.isSendExternalWalletMessages();
        this.saveGameSidByRound = bankInfo.isSaveGameSidByRound();
        this.saveShortBetInfo = bankInfo.isSaveShortBetInfo() && gameSession.isRealMoney() &&
                (!gameSession.isBonusGameSession() && !gameSession.isFRBonusGameSession() || bankInfo.isSaveBonusShortBetInfo());
        initPlayerGameSettings(bankInfo.getPgsType(), (int) gameId);
        this.logPrefix = "DBLink [accountId=" + accountId + ", nickName=" + nickName + ", gameId=" + gameId
                + ", gameSessionId=" + gameSessionId + "] ";
        logoutOnError = bankInfo.isLogoutOnError();
        maxBetInCredits = -1;
        if (gameInfo.getProperty(BaseGameConstants.KEY_MAX_BET_IN_CREDITS) != null) {
            maxBetInCredits = Long.parseLong(gameInfo.getProperty(BaseGameConstants.KEY_MAX_BET_IN_CREDITS));
        }
        if (isForReal() && !isFRBGame() && !isBonusGameSession()) {
            //craps and MQ not store roundId in lasthand
            boolean mpGame = isMultiplayerGame(gameId);
            logDebug("DBLink: accountId:" + accountId + " gameId:" + gameId + " gameName:" + gameName +
                    " gameSessionId:" + gameSessionId + "mpGame:" + mpGame);
            if (mpGame) {
                try {
                    Long roundIdFromLH = getRoundIdFromLastHand();
                    Long roundId = roundIdFromLH != null ? roundIdFromLH : getRoundIdFromWallet((int) gameId);
                    logDebug("DBLink: accountId:" + accountId + " gameId:" + gameId + " gameName:" + gameName +
                            " gameSessionId:" + gameSessionId + "roundId:" + roundId);

                    setRoundId(roundId);

                } catch (Exception e) {
                    logError("DBLink: Can't set roundId accountId=" + accountId, e);
                }
            } else {
                Long roundId = getRoundIdFromLastHand();
                logDebug("DBLink: accountId:" + accountId + " gameId:" + gameId + " gameName:" + gameName +
                        " gameSessionId:" + gameSessionId + "roundId:" + roundId);

                setRoundId(roundId);
            }

            if (bankInfo.isUseJvmSessionTracking() && bankInfo.getRealModeSessionTimeout() != null) {
                LocalSessionTracker localSessionTracker = applicationContext.getBean(LocalSessionTracker.class);
                localSessionTracker.addSession(accountId, bankInfo.getRealModeSessionTimeout());
            }
        }
    }

    public DBLink(AccountInfo accountInfo, long gameId, Long gameSessionId, SessionInfo sessionInfo,
                  IBaseGameInfo<?, ?> gameInfo, String lang) throws CommonException {
        if (gameSessionId == null) {
            throw new CommonException("gameSession id is null, accountId = " + accountInfo.getId() +
                    ", gameId=" + gameId);
        }
        long now = System.currentTimeMillis();
        try {
            this.accountId = accountInfo.getId();
            this.nickName = accountInfo.getNickName();
            this.bankId = accountInfo.getBankId();
            this.currency = accountInfo.getCurrency();
            StatisticsManager.getInstance().updateRequestStatistics("DBLink 1",
                    System.currentTimeMillis() - now, accountId);
            now = System.currentTimeMillis();
            this.gameId = gameInfo.getId();
            this.gameName = gameInfo.getName();
            GameSession gameSession = GameSessionPersister.getInstance().getGameSession(gameSessionId);
            BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(accountInfo.getBankId());
            ApplicationContext applicationContext = ApplicationContextHelper.getApplicationContext();
            this.currencyConverter = applicationContext.getBean("currencyRatesManager", CurrencyRatesManager.class);
            this.gameSettingsManager = applicationContext.getBean("gameSettingsManager", GameSettingsManager.class);
            this.playerBetPersistenceManager = applicationContext.getBean("playerBetPersistenceManager", PlayerBetPersistenceManager.class);
            this.promoCampaignManager = applicationContext.getBean(IPromoCampaignManager.class);
            this.gameInfo = gameInfo;
            this.payoutPercent = gameInfo.getPayoutPercent();
            this.walletBank = WalletProtocolFactory.getInstance().isWalletBank(getBankId());
            StatisticsManager.getInstance().updateRequestStatistics("DBLink 2",
                    System.currentTimeMillis() - now, accountId);
            now = System.currentTimeMillis();
            this.sendVbaToExternalSystem = bankInfo.isSendVbaToExternalSystem();
            this.sendRoundId = bankInfo.isSendRoundId();
            this.sendExternalWalletMessages = bankInfo.isSendExternalWalletMessages();
            this.saveGameSidByRound = bankInfo.isSaveGameSidByRound();
            if (gameSession == null) {
                NtpTimeProvider ntpTimeProvider = UtilsApplicationContextHelper.getApplicationContext()
                        .getBean("timeProvider", NtpTimeProvider.class);
                gameSession = new GameSession(gameSessionId, accountId, accountInfo.getBankId(), gameId,
                        ntpTimeProvider.getTime(), 0L, 0L, 0, 0, true, isForReal(),
                        currency, sessionInfo.getExternalSessionId(), lang,
                        bankInfo.isPersistBets(), gameInfo.getProfileId());
                if (sessionInfo.getClientType() != null) {
                    gameSession.setClientType(sessionInfo.getClientType());
                }
                gameSession.setStartBalance(accountInfo.getBalance());
                checkAvailablePromos(gameSession);
                GameSessionPersister.getInstance().save(gameSession);
                StatisticsManager.getInstance().updateRequestStatistics("DBLink 3",
                        System.currentTimeMillis() - now, accountId);
            } else {
                logDebug("creating DBlink for already existed gameSession, accountId:" + accountId + " gameId:" +
                        gameId + " gameName:" + gameName + " gameSessionId:" + gameSessionId);
            }
            this.saveShortBetInfo = bankInfo.isSaveShortBetInfo() && gameSession.isRealMoney() &&
                    (!gameSession.isBonusGameSession() && !gameSession.isFRBonusGameSession() || bankInfo.isSaveBonusShortBetInfo());
            if (sessionInfo.getLastGameSessionId() != null) {
                sessionInfo.setLastCloseGameReason(""); //need reset if session reused
            }
            CassandraPersistenceManager persistenceManager = applicationContext
                    .getBean("persistenceManager", CassandraPersistenceManager.class);
            playerGameSettingsPersister = persistenceManager.getPersister(CassandraPlayerGameSettingsPersister.class);
            now = System.currentTimeMillis();
            initPlayerGameSettings(bankInfo.getPgsType(), (int) gameId);
            StatisticsManager.getInstance().updateRequestStatistics("DBLink 4",
                    System.currentTimeMillis() - now, accountId);
            this.gameSessionId = gameSessionId;
            logPrefix = "DBLink [accountId=" + accountId + ", nickName=" + nickName + ", gameId=" + gameId
                    + ", gameSessionId=" + gameSessionId + "] ";
            maxBetInCredits = -1;
            if (gameInfo.getProperty(BaseGameConstants.KEY_MAX_BET_IN_CREDITS) != null) {
                maxBetInCredits = Long.parseLong(gameInfo.getProperty(BaseGameConstants.KEY_MAX_BET_IN_CREDITS));
            }
            logoutOnError = bankInfo.isLogoutOnError();
            if (isForReal() && !isFRBGame() && !isBonusGameSession()) {
                //craps and MQ not store roundId in lasthand
                boolean mpGame = isMultiplayerGame(gameId);
                if (mpGame) {
                    try {
                        Long roundIdFromLH = getRoundIdFromLastHand();
                        setRoundId(roundIdFromLH != null ? roundIdFromLH : getRoundIdFromWallet((int) gameId));
                    } catch (Exception e) {
                        logError("Can't set roundId accountId=" + accountId, e);
                    }
                } else {
                    setRoundId(getRoundIdFromLastHand());
                }
                if (bankInfo.isUseJvmSessionTracking() && bankInfo.getRealModeSessionTimeout() != null) {
                    LocalSessionTracker localSessionTracker = applicationContext.getBean(LocalSessionTracker.class);
                    localSessionTracker.addSession(accountId, bankInfo.getRealModeSessionTimeout());
                }
            }
        } catch (Exception e) {
            logError("dbLink init error:", e);
            throw new CommonException(e);
        }
    }

    @Override
    public void saveGameSessionRealityCheckParams() {
        //code removed
    }

    @Override
    public boolean isBonusInstantLostOnThreshold() {
        return false;
    }

    private Long getRoundIdFromWallet(int gameId) {
        IWallet wallet = getWallet();
        logDebug("getRoundIdFromWallet: accountId:" + accountId + " gameId:" +
                gameId + " gameName:" + gameName + " gameSessionId:" + gameSessionId + "wallet:" + wallet);

        if (wallet == null) {
            return null;
        }

        CommonGameWallet gameWallet = wallet.getGameWallet(gameId);
        logDebug("getRoundIdFromWallet: accountId:" + accountId + " gameId:" +
                gameId + " gameName:" + gameName + " gameSessionId:" + gameSessionId + "gameWallet:" + gameWallet);

        Long roundId = gameWallet == null ? null : gameWallet.getRoundId();
        logDebug("getRoundIdFromWallet: accountId:" + accountId + " gameId:" +
                gameId + " gameName:" + gameName + " gameSessionId:" + gameSessionId + "roundId:" + roundId);

        return roundId;
    }

    private void checkAvailablePromos(GameSession gameSession) throws CommonException {
        if (isForReal() && GameMode.REAL.equals(getMode()) && !isFRBGame()) {
            ClientType clientType = gameSession.getClientType();
            Long clientTypeId = null;
            if (clientType != null) {
                clientTypeId = clientType.getId();
            }
            Set<IPromoCampaign> active = promoCampaignManager.getActive(bankId, gameId, null,
                    clientTypeId, getAccount());
            if (CollectionUtils.isNotEmpty(active)) {
                List<Long> ids = new ArrayList<>(active.size());
                for (IPromoCampaign campaign : active) {
                    String baseCurrency = campaign.getBaseCurrency();
                    String gameSessionCurrency = gameSession.getCurrency().getCode();
                    boolean canBeConverted = currencyCanBeConverted(baseCurrency, gameSessionCurrency);
                    if (canBeConverted) {
                        LOG.debug("checkAvailablePromos: found={}", campaign);
                        ids.add(campaign.getId());
                        gameSession.setPromoCampaignIds(ids);
                    } else {
                        LOG.warn("checkAvailablePromos: currency for campaign = {} cannot be converted from {} to {}, skip",
                                campaign.getId(), baseCurrency, gameSessionCurrency);
                    }
                }
            }
        }
    }

    private boolean currencyCanBeConverted(String source, String dest) {
        try {
            currencyConverter.convert(1, source, dest);
            return true;
        } catch (CommonException e) {
            LOG.warn("Cannot convert currency from {} to {}, reason = {}", source, dest, e.getMessage());
            return false;
        }
    }

    private void initPlayerGameSettings(final PlayerGameSettingsType pgsType, final int gameId) {
        this.usePlayerGameSettings = pgsType != PlayerGameSettingsType.NONE && isForReal() && !isFRBGame() && !isBonusGameSession();
        if (this.usePlayerGameSettings) {
            if (pgsType == PlayerGameSettingsType.DEDICATED) {
                final PlayerGameSettings playerGameSettings =
                        playerGameSettingsPersister.get(accountId, gameId);
                SessionHelper.getInstance().getTransactionData().setPlayerGameSettings(playerGameSettings);
                this.playerGameSettings = playerGameSettings;
            } else {
                PlayerGameSettings playerGameSettings = SessionHelper.getInstance().getTransactionData().getPlayerGameSettings();
                this.playerGameSettings = playerGameSettings != null && playerGameSettings.getGameId() == gameId ? playerGameSettings : null;
                if (this.playerGameSettings == null) {
                    logDebug("playerGameSettings not used. playerGameSettings=" + playerGameSettings +
                            ". gameId=" + gameId +
                            (playerGameSettings != null ? ". gameIds equals=" + (playerGameSettings.getGameId() == gameId) : ""));
                }
            }
        }
    }

    @Override
    public AbstractSharedGameState<?, ?> getSharedGameState(String unjExtraId) throws CommonException {
        //code removed
        return null;
    }

    @Override
    public void updateSharedGameState(String unjExtraId, AbstractSharedGameState state) throws CommonException {
        //code removed
    }

    @Override
    public void interceptRoundFinished() {
        if (sendBetInterceptor != null) {
            sendBetInterceptor.processRoundFinished(getRoundId(), getGameSession(),
                    getAccount(), LOG);
        }
    }

    @Override
    public SessionInfo getSessionInfo() {
        return SessionHelper.getInstance().getTransactionData().getPlayerSession();
    }

    @Override
    public Currency getCurrency() {
        return currency;
    }

    @Override
    public Currency getActiveCurrency() {
        return currency;
    }

    @Override
    public boolean isWalletBank() {
        return walletBank;
    }

    @Override
    public long getGameSessionId() {
        return gameSessionId;
    }

    @Override
    public long getBankId() {
        return bankId;
    }

    @Override
    public boolean isSaveGameSidByRound() {
        return saveGameSidByRound;
    }

    @Override
    public boolean isSaveShortBetInfo() {
        return saveShortBetInfo;
    }

    @Override
    public Double getMinBet() {
        return minBet;
    }

    @Override
    public Double getConvertedMinBet() {
        return minBet;
    }

    @Override
    public void setMinBet(Double minBet) {
        this.minBet = minBet;
    }

    @Override
    public Double getMaxBet() {
        return maxBet;
    }

    @Override
    public Double getConvertedMaxBet() {
        return maxBet;
    }

    @Override
    public void setMaxBet(Double maxBet) {
        this.maxBet = maxBet;
    }

    @Override
    public double getPayoutPercent() {
        return payoutPercent;
    }

    @Override
    public double[] getCOINSEQ() {
        return COINSEQ;
    }

    @Override
    public double[] getConvertedCOINSEQ() {
        return COINSEQ;
    }

    @Override
    public void setCOINSEQ(double[] COINSEQ) {
        this.COINSEQ = COINSEQ;
    }

    @Override
    public boolean isLimitsChanged() {
        return limitsChanged;
    }

    @Override
    public void setLimitsChanged(boolean limitsChanged) {
        this.limitsChanged = limitsChanged;
    }

    @Override
    public Map<String, String> getLastHandLimits() {
        return lastHandLimits;
    }

    @Override
    public void setLastHandLimit(String key, String value) {
        getLogger().debug("setLastHandLimit:: key=" + key);
        this.lastHandLimits.put(key, value);
    }

    @Override
    public void resetLasthandLimits() {
        lastHandLimits.clear();
    }

    @Override
    public long getAccountId() {
        return accountId;
    }

    @Override
    public long getGameId() {
        return gameId;
    }

    @Override
    public Long getRoomId() {
        return null;
    }

    @Override
    public GameSession getGameSession() {
        return SessionHelper.getInstance().getTransactionData().getGameSession();
    }

    @Override
    public GameMode getMode() {
        return GameMode.REAL;
    }

    @Override
    public String getNickName() {
        return nickName;
    }

    @Override
    public Long getRoundId() {
        return roundId;
    }

    @Override
    public void setRoundId(Long roundId) {
        this.roundId = roundId;
    }

    @Override
    public long getWinAmount() {
        return winAmount;
    }

    @Override
    public void setWinAmount(long winAmount) {
        this.winAmount = winAmount;
    }

    @Override
    public void resetCurrentBetWin() {
        getLogger().debug("resetCurrentBetWin(); prev currentBet=" + currentBet + "; currentWin=" + currentWin);
        this.currentBet = 0;
        this.currentWin = 0;
        this.requestParameters.clear();
    }

    @Override
    public void updateCurrentBetWin(long betAmount, long winAmount) {
        this.currentBet += betAmount;
        this.currentWin += winAmount;
    }

    @Override
    public long getCurrentBet() {
        return this.currentBet;
    }

    @Override
    public long getCurrentWin() {
        return this.currentWin;
    }

    @Override
    public boolean isUsePlayerGameSettings() {
        return usePlayerGameSettings;
    }

    @Override
    public PlayerGameSettings getPlayerGameSettings() {
        return playerGameSettings;
    }

    @Override
    public GameSession finishGameSession(GameSession gameSession, SessionInfo sessionInfo) throws CommonException {
        long now = System.currentTimeMillis();
        if (getLogger().isDebugEnabled()) {
            logDebug("finishGameSession started, gameSession is:" + gameSession);
        }
        if (gameSession == null) {
            throw new CommonException("gameSession is null");
        }
        LasthandInfo lasthand = getLasthandInfo(gameId);
        StatisticsManager.getInstance().updateRequestStatistics("DBlink: finishGameSession: getLasthand",
                System.currentTimeMillis() - now, accountId);
        now = System.currentTimeMillis();
        PlayerBet bet = null;
        PlayerBet currentPlayerBet = lastSavedBet != null ? lastSavedBet : playerBetPersistenceManager.
                getCurrentBet(gameSession);
        StatisticsManager.getInstance().updateRequestStatistics("DBlink: finishGameSession: getCurrentBet",
                System.currentTimeMillis() - now, accountId);
        now = System.currentTimeMillis();
        long balanceLong = Double.valueOf(adjustMoneyValue(getBalanceLong())).longValue();
        if (gameId == 199 && currentPlayerBet != null && ((lasthand == null ||
                StringUtils.isTrimmedEmpty(lasthand.getLasthandData())) ||
                (!lasthand.getLasthandData().contains("BETS")))) {
            saveLastHandOnClose(lasthand);
            playerBetPersistenceManager.updateBet(gameSession, currentPlayerBet, ArchiveBetTools.GS_ENDGAME,
                    null, null, 0, 0, balanceLong, null, isSaveShortBetInfo());
            StatisticsManager.getInstance().updateRequestStatistics("DBlink: finishGameSession: updateBet",
                    System.currentTimeMillis() - now, accountId);
        } else {
            saveLastHandOnClose(getLasthandInfo(gameId));
//            int gameStateId = lasthand == null || StringUtils.isTrimmedEmpty(lasthand.getLasthandData()) ?
//                    ArchiveBetTools.GS_ENDGAME : ArchiveBetTools.GS_NOTFINISHED;
            final IStartGameHelper helper = StartGameHelpers.getInstance().getHelper(gameId);
            int gameStateId =
                    (lasthand == null || helper.isRoundFinished(lasthand.getLasthandData(), gameSession)) ?
                            ArchiveBetTools.GS_ENDGAME : ArchiveBetTools.GS_NOTFINISHED;
            bet = new PlayerBet(gameSession.getLastPlayerBetId() + 1, gameSession.getRoundsCount() + 1,
                    gameStateId, null, null, 0, 0, balanceLong,
                    null, System.currentTimeMillis());
            StatisticsManager.getInstance().updateRequestStatistics("DBlink: finishGameSession: newBet",
                    System.currentTimeMillis() - now, accountId);
        }
        now = System.currentTimeMillis();
        long endTime = NtpTimeProvider.getInstance().getTime();
        if (gameSession.getStartTime() >= endTime) {
            endTime = gameSession.getStartTime() + 1;
        }
        playerBetPersistenceManager.finishGameSession(gameSession, endTime, bet, sendVbaToExternalSystem);
        StatisticsManager.getInstance().updateRequestStatistics("DBlink: finishGameSession: finishGameSession",
                System.currentTimeMillis() - now, accountId);
        now = System.currentTimeMillis();
        playerBetPersistenceManager.flushGameSessionHistory(gameSession, getAccount().getExternalId());
        StatisticsManager.getInstance().updateRequestStatistics("DBlink: finishGameSession: flushGameSessionHistory",
                System.currentTimeMillis() - now, accountId);
        now = System.currentTimeMillis();
        if (isWalletBank()) {
            IWallet wallet = getWallet();
            if (wallet != null && !wallet.isAnyWalletOperationExist()) {
                StatisticsManager.getInstance().updateRequestStatistics("DBlink: finishGameSession: " +
                        "isAnyFRBWinOperationExist", System.currentTimeMillis() - now, accountId);
                now = System.currentTimeMillis();
                Long walletGameSessionId = wallet.getGameWalletGameSessionId((int) gameId);
                if (walletGameSessionId == null) {
                    //check by sessionInfo need for CW v.1 and SBWallet
                    //removeWalletWithCheckBySessionInfo(sessionInfo);
                } else if (walletGameSessionId == gameSessionId) {
                    if (LOG.isDebugEnabled()) {
                        logDebug("finishGameSession: found wallet for this walletGameSessionId: " +
                                walletGameSessionId + ", remove");
                    }
                    // removeWalletWithCheckBySessionInfo(sessionInfo);
                } else {
                    if (LOG.isDebugEnabled()) {
                        logDebug("finishGameSession: not found wallet for this walletGameSessionId: " +
                                walletGameSessionId + ", don't remove");
                    }
                }
                StatisticsManager.getInstance().updateRequestStatistics("DBlink: finishGameSession: " +
                                "removeWalletWithCheckBySessionInfo",
                        System.currentTimeMillis() - now, accountId);
                now = System.currentTimeMillis();
            }
        }
        if (isFRBGame()) {
            FRBonusDBLink frBonusDBLink = (FRBonusDBLink) this;
            IFRBonusWin frBonusWin = frBonusDBLink.getFrbonusWin();
            if (frBonusWin != null && !frBonusWin.isAnyFRBWinOperationExist()) {
                StatisticsManager.getInstance().updateRequestStatistics("FRBonusWin: finishGameSession: " +
                        "isAnyFRBWinOperationExist", System.currentTimeMillis() - now, accountId);
                now = System.currentTimeMillis();
                Long frbonusWinGameSessionId = frBonusWin.getFRBonusWinGameSessionId(gameId);
                if (frbonusWinGameSessionId == null) {
                    //removeFRBonusWinWithCheckBySessionInfo(sessionInfo);
                } else if (frbonusWinGameSessionId == gameSessionId) {
                    if (LOG.isDebugEnabled()) {
                        logDebug("finishGameSession: found frbonusWin for this frbonusWinGameSessionId: " +
                                frbonusWinGameSessionId + ", remove");
                    }
                    //removeFRBonusWinWithCheckBySessionInfo(sessionInfo);
                } else {
                    if (LOG.isDebugEnabled()) {
                        logDebug("finishGameSession: not found frbonusWin for this frbonusWinGameSessionId: " +
                                frbonusWinGameSessionId + ", don't remove");
                    }
                }
                StatisticsManager.getInstance().updateRequestStatistics("FRBonusWin: finishGameSession: " +
                                "removeFRBonusWithCheckBySessionInfo", System.currentTimeMillis() - now,
                        accountId);
                now = System.currentTimeMillis();
            }
        }
        StatisticsManager.getInstance().updateRequestStatistics("DBlink: finishGameSession: flushGameSessionNoBets",
                System.currentTimeMillis() - now, accountId);
        if (getLogger().isDebugEnabled()) {
            logDebug("::finishGameSession REAL gameSessionId:" + gameSession.getId() + " finished OK");
        }
        return gameSession;
    }

    protected void saveLastHandOnClose(LasthandInfo lasthand) {
        LasthandPersister.getInstance().saveOnClose(accountId, gameId, null, null, lasthand);
    }

    @Override
    public void saveLasthand(String data) {
        saveLasthandInternal(gameId, data);
    }

    @Override
    public void setLasthand(String data) throws DBException {
        saveLasthandInternal(gameId, data);
    }

    @Override
    public String getLasthand() throws CommonException {
        return getLasthandData(gameId);
    }

    @Override
    public boolean isForReal() {
        return true;
    }

    @Override
    public void refreshGameSettings() {
        String profileId = getGameSession().getProfileId();
        this.gameInfo = getGameInfo(bankId, gameId, getActiveCurrency(), profileId);
    }

    private IBaseGameInfo<?, ?> getGameInfo(long bankId, long gameId, Currency currency, String profileId) {
        return StringUtils.isTrimmedEmpty(profileId) ?
                BaseGameCache.getInstance().getGameInfoById(bankId, gameId, currency) :
                BaseGameCache.getInstance().getGameInfoByIdProfiled(bankId, gameId, currency, profileId);
    }

    @Override
    public IBaseGameInfo<?, ?> getGameSettings() {
        return gameInfo;
    }

    @Override
    public double getDBBonus() {
        return ZERO_MONEY;
    }

    @Override
    public double getBalance() {
        return ((double) getBalanceLong()) / 100;
    }

    @Override
    public long getBalanceLong() {
        return getAccount().getBalance();
    }

    @Override
    public AccountInfo getAccount() {
        return SessionHelper.getInstance().getTransactionData().getAccount();
    }

    protected void saveLasthandInternal(long gameId, String data) {
        if (getLogger().isTraceEnabled()) {
            logTrace("saveLasthandInternal: " + data);
        }
        LasthandPersister.getInstance().save(gameId, data);
    }

    @Override
    public int getActiveGameID() {
        return (int) gameId;
    }

    @Override
    public void markEnterGame(int gameID, String gameName, int gamestate) throws CBGameException {
        if (gameID != getActiveGameID()) {
            logError("markEnterGame: " + "DBLink:: invalid gameId:" + gameID + " required:"
                    + getActiveGameID() + ", accountId = " + accountId);
            throw new CBGameException(IGameController.RESINTERNALERROR, "DBLink:: invalid gameId:" + gameID + " required:"
                    + getActiveGameID() + ", accountId = " + accountId);
        }
        GameSession gameSession = getGameSession();
        if (gameSession == null) {
            throw new CBGameException("game session not found");
        }
        gameSession.setEnterDate(System.currentTimeMillis());
    }

    @Override
    public double adjustMoneyValue(double originalValue) {
        return originalValue;
    }

    private boolean isMultiplayerGame(long gameId) {
        return BaseGameInfoTemplateCache.getInstance().getBaseGameInfoTemplateById(gameId).isMultiplayerGame();
    }

    @Override
    public void setLastSavedBet(PlayerBet bet) {
        this.lastSavedBet = bet;
    }

    public PlayerBet getLastSavedBet() {
        return lastSavedBet;
    }

    @Override
    public boolean isFRBGame() {
        return false;
    }

    @Override
    public void saveWinAmount(long winAmount) {
        setWinAmount(getWinAmount() + winAmount);
    }

    @Override
    public void incrementBalance(long bet, long win) throws CommonException {
        getAccount().incrementBalance(bet, win, false);
    }

    @Override
    public void interceptBet(long bet, long win) throws CommonException {
        if (sendBetInterceptor != null) {
            sendBetInterceptor.processBet(getRoundId(), getGameSession(), getAccount(), LOG, bet, win);
        }
    }

    @Override
    public boolean getLastHand(Map publicLastHand, Map privateLastHand, Map autoPublic,
                               Map autoPrivate) throws CBGameException {
        return getLastHand((int) gameId, publicLastHand, privateLastHand, autoPublic, autoPrivate);
    }

    @Override
    public boolean getLastHand(int gameID, Map publicLastHand, Map privateLastHand, Map autoPublic,
                               Map autoPrivate) throws CBGameException {
        try {
            String lastHandData = getLasthandData(gameID);
            // logDebug("getLastHand: lastHandData = " + lastHandData +
            // ", gameID = " +gameID);
            if (!StringUtils.isTrimmedEmpty(lastHandData)) {
                List<Map<String, String>> data = LasthandHelper.unpack(lastHandData);
                publicLastHand.putAll(data.get(0));
                if (data.get(1) != null && privateLastHand != null) {
                    privateLastHand.putAll(data.get(1));
                }
                if (data.get(2) != null && autoPublic != null) {
                    autoPublic.putAll(data.get(2));
                }
                if (data.get(3) != null && autoPrivate != null) {
                    autoPrivate.putAll(data.get(3));
                }
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            logError(" getLasthand exception", e);
            throw new CBGameException("DBLink::getLastHand exception!", e.getMessage());
        }
    }

    protected String getLasthandData(long gameID) {
        LasthandInfo lasthandInfo = getLasthandInfo(gameID);
        return lasthandInfo == null || lasthandInfo.getLasthandData() == null ? "" : lasthandInfo.getLasthandData();
    }

    @Override
    public LasthandInfo getLasthandInfo(long gameID) {
        return SessionHelper.getInstance().getTransactionData().getLasthand();
    }

    @Override
    public String getLasthand(long gameID) {
        return getLasthandData(gameID);
    }

    @Override
    public boolean isLocked() {
        return getAccount().isLocked();
    }

    @Override
    public boolean isAuthState() {
        return authState;
    }

    @Override
    public void setAuthState(boolean authState) {
        this.authState = authState;
    }

    @Override
    public void setRoundFinished() throws CommonException {
        GameSession gameSession = getGameSession();
        if (gameSession != null) {
            gameSession.incrementRoundsCount(1);
        } else {
            logWarn("Cannot set round finished, gameSession not found");
        }
    }

    /**
     * starting identifier for game
     */
    protected static final DecimalFormat fourZeroNumberFormat = new DecimalFormat();

    static {
        DecimalFormatSymbols symb = new DecimalFormatSymbols();
        symb.setDecimalSeparator('.');
        fourZeroNumberFormat.setDecimalFormatSymbols(symb);
        fourZeroNumberFormat.applyPattern("0.0000");
    }

    @Override
    public String getGameName() {
        return gameName;
    }

    public static double toMoney(double x) {
        return Double.parseDouble(DigitFormatter.doubleToMoney(x));
    }

    protected org.apache.log4j.Logger getLogger() {
        if (logger == null) {
            logger = GameLog.getInstance().log(getGameName().toLowerCase());
        }
        return logger;
    }

    public void logWarn(String s) {
        getLogger().warn(getLogPrefix() + s);
    }

    public void logDebug(String s) {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug(getLogPrefix() + s);
        }
    }

    public void logTrace(String s) {
        if (getLogger().isTraceEnabled()) {
            getLogger().trace(getLogPrefix() + s);
        }
    }

    public void logError(String s) {
        getLogger().error(getLogPrefix() + s);
    }

    public void logError(String s, Throwable e) {
        getLogger().error(getLogPrefix() + s, e);
    }

    @Override
    public String getLogPrefix() {
        return logPrefix;
    }

    public String encode(byte[] bytes) {
        return Base64.encodeBase64String(bytes);
    }

    @Override
    public IWallet getWallet() {
        if (!isWalletBank()) {
            return null;
        }
        return SessionHelper.getInstance().getTransactionData().getWallet();
    }

    @Override
    public long getLastActivity() {
        return lastActivity;
    }

    @Override
    public void updateLastActivity() {
        lastActivity = System.currentTimeMillis();
    }

    @Override
    public boolean isNeedUpdateLastActivity() {
        return System.currentTimeMillis() - lastActivity > UPDATE_ACTIVITY_PERIOD;
    }

    @Override
    public void setLastPaymentOperationId(Long lastPaymentOperationId) {
        this.lastPaymentOperationId = lastPaymentOperationId;
    }

    @Override
    public boolean isSendRoundId() {
        return sendRoundId;
    }

    @Override
    public boolean isSendExternalWalletMessages() {
        return sendExternalWalletMessages;
    }

    @Override
    public boolean isCloseOldGameAfterRoundFinished() {
        return closeOldGameAfterRoundFinished;
    }

    @Override
    public boolean isGameLogEnabled() {
        return true;
    }

    @Override
    public boolean isBonusGameSession() {
        return false;
    }

    @Override
    public String getDefaultBetPerLineNotFRB() {
        return getGameSettingsProperty(BaseGameConstants.KEY_DEFAULTBETPERLINE);
    }

    @Override
    public String getDefaultNumLinesNotFRB() {
        return getGameSettingsProperty(BaseGameConstants.KEY_DEFAULTNUMLINES);
    }

    @Override
    public String getCurrentDefaultBetPerLine() {
        return getDefaultBetPerLineNotFRB();
    }

    @Override
    public String getCurrentDefaultNumLines() {
        return getDefaultNumLinesNotFRB();
    }

    @Override
    public String getFRBDefaultBetPerLine() {
        return getGameSettingsProperty(BaseGameConstants.KEY_FRB_DEFAULTBETPERLINE);
    }

    @Override
    public String getFRBDefaultNumLines() {
        return getGameSettingsProperty(BaseGameConstants.KEY_FRB_DEFAULTNUMLINES);
    }

    @Override
    public String getFRBCoin() {
        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(getBankId());
        return gameSettingsManager.getFRBCoin(bankInfo, this);
    }

    @Override
    public Integer getDefaultCoin() {
        return gameSettingsManager.getDefaultCoin(this);
    }

    @Override
    public String getChipValues() {
        return getGameSettings().getChipValues();
    }

    @Override
    public List<Coin> getCoins() {
        return gameSettingsManager.getCoins(this);
    }

    @Override
    public List<? extends ICoin<?>> getCoins(boolean withCache) {
        return gameSettingsManager.getCoins(this, withCache);
    }

    @Override
    public boolean isUseDynamicLevels() {
        return BaseGameInfoTemplateCache.getInstance().getBaseGameInfoTemplateById(getGameId()).isDynamicLevelsSupported();
    }

    @Override
    public Limit getLimit() {
        return (Limit) getGameSettings().getLimit();
    }

    @Override
    public String getGameSettingsProperty(String key) {
        return getGameSettings().getProperty(key);
    }

    @Override
    public int getSubCasinoId() {
        return getAccount().getSubCasinoId();
    }

    @Override
    public int getTimeZoneOffset() throws CommonException {
        if (timeZone == null) {
            BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(getBankId());
            String timeZoneName = bankInfo.getTimeZone();
            if (timeZoneName == null) {
                throw new CommonException("TimeZone not set bankId=" + getBankId());
            }
            timeZone = SimpleTimeZone.getTimeZone(timeZoneName);
            if (timeZone == null) {
                throw new CommonException("Wrong timeZone bankId=" + getBankId());
            }
        }
        return timeZone.getOffset(System.currentTimeMillis()) / 60000;
    }

    public boolean isMaxBet(long betAmount) {
        if (maxBetInCredits == -1)
            return true;
        else
            return betAmount == maxBetInCredits;
    }

    @Override
    public Map<String, String[]> getRequestParameters() {
        return requestParameters;
    }

    @Override
    public void setRequestParameters(Map<String, String[]> parameters) {
        this.requestParameters = new HashMap<>(parameters);
    }

    @Override
    public String getRequestParameterValue(String parameterName) {
        if (gameId == 209) {
            return null;
        }
        String[] values = requestParameters.get(parameterName.toUpperCase());
        return values == null || values.length == 0 ? null : values[0];
    }

    @Override
    public Long getLastCheckTime() {
        return lastCheckTime;
    }

    @Override
    public void setLastCheckTime(Long lastCheckTime) {
        this.lastCheckTime = lastCheckTime;
    }

    @Override
    public String getLasthandParameter(final String parameterName) {
        String lastHandData = getLasthandData(gameId);
        LOG.debug("getLasthandParameter:: gameId: {}, lasthandData: {}", gameId, lastHandData);
        if (!StringUtils.isTrimmedEmpty(lastHandData)) {
            try {
                List<Map<String, String>> lasthandList = LasthandHelper.unpack(lastHandData);
                return lasthandList.stream()
                        .filter(Objects::nonNull)
                        .map(lasthand -> lasthand.get(parameterName))
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElse(EMPTY);
            } catch (GameException e) {
                logError("Unable to unpack lasthand", e);
            }
        }
        return EMPTY;
    }

    @Override
    public boolean isNotFixAnyChanges() {
        return false;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("DBLink");
        sb.append("[accountId=").append(accountId);
        sb.append(", nickName='").append(nickName).append('\'');
        sb.append(", bankId=").append(bankId);
        sb.append(", gameId=").append(gameId);
        sb.append(", gameName='").append(gameName).append('\'');
        sb.append(", gameSessionId=").append(gameSessionId);
        sb.append(", minBet=").append(minBet);
        sb.append(", maxBet=").append(maxBet);
        sb.append(", currentBet=").append(currentBet);
        sb.append(", currentWin=").append(currentWin);
        sb.append(", COINSEQ=").append(COINSEQ == null ? "null" : "");
        for (int i = 0; COINSEQ != null && i < COINSEQ.length; ++i) {
            sb.append(i == 0 ? "" : ", ").append(COINSEQ[i]);
        }
        sb.append(", limitsChanged=").append(limitsChanged);
        sb.append(", roundId=").append(roundId);
        sb.append(", winAmount=").append(winAmount);
        sb.append(']');
        return sb.toString();
    }

    @Override
    public long generateRoundId() {
        return IdGenerator.getInstance().getNext(IWallet.class);
    }

    protected Long getRoundIdFromLastHand() {
        try {
            Map<String, String> publicLastHand = new HashMap<>();
            Map<String, String> privateLastHand = new HashMap<>();

            getLastHand(publicLastHand, privateLastHand, null, null);
            logDebug("getRoundIdFromLastHand: accountId:" + accountId + " gameId:" + gameId + " gameName:" + gameName +
                    " gameSessionId:" + gameSessionId + "publicLastHand:" + publicLastHand + "privateLastHand:" + privateLastHand);

            String strRoundId = privateLastHand.get(IGameController.PARAMROUNDID);
            logDebug("getRoundIdFromLastHand: accountId:" + accountId + " gameId:" + gameId + " gameName:" + gameName +
                    " gameSessionId:" + gameSessionId + "strRoundId:" + strRoundId);

            return strRoundId != null ? Long.valueOf(strRoundId) : null;

        } catch (CBGameException e) {
            getLogger().error("Unable get lasthand", e);
            return null;
        } catch (NumberFormatException e) {
            getLogger().error("Unable parse roundId", e);
            return null;
        }
    }

    @Override
    public boolean isLogoutOnError() {
        return logoutOnError;
    }

    protected static class NoWinJPWinQualifier implements IJPWinQualifier {
        @Override
        public boolean isJpCanBeWonForCoin(long coinId) {
            return false;
        }

        @Override
        public boolean isJpCanBeWonForAmount(double amount) {
            return false;
        }
    }

    @Override
    public Long getWalletTransactionId() {
        IWallet wallet = getWallet();
        if (wallet != null) {
            IWalletOperation walletOperation = wallet.getCurrentWalletOperation((int) getGameId());
            return walletOperation != null ? walletOperation.getId() : null;
        }
        return null;
    }
}
