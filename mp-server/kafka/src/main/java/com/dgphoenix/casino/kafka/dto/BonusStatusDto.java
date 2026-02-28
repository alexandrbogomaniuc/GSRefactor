package com.abs.casino.kafka.dto;

public class BonusStatusDto implements KafkaRequest {
    private String status;
    private long bonusId;
    long accountId;

    public BonusStatusDto() {}

    public BonusStatusDto(String status, long bonusId, long accountId) {
        this.status = status;
        this.bonusId = bonusId;
        this.accountId = accountId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getBonusId() {
        return bonusId;
    }

    public void setBonusId(long bonusId) {
        this.bonusId = bonusId;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }
}
