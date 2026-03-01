package com.abs.casino.common.cache.data.payment;

import com.abs.casino.common.cache.IDistributedCacheEntry;
import com.abs.casino.common.exception.WalletException;
import com.dgphoenix.casino.gs.managers.payment.wallet.CommonGameWallet;
import com.abs.casino.gs.managers.payment.wallet.IWalletOperation;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * User: plastical
 * Date: 04.03.2010
 */
public interface IWallet extends IDistributedCacheEntry {
    Set<Integer> getWalletGamesIds();

    long getAccountId();

    IWalletOperation getCurrentWalletOperation(int gameId);

    IWalletOperation getCurrentWalletOperation(CommonGameWallet commonGameWallet);

    boolean isAnyWalletOperationExist();

    boolean hasGameWallets();

    boolean isHasAnyGameWalletWithAnyAmount();

    boolean isHasAnyGameWalletWithNotEmptyRoundId();

    //gameId:roundId
    Map<Integer, Long> getUnfinishedGames();

    int getGameWalletsSize();

    long getServerBalance();

    void setServerBalance(long serverBalance);

    CommonGameWallet getGameWallet(int gameId);

    Collection<CommonGameWallet> getCommonGameWallets();

    Long getGameWalletGameSessionId(int gameId);

    Long getGameWalletRoundId(int gameId) throws WalletException;

    void setGameWalletRoundId(int gameId, Long roundId) throws WalletException;

    void increaseWinAmount(int gameId, long winAmount) throws WalletException;

    void removeGameWallet(int gameId);

    boolean removeGameWalletSafely(int gameId);
}
