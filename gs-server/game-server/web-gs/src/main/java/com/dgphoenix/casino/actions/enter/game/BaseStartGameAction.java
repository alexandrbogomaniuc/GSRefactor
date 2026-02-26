package com.dgphoenix.casino.actions.enter.game;

import com.dgphoenix.casino.account.AccountManager;
import com.dgphoenix.casino.actions.enter.AccountInfoAndSessionInfoPair;
import com.dgphoenix.casino.actions.enter.CommonActionForm;
import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.cassandra.persist.CassandraExtendedAccountInfoPersister;
import com.dgphoenix.casino.cassandra.persist.CassandraLasthandPersister;
import com.dgphoenix.casino.cassandra.persist.CassandraPlayerSessionState;
import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.BaseGameCache;
import com.dgphoenix.casino.common.cache.BaseGameInfoTemplateCache;
import com.dgphoenix.casino.common.cache.CurrencyCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.account.LasthandInfo;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.currency.Currency;
import com.dgphoenix.casino.common.cache.data.game.BaseGameInfoTemplate;
import com.dgphoenix.casino.common.cache.data.game.GameMode;
import com.dgphoenix.casino.common.cache.data.game.IBaseGameInfo;
import com.dgphoenix.casino.common.cache.data.payment.bonus.FRBonusNotification;
import com.abs.casino.common.cache.data.payment.frb.IFRBonusWin;
import com.dgphoenix.casino.common.cache.data.server.ServerInfo;
import com.dgphoenix.casino.common.cache.data.session.ClientType;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.dgphoenix.casino.common.config.GameServerConfigTemplate;
import com.dgphoenix.casino.common.config.HostConfiguration;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.exception.FRBException;
import com.dgphoenix.casino.common.exception.MaintenanceModeException;
import com.dgphoenix.casino.common.promo.IPrize;
import com.dgphoenix.casino.common.promo.IPromoCampaign;
import com.dgphoenix.casino.common.promo.IPromoCampaignManager;
import com.dgphoenix.casino.common.promo.TournamentPrize;
import com.dgphoenix.casino.common.transactiondata.ITransactionData;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.dgphoenix.casino.common.util.Pair;
import com.dgphoenix.casino.common.util.RNG;
import com.dgphoenix.casino.common.util.string.StringIdGenerator;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.common.web.BaseAction;
import com.dgphoenix.casino.common.web.ClientTypeFactory;
import com.dgphoenix.casino.common.web.ShellDetector;
import com.dgphoenix.casino.common.web.login.apub.APUBConstants;
import com.dgphoenix.casino.common.web.login.apub.GameServerResponse;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import com.dgphoenix.casino.gs.GameServer;
import com.dgphoenix.casino.gs.GameServerComponentsHelper;
import com.dgphoenix.casino.gs.managers.game.StartGameSessionHelper;
import com.dgphoenix.casino.gs.managers.payment.bonus.FRBonusManager;
import com.dgphoenix.casino.gs.managers.payment.bonus.FRBonusNotificationManager;
import com.dgphoenix.casino.gs.managers.payment.transfer.PaymentManager;
import com.dgphoenix.casino.gs.managers.payment.wallet.IWalletProtocolManager;
import com.dgphoenix.casino.gs.managers.payment.wallet.WalletProtocolFactory;
import com.dgphoenix.casino.gs.managers.payment.wallet.v3.CommonWalletAuthResult;
import com.dgphoenix.casino.gs.managers.payment.wallet.v3.ICommonWalletClient;
import com.dgphoenix.casino.gs.persistance.GameSessionPersister;
import com.dgphoenix.casino.services.mp.MPGameSessionService;
import com.dgphoenix.casino.sm.CWPlayerSessionManager;
import com.dgphoenix.casino.sm.IPlayerSessionManager;
import com.dgphoenix.casino.sm.PlayerSessionFactory;
import com.dgphoenix.casino.support.ErrorPersisterHelper;
import com.dgphoenix.casino.system.configuration.GameServerConfiguration;
import com.dgphoenix.casino.web.history.GameHistoryURLBuilder;
import com.google.common.base.Joiner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.springframework.context.ApplicationContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

import static com.dgphoenix.casino.common.util.string.StringUtils.isTrimmedEmpty;

/**
 * User: flsh
 * Date: 7/22/11
 */
public abstract class BaseStartGameAction<T extends ActionForm & IStartGameForm> extends BaseAction<T> implements
        APUBConstants {
    public static final String ACCOUNT_ID = "accountId";
    public static final String SUBCASINO_ID = "subCasinoId";
    protected static final Set<Integer> BANKS_WITHOUT_MP_VALIDATION = new HashSet<>(Arrays.asList(6274, 6275, 9128, 9129, 9522));
    protected static final String NEW_GAMES_ROUTE_GAME_ID_KEY = "NEW_GAMES_ROUTE_GAME_ID";
    protected static final String NEW_GAMES_CLIENT_URL_KEY = "NEW_GAMES_CLIENT_URL";
    protected static final String NEW_GAMES_API_URL_KEY = "NEW_GAMES_API_URL";
    protected static final String NEW_GAMES_GS_INTERNAL_BASE_URL_KEY = "NEW_GAMES_GS_INTERNAL_BASE_URL";
    protected static final String NEW_GAMES_ROUTE_GAME_ID_DEFAULT = "00010";
    protected static final String NEW_GAMES_CLIENT_URL_DEFAULT = "http://localhost:5174/";
    protected static final String NEW_GAMES_API_URL_DEFAULT = "http://localhost:6400";
    protected static final String NEW_GAMES_GS_INTERNAL_BASE_URL_DEFAULT = "http://localhost:81";
    private static final Logger LOG = LogManager.getLogger(BaseStartGameAction.class);

    private final CassandraExtendedAccountInfoPersister extendedAccountInfoPersister;
    private final CassandraLasthandPersister lasthandPersister;
    protected final HostConfiguration hostConfiguration;
    protected final ErrorPersisterHelper errorPersisterHelper;
    protected final GameServerConfiguration gameServerConfiguration;
    protected final MPGameSessionService mpGameSessionService;

    public BaseStartGameAction() {
        ApplicationContext applicationContext = ApplicationContextHelper.getApplicationContext();
        CassandraPersistenceManager persistenceManager = applicationContext.getBean("persistenceManager", CassandraPersistenceManager.class);
        gameServerConfiguration = applicationContext.getBean("gameServerConfiguration", GameServerConfiguration.class);
        mpGameSessionService = applicationContext.getBean("mpGameSessionService", MPGameSessionService.class);
        extendedAccountInfoPersister = persistenceManager.getPersister(CassandraExtendedAccountInfoPersister.class);
        lasthandPersister = persistenceManager.getPersister(CassandraLasthandPersister.class);
        errorPersisterHelper = applicationContext.getBean(ErrorPersisterHelper.class);
        hostConfiguration = applicationContext.getBean(HostConfiguration.class);
    }

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request,
                                 HttpServletResponse response) throws Exception {
        T form = (T) actionForm;
        if (isTournamentLobbyRequest(form)) {
            String tournamentLobbyUrl = "/tournamentlobby.do?" + request.getQueryString();
            return new ActionForward(tournamentLobbyUrl);
        }
        return super.execute(mapping, actionForm, request, response);
    }

    protected boolean isTournamentLobbyRequest(T actionForm) {
        return "1".equals(actionForm.getGameId()) || "0".equals(actionForm.getGameId());
    }

    @Override
    protected ActionForward process(ActionMapping mapping, T actionForm, HttpServletRequest request,
                                    HttpServletResponse response) throws Exception {
        //nop
        return null;
    }

    @Override
    protected Logger getLog() {
        return LOG;
    }

    protected void saveReferer(HttpServletRequest request, AccountInfo accountInfo) {
        String referer = request.getHeader("Referer");
        if (!StringUtils.isTrimmedEmpty(referer)) {
            extendedAccountInfoPersister.persist(accountInfo.getBankId(),
                    accountInfo.getExternalId(), "REFERER", referer);
        }
    }

    protected Pair<GameSession, Boolean> finishGameSessionAndMakeSitOut(String sid, String privateRoomId) {
        LOG.debug("finishGameSessionAndMakeSitOut: sid={}, privateRoomId={}", sid, privateRoomId);
        try {
            return mpGameSessionService.finishGameSessionAndMakeSitOut(sid, privateRoomId);
        } catch (Exception e) {
            LOG.warn("finishGameSessionAndMakeSitOut: Exception:{}", e.getMessage(), e);
        }
        return null;
    }

    protected CassandraPlayerSessionState getPlayerSessionWithUnfinishedSid(String extId) {
        return mpGameSessionService.getPlayerSessionWithUnfinishedSid(extId);
    }

    protected void savePlayerSessionState(String sid, String extId, String privateRoomId, boolean isFinishGameSession, long dateTime) {
        mpGameSessionService.savePlayerSessionState(sid, extId, privateRoomId, isFinishGameSession, dateTime);
    }

    protected boolean isNeedStartUnfinishedGame(GameMode mode, BankInfo bankInfo, AccountInfo accountInfo, long gameId) {
        boolean needStartUnfinishedGame = !accountInfo.isTestUser() && mode == GameMode.REAL &&
                bankInfo.isOverrideGameIdIfFoundUnfinished();
        if (needStartUnfinishedGame) {
            try {
                FRBonusManager.getInstance().checkMassAwardsForAccount(accountInfo);
                Long bonusId = FRBonusManager.getInstance().getEarlestActiveFRBonusId(accountInfo.getId(), gameId);
                return bonusId == null;
            } catch (Exception e) {
                getLog().error("isNeedStartUnfinishedGame error", e);
                return false;
            }
        } else {
            return false;
        }
    }

    protected long getUnfinishedGameId(long bankId, long currentGameId, AccountInfo accountInfo) {
        LasthandInfo lasthand = SessionHelper.getInstance().getTransactionData().getLasthand();
        //if found unfinished online && not in maintenanceMode
        if (lasthand != null && !StringUtils.isTrimmedEmpty(lasthand.getLasthandData())) {
            long lasthandGameId = lasthand.getId();
            BaseGameInfoTemplate template =
                    BaseGameInfoTemplateCache.getInstance().getBaseGameInfoTemplateById(lasthandGameId);
            if (template != null && !template.isRoundFinished(lasthand.getLasthandData())) {
                IBaseGameInfo defCurrencyBgi = BaseGameCache.getInstance().getGameInfo(bankId, lasthandGameId, "");
                if (defCurrencyBgi != null && !defCurrencyBgi.isMaintenanceMode()) {
                    return lasthandGameId;
                }
            }
        }
        Map<Long, String> lasthands = lasthandPersister.getRealModeLasthands(accountInfo.getId());
        List<Long> unfinishedGameIds = new ArrayList<>(lasthands.size());
        for (Map.Entry<Long, String> entry : lasthands.entrySet()) {
            long gameId = entry.getKey();
            String lh = entry.getValue();
            if (!StringUtils.isTrimmedEmpty(lh)) {
                BaseGameInfoTemplate template =
                        BaseGameInfoTemplateCache.getInstance().getBaseGameInfoTemplateById(gameId);
                if (template != null && !template.isRoundFinished(lh)) {
                    IBaseGameInfo defCurrencyBgi = BaseGameCache.getInstance().getGameInfo(bankId, gameId, "");
                    if (defCurrencyBgi != null && !defCurrencyBgi.isMaintenanceMode()) {
                        unfinishedGameIds.add(gameId);
                    }
                }
            }
        }
        Collections.sort(unfinishedGameIds);
        getLog().debug("getUnfinishedGameId: unfinishedGameIds={}", unfinishedGameIds);
        return unfinishedGameIds.isEmpty() ? currentGameId : unfinishedGameIds.get(0);
    }

    protected AccountInfoAndSessionInfoPair loginGuest(T form, HttpServletRequest request, Currency currency) throws CommonException {
        try {
            AccountInfoAndSessionInfoPair pair = createGuestAccount(form.getBankId(), form.getSubCasinoId(), currency);
            AccountInfo accountInfo = pair.getAccount();
            long bankId = form.getBankId();
            getLog().debug("TransactionData: " + SessionHelper.getInstance().getTransactionData());
            IPlayerSessionManager psm =
                    PlayerSessionFactory.getInstance().getPlayerSessionManager(bankId);
            String fakeExternalSessionId = StringIdGenerator.generateSessionId(GameServer.getInstance().getServerId(),
                    accountInfo.getBankId(), accountInfo.getExternalId());
            SessionInfo sessionInfo = psm.login(accountInfo, fakeExternalSessionId, request.getRemoteHost(), form.getClientType());
            pair.setSessionInfo(sessionInfo);
            return pair;
        } catch (CommonException e) {
            getLog().error("process unable to login guest player", e);
            throw new CommonException("Cannot login guest player", e);
        }
    }

    /**
     *  Makes auth request to external side use input params of action form.
     * @param form  action form
     * @param token token from action form
     * @param bankInfo bank info of system getting from bankId parameter of action form
     * @param remoteHost remoteHost from request
     * @param request request
     * @return {@code CommonWalletAuthResult} result of auth request to external side.
     * @throws CommonException if any unexpected error occur
     */
    protected CommonWalletAuthResult getAuthInfo(T form, String token, BankInfo bankInfo, String remoteHost, HttpServletRequest request)
            throws CommonException {
        if (isTrimmedEmpty(token) && form.getGameMode() == GameMode.REAL) {
            throw new CommonException("incorrect parameters: empty token and real mode");
        }
        getLog().debug("loginV3 token = " + token +
                ", bankId = " + form.getBankId() + ", subCasinoId = " + form.getSubCasinoId() +
                ", host = " + remoteHost + ", mode=" + form.getMode());
        IWalletProtocolManager ocwm = WalletProtocolFactory.getInstance().getWalletProtocolManager(form.getBankId());
        ICommonWalletClient client = (ICommonWalletClient) ocwm.getClient();
        CommonWalletAuthResult authResult = auth(client, token, form, request, bankInfo);
        processCommonWalletAuthResult(form, authResult, bankInfo);

        return authResult;
    }

    protected CommonWalletAuthResult auth(ICommonWalletClient client, String token, T form,
                                          HttpServletRequest request, BankInfo bankInfo) throws CommonException {
        long subCasinoId = bankInfo.getSubCasinoId();
        if (subCasinoId == 39 || subCasinoId == 62 || subCasinoId == 212) {
            // 0002807: VERA JOHN -- INTEGRATION AND SUPPORT
            // 0002573: X18
            return client.auth(token, form.getGameId(), ClientTypeFactory.getByHttpRequest(request));

        } else {
            if (bankInfo.isSendGameIdOnAuth()) {
                return client.auth(token, form.getGameId(), ClientTypeFactory.getByHttpRequest(request));
            } else {
                return client.auth(token, ClientTypeFactory.getByHttpRequest(request));
            }
        }
    }

    /**
     * Process of login.  As a result, a player session will be created in the system.
     * @param form form of start action
     * @param token token of playr from action form
     * @param remoteHost remote host from request
     * @param gameId gameId
     * @param mode mode (FREE|REAL|BONUS)
     * @param accountId accountId of player
     * @return {@code AccountInfoAndSessionInfoPair} pair of account info and player session info.
     * @throws CommonException if any unexpected error occur
     */
    protected AccountInfoAndSessionInfoPair loginV3(T form, String token, String remoteHost,
                                                    long gameId, GameMode mode, long accountId)
            throws CommonException {
        return loginV3(form, token, remoteHost, gameId, mode, accountId, true);
    }

    protected AccountInfoAndSessionInfoPair loginV3(T form, String token, String remoteHost,
                                                    long gameId, GameMode mode, long accountId,
                                                    boolean checkWalletOps)
            throws CommonException {
        AccountInfo accountInfo = null;
        CWPlayerSessionManager psm = (CWPlayerSessionManager)
                PlayerSessionFactory.getInstance().getPlayerSessionManager(form.getBankId());
        try {
            long now = System.currentTimeMillis();
            accountInfo = AccountManager.getInstance().getAccountInfo(accountId);

            BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(accountInfo.getBankId());
            StatisticsManager.getInstance().updateRequestStatistics("loginV3 3", System.currentTimeMillis() - now);
            now = System.currentTimeMillis();

            if (checkWalletOps) {
                StartGameSessionHelper.checkWalletOperations((int) gameId, mode, accountInfo, bankInfo);
            } else {
                getLog().info("loginV3: skip legacy wallet/game checks for new-games route, gameId={}", gameId);
            }
            StatisticsManager.getInstance().updateRequestStatistics("loginV3 5", System.currentTimeMillis() - now);
            now = System.currentTimeMillis();

            SessionInfo sessionInfo = psm.login(accountInfo, token, remoteHost, form.getClientType(), form, false);

            StatisticsManager.getInstance().updateRequestStatistics("loginV3 6", System.currentTimeMillis() - now);
            AccountInfoAndSessionInfoPair infoPair = new AccountInfoAndSessionInfoPair(accountInfo, sessionInfo);

            LOG.debug("BaseStartGameAction loginV3: infoPair={}",infoPair);

            return infoPair;
        } catch (Exception e) {
            LOG.error("BaseStartGameAction loginV3: unable to login player: " + (accountInfo == null ? "unknown, token=" + token :
                    accountInfo), e);
            if (e instanceof CommonException) {
                throw (CommonException) e;
            }
            throw new CommonException("Cannot login player", e);
        }
    }

    protected void processCommonWalletAuthResult(T form, CommonWalletAuthResult result, BankInfo bankInfo)
            throws CommonException {
        //nop by default
    }

    protected GameServerResponse startGame(AccountInfo accountInfo, SessionInfo sessionInfo, T form,
                                           HttpServletResponse response, String lang, boolean checkWalletOps)
            throws CommonException {
        long now = System.currentTimeMillis();
        GameServerResponse gameServerResponse = new GameServerResponse();
        IPlayerSessionManager psm = PlayerSessionFactory.getInstance().getPlayerSessionManager(form.getBankId());
        long gameId = Long.parseLong(form.getGameId());
        try {
            long now1 = System.currentTimeMillis();
            long accountId = accountInfo.getId();
            IBaseGameInfo gameInfo = getGameInfo(accountInfo, gameId);
            GameMode mode = form.getGameMode();
            Long bonusId = isNeedBonusCheck(form) ? validateBonusIdParam(form, mode, accountInfo) : null;
            if (bonusId == null) {
                FRBonusManager.getInstance().checkMassAwardsForAccount(accountInfo);
                if (!form.isNotGameFRB()) {
                    bonusId = FRBonusManager.getInstance().getEarlestActiveFRBonusId(accountId, gameInfo.getId());
                    if (bonusId != null) {
                        getLog().info("Earlest FRBonus Id:" + bonusId);
                    }
                }
            }
            Long gameSessionId = getPredefinedGameSessionId(form);
            StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() +
                    ":startGame 1", System.currentTimeMillis() - now1, accountId);
            now1 = System.currentTimeMillis();
            StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() +
                    ":startGame sync", System.currentTimeMillis() - now1, accountId);
            now1 = System.currentTimeMillis();
            StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() +
                    ":startGame 2", System.currentTimeMillis() - now1, accountId);
            if (getLog().isDebugEnabled()) {
                getLog().debug("starting game for accountId:" + accountId + ", sessionInfo:" + sessionInfo);
            }
            now1 = System.currentTimeMillis();
            Long unclosedGameSessionId = sessionInfo.getGameSessionId();
            if (unclosedGameSessionId != null) {
                closeOnlineGame(unclosedGameSessionId, accountInfo, sessionInfo);
            }
            StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() +
                    ":startGame 3", System.currentTimeMillis() - now1, accountId);
            now1 = System.currentTimeMillis();
            if (sessionInfo.getGameSessionId() != null) {
                getLog().warn("starting game for accountId:" + accountId
                        + " sessionId:" + sessionInfo.getSessionId() +
                        " player tries to start new game session not closing old one, " +
                        "old gameSessionId:" + unclosedGameSessionId +
                        " new gameSessionId:" + sessionInfo.getGameSessionId() + "^^^^");
                throw new CommonException("failed to start new game session, need to close previous");
            }
            checkPendingOperations(accountInfo, gameId, sessionInfo, unclosedGameSessionId, mode,
                    checkWalletOps);
            StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() +
                    ":startGame 4", System.currentTimeMillis() - now1, accountId);
            now1 = System.currentTimeMillis();

            GameServer.getInstance().checkMaintenanceMode(mode, lang, accountInfo, gameId);

            additionalProcess(form, response, accountInfo, sessionInfo, gameInfo, mode, gameSessionId);

            gameSessionId = GameServer.getInstance().startGame(sessionInfo, gameInfo, gameSessionId, mode,
                    bonusId, lang, accountInfo);

            StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() +
                    ":startGame 5", System.currentTimeMillis() - now1);
            afterGameStartedProcess(form, response, accountInfo, sessionInfo, gameInfo, mode, gameSessionId);

            gameServerResponse.setStatus(STATUS_SUCCESS);
            gameServerResponse.setGameSessionId(gameSessionId);
            if (getLog().isDebugEnabled()) {
                getLog().debug("starting game for accountId:" + accountId + " sessionId:" +
                        sessionInfo.getSessionId() + " was OK");
            }
        } catch (Throwable e) {
            if (e instanceof CommonException) {
                throw (CommonException) e;
            }
            throw new CommonException("Unexpected error", e);
        } finally {
            StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() +
                    ":startGame", System.currentTimeMillis() - now);
        }
        return gameServerResponse;
    }

    protected boolean isNeedBonusCheck(T form) {
        return true;
    }

    /**
     * Checks pending operations for gameId, accountId
     * @param accountInfo accountInfo of player
     * @param gameId gameId
     * @param sessionInfo player session info
     * @param unclosedGameSessionId old gameSessionId
     * @param mode game mode
     * @param checkWalletOps true, if needed to check uncompleted wallet operations.
     * @throws CommonException if any unexpected error occur or there is uncompleted wallet operations
     */
    protected void checkPendingOperations(AccountInfo accountInfo, long gameId, SessionInfo sessionInfo,
                                          Long unclosedGameSessionId, GameMode mode, boolean checkWalletOps)
            throws CommonException {
        if (!WalletProtocolFactory.getInstance().isWalletBank(accountInfo.getBankId())) {
            Long transactionId = PaymentManager.getInstance().getTrackingTransactionId();
            if (transactionId != null && mode == GameMode.REAL) {
                getLog().warn("starting game for accountId:" + accountInfo.getId()
                        + " sessionId:" + sessionInfo.getSessionId() +
                        " player tries to start new game session not closing old one, " +
                        "old gameSessionId:" + unclosedGameSessionId +
                        " new gameSessionId:" + sessionInfo.getGameSessionId() +
                        " player has unfinished transaction, transactionId:" + transactionId + "^^^^");
                throw new CommonException("failed to start new game session, need to close transaction");
            }
        } else {
            if (checkWalletOps) {
                BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(accountInfo.getBankId());
                StartGameSessionHelper.checkWalletOperations((int) gameId, mode, accountInfo, bankInfo);
            }
        }
        final ITransactionData ITransactionData = SessionHelper.getInstance().getTransactionData();
        if (GameMode.REAL == mode) {//do not start the same frb game if win in tracking
            IFRBonusWin frbonusWin = ITransactionData.getFrbWin();
            FRBonusManager.getInstance().checkPendingOperation(frbonusWin, accountInfo, gameId);
        }
        FRBonusNotification frbonusNotification = ITransactionData.getFrbNotification();
        if (frbonusNotification != null &&
                FRBonusNotificationManager.getInstance().isLaunchPrevented(frbonusNotification)) {
            throw new FRBException("FRB previous notification is not completed: " + frbonusNotification);
        }
    }

    protected ServerInfo assignServer(long bankId, Long gameId, GameMode mode) throws CommonException {
        return GameServer.getInstance().getServerInfo();
    }


    protected IBaseGameInfo getGameInfo(AccountInfo accountInfo, long gameId) throws CommonException {
        Currency currency = accountInfo.getCurrency();
        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(accountInfo.getBankId());
        IBaseGameInfo gameInfo = BaseGameCache.getInstance().getGameInfoById(accountInfo.getBankId(), gameId, currency);
        if (gameInfo == null) {
            throw new CommonException("game is not defined, id=" + gameId);
        }
        if (!gameInfo.isEnabled()) {
            throw new CommonException("game is not enabled, id=" + gameId);
        }
        return gameInfo;
    }

    protected Long validateBonusIdParam(T form, GameMode mode, AccountInfo accountInfo) throws CommonException {
        return null;
    }

    protected Long getPredefinedGameSessionId(T form) {
        return null;
    }

    private void closeOnlineGame(long gameSessionId, AccountInfo accountInfo,
                                 SessionInfo sessionInfo)
            throws CommonException {
        GameSession gameSession = GameSessionPersister.getInstance().getGameSession(gameSessionId);
        LOG.debug("BaseStartGameAction closeOnlineGame: need closeOnlineGame, gameSessionId: {}", gameSessionId);
        GameServer.getInstance().closeOnlineGame(accountInfo, sessionInfo, gameSession, false,
                false);
    }

    protected AccountInfo saveAccount(Long accountId, String externalId, String extCurrency, String extNickName,
                                      String extFirstName, String extLastName, String extEmail,
                                      String countryCode, CommonActionForm actionForm, boolean newAccount)
            throws CommonException {

        return AccountManager.getInstance().saveAccountWithCurrencyUpdate(
                accountId,
                externalId,
                actionForm.getBankInfo(),
                extNickName,
                false,
                false,
                extEmail,
                actionForm.getClientType(),
                extFirstName,
                extLastName,
                extCurrency,
                countryCode,
                newAccount);
    }

    private AccountInfoAndSessionInfoPair createGuestAccount(int bankId, short subCasinoId, Currency currency) throws CommonException {
        String randomStr;
        if (AccountManager.getInstance().isPerfectAccountIdMode(bankId)) {
            randomStr = String.valueOf(-RNG.nextLong());
        } else {
            randomStr = StringIdGenerator.generateTimeAndRandomBased();
        }
        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
        String nickName = (bankInfo.getExternalBankIdDescription() + "_" + randomStr).substring(0, 20);

        SessionHelper.getInstance().lock(bankId, randomStr);
        SessionHelper.getInstance().openSession();
        Currency accountCurrency = (currency == null) ? bankInfo.getDefaultCurrency() : currency;
        AccountInfo account = AccountManager.getInstance().saveAccount(null, randomStr, bankInfo, subCasinoId, nickName, true, false,
                null, ClientType.FLASH, null, null, accountCurrency, null, true);
        return new AccountInfoAndSessionInfoPair(account);
    }

    /**
     * Prepares and makes redirect to start game for usual games (slots, table, ..). Not used for MQ games.
     * @param mapping ActionMapping
     * @param request request
     * @param actionForm action form from start action
     * @param gameId gameId
     * @param host host
     * @param sessionId player sessionId
     * @param startGamePage startGamePage (usually it is launch.jsp)
     * @param mode game mode (FREE|REAL|BONUS)
     * @param lang language
     * @param serverId serverId
     * @param bankInfo bank info for player
     * @param currency currency of player
     * @param showRedirectedUnfinishedGameMessage true, if needed to show message for unfinished game.
     * @param transactionData transaction data for player
     * @return {@code ActionRedirect} data for redirection.
     */
    protected ActionRedirect getForward(ActionMapping mapping,
                                        HttpServletRequest request,
                                        T actionForm,
                                        long gameId,
                                        String host,
                                        String sessionId,
                                        String startGamePage,
                                        GameMode mode,
                                        String lang,
                                        long serverId,
                                        BankInfo bankInfo,
                                        String currency,
                                        boolean showRedirectedUnfinishedGameMessage,
                                        ITransactionData transactionData) {
        String url = "/" + mode.getModePath() + "/" + lang + "/" + startGamePage;
        ActionRedirect redirect = BaseAction.getActionRedirect(request, url);
        redirect.addParameter(BaseAction.BANK_ID_ATTRIBUTE, String.valueOf(actionForm.getBankId()));
        redirect.addParameter(BaseAction.SESSION_ID_ATTRIBUTE, sessionId);
        redirect.addParameter(BaseAction.GAME_ID_ATTRIBUTE, String.valueOf(gameId));
        String gameServerURL = "games" + gameServerConfiguration.getGsDomain(); // Like as games-gp3.discreetgaming.com
        gameServerURL = replaceHostForBank(bankInfo, gameServerURL);
        redirect.addParameter(BaseAction.GAMESERVER_URL_ATTRIBUTE, gameServerURL);
        redirect.addParameter(BaseAction.GAMESERVERID_ATTRIBUTE, String.valueOf(serverId));
        redirect.addParameter(BaseAction.LANG_ID_ATTRIBUTE, lang);
        if (showRedirectedUnfinishedGameMessage) {
            redirect.addParameter(BaseAction.SHOW_REDIRECTED_UNFINISHED_GAME_MESSAGE, Boolean.TRUE);
        }

        String helpUrl = request.getParameter(BaseAction.HELP_URL);
        if (!StringUtils.isTrimmedEmpty(helpUrl)) {
            redirect.addParameter(BaseAction.HELP_URL, helpUrl);
        }

        String realGameUrl = request.getParameter(BaseAction.REAL_GAME_URL);
        if (!StringUtils.isTrimmedEmpty(realGameUrl) && mode.equals(GameMode.FREE)) {
            redirect.addParameter(BaseAction.REAL_GAME_URL, realGameUrl);
        }
        String fpath = request.getParameter(BaseAction.PARAM_SWF_PATH);
        if (fpath != null) {
            redirect.addParameter(BaseAction.PARAM_SWF_PATH, fpath);
        }
        IBaseGameInfo gameInfo = BaseGameCache.getInstance().getGameInfoById(bankInfo.getId(), gameId,
                currency == null ? null : CurrencyCache.getInstance().get(currency));
        if ((mode == GameMode.REAL && !isTrimmedEmpty(currency)) ) {
            AccountInfo account = transactionData.getAccount();
            GameSession gameSession = transactionData.getGameSession();
        }
        GameSession gameSession = transactionData.getGameSession();
        String platform = request.getParameter("platform");
        boolean isForceHtml5 = "html5".equalsIgnoreCase(platform);
        if (isForceHtml5) {
            redirect.addParameter(BaseAction.PLATFORM, "html5");
            gameSession.setEndTime(-1);
        }
        Long bonusId = gameSession.getBonusId();
        if (bonusId != null) {
            redirect.addParameter(BaseAction.PARAM_BONUS_ID, bonusId);
        }
        String homeUrl = request.getParameter(BaseAction.PARAM_HOME_URL);
        if (!StringUtils.isTrimmedEmpty(homeUrl)) {
            redirect.addParameter(BaseAction.PARAM_HOME_URL, homeUrl);
        }
        String cachierUrl = request.getParameter(BaseAction.PARAM_CASHIER_URL);
        if (!StringUtils.isTrimmedEmpty(cachierUrl)) {
            redirect.addParameter(BaseAction.PARAM_CASHIER_URL, cachierUrl);
        }
        if (bankInfo.isInGameHistoryEnabled()) {
            String gameHistoryUrl = getGameHistoryUrl(request, actionForm, sessionId, bankInfo, gameId, lang);
            if (!isTrimmedEmpty(gameHistoryUrl)) {
                redirect.addParameter(BaseAction.GAME_HISTORY_URL, gameHistoryUrl);
            }
        }
        Long frbonusId = gameSession.getFrbonusId();
        if (frbonusId != null) {
            redirect.addParameter(BaseAction.PARAM_FRBONUS_ID, frbonusId);
        }

        boolean hasPromoCampaign = gameSession.hasPromoCampaign();
        if (hasPromoCampaign) {
            redirect.addParameter(PROMO_IDS, getPromoIdsString(gameSession));

            IPromoCampaignManager promoCampaignManager = GameServerComponentsHelper.getPromoCampaignManager();
            List<IPromoCampaign> promoCampaigns = new ArrayList<>(gameSession.getPromoCampaignIds().size());
            for (Long promoId : gameSession.getPromoCampaignIds()) {
                promoCampaigns.add(promoCampaignManager.getPromoCampaign(promoId));
            }

            String promoDetailsURL = getPromoDetailsURL(bankInfo.getId(), promoCampaigns);
            if (!isTrimmedEmpty(promoDetailsURL)) {
                redirect.addParameter(PROMO_DETAILS_URL, promoDetailsURL);
            }

            redirect.addParameter(SHOW_PROMO_BAR, needToShowPromoBar(promoCampaigns));
        }

        String sound = request.getParameter(BaseAction.PARAM_SOUND);
        if (!StringUtils.isTrimmedEmpty(sound)) {
            redirect.addParameter(BaseAction.PARAM_SOUND, sound);
        }
        String cdn = request.getParameter(BaseAction.KEY_CDN);
        if (cdn != null) {
            redirect.addParameter(BaseAction.KEY_CDN, cdn);
        }
        String keepAliveURL = request.getParameter(BaseAction.PARAM_KEEPALIVE_URL);
        if (!isTrimmedEmpty(keepAliveURL)) {
            redirect.addParameter(BaseAction.PARAM_KEEPALIVE_URL, keepAliveURL);
        }
        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            if (entry.getKey().startsWith(UNIVERSAL_GAME_ENGINE_PARAMS_PREFIX)) {
                for (String value : entry.getValue()) {
                    redirect.addParameter(entry.getKey(), value);
                }
            }
        }
        BaseGameInfoTemplate template = BaseGameInfoTemplateCache.getInstance().getBaseGameInfoTemplateById(gameId);
        String userAgent = request.getHeader("user-agent");
        String realStartGamePage = getStartGamePage(bankInfo, mode, template, userAgent, platform);
        addStartGamePageParameter(redirect, realStartGamePage);
        return redirect;
    }

    protected void addStartGamePageParameter(ActionRedirect redirect, String startGamePagec) {
        try {
            String encoded = URLEncoder.encode(startGamePagec, "UTF-8");
            redirect.addParameter(BaseAction.PARAM_SHELL_PATH, encoded);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    protected String getStartGamePage(BankInfo bankInfo, GameMode mode, BaseGameInfoTemplate template, String userAgent,
                                      String platform) {
        return ShellDetector.getShellPath(bankInfo, mode, template, userAgent, platform, false);
    }

    /**
     * Change host name if it specified in bankInfo.
     *
     * @return original or replaced host
     */
    public String replaceHostForBank(BankInfo bankInfo, String host) {
        if (bankInfo != null) {
            String gameServerDomain = bankInfo.getGameServerDomain();
            if (!StringUtils.isTrimmedEmpty(gameServerDomain)) {
                String newHost = gameServerDomain.trim();
                getLog().debug("Host replaced to: " + newHost + " by GameServerDomain bank property.");
                return newHost;

            } else if (bankInfo.isReplaceEndServerName() && host.endsWith(bankInfo.getReplaceEndServerFrom())) {
                String serverNameReplace = bankInfo.getReplaceEndServerFrom();
                String newHost = host.replaceFirst(serverNameReplace, bankInfo.getReplaceEndServerTo());
                getLog().debug("Host replaced to: " + newHost + ". End of host has been replaced.");
                return newHost;
            }
        }
        return host;
    }

    public static String getPromoIdsString(GameSession gameSession) {
        return Joiner.on(BaseAction.PROMO_IDS_DELIMITER).join(gameSession.getPromoCampaignIds());
    }

    public static String getPromoDetailsURL(long bankId, Iterable<IPromoCampaign> promoCampaigns) {
        for (IPromoCampaign promoCampaign : promoCampaigns) {
            String promoDetailsUrl = promoCampaign.getPromoDetailURL(bankId);
            if (!isTrimmedEmpty(promoDetailsUrl)) {
                return promoDetailsUrl;
            }
        }
        return null;
    }

    public static boolean needToShowPromoBar(Iterable<IPromoCampaign> promoCampaigns) {
        for (IPromoCampaign promoCampaign : promoCampaigns) {
            for (IPrize prize : promoCampaign.getPrizePool()) {
                if (!(prize instanceof TournamentPrize)) {
                    return true;
                }
            }
        }
        return false;
    }

    public String getGameHistoryUrl(HttpServletRequest request, T actionForm, String sessionId, BankInfo bankInfo, long gameId,
                                    String lang) {
        return getGameHistoryUrl(request, sessionId, bankInfo, gameId, lang);
    }

    public static String getGameHistoryUrl(HttpServletRequest request, String sessionId, BankInfo bankInfo, long gameId,
                                           String lang) {
        String gameHistoryUrl = request.getParameter(GAME_HISTORY_URL);
        if (isTrimmedEmpty(gameHistoryUrl)) {
            gameHistoryUrl = bankInfo.getGameHistoryUrl();
        }
        if (isTrimmedEmpty(gameHistoryUrl)) {
            gameHistoryUrl = GameHistoryURLBuilder
                    .create(request, bankInfo, sessionId)
                    .addBankId(bankInfo.getId())
                    .addGameId(gameId)
                    .addLang(lang)
                    .build();
        }
        return gameHistoryUrl;
    }

    public void additionalProcess(T form, HttpServletResponse response, AccountInfo accountInfo,
                                  SessionInfo sessionInfo, IBaseGameInfo gameInfo, GameMode mode,
                                  Long gameSessionId)
            throws CommonException {
        //nop by default
    }

    public void afterGameStartedProcess(T form, HttpServletResponse response, AccountInfo accountInfo,
                                        SessionInfo sessionInfo, IBaseGameInfo gameInfo, GameMode mode,
                                        Long gameSessionId)
            throws CommonException {
        //nop by default
    }

    protected boolean isMultiPlayerGame(long gameId) {
        BaseGameInfoTemplate template = BaseGameInfoTemplateCache.getInstance().getBaseGameInfoTemplateById(gameId);
        return template.isMultiplayerGame();
    }

    protected boolean isNewGamesRouteEnabled(BankInfo bankInfo, String rawGameId) {
        if (bankInfo == null || StringUtils.isTrimmedEmpty(rawGameId)) {
            return false;
        }
        String configuredGameId = getBankStringProperty(bankInfo, NEW_GAMES_ROUTE_GAME_ID_KEY);
        if (StringUtils.isTrimmedEmpty(configuredGameId)) {
            configuredGameId = NEW_GAMES_ROUTE_GAME_ID_DEFAULT;
        }

        if (configuredGameId.equals(rawGameId)) {
            return true;
        }

        try {
            return Long.parseLong(configuredGameId) == Long.parseLong(rawGameId);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    protected ActionRedirect getNewGamesForward(T actionForm, HttpServletRequest request, GameMode mode,
                                                BankInfo bankInfo, String sessionId, String lang, long gameId,
                                                String rawGameId) {
        String newGamesClientUrl = getBankStringProperty(bankInfo, NEW_GAMES_CLIENT_URL_KEY);
        if (StringUtils.isTrimmedEmpty(newGamesClientUrl)) {
            newGamesClientUrl = NEW_GAMES_CLIENT_URL_DEFAULT;
        }

        String newGamesApiUrl = getBankStringProperty(bankInfo, NEW_GAMES_API_URL_KEY);
        if (StringUtils.isTrimmedEmpty(newGamesApiUrl)) {
            newGamesApiUrl = NEW_GAMES_API_URL_DEFAULT;
        }

        String gsInternalBaseUrl = getBankStringProperty(bankInfo, NEW_GAMES_GS_INTERNAL_BASE_URL_KEY);
        if (StringUtils.isTrimmedEmpty(gsInternalBaseUrl)) {
            gsInternalBaseUrl = NEW_GAMES_GS_INTERNAL_BASE_URL_DEFAULT;
        }

        ActionRedirect redirect = new ActionRedirect(newGamesClientUrl);
        redirect.addParameter(BaseAction.BANK_ID_ATTRIBUTE, bankInfo.getId());
        redirect.addParameter(BaseAction.SESSION_ID_ATTRIBUTE, sessionId);
        redirect.addParameter(BaseAction.GAME_ID_ATTRIBUTE, StringUtils.isTrimmedEmpty(rawGameId) ?
                String.valueOf(gameId) : rawGameId);
        redirect.addParameter("gameIdNumeric", String.valueOf(gameId));
        redirect.addParameter(BaseAction.LANG_ID_ATTRIBUTE, lang);
        redirect.addParameter(BaseAction.GAMEMODE_ATTRIBUTE, mode.getModePath());
        redirect.addParameter(BaseAction.GAMESERVERID_ATTRIBUTE, GameServer.getInstance().getServerId());
        redirect.addParameter("ngsApiUrl", newGamesApiUrl);
        redirect.addParameter("gsInternalBaseUrl", gsInternalBaseUrl);
        redirect.addParameter("ngsContract", "v1");

        String homeUrl = request.getParameter(BaseAction.PARAM_HOME_URL);
        if (!isTrimmedEmpty(homeUrl)) {
            redirect.addParameter(BaseAction.PARAM_HOME_URL, homeUrl);
        }
        String cachierUrl = request.getParameter(BaseAction.PARAM_CASHIER_URL);
        if (!isTrimmedEmpty(cachierUrl)) {
            redirect.addParameter(BaseAction.PARAM_CASHIER_URL, cachierUrl);
        }
        return redirect;
    }

    protected String getBankStringProperty(BankInfo bankInfo, String key) {
        if (bankInfo == null || StringUtils.isTrimmedEmpty(key) || bankInfo.getProperties() == null) {
            return null;
        }
        return bankInfo.getProperties().get(key);
    }

    /**
     * Prepares and redirect client to MQ side for connection to MQ server
     * @param actionForm action form from start action
     * @param request request
     * @param mode game mode
     * @param bankInfo bankInfo for player bank
     * @param sessionId player sessionId
     * @param lang language of player for localization
     * @param gameId gameId
     * @return {@code ActionRedirect} redirect data for client
     */
    protected ActionRedirect getMultiPlayerForward(T actionForm, HttpServletRequest request, GameMode mode,
                                                   BankInfo bankInfo, String sessionId, String lang, long gameId) {
        String mpLobbyUrl = bankInfo.getMpLobbyWsUrl();
        if (StringUtils.isTrimmedEmpty(mpLobbyUrl)) {
            mpLobbyUrl = gameServerConfiguration.getStringPropertySilent(GameServerConfigTemplate.KEY_MP_LOBBY_WS_HOST);
        }
        if (StringUtils.isTrimmedEmpty(mpLobbyUrl)) {
            LOG.error("MP_LOBBY_WS_URL property not found for bank=" + bankInfo.getId());
            LOG.error("MP_LOBBY_WS_HOST property not found in GameServerConfiguration");
            mpLobbyUrl = "localhost:8080/";
        }

        String forwardedScheme = request.getHeader("X-Forwarded-Proto");
        String webSocketScheme = request.isSecure() || "https".equals(forwardedScheme) ? "wss" : "ws";
        if(hostConfiguration != null && hostConfiguration.isProductionCluster()) {
            webSocketScheme = "wss";
        }
        mpLobbyUrl = webSocketScheme + "://" + mpLobbyUrl ;

        String httpScheme = request.isSecure() || "https".equals(forwardedScheme) ? "https" : "http";
        if(hostConfiguration != null && hostConfiguration.isProductionCluster()) {
            httpScheme = "https";
        }
        String url = httpScheme + "://" + request.getServerName() + "/" + mode.getModePath() + "/mp/template.jsp";

        ActionRedirect redirect = new ActionRedirect(url);
        redirect.addParameter(BaseAction.BANK_ID_ATTRIBUTE, bankInfo.getId());
        redirect.addParameter(BaseAction.SESSION_ID_ATTRIBUTE, sessionId);
        redirect.addParameter(BaseAction.GAME_ID_ATTRIBUTE, String.valueOf(gameId));
        redirect.addParameter(BaseAction.LANG_ID_ATTRIBUTE, lang);
        redirect.addParameter(BaseAction.GAMEMODE_ATTRIBUTE, mode.getModePath());
        redirect.addParameter(BaseAction.WEB_SOCKET_URL, mpLobbyUrl + "/websocket/mplobby");
        redirect.addParameter(BaseAction.GAMESERVERID_ATTRIBUTE, GameServer.getInstance().getServerId());
        String cdn = request.getParameter(BaseAction.KEY_CDN);
        if (!StringUtils.isTrimmedEmpty(cdn)) {
            String serverUrl = bankInfo.getCdnUrlsMap().get(cdn);
            redirect.addParameter(BaseAction.KEY_CDN, serverUrl);
        } else if (!bankInfo.getCdnUrlsMap().isEmpty() && bankInfo.isCdnForceAuto()) {
            String serverUrl = bankInfo.getCdnUrlsMap().values().iterator().next();
            redirect.addParameter(BaseAction.KEY_CDN, serverUrl);
        }
        String homeUrl = request.getParameter(BaseAction.PARAM_HOME_URL);
        if (!isTrimmedEmpty(homeUrl)) {
            redirect.addParameter(BaseAction.PARAM_HOME_URL, homeUrl);
        }
        String cachierUrl = request.getParameter(BaseAction.PARAM_CASHIER_URL);
        if (!isTrimmedEmpty(cachierUrl)) {
            redirect.addParameter(BaseAction.PARAM_CASHIER_URL, cachierUrl);
        }
        return redirect;
    }

    protected ActionRedirect redirectTIIncompleteRoundPage(BankInfo bankInfo, long gameId, Long buyIn, String lang,
            String homeUrl, String privateRoomId, HttpServletRequest request) throws UnsupportedEncodingException {
        String fatalErrorUrl = bankInfo.getFatalErrorPageUrl();

        String incompleteRoundUrl;

        if(!StringUtils.isTrimmedEmpty(privateRoomId)) {

            LOG.debug("BaseStartGameAction redirectTIIncompleteRoundPage: privateRoomId={}, lang={}, homeUrl={}, " +
                            "request={}", privateRoomId, lang, homeUrl, request);

            incompleteRoundUrl = buildPrivateRoomLaunchUrl(lang, homeUrl, privateRoomId, request);

        } else {

            boolean isBattleGroundsMultiplayerGame = BaseGameInfoTemplateCache.getInstance()
                    .getBaseGameInfoTemplateById(gameId)
                    .isBattleGroundsMultiplayerGame();

            LOG.debug("BaseStartGameAction redirectTIIncompleteRoundPage: " +
                            "isBattleGroundsMultiplayerGame={}, gameId={}, buyIn={}, lang={}, homeUrl={}, request={}",
                    isBattleGroundsMultiplayerGame, gameId, buyIn, lang, homeUrl, request);

            incompleteRoundUrl = isBattleGroundsMultiplayerGame ?
                    buildBgLaunchUrl(bankInfo, gameId, buyIn, lang, homeUrl, request) :
                    buildCrashLaunchUrl(bankInfo, gameId, lang, homeUrl, request);
        }

        LOG.debug("BaseStartGameAction redirectTIIncompleteRoundPage: incompleteRoundUrl={}", incompleteRoundUrl);

        ActionRedirect redirect = new ActionRedirect(fatalErrorUrl);
        redirect.addParameter("gameId", gameId);
        redirect.addParameter("incompleteRoundUrl", incompleteRoundUrl);

        return redirect;
    }

    private String buildPrivateRoomLaunchUrl(String lang, String homeUrl, String privateRoomId, HttpServletRequest request) {

        return request.getScheme() + "://" + request.getServerName() + "/battlegroundstartprivategame.do?" +
                "privateRoomId=" + privateRoomId +
                (StringUtils.isTrimmedEmpty(lang) ? "" : "&lang=" + lang) +
                "&continueIncompleteRound=" + "true" +
                (StringUtils.isTrimmedEmpty(homeUrl) ? "" : "&homeUrl=" + homeUrl);
    }

    private String buildBgLaunchUrl(BankInfo bankInfo, long gameId, Long buyIn, String lang, String homeUrl, HttpServletRequest request) {

        return request.getScheme() + "://" + request.getServerName() + "/battlegroundstartgamev2.do?" +
                buildCommonLaunchMpParams(bankInfo, gameId, lang, request) +
                (buyIn == null ? "&buyIn=1" : "&buyIn=" + buyIn) +
                "&continueIncompleteRound=" + "true" +
                (StringUtils.isTrimmedEmpty(homeUrl) ? "" : "&homeUrl=" + homeUrl);
    }

    private String buildCrashLaunchUrl(BankInfo bankInfo, long gameId, String lang, String homeUrl, HttpServletRequest request) {

        return request.getScheme() + "://" + request.getServerName() + "/cwstartgamev2.do?" +
                buildCommonLaunchMpParams(bankInfo, gameId, lang, request) +
                (StringUtils.isTrimmedEmpty(homeUrl) ? "" : "&homeUrl=" + homeUrl);
    }

    private String buildCommonLaunchMpParams(BankInfo bankInfo, long gameId, String lang, HttpServletRequest request) {
        String cdn = getCdn(bankInfo, request);
        String cachierUrl = request.getParameter(BaseAction.PARAM_CASHIER_URL);
        return  "&bankId=" + bankInfo.getId() +
                "&gameId=" + gameId +
                "&MODE=" + GameMode.REAL.getModePath() +
                (StringUtils.isTrimmedEmpty(lang) ? "" : "&lang=" + lang) +
                (StringUtils.isTrimmedEmpty(cdn) ? "" : "&CDN=" + cdn) +
                (StringUtils.isTrimmedEmpty(cachierUrl) ? "" : "&cachierUrl=" + cachierUrl);
    }

    private String getCdn(BankInfo bankInfo, HttpServletRequest request) {
        String cdn = request.getParameter(BaseAction.KEY_CDN);
        if (!StringUtils.isTrimmedEmpty(cdn)) {
            return bankInfo.getCdnUrlsMap().get(cdn);
        } else if (!bankInfo.getCdnUrlsMap().isEmpty() && bankInfo.isCdnForceAuto()) {
            return bankInfo.getCdnUrlsMap().values().iterator().next();
        }
        return "";
    }

    protected void validateMpPass(HttpServletRequest request, Integer bankId) throws MaintenanceModeException {
        if (bankId != null && BANKS_WITHOUT_MP_VALIDATION.contains(bankId)) {
            return;
        }
        String expectedPass = gameServerConfiguration.getStringPropertySilent("maxquestpass");
        if (StringUtils.isTrimmedEmpty(expectedPass)) {
            return;
        }
        String pass = request.getParameter("pass");
        Set<String> passes = Arrays.stream(expectedPass.split(";"))
                .collect(Collectors.toSet());
        if (!passes.contains(pass)) {
            throw new MaintenanceModeException();
        }
    }

    @Override
    protected void persistError(HttpServletRequest request, Exception exception, long exceptionTime) {
        errorPersisterHelper.persistStartGameActionError(request, exception, exceptionTime);
    }

    protected void checkAvailableGameInfo(long gameId, BankInfo bankInfo, String currencyCode) throws CommonException {
        IBaseGameInfo info = BaseGameCache.getInstance().getGameInfo(bankInfo.getId(), gameId, currencyCode);
        if (info == null || !info.isEnabled()) {
            LOG.error("Game {} is not available for bank {}", gameId, bankInfo.getId());
            throw new CommonException("Game is not available");
        }
    }

}
