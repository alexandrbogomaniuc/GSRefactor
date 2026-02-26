package com.abs.casino.common.cache.data.payment.frb;

import com.dgphoenix.casino.common.cache.data.payment.frb.FRBWinOperationStatus;
import com.dgphoenix.casino.common.cache.data.session.ClientType;

public interface IFRBonusWinOperation {
    long getId();

    long getAccountId();

    Long getGameSessionId();

    Long getRoundId();

    long getAmount();

    String getDescription();

    FRBWinOperationStatus getExternalStatus();

    FRBWinOperationStatus getInternalStatus();

    long getStartTime();

    Long getEndTime();

    String getExternalRoundId();

    String getExternalSessionId();

    String getExternalTransactionId();

    boolean isOverdue(long time);

    ClientType getClientType();

    void setClientType(ClientType clientType);
}
