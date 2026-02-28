package com.abs.casino.actions.game;

import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.cassandra.persist.CassandraExtendedAccountInfoPersister;
import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.BaseGameCache;
import com.dgphoenix.casino.common.cache.BaseGameInfoTemplateCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.currency.Currency;
import com.abs.casino.common.cache.data.currency.ICurrency;
import com.dgphoenix.casino.common.cache.data.game.BaseGameInfoTemplate;
import com.dgphoenix.casino.common.cache.data.game.GameMode;
import com.dgphoenix.casino.common.cache.data.game.GameType;
import com.dgphoenix.casino.common.cache.data.game.IBaseGameInfo;
import com.dgphoenix.casino.common.cache.data.payment.bonus.FRBonusNotification;
import com.abs.casino.common.cache.data.payment.frb.IFRBonusWin;
import com.dgphoenix.casino.common.cache.data.server.ServerInfo;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.dgphoenix.casino.common.config.GameServerConfigTemplate;
import com.abs.casino.common.config.HostConfiguration;
import com.dgphoenix.casino.common.exception.CommonException;
import com.abs.casino.common.exception.FRBException;
import com.abs.casino.common.exception.MaintenanceModeException;
import com.dgphoenix.casino.common.promo.IPromoCampaign;
import com.dgphoenix.casino.common.promo.IPromoCampaignManager;
import com.dgphoenix.casino.common.transactiondata.ITransactionData;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.abs.casino.common.web.BaseAction;
import com.abs.casino.common.web.ShellDetector;
import com.abs.casino.common.web.login.apub.APUBConstants;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import com.abs.casino.entities.game.requests.StartGameRequest;
import com.abs.casino.entities.game.requests.StartGameResponse;
import com.abs.casino.exceptions.LoginErrorException;
import com.abs.casino.forms.game.CommonStartGameForm;
import com.dgphoenix.casino.gs.GameServer;
import com.dgphoenix.casino.gs.GameServerComponentsHelper;
import com.abs.casino.gs.managers.game.StartGameSessionHelper;
import com.dgphoenix.casino.gs.managers.payment.bonus.FRBonusManager;
import com.abs.casino.gs.managers.payment.bonus.FRBonusNotificationManager;
import com.abs.casino.gs.managers.payment.transfer.PaymentManager;
import com.dgphoenix.casino.gs.managers.payment.wallet.WalletProtocolFactory;
import com.abs.casino.gs.persistance.GameSessionPersister;
import com.abs.casino.helpers.login.LoginHelper;
import com.abs.casino.sm.login.LoginRequest;
import com.abs.casino.sm.login.LoginResponse;
import com.abs.casino.support.ErrorPersisterHelper;
import com.dgphoenix.casino.system.configuration.GameServerConfiguration;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.dgphoenix.casino.common.util.string.StringUtils.isTrimmedEmpty;

/**
 * User: isirbis
 * Date: 03.10.14
 */
public abstract class BaseStartGameAction<F extends CommonStartGameForm, L extends LoginRequest,
        R extends StartGameRequest>
        extends BaseAction<F> implements APUBConstants {
    protected static final Logger LOG = LogManager.getLogger(BaseStartGameAction.class);

    private final CassandraExtendedAccountInfoPersister extendedAccountInfoPersister;
    protected final HostConfiguration hostConfiguration;
    protected final ErrorPersisterHelper errorPersisterHelper;
    protected final GameServerConfiguration gameServerConfiguration;

    public BaseStartGameAction() {
        ApplicationContext applicationContext = ApplicationContextHelper.getApplicationContext();
        CassandraPersistenceManager persistenceManager = applicationContext.getBean("persistenceManager", CassandraPersistenceManager.class);
        gameServerConfiguration = applicationContext.getBean("gameServerConfiguration", GameServerConfiguration.class);
        extendedAccountInfoPersister = persistenceManager.getPersister(CassandraExtendedAccountInfoPersister.class);
        errorPersisterHelper = applicationContext.getBean(ErrorPersisterHelper.class);
        hostConfiguration = applicationContext.getBean(HostConfiguration.class);
    }

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request,
                                 HttpServletResponse response) throws Exception {
        F form = (F) actionForm;
        if (isTournamentLobbyRequest(form)) {
            String tournamentLobbyUrl = "/tournamentlobby.do?" + request.getQueryString();
            return new ActionForward(tournamentLobbyUrl);
        }
        return super.execute(mapping, actionForm, request, response);
    }

    protected boolean isTournamentLobbyRequest(F actionForm) {
        return actionForm.getGameId() <= 1;
    }

    protected LoginResponse login(L loginRequest) throws LoginErrorException {
        LoginHelper loginHelper = getLoginHelper();
        return loginHelper.login(loginRequest);
    }

    protected StartGameResponse prepareMQStartGame(R startGameRequest, HttpServletResponse response) throws CommonException {
        SessionInfo sessionInfo = startGameRequest.getSessionInfo();
        AccountInfo accountInfo = startGameRequest.getAccountInfo();
        Integer bankId = startGameRequest.getBankId();
        Integer gameId = startGameRequest.getGameId();
        GameMode gameMode = startGameRequest.getGameMode();
        Boolean checkWalletOps = startGameRequest.getCheckWalletOps();
        String language = startGameRequest.getLanguage();

        String sessionId = sessionInfo.getSessionId();
        long accountId = accountInfo.getId();

        long now = System.currentTimeMillis();
        StartGameResponse startGameResponse = new StartGameResponse();
        try {
            long now1 = System.currentTimeMillis();

            Currency currency = accountInfo.getCurrencyFraction() == null ?
                    accountInfo.getCurrency() : accountInfo.getCurrencyFraction();
            IBaseGameInfo gameInfo = getGameInfo(bankId, gameId, currency, startGameRequest.getProfileId());
            startGameResponse.setGameInfo(gameInfo);
            Long bonusId = validateBonusIdParam(startGameRequest, gameMode, accountInfo);
            LOG.debug("prepareMQStartGame: starting game for accountId:" + accountId +
                    " sessionId:" + sessionId + ", " + "bonusId=" + bonusId + ", mode=" + gameMode + ", gameId=" + gameId +
                    ", notGameFRB=" + startGameRequest.isNotGameFRB());
            if (bonusId == null) {
                FRBonusManager.getInstance().checkMassAwardsForAccount(accountInfo);
                bonusId = FRBonusManager.getInstance().getEarlestActiveFRBonusId(accountId, gameInfo.getId());
                if (startGameRequest.isNotGameFRB() && bonusId != null && gameMode.equals(GameMode.REAL)) {
                    LOG.info("prepareMQStartGame: Choice Real Mode Game with Not FRBonus Id:" + bonusId);
                    bonusId = null;
                }
                if (bonusId != null) {
                    LOG.info("prepareMQStartGame: Earlest FRBonus Id:" + bonusId);
                }
            }
            StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() +
                    ":prepareMQStartGame: 1", System.currentTimeMillis() - now1, accountId);
            startGameResponse.setBonusId(bonusId);
            //Long gameSessionId = getPredefinedGameSessionId(startGameRequest);
            now1 = System.currentTimeMillis();
            Long unclosedGameSessionId = sessionInfo.getGameSessionId();
            if (unclosedGameSessionId != null) {
                //closeOnlineGame(unclosedGameSessionId, accountInfo, sessionInfo);
            }
            StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() +
                    ":prepareMQStartGame: 2", System.currentTimeMillis() - now1, accountId);
            now1 = System.currentTimeMillis();
            if (sessionInfo.getGameSessionId() != null) {
                LOG.warn("prepareMQStartGame: starting game for accountId:" + accountId
                        + " sessionId:" + sessionId +
                        " player tries to start new game session not closing old one, " +
                        "old gameSessionId:" + unclosedGameSessionId +
                        " new gameSessionId:" + sessionInfo.getGameSessionId() + "^^^^");
                // throw new CommonException("failed to start new game session, need to close previous");
            }

            checkPendingOperations(accountInfo, gameId, sessionInfo, unclosedGameSessionId, gameMode, checkWalletOps);

            StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() +
                    ":prepareMQStartGame:  3", System.currentTimeMillis() - now1, accountId);

            GameServer.getInstance().checkMaintenanceMode(gameMode, language, accountInfo, gameId);

            //additionalProcess() - typically make deposit request, this is not required for MQ/CT
            //perform deposit on sitIn/startGameSession
            //startGameRequest.getStartGameProcessor().additionalProcess(startGameRequest, response, accountInfo,
            //        sessionInfo, gameInfo, gameMode, gameSessionId);
        } catch (Throwable e) {
            if (e instanceof CommonException) {
                throw (CommonException) e;
            }
            throw new CommonException("Unexpected error", e);
        } finally {
            StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() + ":prepareMQStartGame: ",
                    System.currentTimeMillis() - now);
        }
        return startGameResponse;
    }

    protected StartGameResponse startGame(R startGameRequest, HttpServletResponse response) throws CommonException {
        SessionInfo sessionInfo = startGameRequest.getSessionInfo();
        AccountInfo accountInfo = startGameRequest.getAccountInfo();
        Integer bankId = startGameRequest.getBankId();
        Integer gameId = startGameRequest.getGameId();
        GameMode gameMode = startGameRequest.getGameMode();
        Boolean checkWalletOps = startGameRequest.getCheckWalletOps();
        String language = startGameRequest.getLanguage();

        String sessionId = sessionInfo.getSessionId();
        long accountId = accountInfo.getId();

        long now = System.currentTimeMillis();
        StartGameResponse startGameResponse = new StartGameResponse();
        try {
            long now1 = System.currentTimeMillis();

            Currency currency = accountInfo.getCurrencyFraction() == null ? accountInfo.getCurrency() : accountInfo.getCurrencyFraction();
            IBaseGameInfo gameInfo = getGameInfo(bankId, gameId, currency, startGameRequest.getProfileId());
            Long bonusId = validateBonusIdParam(startGameRequest, gameMode, accountInfo);
            LOG.debug("starting game for accountId:" + accountId + " sessionId:" + sessionId + ", " +
                    "bonusId=" + bonusId + ", mode=" + gameMode + ", gameId=" + gameId +
                    ", notGameFRB=" + startGameRequest.isNotGameFRB());
            if (bonusId == null) {
                FRBonusManager.getInstance().checkMassAwardsForAccount(accountInfo);
                bonusId = FRBonusManager.getInstance().getEarlestActiveFRBonusId(accountId, gameInfo.getId());
                if (startGameRequest.isNotGameFRB() && bonusId != null && gameMode.equals(GameMode.REAL)) {
                    LOG.info("Choice Real Mode Game with Not FRBonus Id:" + bonusId);
                    bonusId = null;
                }
                if (bonusId != null) {
                    LOG.info("Earlest FRBonus Id:" + bonusId);
                }
            }
            Long gameSessionId = getPredefinedGameSessionId(startGameRequest);
            StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() +
                    ":startGame 1", System.currentTimeMillis() - now1, accountId);
            if (LOG.isDebugEnabled()) {
                LOG.debug("starting game for accountId:" + accountId + ", sessionInfo:" + sessionInfo);
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
                LOG.warn("starting game for accountId:" + accountId
                        + " sessionId:" + sessionId +
                        " player tries to start new game session not closing old one, " +
                        "old gameSessionId:" + unclosedGameSessionId +
                        " new gameSessionId:" + sessionInfo.getGameSessionId() + "^^^^");
                GameSession gameSession = SessionHelper.getInstance().getTransactionData().getGameSession();
                if (gameSession != null) {
                    LOG.debug("Found unclosed gameSession after closeGame, gameSession={}", gameSession);
                    throw new CommonException("failed to start new game session, need to close previous");
                } else {
                    LOG.error("Found not null sessionInfo.getGameSessionId() after closeGame, please fix!!!");
                }
            }

            checkPendingOperations(accountInfo, gameId, sessionInfo, unclosedGameSessionId, gameMode, checkWalletOps);

            StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() +
                    ":startGame 4", System.currentTimeMillis() - now1, accountId);
            now1 = System.currentTimeMillis();

            GameServer.getInstance().checkMaintenanceMode(gameMode, language, accountInfo, gameId);

            startGameRequest.getStartGameProcessor().additionalProcess(startGameRequest, response, accountInfo,
                    sessionInfo, gameInfo, gameMode, gameSessionId);

            gameSessionId = GameServer.getInstance().startGame(sessionInfo, gameInfo, gameSessionId, gameMode,
                    bonusId, language, accountInfo);

            StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() + ":startGame 5",
                    System.currentTimeMillis() - now1);
            startGameRequest.getStartGameProcessor().afterGameStartedProcess(startGameRequest, response, accountInfo,
                    sessionInfo, gameInfo, gameMode, gameSessionId);

            startGameResponse.setStatus(STATUS_SUCCESS);
            startGameResponse.setGameSessionId(gameSessionId);
            if (LOG.isDebugEnabled()) {
                LOG.debug("starting game for accountId:" + accountId + " sessionId:" + sessionId + " was OK");
            }
        } catch (Throwable e) {
            if (e instanceof CommonException) {
                throw (CommonException) e;
            }
            throw new CommonException("Unexpected error", e);
        } finally {
            StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() + ":startGame",
                    System.currentTimeMillis() - now);
        }
        return startGameResponse;
    }

    protected void checkPendingOperations(AccountInfo accountInfo, int gameId, SessionInfo sessionInfo,
                                          Long unclosedGameSessionId, GameMode gameMode, boolean checkWalletOps)
            throws CommonException {
        String sessionId = sessionInfo.getSessionId();
        long accountId = accountInfo.getId();
        if (!WalletProtocolFactory.getInstance().isWalletBank(accountInfo.getBankId())) {
            Long transactionId = PaymentManager.getInstance().getTrackingTransactionId();
            if (transactionId != null && gameMode == GameMode.REAL) {
                LOG.warn("starting game for accountId:" + accountId
                        + " sessionId:" + sessionId +
                        " player tries to start new game session not closing old one, " +
                        "old gameSessionId:" + unclosedGameSessionId +
                        " new gameSessionId:" + sessionInfo.getGameSessionId() +
                        " player has unfinished transaction, transactionId:" + transactionId + "^^^^");
                throw new CommonException("failed to start new game session, need to close transaction");
            }
        } else {
            if (checkWalletOps) {
                BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(accountInfo.getBankId());
                StartGameSessionHelper.checkWalletOperations(gameId, gameMode, accountInfo, bankInfo);
            }
        }
        final ITransactionData ITransactionData = SessionHelper.getInstance().getTransactionData();
        if (ITransactionData != null) {
            if (GameMode.REAL == gameMode) {//do not start the same frb game if win in tracking
                IFRBonusWin frbonusWin = ITransactionData.getFrbWin();
                FRBonusManager.getInstance().checkPendingOperation(frbonusWin, accountInfo, gameId);
            }
            FRBonusNotification frbonusNotification = ITransactionData.getFrbNotification();
            if (frbonusNotification != null &&
                    FRBonusNotificationManager.getInstance().isLaunchPrevented(frbonusNotification)) {
                throw new FRBException("FRB previous notification is not completed: " + frbonusNotification);
            }
        }
    }

    protected IBaseGameInfo getGameInfo(int bankId, int gameId, Currency currency, String profileId)
            throws CommonException {
        IBaseGameInfo gameInfo;
        if (!isTrimmedEmpty(profileId)) {
            gameInfo = BaseGameCache.getInstance().getGameInfoByIdProfiled(bankId, gameId, currency, profileId);
        } else {
            gameInfo = BaseGameCache.getInstance().getGameInfoById(bankId, gameId, currency);
        }

        if (gameInfo == null) {
            throw new CommonException("game is not defined, id=" + gameId);
        }
        if (!gameInfo.isEnabled()) {
            throw new CommonException("game is not enabled, id=" + gameId);
        }
        return gameInfo;
    }

    private void closeOnlineGame(long gameSessionId, AccountInfo accountInfo, SessionInfo sessionInfo)
            throws CommonException {
        GameSession gameSession = GameSessionPersister.getInstance().getGameSession(gameSessionId);
        LOG.debug("closeOnlineGame: need closeOnlineGame, gameSessionId: {}", gameSessionId);
        GameServer.getInstance().closeOnlineGame(accountInfo, sessionInfo, gameSession, false,
                false);
    }

    protected Long validateBonusIdParam(R startGameRequest, GameMode mode, AccountInfo accountInfo) throws CommonException {
        return null;
    }

    protected Long getPredefinedGameSessionId(R startGameRequest) {
        return null;
    }

    protected ActionRedirect getForward(ActionMapping mapping, HttpServletRequest request, F actionForm, int gameId,
                                        String host, String sessionId, String startGamePage, GameMode mode, String lang,
                                        short serverId, StartGameResponse startGameResponse, BankInfo bankInfo,
                                        ITransactionData transactionData)
            throws CommonException {

        String url = getUrlForward(request, mode, lang, startGamePage);
        //LOG.info("getForward: url=" + url);
        ActionRedirect redirect = new ActionRedirect(url);

        redirect.addParameter(BaseAction.BANK_ID_ATTRIBUTE, String.valueOf(actionForm.getBankId()));
        redirect.addParameter(BaseAction.SESSION_ID_ATTRIBUTE, sessionId);
        redirect.addParameter(BaseAction.GAME_ID_ATTRIBUTE, String.valueOf(gameId));
        redirect.addParameter(BaseAction.GAMESERVER_URL_ATTRIBUTE, host);
        redirect.addParameter(BaseAction.GAMESERVERID_ATTRIBUTE, String.valueOf(serverId));
        redirect.addParameter(BaseAction.LANG_ID_ATTRIBUTE, lang);

        String playerCurrencyCode = transactionData.getAccount().getCurrency().getCode();
        String helpUrl = request.getParameter(BaseAction.HELP_URL);
        if (!StringUtils.isTrimmedEmpty(helpUrl)) {
            redirect.addParameter(BaseAction.HELP_URL, helpUrl);
        }

        String realGameUrl = request.getParameter(BaseAction.REAL_GAME_URL);
        if (!StringUtils.isTrimmedEmpty(realGameUrl) && mode.equals(GameMode.FREE)) {
            redirect.addParameter(BaseAction.REAL_GAME_URL, realGameUrl);
        }
        String CDN_URL = request.getParameter(BaseAction.KEY_CDN);
        if (CDN_URL != null) {
            redirect.addParameter(BaseAction.KEY_CDN, CDN_URL);
        }
        String homeUrl = request.getParameter(BaseAction.PARAM_HOME_URL);
        if (!isTrimmedEmpty(homeUrl)) {
            redirect.addParameter(BaseAction.PARAM_HOME_URL, homeUrl);
        }
        String cachierUrl = request.getParameter(BaseAction.PARAM_CASHIER_URL);
        if (!isTrimmedEmpty(cachierUrl)) {
            redirect.addParameter(BaseAction.PARAM_CASHIER_URL, cachierUrl);
        }
        if (bankInfo.isInGameHistoryEnabled()) {
            String gameHistoryUrl = com.abs.casino.actions.enter.game.BaseStartGameAction
                    .getGameHistoryUrl(request, sessionId, bankInfo, gameId, lang);
            if (!isTrimmedEmpty(gameHistoryUrl)) {
                redirect.addParameter(BaseAction.GAME_HISTORY_URL, gameHistoryUrl);
            }
        }
        String isStandalone = request.getParameter(BaseAction.STANDALONE);
        if (!isTrimmedEmpty(isStandalone)) {
            redirect.addParameter(BaseAction.STANDALONE, isStandalone);
        }
        GameSession gameSession = transactionData.getGameSession();

        boolean hasPromoCampaign = gameSession.hasPromoCampaign();
        if (hasPromoCampaign) {
            String promoIdsString = com.abs.casino.actions.enter.game.BaseStartGameAction
                    .getPromoIdsString(gameSession);
            redirect.addParameter(PROMO_IDS, promoIdsString);

            IPromoCampaignManager promoCampaignManager = GameServerComponentsHelper.getPromoCampaignManager();
            List<IPromoCampaign> promoCampaigns = new ArrayList<>(gameSession.getPromoCampaignIds().size());
            for (Long promoId : gameSession.getPromoCampaignIds()) {
                promoCampaigns.add(promoCampaignManager.getPromoCampaign(promoId));
            }

            String promoDetailsURL = com.abs.casino.actions.enter.game.BaseStartGameAction
                    .getPromoDetailsURL(bankInfo.getId(), promoCampaigns);
            if (!isTrimmedEmpty(promoDetailsURL)) {
                redirect.addParameter(PROMO_DETAILS_URL, promoDetailsURL);
            }

            redirect.addParameter(SHOW_PROMO_BAR,
                    com.abs.casino.actions.enter.game.BaseStartGameAction.needToShowPromoBar(promoCampaigns));
        }

        //start
        String fpath = request.getParameter(BaseAction.PARAM_SWF_PATH);
        if (fpath != null) {
            redirect.addParameter(BaseAction.PARAM_SWF_PATH, fpath);
        }
        //end
        if (startGameResponse != null) {
            ICurrency currency = null;
            AccountInfo account = transactionData.getAccount();
            if (account != null) {
                currency = account.getCurrency();
            }
            IBaseGameInfo gameInfo = BaseGameCache.getInstance().getGameInfoById(bankInfo.getId(), gameId, currency);
        }

        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            if (entry.getKey().startsWith(UNIVERSAL_GAME_ENGINE_PARAMS_PREFIX)) {
                for (String value : entry.getValue()) {
                    redirect.addParameter(entry.getKey(), value);
                }
            }
        }

        String userAgent = request.getHeader("user-agent");
        String platform = request.getParameter("platform");
        boolean isForceHtml5 = !isTrimmedEmpty(platform) && "html5".equalsIgnoreCase(platform);
        if (isForceHtml5) {
            redirect.addParameter(BaseAction.PLATFORM, "html5");
        }
        boolean isForceFlash = !isTrimmedEmpty(platform) && "flash".equalsIgnoreCase(platform);
        if (isForceFlash) {
            redirect.addParameter(BaseAction.PLATFORM, "flash");
        }
        BaseGameInfoTemplate template = BaseGameInfoTemplateCache.getInstance().getBaseGameInfoTemplateById(gameId);
        String realStartGamePage = getStartGamePage(bankInfo, mode, template, userAgent, platform);

        addStartGamePageParameter(redirect, realStartGamePage);
        return redirect;
    }

    protected void addStartGamePageParameter(ActionRedirect redirect, String path) {
        try {
            String encoded = URLEncoder.encode(path, "UTF-8");
            redirect.addParameter(BaseAction.PARAM_SHELL_PATH, encoded);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    protected String getStartGamePage(BankInfo bankInfo, GameMode mode, BaseGameInfoTemplate template, String userAgent,
                                      String platform) {
        return ShellDetector.getShellPath(bankInfo, mode, template, userAgent, platform, false);
    }

    protected String getServerName(HttpServletRequest request) {
        return request.getScheme() + "://" + request.getServerName();
    }

    protected String getUrlForward(HttpServletRequest request, GameMode mode, String lang, String startGamePage) {
        return getServerName(request) + "/" + mode.getModePath() + "/" + lang + "/" + startGamePage;
    }

    protected void saveReferer(HttpServletRequest request, AccountInfo accountInfo) {
        String referer = request.getHeader("Referer");
        if (!isTrimmedEmpty(referer)) {
            extendedAccountInfoPersister.persist(accountInfo.getBankId(), accountInfo.getExternalId(), "REFERER", referer);
        }
    }

    protected ServerInfo assignServer(Integer bankId, int gameId, GameMode mode) {
        return GameServer.getInstance().getServerInfo();
    }

    protected abstract LoginHelper getLoginHelper();

    protected boolean isMultiPlayerGame(long gameId) throws CommonException {
        BaseGameInfoTemplate template = BaseGameInfoTemplateCache.getInstance().getBaseGameInfoTemplateById(gameId);
        return GameType.MP.equals(template.getGameType());
    }

    protected ActionRedirect getMultiPlayerForward(HttpServletRequest request, GameMode mode, BankInfo bankInfo,
                                                   String sessionId, String lang, boolean validateNotFRBStartGame,
                                                   long gameId) {

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

        redirect.addParameter("noFRB", validateNotFRBStartGame);

        return redirect;
    }

    protected void validateMpPass(HttpServletRequest request) throws MaintenanceModeException {
        String pass = request.getParameter("pass");
        String expectedPass = gameServerConfiguration.getStringPropertySilent("maxquestpass");
        if (!StringUtils.isTrimmedEmpty(expectedPass) && !expectedPass.equals(pass)) {
            throw new MaintenanceModeException();
        }
    }

    @Override
    protected void persistError(HttpServletRequest request, Exception exception, long exceptionTime) {
        errorPersisterHelper.persistStartGameActionError(request, exception, exceptionTime);
    }
}
