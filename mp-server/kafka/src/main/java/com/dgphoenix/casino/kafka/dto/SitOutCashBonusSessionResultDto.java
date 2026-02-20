package com.dgphoenix.casino.kafka.dto;

public class SitOutCashBonusSessionResultDto extends BasicKafkaResponse {
    private CashBonusDto cashBonus;
    private long activeFRBonusId;

    public SitOutCashBonusSessionResultDto() {}

    public SitOutCashBonusSessionResultDto(CashBonusDto cashBonus,
            long activeFRBonusId,
            boolean success,
            int errorCode,
            String errorDetails) {
        super(success, errorCode, errorDetails);
        this.cashBonus = cashBonus;
        this.activeFRBonusId = activeFRBonusId;
    }

    public SitOutCashBonusSessionResultDto(boolean success, int errorCode, String errorDetails) {
        super(success, errorCode, errorDetails);
    }

    public CashBonusDto getCashBonus() {
        return cashBonus;
    }

    public long getActiveFRBonusId() {
        return activeFRBonusId;
    }

    public void setCashBonus(CashBonusDto cashBonus) {
        this.cashBonus = cashBonus;
    }

    public void setActiveFRBonusId(long activeFRBonusId) {
        this.activeFRBonusId = activeFRBonusId;
    }
}
