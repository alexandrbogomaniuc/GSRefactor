package com.abs.casino.helpers.login;

import com.dgphoenix.casino.account.AccountManager;
import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.session.ClientType;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.RNG;
import com.dgphoenix.casino.common.util.string.StringIdGenerator;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.abs.casino.entities.AuthRequest;
import com.abs.casino.exceptions.LoginErrorException;
import com.dgphoenix.casino.gs.GameServer;
import com.dgphoenix.casino.gs.managers.payment.wallet.CommonWalletErrors;
import com.dgphoenix.casino.helpers.login.LoginHelper;
import com.dgphoenix.casino.sm.CWPlayerSessionManager;
import com.dgphoenix.casino.sm.PlayerSessionFactory;
import com.abs.casino.sm.login.CWLoginRequest;
import com.abs.casino.sm.login.LoginResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * User: isirbis
 * Date: 30.09.14
 */
public class CWHelper extends LoginHelper<AuthRequest, CWLoginRequest> {
    private static final Logger LOG = LogManager.getLogger(CWHelper.class);
    private static final CWHelper instance = new CWHelper();

    private CWHelper() {
    }

    public static CWHelper getInstance() {
        return instance;
    }

    public LoginResponse login(CWLoginRequest loginRequest) throws LoginErrorException {
        try {
            int bankId = loginRequest.getBankId();
            short subCasinoId = loginRequest.getSubCasinoId();
            String remoteHost = loginRequest.getRemoteHost();
            ClientType clientType = loginRequest.getClientType();

            boolean isGuest = loginRequest.isGuest();

            if (LOG.isDebugEnabled()) {
                LOG.debug("login=" + loginRequest);
            }

            if (isGuest) {
                return guestLogin(subCasinoId, bankId, remoteHost, clientType, loginRequest);
            } else {
                return login(subCasinoId, bankId, remoteHost, clientType, loginRequest);
            }
        } catch (Throwable e) {
            LOG.error("process unable to login player", e);
            throw new LoginErrorException(CommonWalletErrors.INTERNAL_ERROR);
        }
    }

    private LoginResponse guestLogin(short subCasinoId, int bankId, String remoteHost, ClientType clientType,
                                     CWLoginRequest loginRequest) throws CommonException {
        String randomStr;
        if (AccountManager.getInstance().isPerfectAccountIdMode(bankId)) {
            randomStr = String.valueOf(-RNG.nextLong());
        } else {
            randomStr = StringIdGenerator.generateTimeAndRandomBased();
        }

        SessionInfo sessionInfo;
        AccountInfo accountInfo;

        SessionHelper.getInstance().lock(bankId, randomStr);
        try {
            SessionHelper.getInstance().openSession();
            sessionInfo = commonGuestLogin(randomStr, subCasinoId, bankId, remoteHost, clientType,
                    loginRequest.getFakeExternalSessionId());
            accountInfo = SessionHelper.getInstance().getTransactionData().getAccount();

            Long balance = loginRequest.getBalance();
            if (balance != null) {
                LOG.info("Found balance in request: " + balance + ", sessionId=" + sessionInfo.getSessionId());
                accountInfo.setFreeBalance(balance);
            }

            SessionHelper.getInstance().commitTransaction();
            SessionHelper.getInstance().markTransactionCompleted();
        } finally {
            SessionHelper.getInstance().clearWithUnlock();
        }

        return new LoginResponse(sessionInfo, accountInfo);
    }

    private LoginResponse login(short subCasinoId, int bankId, String remoteHost, ClientType clientType,
                                CWLoginRequest loginRequest) throws CommonException {
        String extUserId = loginRequest.getToken();
        Long balance = loginRequest.getBalance();

        if ((loginRequest.isCheckBalance() && balance == null) || extUserId == null) {
            throw new CommonException("incorrect login params");
        }

        if (StringUtils.isTrimmedEmpty(extUserId) || StringUtils.isTrimmedEmpty(remoteHost)) {
            throw new CommonException("incorrect credentials");
        }

        SessionInfo sessionInfo;
        AccountInfo accountInfo;
        SessionHelper.getInstance().lock(bankId, extUserId);
        boolean isLocked = true;
        try {
            SessionHelper.getInstance().openSession();
            accountInfo = AccountManager.getInstance().getAccountInfo(subCasinoId, bankId, extUserId);
            if (accountInfo == null) {
                throw new LoginErrorException(CommonWalletErrors.USER_NOT_FOUND);
            }
            long accountId = accountInfo.getId();
            GameSession gameSession = SessionHelper.getInstance().getTransactionData().getGameSession();
            BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(accountInfo.getBankId());
            //always close MQ games
            if (GameServer.getInstance().needCloseMultiplayerGame(gameSession, bankInfo, -1)) {
                SessionHelper.getInstance().commitTransaction();
                SessionHelper.getInstance().markTransactionCompleted();
                SessionHelper.getInstance().clearWithUnlock();
                isLocked = false;
                LoginHelper.performMaxQuestSitOut(accountInfo, gameSession, bankInfo);
                SessionHelper.getInstance().lock(bankId, extUserId);
                isLocked = true;
                SessionHelper.getInstance().openSession();
                accountInfo = SessionHelper.getInstance().getTransactionData().getAccount();
            }
            CWPlayerSessionManager psm = (CWPlayerSessionManager) PlayerSessionFactory.getInstance().getPlayerSessionManager(bankId);
            sessionInfo = psm.login(accountInfo, String.valueOf(accountId), remoteHost, clientType);
            if (balance != null) {
                accountInfo.setBalance(balance);
            }
            SessionHelper.getInstance().commitTransaction();
            SessionHelper.getInstance().markTransactionCompleted();
        } finally {
            if (isLocked) {
                SessionHelper.getInstance().clearWithUnlock();
            } else {
                LOG.debug("isLocked=" + isLocked + ", skip SessionHelper.getInstance().clearWithUnlock()");
            }
        }
        return new LoginResponse(sessionInfo, accountInfo);
    }
}
