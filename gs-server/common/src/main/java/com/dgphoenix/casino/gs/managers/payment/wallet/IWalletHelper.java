package com.abs.casino.gs.managers.payment.wallet;

import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.account.LasthandInfo;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.bet.PlayerBet;
import com.dgphoenix.casino.common.cache.data.bonus.BonusSystemType;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.promo.PromoWinInfo;
import com.dgphoenix.casino.gs.managers.payment.wallet.CommonGameWallet;
import com.dgphoenix.casino.gs.managers.payment.wallet.CommonWallet;
import com.dgphoenix.casino.gs.managers.payment.wallet.CommonWalletOperation;
import com.dgphoenix.casino.gs.managers.payment.wallet.IWalletOperation;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * User: flsh
 * Date: 15.11.14.
 */
public interface IWalletHelper {
    long getBankId();

    AccountInfo getAccountInfo(long accountId);

    AccountInfo getAccountInfo(short subcasinoId, int bankId, String extUserId) throws CommonException;

    GameSession getOnlineGameSession(long gameSessionId) throws CommonException;

    GameSession getGameSession(long gameSessionId);

    SessionInfo getSessionInfo();

    LasthandInfo getLasthand(long id, long accountId, long gameId, Long bonusId,
                             BonusSystemType bonusSystemType);

    boolean isLastHandExist(long id, long accountId, long gameId, Long bonusId,
                            BonusSystemType bonusSystemType);

    void addWalletTrackerTask(Long accountId);

    void addHighPriorityWalletTrackerTask(Long accountId);

    boolean isRegisteredForTracking(String trackerName, long trackedObjectId);

    void registerForTracking(String trackerName, long trackedObjectId);

    void unregisterFromTracking(String trackerName, long trackedObjectId);

    void persistWalletOperation(AccountInfo accountInfo, CommonWalletOperation operation);

    void persistWalletOperation(BankInfo bankInfo, AccountInfo accountInfo, CommonWalletOperation operation,
                                long gameId, long balance);

    void updateWalletOperation(BankInfo bankInfo, AccountInfo accountInfo, CommonWalletOperation operation);

    void updateWalletOperation(BankInfo bankInfo, AccountInfo accountInfo, CommonWalletOperation operation, boolean refunded);

    void persistGameWallet(long accountId, CommonGameWallet gameWallet);

    @Deprecated
    void clearPersistedFameWallets(long accountId);

    void persistCommonWallet(CommonWallet wallet);

    void persistDailyWallet(CommonWalletOperation operation, long bankId);

    void persistMinMaxWallet(long subCasinoId, long date, long bankId, String extUserId, long transactionId,
                             long roundId, long gameSessionId, String externalTransactionId,
                             long endTime);

    Map<Long, List<PlayerBet>> getPlayerBets(Map<Long, List<Long>> sessionBetsMap);

    boolean tryResolvePendingOperations(BankInfo bankInfo, String extUserId, Consumer<IWalletOperation> beforeSend,
                                        Integer gameId) throws CommonException;

    PromoWinInfo getPromoWinInfo(long roundId);

    void removePromoWin(long roundId);
}
