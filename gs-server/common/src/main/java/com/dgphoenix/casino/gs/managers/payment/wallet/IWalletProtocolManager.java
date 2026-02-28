package com.dgphoenix.casino.gs.managers.payment.wallet;

import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.game.GameMode;
import com.dgphoenix.casino.common.cache.data.payment.IWallet;
import com.dgphoenix.casino.common.cache.data.payment.WalletOperationStatus;
import com.dgphoenix.casino.common.cache.data.session.ClientType;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.abs.casino.common.exception.WalletException;
import com.dgphoenix.casino.gs.managers.payment.wallet.v2.ICommonWalletClient;

/**
 * User: plastical
 * Date: 02.03.2010
 */
public interface IWalletProtocolManager {
    ICommonWalletClient getClient();

    void init(IWalletHelper helper);

    void setJarInfo(String jarInfo);

    long getBankId();

    BankInfo getBankInfo();

    IWallet handleCreateWallet(AccountInfo accountInfo, long gameSessionId, int gameId, GameMode mode,
                               ClientType clientType);

    void handleDestroyWallet(AccountInfo account, int gameId, GameMode mode, IWallet wallet) throws WalletException;

    void handleDebit(long accountId, long betAmount, IWalletDBLink dbLink, SessionInfo sessionInfo,
                     IExternalWalletTransactionHandler extHandler, long mpRoundId) throws WalletException;

    void handleNegativeBet(long accountId, long bankId, long betAmount, IWalletDBLink dbLink, SessionInfo sessionInfo)
            throws WalletException;

    void handleGameLogicCompleted(long accountId, long betAmount, long winAmount) throws WalletException;

    void handleDebitCompleted(long accountId, boolean isBet, IWalletDBLink dbLink,
                              IExternalWalletTransactionHandler extHandler) throws WalletException;

    void handleCredit(long accountId, boolean isRoundFinished, IWalletDBLink dbLink, SessionInfo sessionInfo,
                      IExternalWalletTransactionHandler extHandler) throws WalletException;

    long getNegativeBet(CommonWallet cWallet, int gameId) throws WalletException;

    void handleCreditCompleted(long accountId, boolean isRoundFinished, IWalletDBLink dbLink,
                               IExternalWalletTransactionHandler extHandler, long mpRoundId) throws WalletException;

    void handleFailure(AccountInfo accountInfo) throws WalletException;

    void handleFailure(AccountInfo accountInfo, int gameId) throws WalletException;

    boolean isWalletRemovingEnabled();

    boolean removeWallet(AccountInfo account, int gameId);

    long getWalletRoundId(IWalletDBLink dbLink) throws WalletException;

    boolean isPersistWalletOperation();

    void setPersistWalletOperation(boolean persistWalletOperation);

    boolean credit(AccountInfo accountInfo, long gameId, long winAmount, Boolean isRoundFinished,
                   CommonWallet cWallet, CommonWalletOperation operation, boolean isSyncOperation, long mpRoundId)
            throws WalletException;

    void completeOperation(AccountInfo accountInfo, long gameId, WalletOperationStatus internalStatus,
                           CommonGameWallet gameWallet, CommonWalletOperation operation,
                           IExternalWalletTransactionHandler extHandler) throws WalletException;
}
