package com.betsoft.casino.mp.model;

import com.betsoft.casino.utils.IServerMessage;
import com.betsoft.casino.utils.ITransportObject;

public interface IFRBEnded  extends ITransportObject, IServerMessage {
    long getWinSum();

    void setWinSum(long winSum);

    String getCloseReason();

    void setCloseReason(String closeReason);

    boolean isHasNextFrb();

    void setHasNextFrb(boolean hasNextFrb);

    long getRealWinSum();
}
