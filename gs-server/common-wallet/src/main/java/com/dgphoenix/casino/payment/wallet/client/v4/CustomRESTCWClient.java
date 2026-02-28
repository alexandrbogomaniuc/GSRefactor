package com.abs.casino.payment.wallet.client.v4;

import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.payment.WalletOperationStatus;
import com.dgphoenix.casino.common.cache.data.session.ClientType;
import com.abs.casino.common.client.AbstractLoggableClient;
import com.dgphoenix.casino.common.exception.CommonException;
import com.abs.casino.common.exception.WalletException;
import com.abs.casino.common.rest.CustomRestTemplate;
import com.dgphoenix.casino.gs.managers.payment.wallet.*;
import com.abs.casino.gs.managers.payment.wallet.v3.CommonWalletAuthResult;
import com.dgphoenix.casino.gs.managers.payment.wallet.v4.CWMType;
import com.dgphoenix.casino.gs.managers.payment.wallet.v4.ICommonWalletClient;

import java.util.Collections;

public abstract class CustomRESTCWClient<T extends CustomRestTemplate> extends AbstractLoggableClient implements ICommonWalletClient {
    protected final BankInfo bankInfo;
    protected final T restTemplate;
    protected final SessionHelper sessionHelper;

    public CustomRESTCWClient(T restTemplate) {
        this(restTemplate.getBankInfo(), restTemplate, SessionHelper.getInstance());
    }

    public CustomRESTCWClient(BankInfo bankInfo, T restTemplate, SessionHelper sessionHelper) {
        this.bankInfo = bankInfo;
        this.restTemplate = restTemplate;
        this.sessionHelper = sessionHelper;
        restTemplate.setLoggableClient(this);
    }

    @Override
    public CommonWalletAuthResult auth(String token, ClientType clType) throws CommonException {
        return auth(token, null, clType);
    }

    @Override
    public CommonWalletAuthResult auth(String token, String gameId, ClientType clType) throws CommonException {
        return auth(token, gameId, null, clType);
    }

    @Override
    public CommonWalletAuthResult auth(String token, String gameId, String serverId, ClientType clType) throws CommonException {
        return auth(token, gameId, serverId, clType, Collections.emptyMap());
    }

    @Override
    public CommonWalletStatusResult getExternalTransactionStatus(long accountId, String extUserId, long transactionId, long bankId, CommonWalletOperation operation) throws CommonException {
        return null;
    }

    @Override
    public void cancelTransaction(long accountId, String extUserId, long transactionId, long bankId) throws CommonException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setWalletHelper(IWalletHelper walletHelper) {

    }

    @Override
    public void setAdditionalOperationProperties(CommonWalletOperation operation, IWalletDBLink dbLink) {

    }

    @Override
    public void completeOperation(AccountInfo accountInfo, long gameId, WalletOperationStatus internalStatus, CommonGameWallet gameWallet, CommonWalletOperation operation, IExternalWalletTransactionHandler extHandler) {

    }

    @Override
    public void revokeDebit(AccountInfo accountInfo, long bankId, long gameId, CommonWallet cWallet, CommonWalletOperation debitOperation, IExternalWalletTransactionHandler extHandler) throws WalletException {

    }

    @Override
    public boolean isAlwaysCompleteFailedCreditOperations() {
        return false;
    }

    @Override
    public boolean postProcessCreditException(WalletException e, CommonWallet cWallet, CommonWalletOperation creditOperation, AccountInfo accountInfo, long gameId) {
        return false;
    }

    @Override
    public void postProcessSuccessCredit(AccountInfo accountInfo, long gameId, long winAmount, Boolean isRoundFinished, CommonWallet cWallet, CommonWalletOperation operation) throws WalletException {

    }

    @Override
    public void postProcessSuccessDebit(AccountInfo accountInfo, long gameId, long betAmount, CommonWallet cWallet, CommonWalletOperation operation) throws WalletException {

    }

    @Override
    public boolean isIgnoreRoundFinishedParamOnWager() {
        return false;
    }

    @Override
    public boolean isCreditCondition(long winAmount, long negativeBetAmount, boolean isRoundFinished, IWalletDBLink dbLink) {
        CWMType cwmType = CWMType.getCWMTypeByString(bankInfo.getCWMType());
        return cwmType.isCreditCondition(winAmount, negativeBetAmount, isRoundFinished);
    }

    @Override
    public void setAdditionalRoundInfo(long accountId, long gameId, Long betAmount, Long winAmount, CommonWallet cWallet, IWalletDBLink dbLink) {

    }

    @Override
    public void postProcessCredit(AccountInfo accountInfo, long gameId, long winAmount, Boolean isRoundFinished, CommonWallet cWallet, CommonWalletOperation operation) {

    }

    @Override
    public void postProcessDebit(AccountInfo accountInfo, long gameId, long betAmount, CommonWallet cWallet, CommonWalletOperation operation) {

    }

}
