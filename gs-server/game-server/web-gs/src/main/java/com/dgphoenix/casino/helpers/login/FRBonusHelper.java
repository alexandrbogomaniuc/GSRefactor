package com.abs.casino.helpers.login;

import com.dgphoenix.casino.account.AccountManager;
import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.bonus.BonusStatus;
import com.dgphoenix.casino.common.cache.data.bonus.FRBonus;
import com.dgphoenix.casino.common.cache.data.game.GameMode;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.exception.PlayerHasBetsException;
import com.dgphoenix.casino.common.util.DigitFormatter;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.abs.casino.entities.AuthRequest;
import com.abs.casino.exceptions.LoginErrorException;
import com.dgphoenix.casino.gs.GameServer;
import com.dgphoenix.casino.gs.managers.payment.bonus.FRBonusManager;
import com.dgphoenix.casino.gs.managers.payment.wallet.CWError;
import com.dgphoenix.casino.gs.managers.payment.wallet.CommonWalletErrors;
import com.dgphoenix.casino.gs.managers.payment.wallet.v3.CommonWalletAuthResult;
import com.dgphoenix.casino.helpers.login.LoginHelper;
import com.abs.casino.sm.CWv3PlayerSessionManager;
import com.dgphoenix.casino.sm.PlayerSessionFactory;
import com.abs.casino.sm.login.BonusGameLoginRequest;
import com.abs.casino.sm.login.LoginResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * User: isirbis
 * Date: 03.10.14
 */
public class FRBonusHelper extends LoginHelper<AuthRequest, BonusGameLoginRequest> {
    private static final Logger LOG = LogManager.getLogger(FRBonusHelper.class);
    private static final FRBonusHelper instance = new FRBonusHelper();

    private FRBonusHelper() {
    }

    public static FRBonusHelper getInstance() {
        return instance;
    }

    public LoginResponse login(BonusGameLoginRequest loginRequest) throws LoginErrorException {
        CWError error = null;
        SessionInfo sessionInfo = null;
        AccountInfo accountInfo = null;
        try {

            LOG.debug("login = {}", loginRequest);

            if (StringUtils.isTrimmedEmpty(loginRequest.getToken()) && loginRequest.getGameMode().equals(GameMode.REAL)) {
                throw new CommonException("incorrect parameters");
            }

            CommonWalletAuthResult authResult;
            BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(loginRequest.getBankId());

            AuthRequest authRequest = new AuthRequest();
            authRequest.setToken(loginRequest.getToken());
            authRequest.setGameMode(loginRequest.getGameMode());
            authRequest.setRemoteHost(loginRequest.getRemoteHost());
            authRequest.setClientType(loginRequest.getClientType());
            authRequest.setProperties(loginRequest.getProperties());

            authResult = getAuthInfo(authRequest, loginRequest, bankInfo);

            SessionHelper.getInstance().lock(loginRequest.getBankId(), authResult.getUserId());
            boolean isLocked = true;
            try {
                SessionHelper.getInstance().openSession();
                final String externalId = authResult.getUserId();
                accountInfo = AccountManager.getInstance().getByCompositeKey(loginRequest.getSubCasinoId(), loginRequest.getBankId(), externalId);
                if (accountInfo == null) {
                    try {
                        accountInfo = createAccount(externalId, authResult.getCurrency(), authResult.getUserName(), authResult.getFirstName(),
                                authResult.getLastName(), authResult.getEmail(), loginRequest, bankInfo, authResult.getCountryCode());
                    } catch (CommonException e) {
                        error = CommonWalletErrors.USER_NOT_FOUND;
                        throw e;
                    }
                }
                FRBonus frBonus = FRBonusManager.getInstance().getById(loginRequest.getBonusId());
                if (!frBonus.getStatus().equals(BonusStatus.ACTIVE)) {
                    throw new CommonException("incorrect parameters::FRBonus is not active");
                }
                List<Long> frBonusList = FRBonusManager.getInstance().getFRBonusIdsList(accountInfo.getId());
                if (frBonusList == null || !frBonusList.contains(frBonus.getId())) {
                    throw new CommonException("incorrect parameters::FRBonusId is not found for this accountId=" +
                            accountInfo.getId() + ", bonusId=" + loginRequest.getBonusId());
                }
                if (!frBonus.getGameIds().contains(loginRequest.getGameId().longValue())) {
                    throw new CommonException("incorrect parameters::FRBonus is not contains for this game" + loginRequest.getGameId());
                }
                GameSession gameSession = SessionHelper.getInstance().getTransactionData().getGameSession();
                //always close MQ
                if (GameServer.getInstance().needCloseMultiplayerGame(gameSession, bankInfo, -1)) {
                    SessionHelper.getInstance().commitTransaction();
                    SessionHelper.getInstance().markTransactionCompleted();
                    SessionHelper.getInstance().clearWithUnlock();
                    isLocked = false;
                    LoginHelper.performMaxQuestSitOut(accountInfo, gameSession, bankInfo);
                    SessionHelper.getInstance().lock(loginRequest.getBankId(), authResult.getUserId());
                    isLocked = true;
                    SessionHelper.getInstance().openSession();
                    accountInfo = SessionHelper.getInstance().getTransactionData().getAccount();
                }
                CWv3PlayerSessionManager psm =
                        (CWv3PlayerSessionManager) PlayerSessionFactory.getInstance().getPlayerSessionManager(
                                loginRequest.getBankId());
                sessionInfo = psm.login(accountInfo, loginRequest.getToken(), loginRequest.getRemoteHost(),
                        loginRequest.getClientType());
                if (bankInfo.isAddTokenMode()) {
                    accountInfo.setFinsoftSessionId(loginRequest.getToken());
                }
                long balance;
                if (bankInfo.isParseLong()) {
                    balance = (long) authResult.getBalance();
                } else {
                    balance = DigitFormatter.getCentsFromCurrency(authResult.getBalance());
                }
                accountInfo.setBalance(balance);
                SessionHelper.getInstance().commitTransaction();
                SessionHelper.getInstance().markTransactionCompleted();
            } finally {
                if (isLocked) {
                    SessionHelper.getInstance().clearWithUnlock();
                } else {
                    LOG.debug("isLocked=" + isLocked + ", skip SessionHelper.getInstance().clearWithUnlock()");
                }
            }
        } catch (PlayerHasBetsException e) {
            LOG.error("CommonWalletLoginAction::process unable to login player due CDR", e);
            error = CommonWalletErrors.PLAYER_HAS_BETS_IN_CDR;
        } catch (Throwable e) {
            LOG.error("CommonWalletLoginAction::process unable to login player", e);
            if (error == null) {
                error = CommonWalletErrors.INTERNAL_ERROR;
            }
        }

        if (sessionInfo == null || error != null) {
            if (error == null) {
                error = CommonWalletErrors.INTERNAL_ERROR;
            }
            LOG.error("CommonWalletLoginAction::error: " + error);
            throw new LoginErrorException(error);
        }

        return new LoginResponse(sessionInfo, accountInfo);
    }

    private AccountInfo createAccount(String externalId, String extCurrency, String extNickName,
                                      String extFirstName, String extLastName, String extEmail,
                                      BonusGameLoginRequest loginRequest, BankInfo bankInfo, String countryCode)
            throws CommonException {
        return AccountManager.getInstance().saveAccountWithCurrencyUpdate(null, externalId,
                bankInfo, extNickName, false, false,
                extEmail, loginRequest.getClientType(), extFirstName, extLastName, extCurrency,
                countryCode, true);
    }
}
