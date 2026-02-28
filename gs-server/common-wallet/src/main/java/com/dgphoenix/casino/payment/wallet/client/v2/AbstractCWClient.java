package com.abs.casino.payment.wallet.client.v2;

import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.abs.casino.gs.managers.payment.wallet.CommonWalletErrors;
import com.abs.casino.gs.managers.payment.wallet.IWalletHelper;
import com.dgphoenix.casino.gs.managers.payment.wallet.*;
import com.dgphoenix.casino.gs.managers.payment.wallet.v2.ICommonWalletClient;
import com.abs.casino.gs.managers.payment.wallet.v4.CWMType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * User: plastical
 * Date: 17.05.2010
 */
public abstract class AbstractCWClient implements ICommonWalletClient {
    private static final Logger LOG = LogManager.getLogger(AbstractCWClient.class);
    private long bankId;
    private boolean notIgnoreRoundFinishedParamOnWager;
    protected IWalletHelper walletHelper;

    public AbstractCWClient(long bankId) {
        this.bankId = bankId;
        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
        notIgnoreRoundFinishedParamOnWager = bankInfo.isNotIgnoreRoundFinishedParamOnWager();
    }

    public long getBankId() {
        return bankId;
    }

    public void setBankId(long bankId) {
        this.bankId = bankId;
    }

    @Override
    public void setWalletHelper(IWalletHelper walletHelper) {
        this.walletHelper = walletHelper;
    }

    protected boolean isPreciseWagerError(String errorCode) {
        if (!StringUtils.isTrimmedEmpty(errorCode)) {
            try {
                Long code = Long.valueOf(errorCode);
                CWError error = CommonWalletErrors.getCWErrorByCode(code.intValue());
                return (error != null && error.needCancelOperation()) ||
                        CommonWalletErrors.INSUFFICIENT_FUNDS.getCode() == code ||
                        CommonWalletErrors.OPERATION_FAILED.getCode() == code ||
                        CommonWalletErrors.UNKNOWN_USER_ID.getCode() == code ||
                        CommonWalletErrors.INVALID_HASH.getCode() == code ||
                        CommonWalletErrors.TRANSACTION_WAGER_LIMIT_REACHED.getCode() == code ||
                        CommonWalletErrors.WEEK_WAGER_LIMIT_REACHED.getCode() == code ||
                        CommonWalletErrors.RESPONSIBLE_LIMIT_REACHED.getCode() == code ||
                        CommonWalletErrors.MAXIMUM_BONUS_BET.getCode() == code ||
                        CommonWalletErrors.BONUS_WAGER_REQUIREMENT_REACHED.getCode() == code ||
                        CommonWalletErrors.SESSION_CLOSED.getCode() == code ||
                        CommonWalletErrors.REALITY_CHECK_REQUIRED.getCode() == code;
            } catch (NumberFormatException e) {
                LOG.error("NumberFormatException: " + errorCode);
            }
        }
        return false;
    }

    @Override
    public boolean isIgnoreRoundFinishedParamOnWager() {
        return !notIgnoreRoundFinishedParamOnWager;
    }

    @Override
    public boolean isCreditCondition(long winAmount, long negativeBetAmount, boolean isRoundFinished, IWalletDBLink dbLink) {
        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
        CWMType cwmType = CWMType.getCWMTypeByString(bankInfo.getCWMType());
        return cwmType.isCreditCondition(winAmount, negativeBetAmount, isRoundFinished);
    }

    @Override
    public void setAdditionalRoundInfo(long accountId, long gameId, Long betAmount, Long winAmount, CommonWallet cWallet, IWalletDBLink dbLink) {
        // nop by default
    }

    @Override
    public void postProcessCredit(AccountInfo accountInfo, long gameId, long winAmount, Boolean isRoundFinished, CommonWallet cWallet, CommonWalletOperation operation) {
        if (isRoundFinished) {
            cWallet.getGameWallet((int) gameId).clearAdditionalRoundInfo();
        }
    }

    @Override
    public void postProcessDebit(AccountInfo accountInfo, long gameId, long betAmount, CommonWallet cWallet, CommonWalletOperation operation) {
        CommonGameWallet gameWallet = cWallet.getGameWallet((int) gameId);
        if (gameWallet != null && gameWallet.getRoundId() == null) {
            gameWallet.clearAdditionalRoundInfo();
        }
    }
}
