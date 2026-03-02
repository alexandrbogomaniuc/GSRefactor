package com.abs.casino.kafka.dto;

import com.abs.casino.kafka.dto.BasicKafkaResponse;

public class CloseFRBonusResultDto extends BasicKafkaResponse {
    private long nextFRBonusId;
    private long balance;
    private long realWinSum;

    public CloseFRBonusResultDto() {}

    public CloseFRBonusResultDto(long nextFRBonusId, long balance, long realWinSum, boolean success, int statusCode, String reasonPhrases) {
        super(success, statusCode, reasonPhrases);
        this.nextFRBonusId = nextFRBonusId;
        this.balance = balance;
        this.realWinSum = realWinSum;
    }

    public CloseFRBonusResultDto(boolean success, int statusCode, String reasonPhrases) {
        super(success, statusCode, reasonPhrases);
    }

    public CloseFRBonusResultDto(long nextFRBonusId, long balance, long realWinSum) {
        super(true, 0, "");
        this.nextFRBonusId = nextFRBonusId;
        this.balance = balance;
        this.realWinSum = realWinSum;
    }

    public long getNextFRBonusId() {
        return nextFRBonusId;
    }

    public long getBalance() {
        return balance;
    }

    public long getRealWinSum() {
        return realWinSum;
    }

    public void setNextFRBonusId(long nextFRBonusId) {
        this.nextFRBonusId = nextFRBonusId;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public void setRealWinSum(long realWinSum) {
        this.realWinSum = realWinSum;
    }


}
