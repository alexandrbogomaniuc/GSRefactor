package com.abs.casino.gs.managers.payment.wallet;

import com.abs.casino.common.cache.IDistributedCacheEntry;
import com.abs.casino.common.cache.data.payment.WalletOperationStatus;
import com.abs.casino.common.cache.data.payment.WalletOperationType;

/**
 * User: plastical
 * Date: 05.03.2010
 */
public interface IWalletOperation extends IDistributedCacheEntry {
    long getId();

    long getAccountId();

    Long getGameSessionId();

    Long getRoundId();

    long getAmount();

    WalletOperationType getType();

    String getDescription();

    WalletOperationStatus getExternalStatus();

    void setExternalStatus(WalletOperationStatus status);

    WalletOperationStatus getInternalStatus();

    long getStartTime();

    long getEndTime();

    String getExternalRoundId();

    String getExternalSessionId();

    String getExternalTransactionId();

    boolean isOverdue(long time);

    long getNegativeBet();

    String getAdditionalProperties();

    void setAdditionalProperties(String additionalProperties);
}
