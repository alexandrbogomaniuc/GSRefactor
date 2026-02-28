package com.abs.casino.kafka.dto;

import com.dgphoenix.casino.kafka.dto.BasicKafkaResponse;

public class CashBonusDto extends BasicKafkaResponse {
    private long bonusId;
    private long awardDate;
    private long expirationDate;
    private long balance;
    private long amount;
    private long amountToRelease;
    private double rolloverMultiplier;
    private long betSum;
    private String status;
    private long maxWinLimit;

    public CashBonusDto() {}

    public CashBonusDto(boolean success, int errorCode, String errorDetails) {
        super(success, errorCode, errorDetails);
    }

    public CashBonusDto(long bonusId,
            long awardDate,
            long expirationDate,
            long balance,
            long amount,
            long amountToRelease,
            double rolloverMultiplier,
            long betSum,
            String status,
            long maxWinLimit) {
        super(true, 0, "");
        this.bonusId = bonusId;
        this.awardDate = awardDate;
        this.expirationDate = expirationDate;
        this.balance = balance;
        this.amount = amount;
        this.amountToRelease = amountToRelease;
        this.rolloverMultiplier = rolloverMultiplier;
        this.betSum = betSum;
        this.status = status;
        this.maxWinLimit = maxWinLimit;
    }

    public long getBonusId() {
        return bonusId;
    }

    public void setBonusId(long bonusId) {
        this.bonusId = bonusId;
    }

    public long getAwardDate() {
        return awardDate;
    }

    public void setAwardDate(long awardDate) {
        this.awardDate = awardDate;
    }

    public long getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(long expirationDate) {
        this.expirationDate = expirationDate;
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public long getAmountToRelease() {
        return amountToRelease;
    }

    public void setAmountToRelease(long amountToRelease) {
        this.amountToRelease = amountToRelease;
    }

    public double getRolloverMultiplier() {
        return rolloverMultiplier;
    }

    public void setRolloverMultiplier(double rolloverMultiplier) {
        this.rolloverMultiplier = rolloverMultiplier;
    }

    public long getBetSum() {
        return betSum;
    }

    public void setBetSum(long betSum) {
        this.betSum = betSum;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getMaxWinLimit() {
        return maxWinLimit;
    }

    public void setMaxWinLimit(long maxWinLimit) {
        this.maxWinLimit = maxWinLimit;
    }
}
