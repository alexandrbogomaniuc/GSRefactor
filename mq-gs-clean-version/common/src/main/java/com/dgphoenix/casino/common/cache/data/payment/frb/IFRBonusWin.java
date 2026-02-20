package com.dgphoenix.casino.common.cache.data.payment.frb;

import com.dgphoenix.casino.common.cache.IDistributedCacheEntry;


public interface IFRBonusWin extends IDistributedCacheEntry {

    long getAccountId();

    IFRBonusWinOperation getCurrentFRBonusWinOperation(Long gameId);

    IFRBonusWin copy(IFRBonusWin source);

    boolean isAnyFRBWinOperationExist();

    boolean isHasAnyFRBonusWinWithAnyAmount();

    Long getFRBonusWinGameSessionId(long gameId);
}
