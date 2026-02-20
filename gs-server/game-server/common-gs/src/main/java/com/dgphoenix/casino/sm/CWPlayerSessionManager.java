package com.dgphoenix.casino.sm;

import com.dgphoenix.casino.actions.enter.game.IStartGameForm;
import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.BaseGameInfoTemplateCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.session.ClientType;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.string.StringIdGenerator;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import com.dgphoenix.casino.gs.GameServer;
import com.dgphoenix.casino.gs.persistance.GameSessionPersister;
import com.dgphoenix.casino.sm.login.LoginRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public class CWPlayerSessionManager<F extends LoginRequest, L extends IStartGameForm> extends
        AbstractPlayerSessionManager {
    private static final Logger LOG = LogManager.getLogger(CWPlayerSessionManager.class);

    public CWPlayerSessionManager(long bankId) {
        super(bankId);
    }

    @Override
    public SessionInfo login(AccountInfo accountInfo, String externalSessionId, String userHost, ClientType clientType, boolean forceReuseGameSession)
            throws CommonException {
        return login(accountInfo, externalSessionId, userHost, clientType, (L) null, forceReuseGameSession);
    }

    public SessionInfo login(AccountInfo accountInfo, String externalSessionId, String host, ClientType type)
            throws CommonException {
        return this.login(accountInfo, externalSessionId, host, type, (L) null, false);
    }

    public SessionInfo login(AccountInfo accountInfo, String externalSessionId, String host, ClientType type, L form, boolean forceReuseGameSession)
            throws CommonException {
        if (accountInfo == null) {
            throw new CommonException("account doesn't exist");
        }
        long accountId = accountInfo.getId();
        long now = System.currentTimeMillis();
        SessionInfo sessionInfo;
        String generatedSessionId = StringIdGenerator.generateSessionId(GameServer.getInstance().getServerId(),
                accountInfo.getBankId(), accountInfo.getExternalId());
        getLog().debug("CWPlayerSessionManager login: New generatedSessionId={}, for serverId={}; bankId={}; externalId={}",
            generatedSessionId, GameServer.getInstance().getServerId(), accountInfo.getBankId(), accountInfo.getExternalId());

        boolean reusePlayerSession = forceReuseGameSession || isReusePlayerSession(accountInfo.getBankId());
        StatisticsManager.getInstance().updateRequestStatistics("CWPlayerSessionManager: login sync account",
                System.currentTimeMillis() - now);
        getLog().debug("CWPlayerSessionManager login: reusePlayerSession={}, for forceReuseGameSession={}; and generatedSessionId={}",
            reusePlayerSession, forceReuseGameSession, generatedSessionId);

        now = System.currentTimeMillis();
        SessionInfo oldSessionInfo = SessionHelper.getInstance().getTransactionData().getPlayerSession();
        StatisticsManager.getInstance().updateRequestStatistics("CWPlayerSessionManager: " +
                "login getSessionInfoByAccountId", System.currentTimeMillis() - now);
        getLog().debug("CWPlayerSessionManager login: get oldSessionInfo={}, for generatedSessionId={}", oldSessionInfo, generatedSessionId);

        if (oldSessionInfo != null && reusePlayerSession && !forceReuseGameSession) {
            now = System.currentTimeMillis();
            prepareForRelogin(accountInfo, oldSessionInfo);
            StatisticsManager.getInstance().updateRequestStatistics("CWPlayerSessionManager: login prepareForRelogin",
                    System.currentTimeMillis() - now);
            getLog().debug("CWPlayerSessionManager login: prepare for login generatedSessionId={}", generatedSessionId);
        }

        if (oldSessionInfo != null && !reusePlayerSession) {
            now = System.currentTimeMillis();
            //do some preparations
            LOG.debug("CWPlayerSessionManager login: before logout accountId={} externalSessionId={} host={} performing logout first", accountId, externalSessionId, host);
            logout_impl(accountInfo, oldSessionInfo, "from login", true);
            LOG.debug("CWPlayerSessionManager login: after logout completed accountId={}", accountId);

            StatisticsManager.getInstance().updateRequestStatistics("CWPlayerSessionManager: login logout_impl",
                    System.currentTimeMillis() - now);
        }

        now = System.currentTimeMillis();

        LOG.debug("CWPlayerSessionManager login: performing login accountId={} externalSessionId={} host={} clientType={}", accountId, externalSessionId, host, type);

        sessionInfo = login_impl(accountInfo, externalSessionId, host, type, oldSessionInfo, generatedSessionId, reusePlayerSession, null);
        StatisticsManager.getInstance().updateRequestStatistics("CWPlayerSessionManager: login login_impl",
                System.currentTimeMillis() - now);

        if (form != null) {
            addSessionParameters(accountInfo, sessionInfo, form);
        }

        SessionHelper.getInstance().getTransactionData().setAppliedAutoFinishLogic(false);
        LOG.debug("CWPlayerSessionManager login: login completed accountId={} externalSessionId={}", accountId, externalSessionId);

        return sessionInfo;
    }

    //todo: after all changes with actions use old login method
    public SessionInfo login(AccountInfo accountInfo, String externalSessionId, String host, ClientType type,
                             F loginRequest) throws CommonException {
        if (accountInfo == null) {
            throw new CommonException("account doesn't exist");
        }

        long now = System.currentTimeMillis();
        SessionInfo sessionInfo;
        String generatedSessionId = StringIdGenerator.generateSessionId(GameServer.getInstance().getServerId(),
                accountInfo.getBankId(), accountInfo.getExternalId());
        boolean reusePlayerSession = isReusePlayerSession(accountInfo.getBankId());
        StatisticsManager.getInstance().updateRequestStatistics("CWPlayerSessionManager: login sync account",
                System.currentTimeMillis() - now);
        now = System.currentTimeMillis();
        SessionInfo oldSessionInfo = SessionHelper.getInstance().getTransactionData().getPlayerSession();
        StatisticsManager.getInstance().updateRequestStatistics("CWPlayerSessionManager: " +
                "login getSessionInfoByAccountId", System.currentTimeMillis() - now);
        if (isBattlegroundGameBySession(oldSessionInfo)) {
            return oldSessionInfo;
        }
        if (oldSessionInfo != null && reusePlayerSession) {
            now = System.currentTimeMillis();
            prepareForRelogin(accountInfo, oldSessionInfo);
            StatisticsManager.getInstance()
                    .updateRequestStatistics("CWPlayerSessionManager: login prepareForRelogin",
                            System.currentTimeMillis() - now);
        }
        if (oldSessionInfo != null && !reusePlayerSession) {
            now = System.currentTimeMillis();
            //do some preparations

            LOG.debug("CWPlayerSessionManager login:  performing logout first accountId={}, externalSessionId={}, host={}",
                        accountInfo.getId(), externalSessionId, host);

            logout_impl(accountInfo, oldSessionInfo, "from login", true);

            LOG.debug("CWPlayerSessionManager login: logout completed accountId={}", accountInfo.getId());

            StatisticsManager.getInstance().updateRequestStatistics("CWPlayerSessionManager: login logout_impl",
                    System.currentTimeMillis() - now);
        }
        now = System.currentTimeMillis();

        LOG.debug("CWPlayerSessionManager login:  performing login accountId={}, externalSessionId={}, host={}, clientType={}",
                accountInfo.getId(), externalSessionId, host, type);

        sessionInfo = login_impl(accountInfo, externalSessionId, host, type, oldSessionInfo, generatedSessionId, reusePlayerSession,
                loginRequest != null ? loginRequest.getPrivateRoomId() : null);

        StatisticsManager.getInstance().updateRequestStatistics("CWPlayerSessionManager: login login_impl",
                System.currentTimeMillis() - now);

        if (loginRequest != null) {
            addSessionParameters(accountInfo, sessionInfo, loginRequest.getProperties());

        }

        LOG.debug("CWPlayerSessionManager login: login completed accountId={}, externalSessionId={}",accountInfo.getId(), externalSessionId);

        return sessionInfo;
    }

    private boolean isBattlegroundGameBySession(SessionInfo sessionInfo) throws CommonException {
        if (sessionInfo == null || sessionInfo.getGameSessionId() == null) {
            return false;
        }
        GameSession gameSession = GameSessionPersister.getInstance().getGameSession(sessionInfo.getGameSessionId());
        return gameSession != null && BaseGameInfoTemplateCache.getInstance()
                .getBaseGameInfoTemplateById(gameSession.getGameId()).isBattleGroundsMultiplayerGame();
    }

    protected void addSessionParameters(AccountInfo accountInfo, SessionInfo sessionInfo, Map<String, String> properties)
            throws CommonException {

    }

    protected void addSessionParameters(AccountInfo accountInfo, SessionInfo sessionInfo, L form)
            throws CommonException {

    }
}