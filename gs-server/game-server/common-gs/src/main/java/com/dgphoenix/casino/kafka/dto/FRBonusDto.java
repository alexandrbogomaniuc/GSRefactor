package com.abs.casino.kafka.dto;

public class FRBonusDto {
    private long bonusId;
    private long awardDate;
    private long startDate;
    private long expirationDate;
    private long rounds;
    private long roundsLeft;
    private long winSum;
    private long spinCost;
    private long maxWinLimit;

    public FRBonusDto() {
    }

    public FRBonusDto(long bonusId,
            long awardDate,
            long startDate,
            long expirationDate,
            long rounds,
            long roundsLeft,
            long winSum,
            long spinCost,
            long maxWinLimit) {
        super();
        this.bonusId = bonusId;
        this.awardDate = awardDate;
        this.startDate = startDate;
        this.expirationDate = expirationDate;
        this.rounds = rounds;
        this.roundsLeft = roundsLeft;
        this.winSum = winSum;
        this.spinCost = spinCost;
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

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public long getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(long expirationDate) {
        this.expirationDate = expirationDate;
    }

    public long getRounds() {
        return rounds;
    }

    public void setRounds(long rounds) {
        this.rounds = rounds;
    }

    public long getRoundsLeft() {
        return roundsLeft;
    }

    public void setRoundsLeft(long roundsLeft) {
        this.roundsLeft = roundsLeft;
    }

    public long getWinSum() {
        return winSum;
    }

    public void setWinSum(long winSum) {
        this.winSum = winSum;
    }

    public long getSpinCost() {
        return spinCost;
    }

    public void setSpinCost(long spinCost) {
        this.spinCost = spinCost;
    }

    public long getMaxWinLimit() {
        return maxWinLimit;
    }

    public void setMaxWinLimit(long maxWinLimit) {
        this.maxWinLimit = maxWinLimit;
    }
}
