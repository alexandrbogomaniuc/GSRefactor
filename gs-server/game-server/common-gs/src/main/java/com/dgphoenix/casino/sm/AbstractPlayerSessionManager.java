package com.dgphoenix.casino.sm;

import com.dgphoenix.casino.account.AccountManager;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.session.ClientType;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.dgphoenix.casino.common.exception.AccountLockedException;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.ITimeProvider;
import com.dgphoenix.casino.common.util.NtpTimeProvider;
import com.dgphoenix.casino.common.util.string.StringIdGenerator;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import com.dgphoenix.casino.gs.GameServer;
import com.dgphoenix.casino.gs.managers.payment.bonus.BonusManager;
import com.dgphoenix.casino.gs.managers.payment.bonus.IBonusClient;
import com.abs.casino.gs.managers.payment.bonus.client.BonusAccountInfoResult;
import com.dgphoenix.casino.gs.managers.payment.transfer.PaymentManager;
import com.dgphoenix.casino.gs.persistance.GameSessionPersister;
import com.dgphoenix.casino.gs.persistance.PlayerSessionPersister;
import com.dgphoenix.casino.sm.tracker.logout.LogoutTracker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * User: plastical
 * Date: 25.02.2010
 */
public abstract class AbstractPlayerSessionManager implements IPlayerSessionManager {
    private static final Logger LOG = LogManager.getLogger(AbstractPlayerSessionManager.class);
    private final long bankId;
    private String alertsEmailAddress;
    private boolean sendLoginErrorsToEmail;
    protected final ITimeProvider timeProvider;

    protected AbstractPlayerSessionManager(long bankId) {
        this.bankId = bankId;
        refreshBankproperies();
        timeProvider = NtpTimeProvider.getInstance();
    }

    public long getBankId() {
        return bankId;
    }

    public void refreshBankproperies() {
        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
        alertsEmailAddress = bankInfo.getAlertsEmailAddress();
        sendLoginErrorsToEmail = bankInfo.isSendLoginErrorsToEmail();
    }

    public static String updateCurrency(BankInfo bankInfo, String userId, String original) throws CommonException {
        return updateCurrency(bankInfo, userId, original, null);
    }

    public static String updateCurrency(BankInfo bankInfo, String userId, String original,
                                        IGetAccountInfoProvider getAccountInfoProvider) throws CommonException {
        if (bankInfo.isNoUseAccountInfoUrlForAuth()) {
            return original;
        }
        try {
            if (getAccountInfoProvider == null) {
                getAccountInfoProvider = getDefaultAccountInfoProvider(bankInfo);
            }
            BonusAccountInfoResult r = getAccountInfoProvider.getAccountInfo(userId);
            if (r == null) {
                throw new CommonException("Empty request result");
            }
            String newCurrency = r.getCurrency();
            if (StringUtils.isTrimmedEmpty(newCurrency)) {
                throw new CommonException("Get account info currency empty");
            }
            if (!newCurrency.equals(original)) {
                LOG.warn("Currency change from {} to {}", original, newCurrency);
            }
            return newCurrency;
        } catch (Exception ex) {
            LOG.error("Can't get account Info ", ex);
            throw new CommonException("Error in client request", ex);
        }
    }

    public String getAlertsEmailAddress() {
        return alertsEmailAddress;
    }

    public boolean isSendLoginErrorsToEmail() {
        return sendLoginErrorsToEmail;
    }

    public boolean isReusePlayerSession(long bankId) {
        return bankId == 121 || bankId == 221 || bankId == 226 || /*bankId == 160 || */bankId == 122;
    }

    /*
    this method must be synchronized on accountInfo in PSM-licencee impl
     */
    protected SessionInfo login_impl(AccountInfo accountInfo, String externalSessionId, String userHost,
                                     ClientType clientType, SessionInfo oldSession, String generatedSessionId,
                                     boolean reusePlayerSession, String privateRoomId)
            throws CommonException {

        if (accountInfo.isLocked()) {
            throw new AccountLockedException("account is locked, accountId=" + accountInfo.getId());
        }

        long now = System.currentTimeMillis();
        SessionInfo sessionInfo;

        if (reusePlayerSession && oldSession != null) {
            sessionInfo = oldSession;
            StatisticsManager.getInstance().updateRequestStatistics("AbstractPlayerSessionManager:login_impl reuse",
                    System.currentTimeMillis() - now);
        } else {
            now = System.currentTimeMillis();
            if (generatedSessionId == null) {
                generatedSessionId = StringIdGenerator.generateSessionId(GameServer.getInstance().getServerId(),
                        accountInfo.getBankId(), accountInfo.getExternalId());
            }
            sessionInfo = createSessionInfo(accountInfo, externalSessionId, userHost, clientType, generatedSessionId, privateRoomId);
            StatisticsManager.getInstance().updateRequestStatistics("AbstractPlayerSessionManager:login_impl put",
                    System.currentTimeMillis() - now);
        }

        PlayerSessionPersister.getInstance().save(sessionInfo);

        now = System.currentTimeMillis();
        accountInfo.setLastLoginTime(System.currentTimeMillis());
        accountInfo.setCurrentGameServer(GameServer.getInstance().getServerId());
        if (getLog().isInfoEnabled()) {
            getLog().info("login_impl: after save accountInfo: {}", accountInfo);
        }
        StatisticsManager.getInstance().updateRequestStatistics("AbstractPlayerSessionManager:login_impl registerSession",
                System.currentTimeMillis() - now);
        return sessionInfo;
    }

    protected SessionInfo createSessionInfo(AccountInfo accountInfo, String externalSessionId, String userHost,
                                            ClientType clientType, String generatedSessionId, String privateRoomId) {
        return new SessionInfo(accountInfo.getId(), clientType, generatedSessionId, userHost,
                externalSessionId, GameServer.getInstance().getServerId(), timeProvider.getTime(), privateRoomId);
    }

    protected void prepareForRelogin(AccountInfo accountInfo, SessionInfo sessionInfo) throws CommonException {
        long now = System.currentTimeMillis();
        Long gameSessionId = sessionInfo.getGameSessionId();
        if (gameSessionId != null) {
            sessionInfo.reuse();
            StatisticsManager.getInstance().updateRequestStatistics("prepareForRelogin: reuse",
                    System.currentTimeMillis() - now);
            now = System.currentTimeMillis();
            GameSession gameSession = GameSessionPersister.getInstance().getGameSession(gameSessionId);
            if (gameSession != null) {
                LOG.debug("prepareForRelogin: need closeOnlineGame, gameSessionId: {}", gameSession.getId());
                GameServer.getInstance().closeOnlineGame(accountInfo, sessionInfo, gameSession, false,
                        false);
            }
            StatisticsManager.getInstance().updateRequestStatistics("prepareForRelogin: closeGameSession sync", System.currentTimeMillis() - now);
        }
    }

    /*
    this method must be synchronized on accountInfo in PSM-licencee impl
     */
    protected void logout_impl(AccountInfo accountInfo, SessionInfo sessionInfo, String logoutReason)
            throws CommonException {
        logout_impl(accountInfo, sessionInfo, logoutReason, false);
    }

    protected void logout_impl(AccountInfo accountInfo, SessionInfo sessionInfo, String logoutReason, boolean fromLogin)
            throws CommonException {
        long now = System.currentTimeMillis();
        Long gameSessionId = sessionInfo.getGameSessionId();
        GameSession gameSession = null;
        if (gameSessionId != null) {
            gameSession = GameSessionPersister.getInstance().getGameSession(gameSessionId);
            LOG.debug("logout_impl: need closeOnlineGame, gameSessionId: {}", gameSessionId);
            GameServer.getInstance().closeOnlineGame(accountInfo, sessionInfo, gameSession, false,
                    false);
        }

        long sessionEndTime = System.currentTimeMillis();
        if (sessionInfo.getEndTime() == null) {
            sessionInfo.setEndTime(sessionEndTime);
        }
        StatisticsManager.getInstance().updateRequestStatistics("AbstractPlayerSessionManager:logout_impl 1",
                System.currentTimeMillis() - now);
        now = System.currentTimeMillis();
        boolean isFreeModeAccount = accountInfo.isGuest();

        StatisticsManager.getInstance().updateRequestStatistics("AbstractPlayerSessionManager:logout_impl 2",
                System.currentTimeMillis() - now);
        now = System.currentTimeMillis();

        String sessionId = sessionInfo.getSessionId();
        final BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(accountInfo.getBankId());

        if (bankInfo.isVietbetLogoutWithWithdrawal() && !isFreeModeAccount && accountInfo.getBalance() > 0) {
            Long gameId = gameSession == null ? null : gameSession.getGameId();
            long amount = accountInfo.getBalance();
            accountInfo.incrementBalance(-amount, false);
            PaymentManager.getInstance().processWithdrawal(accountInfo, bankInfo,
                    gameSessionId != null ? gameSessionId : -1, gameId != null ? gameId : -1L, amount, null, true,
                    sessionInfo.getClientType(), null);
            LOG.debug("trace_viet id : {}, extId : {}, amount : {}", accountInfo.getId(),
                    accountInfo.getExternalId(), amount);
        }
        PlayerSessionPersister.getInstance().remove(sessionInfo, logoutReason, true, bankInfo,
                !isFreeModeAccount);
        if (!fromLogin) {
            AccountManager.getInstance().remove(accountInfo, bankInfo);
        }
        StatisticsManager.getInstance().updateRequestStatistics("AbstractPlayerSessionManager:logout_impl 4",
                System.currentTimeMillis() - now);
    }

    public void logout(String sessionId) throws CommonException {
        SessionInfo sessionInfo = PlayerSessionPersister.getInstance().getSessionInfo();
        if (sessionInfo == null || !sessionInfo.getSessionId().equals(sessionId)) {
            getLog().error("Old sessionInfo was found={}", sessionId);
            return;
        }
        logout(AccountManager.getInstance().getAccountInfo(sessionInfo.getAccountId()), "logout by player", sessionInfo);
    }

    @Override
    public void logout(AccountInfo accountInfo, String logoutReason, SessionInfo sessionInfo)
            throws CommonException {
        try {
            if (sessionInfo != null) {
                logout_impl(accountInfo, sessionInfo, logoutReason);
            }
            if (getLog().isDebugEnabled()) {
                getLog().debug("login accountId: {} logout completed", accountInfo.getId());
            }
        } catch (Exception e) {
            getLog().error("logout[accountId: {}] error:", accountInfo.getId(), e);
            LogoutTracker.getInstance().addTask(accountInfo.getId(), accountInfo.getExternalId(),
                    accountInfo.getBankId());
            throw new CommonException(e);
        }
    }

    protected Logger getLog() {
        return LOG;
    }

    private static IGetAccountInfoProvider getDefaultAccountInfoProvider(BankInfo bankInfo) {
        return userId -> {
            if (StringUtils.isTrimmedEmpty(bankInfo.getBonusAccountInfoUrl())) {
                throw new CommonException("getBonusAccountInfoUrl not set in bankInfo " + bankInfo.getId());
            }
            IBonusClient cl = BonusManager.getInstance().getClient(bankInfo.getId());
            if (cl == null) {
                throw new CommonException("Can't get client bankId:" + bankInfo.getId());
            }
            return cl.getAccountInfo(userId);
        };
    }
}
