package com.betsoft.casino.mp.model;

/**
 * User: flsh
 * Date: 16.07.2020.
 */
public class SitOutCashBonusSessionResult extends SitOutResult implements ISitOutCashBonusSessionResult {
    private ActiveCashBonusSession cashBonusSession;
    private Long activeFRBonusId;

    public SitOutCashBonusSessionResult(boolean success, int errorCode, String errorDetails,
                                        ActiveCashBonusSession cashBonusSession, Long activeFRBonusId) {
        super(success, errorCode, errorDetails);
        this.cashBonusSession = cashBonusSession;
        this.activeFRBonusId = activeFRBonusId;
    }

    @Override
    public IActiveCashBonusSession getCashBonus() {
        return cashBonusSession;
    }

    @Override
    public Long getActiveFRBonusId() {
        return activeFRBonusId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SitOutCashBonusSessionResult [");
        sb.append("cashBonusSession=").append(cashBonusSession);
        sb.append(", success=").append(success);
        sb.append(", activeFRBonusId=").append(activeFRBonusId);
        sb.append(", errorCode=").append(errorCode);
        sb.append(", errorDetails='").append(errorDetails).append('\'');
        sb.append(']');
        return sb.toString();
    }
}
