package com.betsoft.casino.mp.model.battleground;

import com.betsoft.casino.utils.IServerMessage;
import com.betsoft.casino.utils.ITransportObject;

public interface ICancelBattlegroundRound extends ITransportObject, IServerMessage {
    long getRefundedAmount();

    void setRefundedAmount(long refundedAmount);

    String getReason();

    void setReason(String reason);
}
