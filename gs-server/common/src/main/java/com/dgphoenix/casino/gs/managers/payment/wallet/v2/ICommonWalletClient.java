package com.dgphoenix.casino.gs.managers.payment.wallet.v2;

import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.currency.Currency;
import com.dgphoenix.casino.common.cache.data.payment.WalletOperationStatus;
import com.dgphoenix.casino.common.cache.data.session.ClientType;
import com.dgphoenix.casino.common.exception.CommonException;
import com.abs.casino.common.exception.WalletException;
import com.abs.casino.gs.managers.payment.wallet.CommonWalletStatusResult;
import com.abs.casino.gs.managers.payment.wallet.CommonWalletWagerResult;
import com.abs.casino.gs.managers.payment.wallet.IWalletHelper;
import com.dgphoenix.casino.gs.managers.payment.wallet.*;

/**
 * User: plastical
 * Date: 17.05.2010
 */
public interface ICommonWalletClient {
    String CLIENT_TYPE_PARAM = "clientType";

    CommonWalletWagerResult wager(long accountId, String extUserId,
                                  String bet, //bet_amount|transactionID
                                  String win, //win_amount|transactionID
                                  Boolean isRoundFinished, long gsRoundId, long mpRoundId,
                                  long gameId, long bankId,
                                  CommonWalletOperation operation,
                                  CommonWallet wallet, ClientType clienttype,
                                  Currency currency)
            throws CommonException;

    CommonWalletStatusResult getExternalTransactionStatus(long accountId, String extUserId,
                                                          long transactionId, long bankId,
                                                          CommonWalletOperation operation)
            throws CommonException;

    void setWalletHelper(IWalletHelper walletHelper);

    double getBalance(long accountId, String extUserId, long bankId, Currency currency) throws CommonException;

    void cancelTransaction(long accountId, String extUserId, long transactionId, long bankId)
            throws CommonException;

    void setAdditionalOperationProperties(CommonWalletOperation operation, IWalletDBLink dbLink);

    void completeOperation(AccountInfo accountInfo, long gameId, WalletOperationStatus internalStatus,
                           CommonGameWallet gameWallet, CommonWalletOperation operation,
                           IExternalWalletTransactionHandler extHandler);

    void revokeDebit(AccountInfo accountInfo, long bankId, long gameId, CommonWallet cWallet,
                     CommonWalletOperation debitOperation,
                     IExternalWalletTransactionHandler extHandler) throws WalletException;

    boolean isAlwaysCompleteFailedCreditOperations();

    //return true if need re-throw exception
    boolean postProcessCreditException(WalletException e, CommonWallet cWallet,
                                       CommonWalletOperation creditOperation,
                                       AccountInfo accountInfo, long gameId);

    void postProcessSuccessCredit(AccountInfo accountInfo, long gameId, long winAmount, Boolean isRoundFinished,
                                  CommonWallet cWallet, CommonWalletOperation operation)
            throws WalletException;

    void postProcessSuccessDebit(AccountInfo accountInfo, long gameId, long betAmount,
                                 CommonWallet cWallet, CommonWalletOperation operation)
            throws WalletException;

    boolean isIgnoreRoundFinishedParamOnWager();

    boolean isCreditCondition(long winAmount, long negativeBetAmount, boolean isRoundFinished, IWalletDBLink dbLink);

    void setAdditionalRoundInfo(long accountId, long gameId, Long betAmount, Long winAmount, CommonWallet cWallet, IWalletDBLink dbLink);

    void postProcessCredit(AccountInfo accountInfo, long gameId, long winAmount, Boolean isRoundFinished,
                           CommonWallet cWallet, CommonWalletOperation operation);

    void postProcessDebit(AccountInfo accountInfo, long gameId, long betAmount,
                          CommonWallet cWallet, CommonWalletOperation operation);
}
