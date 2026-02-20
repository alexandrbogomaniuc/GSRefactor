package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.battleground.ICancelBattlegroundRound;
import com.betsoft.casino.utils.TInboundObject;

import java.util.Objects;

public class CancelBattlegroundRound extends TInboundObject implements ICancelBattlegroundRound {
    private long refundedAmount;
    private String reason;

    public CancelBattlegroundRound(long date, int rid, long refundedAmount, String reason) {
        super(date, rid);
        this.refundedAmount = refundedAmount;
        this.reason = reason;
    }

    public long getRefundedAmount() {
        return refundedAmount;
    }

    public void setRefundedAmount(long refundedAmount) {
        this.refundedAmount = refundedAmount;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        CancelBattlegroundRound that = (CancelBattlegroundRound) o;
        return refundedAmount == that.refundedAmount && reason.equals(that.reason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), refundedAmount, reason);
    }

    @Override
    public String toString() {
        return "CancelBattlegroundRound{" +
                "date=" + date +
                ", rid=" + rid +
                ", refundedAmount=" + refundedAmount +
                ", reason='" + reason + '\'' +
                '}';
    }
}
