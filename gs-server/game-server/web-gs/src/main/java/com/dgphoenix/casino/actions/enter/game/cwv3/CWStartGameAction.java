package com.dgphoenix.casino.actions.enter.game.cwv3;

import com.dgphoenix.casino.account.AccountManager;
import com.dgphoenix.casino.actions.enter.AccountInfoAndSessionInfoPair;
import com.dgphoenix.casino.actions.enter.LanguageDetector;
import com.dgphoenix.casino.actions.enter.game.BaseStartGameAction;
import com.abs.casino.actions.enter.game.routing.GameplayOrchestratorRoutingBridge;
import com.abs.casino.actions.enter.game.routing.MultiplayerServiceRoutingBridge;
import com.abs.casino.actions.enter.game.routing.ProtocolAdapterRoutingBridge;
import com.abs.casino.actions.enter.game.routing.SessionServiceRoutingBridge;
import com.dgphoenix.casino.cassandra.persist.CassandraPlayerSessionState;
import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.BaseGameInfoTemplateCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.currency.Currency;
import com.dgphoenix.casino.common.cache.data.game.GameMode;
import com.dgphoenix.casino.common.cache.data.payment.IWallet;
import com.dgphoenix.casino.common.cache.data.server.ServerInfo;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.dgphoenix.casino.common.exception.*;
import com.dgphoenix.casino.common.util.DigitFormatter;
import com.dgphoenix.casino.common.util.Pair;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.common.web.MobileDetector;
import com.abs.casino.common.web.login.apub.GameServerResponse;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import com.dgphoenix.casino.gs.GameServer;
import com.dgphoenix.casino.gs.managers.payment.bonus.FRBonusManager;
import com.dgphoenix.casino.gs.managers.payment.bonus.IFRBonusClient;
import com.dgphoenix.casino.gs.managers.payment.currency.CurrencyManager;
import com.dgphoenix.casino.gs.managers.payment.wallet.WalletProtocolFactory;
import com.dgphoenix.casino.gs.managers.payment.wallet.v3.CommonWalletAuthResult;
import com.dgphoenix.casino.gs.persistance.GameSessionPersister;
import com.dgphoenix.casino.helpers.login.LoginHelper;
import com.dgphoenix.casino.system.configuration.GameServerConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Struts action for launch game.
 * /cwstartgamev2.do?gameId=&mode=real&token=userToken&bankId=
 */
public class CWStartGameAction extends BaseStartGameAction<CWStartGameForm> {
    private static final Logger LOG = LogManager.getLogger(CWStartGameAction.class.getName() + "_v3");

    @Override
    protected Logger getLog() {
        return LOG;
    }

    /**
     * Performs checks, make login player to the server and redirects to the game launch page or errors.
     * @param mapping struts action mapping
     * @param actionForm {@code CWStartGameForm} action form.
     * @param request http request
     * @param response http response
     * @return {@code ActionForward} action redirect result
     * @throws Exception if any unexpected error occur
     */
    @Override
    protected ActionForward process(ActionMapping mapping, CWStartGameForm actionForm, HttpServletRequest request,
                                    HttpServletResponse response) throws Exception {

        getLog().debug("CWStartGameAction process: enter process mapping={}, actionForm={}, request={}", mapping, actionForm, request);

        try {
            long now = System.currentTimeMillis();
            BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(actionForm.getBankId());
            SessionServiceRoutingBridge.RouteDecision sessionRouteDecision =
                    SessionServiceRoutingBridge.getInstance().decide(actionForm.getBankId());
            request.setAttribute(SessionServiceRoutingBridge.REQUEST_ROUTE_ATTRIBUTE, sessionRouteDecision.getReason());
            getLog().debug("CWStartGameAction process: session route decision bankId={}, route={}, reason={}, endpoint={}",
                    actionForm.getBankId(),
                    sessionRouteDecision.isRouteToSessionService(),
                    sessionRouteDecision.getReason(),
                    sessionRouteDecision.getEndpoint());
            StatisticsManager.getInstance().updateRequestStatistics("CWStartGameAction process 1",
                    System.currentTimeMillis() - now);
            now = System.currentTimeMillis();
            StatisticsManager.getInstance().updateRequestStatistics("CWStartGameAction process 2",
                    System.currentTimeMillis() - now);
            now = System.currentTimeMillis();

            String rawGameId = actionForm.getGameId();
            long gameId = Long.parseLong(rawGameId);
            boolean newGamesRouteEnabled = isNewGamesRouteEnabled(bankInfo, rawGameId);
            GameMode mode = actionForm.getGameMode();
            String lang = actionForm.getLang();
            String userAgent = request.getHeader("User-Agent");

            //Makes authentication request to external side.
            final CommonWalletAuthResult authResult = getAuthInfo(actionForm, actionForm.getToken(), bankInfo,
                    request.getRemoteHost(), request);


            final String extId = authResult != null ? authResult.getUserId() : null;

            LOG.debug("CWStartGameAction process: call getPlayerSessionWithUnfinishedSid for externalId={}", extId);
            CassandraPlayerSessionState playerSessionUnfinishedSid = getPlayerSessionWithUnfinishedSid(extId);
            LOG.debug("CWStartGameAction process: playerSessionUnfinishedSid={}", playerSessionUnfinishedSid);

            if (playerSessionUnfinishedSid != null) {

                Pair<GameSession, Boolean> resultPair = finishGameSessionAndMakeSitOut(
                        playerSessionUnfinishedSid.getSid(),
                        playerSessionUnfinishedSid.getPrivateRoomId()
                );

                LOG.debug("CWStartGameAction process: finishGameSessionAndMakeSitOut resultPair={}", resultPair);
            }

            GameServerResponse serverResponse;
            Long accountId;

            AccountInfo accountInfo;
            SessionHelper.getInstance().lock(actionForm.getBankId(), authResult.getUserId());
            boolean isLocked = true;
            try {
                String authCurrency = CurrencyManager.getInstance().getCurrencyCodeByAlias(authResult.getCurrency(), bankInfo);
                SessionHelper.getInstance().openSession();
                accountInfo = AccountManager.getInstance().getByCompositeKey(actionForm.getSubCasinoId(),
                        bankInfo, extId);

                getLog().debug("CWStartGameAction process: accountInfo={}",accountInfo);

                //  If user is not found (for new players), creates new account info
                if (accountInfo == null) {
                    accountInfo = saveAccount(
                            null,
                            extId,
                            authCurrency,
                            authResult.getUserName(),
                            authResult.getFirstName(),
                            authResult.getLastName(),
                            authResult.getEmail(),
                            authResult.getCountryCode(),
                            actionForm,
                            true);

                    getLog().debug("CWStartGameAction process: created accountInfo={}",accountInfo);

                } else if (accountInfo.getLastLoginTime() == 0) {//mass-frb created account

                    getLog().debug("CWStartGameAction process: mass-frb created account. accountInfo={}",accountInfo);

                    if (StringUtils.isTrimmedEmpty(authCurrency)) {
                        try {
                            if (bankInfo.isNeedCurrencyCheckInAuth()) {
                                IFRBonusClient frbClient = FRBonusManager.getInstance().getClient(bankInfo.getId());
                                String extCurrency = frbClient.getAccountInfo(authResult.getUserId()).getCurrency();
                                authCurrency = CurrencyManager.getInstance().getCurrencyCodeByAlias(extCurrency, bankInfo);
                            }
                        } catch (Exception e) {
                            LOG.debug("CWStartGameAction process: Currency check error for account=" + accountInfo.getId(), e);
                        }

                        if (StringUtils.isTrimmedEmpty(authCurrency)) {
                            throw new WalletException("Auth result does not contain currency code", accountInfo.getId());
                        }
                    }
                    // Checks and update player currency if needed.
                    if (!accountInfo.getCurrency().getCode().equals(authCurrency)) {
                        Currency newCurrency = CurrencyManager.getInstance().setupCurrency(authCurrency, authCurrency,
                                bankInfo.getId());
                        accountInfo.setCurrency(newCurrency);
                        AccountManager.getInstance().update(accountInfo.getId(), accountInfo.getNickName(),
                                accountInfo.getFirstName(), accountInfo.getLastName(), accountInfo.getEmail(),
                                accountInfo.getPassword(), accountInfo.getAgentId(), accountInfo.isLocked());
                    }

                    if (bankInfo.isNoUseAccountInfoUrlForAuth()) {
                        AccountManager.getInstance().update(accountInfo.getId(), authResult.getUserName(),
                                authResult.getFirstName(), authResult.getLastName(), authResult.getEmail(),
                                accountInfo.getPassword(), accountInfo.getAgentId(), accountInfo.isLocked());
                    }

                } else if (!StringUtils.isTrimmedEmpty(authResult.getUserName()) &&
                        !authResult.getUserName().equals(accountInfo.getNickName())) {

                    getLog().debug("CWStartGameAction process: update accountInfo={}",accountInfo);

                    accountInfo = saveAccount(
                            accountInfo.getId(),
                            extId,
                            authCurrency,
                            authResult.getUserName(),
                            accountInfo.getFirstName(),
                            accountInfo.getLastName(),
                            accountInfo.getEmail(),
                            accountInfo.getCountryCode(),
                            actionForm,
                            false);
                }

                CurrencyManager.getInstance().setupCurrency(authCurrency, authCurrency, bankInfo.getId());

                if (mode == null) {
                    getLog().error("Unexpected null value: account={}, actionForm={}, request={}, mode={}",
                            accountInfo, actionForm, request, mode);
                }

                if (bankInfo.isAddTokenMode()) {
                    getLog().debug("CWStartGameAction process: save token={} into accountInfo={}",actionForm.getToken(), accountInfo);

                    accountInfo.setFinsoftSessionId(actionForm.getToken());
                }

                boolean showRedirectedUnfinishedGameMessage = false;

                if (isNeedStartUnfinishedGame(mode, bankInfo, accountInfo, gameId)) {
                    long originalGameId = gameId;
                    try {
                        gameId = getUnfinishedGameId(bankInfo.getId(), gameId, accountInfo);
                    } catch (Exception e) {
                        getLog().error("Unexpected error on check unfinished games", e);
                    }
                    getLog().debug("isNeedStartUnfinishedGame is true. originalGameId={}. new gameId={}",
                            originalGameId, gameId);
                    showRedirectedUnfinishedGameMessage = originalGameId == gameId;
                }

                accountId = accountInfo.getId();
                GameSession oldGameSession = SessionHelper.getInstance().getTransactionData().getGameSession();

                if (GameServer.getInstance().isMultiplayerGame(oldGameSession) && oldGameSession.isRealMoney() && GameMode.FREE.equals(actionForm.getGameMode())) {
                    getLog().error("CWStartGameAction process: found unclosed MQ in real mode");
                    addError(request, "error.login.unclosedRealModeSession", "found unclosed MQ in real mode");
                    return mapping.findForward(ERROR_FORWARD);
                }

                //always close MQ game
                if (GameServer.getInstance().needCloseMultiplayerGame(oldGameSession, bankInfo, -1)) {
                    getLog().debug("CWStartGameAction process: needCloseMultiplayerGame oldGameSessionId={}", oldGameSession.getGameId());

                    Long serverBalance = null;
                    IWallet wallet = SessionHelper.getInstance().getTransactionData().getWallet();
                    if (wallet != null) {
                        serverBalance = wallet.getServerBalance();
                    }
                    SessionHelper.getInstance().commitTransaction();
                    SessionHelper.getInstance().markTransactionCompleted();
                    SessionHelper.getInstance().clearWithUnlock();
                    isLocked = false;

                    getLog().debug("CWStartGameAction process: old transactions are commited for accountId={}",accountInfo.getId());

                    LoginHelper.performMaxQuestSitOut(accountInfo, oldGameSession, bankInfo);

                    getLog().debug("CWStartGameAction process: performMaxQuestSitOut success for accountInfo={}",accountInfo);

                    SessionHelper.getInstance().lock(actionForm.getBankId(), authResult.getUserId());
                    isLocked = true;
                    SessionHelper.getInstance().openSession();
                    accountInfo = SessionHelper.getInstance().getTransactionData().getAccount();
                    wallet = SessionHelper.getInstance().getTransactionData().getWallet();
                    //after sitOut balance may be changed, need update if possible
                    LOG.debug("CWStartGameAction process: After MP SitOut current accountInfo.balance={}, wallet.balance={}, " +
                                    "old wallet.serverBalance={}, form.balance={}",
                            accountInfo.getBalance(), wallet == null ? null : wallet.getServerBalance(),
                            serverBalance, actionForm.getBalance());

                    if (WalletProtocolFactory.getInstance().isWalletBankWithGetBalanceSupported(bankInfo)) {
                        com.dgphoenix.casino.gs.managers.payment.wallet.v2.ICommonWalletClient client =
                                WalletProtocolFactory.getInstance()
                                        .getWalletProtocolManager(accountInfo.getBankId())
                                        .getClient();
                        try {
                            double dBalance = client.getBalance(accountInfo.getId(), accountInfo.getExternalId(),
                                    accountInfo.getBankId(), accountInfo.getCurrency());
                            long balance = bankInfo.isParseLong() ? (long) dBalance :
                                    DigitFormatter.getCentsFromCurrency(dBalance);
                            LOG.debug("CWStartGameAction process: Success update balance={}", balance);
                            actionForm.setBalance(balance);
                        } catch (Exception e) {
                            LOG.error("CWStartGameAction process: Unable to refresh balance for accountId=" + accountId, e);
                        }
                    }

                    if (wallet != null && (serverBalance == null || serverBalance != wallet.getServerBalance())) {
                        LOG.debug("CWStartGameAction process: Wallet.serverBalance changed after sitOut, set new={}",
                                wallet.getServerBalance());
                        actionForm.setBalance(wallet.getServerBalance());
                    }
                }

                GameSession gameSessionAfterSitOut = SessionHelper.getInstance().getTransactionData().getGameSession();

                boolean needRedirectToIncompleteActiveRound = LoginHelper.needRedirectToIncompleteActiveRound(gameId, gameSessionAfterSitOut);

                AccountInfoAndSessionInfoPair infoPair = loginV3(actionForm, actionForm.getToken(),
                        request.getRemoteHost(), gameId, mode, accountId, !newGamesRouteEnabled);
                StatisticsManager.getInstance().updateRequestStatistics("CWStartGameAction process 3",
                        System.currentTimeMillis() - now);
                now = System.currentTimeMillis();

                AccountInfo account = infoPair.getAccount();
                String externalId = account != null ? account.getExternalId() : null;

                SessionInfo sessionInfo = infoPair.getSessionInfo();
                String sessionId = sessionInfo != null ? sessionInfo.getSessionId() : null;
                String privateRoomId = sessionInfo != null ? sessionInfo.getPrivateRoomId() : null;
                boolean multiplayerRoute = GameServer.getInstance().isMultiplayerGame(gameSessionAfterSitOut);

                MultiplayerServiceRoutingBridge.RouteDecision multiplayerServiceRouteDecision =
                        MultiplayerServiceRoutingBridge.getInstance().decide(
                                actionForm.getBankId(),
                                (int) gameId,
                                sessionId,
                                multiplayerRoute
                        );
                request.setAttribute(MultiplayerServiceRoutingBridge.REQUEST_ROUTE_ATTRIBUTE,
                        multiplayerServiceRouteDecision.getReason());
                getLog().debug("CWStartGameAction process: multiplayer route decision bankId={}, gameId={}, route={}, reason={}, endpoint={}, bankMultiplayerEnabled={}",
                        actionForm.getBankId(),
                        (int) gameId,
                        multiplayerServiceRouteDecision.isRouteToMultiplayerService(),
                        multiplayerServiceRouteDecision.getReason(),
                        multiplayerServiceRouteDecision.getEndpoint(),
                        multiplayerServiceRouteDecision.isBankMultiplayerEnabled());

                GameplayOrchestratorRoutingBridge.RouteDecision gameplayRouteDecision =
                        GameplayOrchestratorRoutingBridge.getInstance().decide(actionForm.getBankId(), multiplayerRoute);
                request.setAttribute(GameplayOrchestratorRoutingBridge.REQUEST_ROUTE_ATTRIBUTE,
                        gameplayRouteDecision.getReason());
                getLog().debug("CWStartGameAction process: gameplay route decision bankId={}, gameId={}, route={}, reason={}, endpoint={}, isMultiplayer={}",
                        actionForm.getBankId(),
                        (int) gameId,
                        gameplayRouteDecision.isRouteToGameplayService(),
                        gameplayRouteDecision.getReason(),
                        gameplayRouteDecision.getEndpoint(),
                        multiplayerRoute);

                ProtocolAdapterRoutingBridge.RouteDecision protocolRouteDecision =
                        ProtocolAdapterRoutingBridge.getInstance().decide(actionForm.getBankId());
                request.setAttribute(ProtocolAdapterRoutingBridge.REQUEST_ROUTE_ATTRIBUTE,
                        protocolRouteDecision.getReason());
                getLog().debug("CWStartGameAction process: protocol route decision bankId={}, route={}, reason={}, endpoint={}",
                        actionForm.getBankId(),
                        protocolRouteDecision.isRouteToProtocolAdapter(),
                        protocolRouteDecision.getReason(),
                        protocolRouteDecision.getEndpoint());

                SessionServiceRoutingBridge.getInstance().shadowCreateSession(
                        sessionRouteDecision,
                        actionForm.getBankId(),
                        sessionId,
                        externalId,
                        (int) gameId,
                        mode == null ? null : mode.name()
                );

                MultiplayerServiceRoutingBridge.getInstance().shadowSessionSync(
                        multiplayerServiceRouteDecision,
                        actionForm.getBankId(),
                        sessionId,
                        externalId,
                        (int) gameId,
                        privateRoomId,
                        "LAUNCH_SYNC"
                );

                GameplayOrchestratorRoutingBridge.getInstance().shadowLaunchIntent(
                        gameplayRouteDecision,
                        actionForm.getBankId(),
                        sessionId,
                        gameId,
                        mode == null ? null : mode.name()
                );

                ProtocolAdapterRoutingBridge.getInstance().shadowNormalizeStartGame(
                        protocolRouteDecision,
                        actionForm.getBankId(),
                        sessionId,
                        gameId,
                        mode == null ? null : mode.name(),
                        lang,
                        newGamesRouteEnabled ? "new-games" : "legacy-gs"
                );

                if (StringUtils.isTrimmedEmpty(lang)) {
                    lang = newGamesRouteEnabled ? "en" : LanguageDetector.getAlternateLanguage(bankInfo, gameId, accountInfo);
                }

                lang = LanguageDetector.resolveLanguageAlias(lang);
                if (StringUtils.isTrimmedEmpty(lang)) {
                    lang = "en";
                }

                if (needRedirectToIncompleteActiveRound) {
                    getLog().debug("CWStartGameAction process: needRedirectToIncompleteActiveRound gameId={}, lang={}, privateRoomId={}, request={}",
                        gameSessionAfterSitOut.getGameId(), lang, privateRoomId, request);

                    return redirectTIIncompleteRoundPage(bankInfo, gameSessionAfterSitOut.getGameId(),
                            null, lang, null, privateRoomId, request);
                }

                if (sessionInfo!= null && !StringUtils.isTrimmedEmpty(authResult.getCashierToken())) { //Hack for PlanetWin
                    sessionInfo.setSecretKey(authResult.getCashierToken());
                }

                // For New Games route (phase 1: gameId=00010 by default or bank override)
                if (newGamesRouteEnabled) {
                    getLog().info("CWStartGameAction process: route to new-games module, bankId={}, gameId={}, sessionId={}",
                            bankInfo.getId(), rawGameId, sessionId);
                    getLog().info("CWStartGameAction process: skip legacy game validations for new-games route, rawGameId={}",
                            rawGameId);

                    ActionRedirect ngsRedirect = getNewGamesForward(actionForm, request, mode, bankInfo,
                            sessionId, lang, gameId, rawGameId);
                    SessionHelper.getInstance().commitTransaction();
                    SessionHelper.getInstance().markTransactionCompleted();

                    getLog().debug("CWStartGameAction process: SID={}, ExternalId={}, PrivateRoomId={}, " +
                                    "isFinishGameSession=false", sessionId, externalId, privateRoomId
                    );

                    savePlayerSessionState(sessionId, externalId, privateRoomId, false, System.currentTimeMillis());

                    getLog().debug("CWStartGameAction process: ngsRedirect={}", ngsRedirect);
                    return ngsRedirect;
                }

                // For MQ games only
                if (isMultiPlayerGame(gameId)) {
                    //validates password from game server config for allow launch game. (is not for production, usual used for  demo with password)
                    validateMpPass(request, actionForm.getBankId());

                    // for launching BTG version there is separate action battlegroundstartgamev2, see struts-config.xml
                    if(BaseGameInfoTemplateCache.getInstance().getBaseGameInfoTemplateById(gameId).isBattleGroundsMultiplayerGame()){
                        getLog().error("CWStartGameAction process: battle is not allowed sessionId={}", sessionId);
                        addError(request, "error.login.incorrectParameters");
                        return mapping.findForward(ERROR_FORWARD);
                    }
                    GameServer.getInstance().checkMaintenanceMode(mode, lang, accountInfo, gameId);
                    checkPendingOperations(accountInfo, gameId, sessionInfo, null, mode, true);

                    if (!LanguageDetector.isLocalizationAvailable(lang, bankInfo, gameId, accountInfo)) {
                        if (bankInfo.isShowGameLocalizationError()) {
                            getLog().error("CWStartGameAction process: localization error sessionId={}", sessionId);
                            addError(request, "error.login.localizationError");
                            return mapping.findForward(ERROR_FORWARD);
                        } else {
                            lang = LanguageDetector.getAlternateLanguage(bankInfo, gameId, accountInfo);
                        }
                    }

                    if (GameMode.FREE == mode) {
                        AccountManager.getInstance().setFreeBalance(accountInfo, gameId);
                    }

                    ActionRedirect mpRedirect = getMultiPlayerForward(actionForm, request, mode, bankInfo,
                            sessionId, lang, gameId);
                    SessionHelper.getInstance().commitTransaction();
                    SessionHelper.getInstance().markTransactionCompleted();

                    getLog().debug("CWStartGameAction process: SID={}, ExternalId={}, PrivateRoomId={}, " +
                                    "isFinishGameSession=false", sessionId, externalId, privateRoomId
                    );

                    savePlayerSessionState(sessionId, externalId, privateRoomId, false, System.currentTimeMillis());

                    getLog().debug("CWStartGameAction process: mpRedirect={}", mpRedirect);
                    return mpRedirect;
                }

                // For usual casino games (slots, tables, ...)
                serverResponse = startGame(infoPair.getAccount(), sessionInfo, actionForm, response, lang, false);
                saveReferer(request, accountInfo);

                gameId = GameSessionPersister.getInstance().getGameSession(serverResponse.getGameSessionId()).getGameId();
                if (MobileDetector.isMobile(userAgent) && bankInfo.isUseSingleGameIdForAllDevices()) {
                    if (!LanguageDetector.isLocalizationAvailable(lang, bankInfo, gameId, accountInfo, userAgent)) {
                        if (bankInfo.isShowGameLocalizationError()) {
                            getLog().error("localization error");
                            addError(request, "error.login.localizationError");
                            return mapping.findForward(ERROR_FORWARD);
                        } else {
                            lang = LanguageDetector.getAlternateLanguage(bankInfo, gameId, accountInfo);
                        }
                    }
                } else {
                    if (!LanguageDetector.isLocalizationAvailable(lang, bankInfo, gameId, accountInfo)) {
                        if (bankInfo.isShowGameLocalizationError()) {
                            getLog().error("localization error");
                            addError(request, "error.login.localizationError");
                            return mapping.findForward(ERROR_FORWARD);
                        } else {
                            lang = LanguageDetector.getAlternateLanguage(bankInfo, gameId, accountInfo);
                        }
                    }
                }

                if (mode != GameMode.FREE) {
                    applyLangToGameSession(serverResponse.getGameSessionId(), lang);
                }

                getLog().debug("login with sessionId: {}", sessionId);
                ServerInfo serverInfo = assignServer(actionForm.getBankId(), gameId, mode);
                ActionForward forward = getForward(mapping, request, actionForm, gameId,
                        null, // Host ignored after migration from sb
                        sessionId, GAME_LAUNCHER_JSP, mode, lang, serverInfo.getServerId(), bankInfo,
                        accountInfo.getCurrency().getCode(), showRedirectedUnfinishedGameMessage,
                        SessionHelper.getInstance().getTransactionData());
                request.setAttribute(PLAYER_SESSION_ATTRIBUTE, sessionInfo);
                StatisticsManager.getInstance().updateRequestStatistics("CWStartGameAction process 5",
                        System.currentTimeMillis() - now);

                StatisticsManager.getInstance().updateRequestStatistics("CWStartGameAction process 4",
                        System.currentTimeMillis() - now);
                SessionHelper.getInstance().commitTransaction();
                SessionHelper.getInstance().markTransactionCompleted();
                return forward;
            } finally {
                if (isLocked) {
                    SessionHelper.getInstance().clearWithUnlock();
                } else {
                    getLog().debug("isLocked={}, skip SessionHelper.getInstance().clearWithUnlock()", isLocked);
                }
            }
        } catch (MaintenanceModeException e) {
            getLog().warn(e.getMessage());
            request.setAttribute(StartParameters.class.getSimpleName(), e.getParameters());
            return new ActionForward(GameServerConfiguration.getInstance().getMaintenancePage());
        } catch (UnknownCurrencyException e) {
            getLog().error(e.getMessage());
            addError(request, "error.login.currencyCodeValidationError");
            return mapping.findForward(ERROR_FORWARD);
        } catch (InvalidCurrencyRateException e) {
            getLog().error(e.getMessage());
            addError(request, "error.login.invalidCurrencyRate");
            return mapping.findForward(ERROR_FORWARD);
        } catch (CurrencyMismatchException e) {
            getLog().error(e.getMessage());
            return mapping.findForward(ERROR_FORWARD);
        } catch (Exception e) {
            getLog().error("process error", e);
            if (e instanceof WalletException && (actionForm.getBankId() == 121 || actionForm.getBankId() == 221
                    || actionForm.getBankId() == 226)) {
                addError(request, "error.login.walletError121", String.valueOf(((WalletException) e).getAccountId()));
            } else {
                addErrorWithPersistence(request, "error.login.internalError", e, System.currentTimeMillis());
            }
            return mapping.findForward(ERROR_FORWARD);
        }
    }

    @Override
    protected void processCommonWalletAuthResult(CWStartGameForm form, CommonWalletAuthResult result, BankInfo bankInfo)
            throws CommonException {
        super.processCommonWalletAuthResult(form, result, bankInfo);
        long balance;
        if (bankInfo.isParseLong()) {
            balance = (long) result.getBalance();
        } else {
            balance = DigitFormatter.getCentsFromCurrency(result.getBalance());
        }
        form.setBalance(balance);
    }
}
