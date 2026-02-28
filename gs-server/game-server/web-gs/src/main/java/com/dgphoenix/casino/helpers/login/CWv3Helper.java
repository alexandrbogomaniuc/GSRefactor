package com.abs.casino.helpers.login;

import com.dgphoenix.casino.account.AccountManager;
import com.dgphoenix.casino.cassandra.persist.CassandraPlayerSessionState;
import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.BaseGameInfoTemplateCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.game.BaseGameInfoTemplate;
import com.dgphoenix.casino.common.cache.data.game.GameMode;
import com.dgphoenix.casino.common.cache.data.game.GameType;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.dgphoenix.casino.common.exception.CommonException;
import com.abs.casino.common.exception.PlayerHasBetsException;
import com.abs.casino.common.exception.ServiceNotAvailableException;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.abs.casino.common.util.DigitFormatter;
import com.dgphoenix.casino.common.util.Pair;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.abs.casino.entities.AuthRequest;
import com.abs.casino.exceptions.LoginErrorException;
import com.dgphoenix.casino.gs.GameServer;
import com.dgphoenix.casino.gs.managers.payment.wallet.CWError;
import com.abs.casino.gs.managers.payment.wallet.CommonWalletErrors;
import com.abs.casino.gs.managers.payment.wallet.v3.CommonWalletAuthResult;
import com.dgphoenix.casino.gs.persistance.PlayerSessionPersister;
import com.abs.casino.helpers.login.LoginHelper;
import com.dgphoenix.casino.services.mp.MPGameSessionService;
import com.abs.casino.sm.CWv3PlayerSessionManager;
import com.abs.casino.sm.PlayerSessionFactory;
import com.abs.casino.sm.login.GameLoginRequest;
import com.abs.casino.sm.login.LoginResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;

/**
 * User: isirbis
 * Date: 02.10.14
 */
public class CWv3Helper extends LoginHelper<AuthRequest, GameLoginRequest> {
    private static final Logger LOG = LogManager.getLogger(CWv3Helper.class);
    private static final CWv3Helper instance = new CWv3Helper();
    protected final MPGameSessionService mpGameSessionService;

    private CWv3Helper() {
        ApplicationContext applicationContext = ApplicationContextHelper.getApplicationContext();
        mpGameSessionService = applicationContext.getBean("mpGameSessionService", MPGameSessionService.class);
    }

    public static CWv3Helper getInstance() {
        return instance;
    }

    public LoginResponse login(GameLoginRequest loginRequest) throws LoginErrorException {
        return login(loginRequest, true);
    }

    public LoginResponse login(GameLoginRequest loginRequest, boolean closeCurrentSession) throws LoginErrorException {

        LOG.debug("login: closeCurrentSession={}, loginRequest={}", closeCurrentSession, loginRequest);

        CWError error = null;
        SessionInfo sessionInfo = null;
        AccountInfo accountInfo = null;

        try {

            if (StringUtils.isTrimmedEmpty(loginRequest.getToken()) && loginRequest.getGameMode().equals(GameMode.REAL)) {
                throw new CommonException("incorrect parameters");
            }

            BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(loginRequest.getBankId());

            AuthRequest authRequest = new AuthRequest();
            authRequest.setToken(loginRequest.getToken());
            authRequest.setGameMode(loginRequest.getGameMode());
            authRequest.setRemoteHost(loginRequest.getRemoteHost());
            authRequest.setClientType(loginRequest.getClientType());
            authRequest.setProperties(loginRequest.getProperties());
            LOG.debug("login: authRequest={}", authRequest);

            CommonWalletAuthResult authResult = getAuthInfo(authRequest, loginRequest, bankInfo);
            LOG.debug("login: authResult={}", authResult);

            try {

                String externalId = authResult != null ? authResult.getUserId() : null;

                LOG.debug("login: mpGameSessionService is not null and externalId={}", externalId);

                CassandraPlayerSessionState playerSessionUnfinishedSid
                        = mpGameSessionService.getPlayerSessionWithUnfinishedSid(externalId);

                LOG.debug("login: playerSessionUnfinishedSid={}", playerSessionUnfinishedSid);

                if (playerSessionUnfinishedSid != null) {

                    Pair<GameSession, Boolean> resultPair = mpGameSessionService
                            .finishGameSessionAndMakeSitOut(
                                playerSessionUnfinishedSid.getSid(),
                                playerSessionUnfinishedSid.getPrivateRoomId()
                            );

                    LOG.debug("login: finishGameSessionAndMakeSitOut resultPair={}", resultPair);
                }
            } catch (Exception e) {
                LOG.warn("login: Exception during old Player Session Processing, {}", e.getMessage(), e);
            }

            SessionHelper.getInstance().lock(loginRequest.getBankId(), authResult.getUserId());
            boolean isLocked = true;

            try {

                SessionHelper.getInstance().openSession();
                final String externalId = authResult.getUserId();

                accountInfo = AccountManager.getInstance().getByCompositeKey(loginRequest.getSubCasinoId(), loginRequest.getBankId(), externalId);
                LOG.debug("login: accountInfo={}", accountInfo);

                if (accountInfo == null) {

                    try {

                        accountInfo = createAccount(externalId, authResult.getCurrency(), authResult.getUserName(), authResult.getFirstName(),
                                authResult.getLastName(), authResult.getEmail(), loginRequest, bankInfo, authResult.getCountryCode());
                        LOG.debug("login: created accountInfo={}", accountInfo);

                    } catch (CommonException e) {
                        error = CommonWalletErrors.USER_NOT_FOUND;
                        throw e;
                    }
                }

                if (bankInfo.isAddTokenMode()) {
                    accountInfo.setFinsoftSessionId(loginRequest.getToken());
                }

                CWv3PlayerSessionManager playerSessionManager = (CWv3PlayerSessionManager) PlayerSessionFactory.getInstance().
                        getPlayerSessionManager(loginRequest.getBankId());

                sessionInfo = PlayerSessionPersister.getInstance().getSessionInfo();
                LOG.debug("login: sessionInfo={}", sessionInfo);

                if (closeCurrentSession || sessionInfo == null) {

                    GameSession gameSession = SessionHelper.getInstance().getTransactionData().getGameSession();
                    LOG.debug("login: gameSession={}", gameSession);

                    if (GameServer.getInstance().needCloseMultiplayerGame(gameSession, bankInfo, -1)) {

                        SessionHelper.getInstance().commitTransaction();
                        SessionHelper.getInstance().markTransactionCompleted();
                        SessionHelper.getInstance().clearWithUnlock();

                        isLocked = false;

                        LOG.debug("login: call LoginHelper.performMaxQuestSitOut for accountInfo={}, " +
                                "gameSession={}, bankInfo={}", accountInfo, gameSession, bankInfo);

                        LoginHelper.performMaxQuestSitOut(accountInfo, gameSession, bankInfo);

                        SessionHelper.getInstance().lock(loginRequest.getBankId(), authResult.getUserId());
                        isLocked = true;

                        SessionHelper.getInstance().openSession();
                        accountInfo = SessionHelper.getInstance().getTransactionData().getAccount();
                        LOG.debug("login: SessionHelper accountInfo={}", accountInfo);
                    }

                    GameSession gameSessionAfterSitOut = SessionHelper.getInstance().getTransactionData().getGameSession();
                    LOG.debug("login: gameSessionAfterSitOut={}", gameSessionAfterSitOut);

                    boolean needRedirectToIncompleteActiveRound =
                            needRedirectToIncompleteActiveRound(loginRequest.getGameId(), gameSessionAfterSitOut);

                    LOG.debug("login: needRedirectToIncompleteActiveRound={}", needRedirectToIncompleteActiveRound);

                    if (loginRequest.getGameId() != null && needRedirectToIncompleteActiveRound) {

                        LoginResponse loginResponse = new LoginResponse(sessionInfo, accountInfo, gameSessionAfterSitOut.getGameId());
                        LOG.debug("login: loginResponse={}", loginResponse);

                        return loginResponse;
                    }

                    String extSessionId = loginRequest.getExternalSessionId() != null
                            ? loginRequest.getExternalSessionId() : loginRequest.getToken();

                    sessionInfo = playerSessionManager.login(accountInfo, extSessionId, loginRequest.getRemoteHost(),
                            loginRequest.getClientType(), loginRequest);
                    LOG.debug("login: playerSessionManager.login sessionInfo={}", sessionInfo);

                    if (!SessionHelper.getInstance().getTransactionData().isAppliedAutoFinishLogic()) {

                        long balance;
                        if (bankInfo.isParseLong()) {
                            balance = (long) authResult.getBalance();
                        } else {
                            balance = DigitFormatter.getCentsFromCurrency(authResult.getBalance());
                        }

                        accountInfo.setBalance(balance);
                    }

                } else if (loginRequest.getGameId() != null) {
                    //MP games always try reuse GameSession, but balance must be refreshed, see https://jira.dgphoenix.com/browse/MQ-596
                    BaseGameInfoTemplate template = BaseGameInfoTemplateCache.getInstance().
                            getBaseGameInfoTemplateById(loginRequest.getGameId());

                    LOG.debug("login: template={}", template);

                    if (GameType.MP.equals(template.getGameType())) {
                        long balance;
                        if (bankInfo.isParseLong()) {
                            balance = (long) authResult.getBalance();
                        } else {
                            balance = DigitFormatter.getCentsFromCurrency(authResult.getBalance());
                        }
                        accountInfo.setBalance(balance);
                    }
                }

                SessionHelper.getInstance().commitTransaction();
                SessionHelper.getInstance().markTransactionCompleted();

            } finally {
                if (isLocked) {
                    LOG.debug("login: isLocked=" + isLocked + ", call SessionHelper.getInstance().clearWithUnlock()");
                    SessionHelper.getInstance().getTransactionData().setAppliedAutoFinishLogic(false);
                    SessionHelper.getInstance().clearWithUnlock();
                } else {
                    LOG.debug("login: isLocked=" + isLocked + ", skip SessionHelper.getInstance().clearWithUnlock()");
                }
            }
        } catch (PlayerHasBetsException e) {
            LOG.error("CommonWalletLoginAction::process unable to login player due CDR", e);
            error = CommonWalletErrors.PLAYER_HAS_BETS_IN_CDR;
        } catch (ServiceNotAvailableException e) {
            LOG.error("CommonWalletLoginAction:: service not available", e);
            error = CommonWalletErrors.REMOTE_SERVICE_NOT_AVAILABLE;
        } catch (Throwable e) {
            LOG.error("CommonWalletLoginAction::process unable to login player", e);
            if (error == null) {
                error = CommonWalletErrors.INTERNAL_ERROR;
            }
        }

        LOG.debug("login: SessionInfo: {}; AccountInfo: {}; error: {}" , sessionInfo, accountInfo, error);

        if (sessionInfo == null || error != null) {
            if (error == null) {
                error = CommonWalletErrors.INTERNAL_ERROR;
            }
            LOG.error("login: CommonWalletLoginAction::error:{} ", error);
            throw new LoginErrorException(error);
        }

        return new LoginResponse(sessionInfo, accountInfo);
    }

    private AccountInfo createAccount(String externalId, String extCurrency, String extNickName, String extFirstName,
                                      String extLastName, String extEmail, GameLoginRequest loginRequest, BankInfo bankInfo,
                                      String countryCode) throws CommonException {

        AccountInfo accountInfo = AccountManager.getInstance().saveAccountWithCurrencyUpdate(null, externalId, bankInfo, extNickName,
                false, false, extEmail, loginRequest.getClientType(), extFirstName, extLastName,
                extCurrency, countryCode, true);

        LOG.debug("createAccount: accountInfo created: {}" , accountInfo);

        return accountInfo;
    }
}
