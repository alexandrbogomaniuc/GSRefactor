package com.abs.casino.common.transactiondata;

import com.abs.casino.common.cache.IDistributedCache;
import com.abs.casino.common.lock.LockingInfo;
import com.abs.casino.common.lock.ServerLockInfo;
import com.abs.casino.common.transactiondata.ITransactionData;

import java.util.List;

/**
 * User: flsh
 * Date: 7/4/12
 */
public interface ITransactionDataPersister extends IDistributedCache<String, ITransactionData> {
    void setGameServerId(int gameServerId);

    int getGameServerId();

    void persist(ITransactionData data);

    ITransactionData get(LockingInfo lockInfo);

    boolean delete(String lockId);

    boolean delete(ITransactionData data);

    void invalidate(ServerLockInfo lockInfo);

    void persistWallet(ITransactionData data);

    void persistAccount(ITransactionData data);

    void persistPaymentTransaction(ITransactionData data, boolean saveAccount);

    void persistBonus(ITransactionData data);

    void persistFrBonus(ITransactionData data);

    void persistFrbNotification(ITransactionData data);

    void persistFrbWin(ITransactionData data);

    List<OnlineSessionInfo> getOnlineSessionInfos(Integer gameServerId, Integer bankId, boolean withEmptyGameSession);

    void persistPlayerBet(ITransactionData data);
}
