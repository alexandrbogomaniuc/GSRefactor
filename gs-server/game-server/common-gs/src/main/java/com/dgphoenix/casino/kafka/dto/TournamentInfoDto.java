package com.abs.casino.kafka.dto;

import com.abs.casino.kafka.dto.BasicKafkaResponse;

public class TournamentInfoDto extends BasicKafkaResponse {
    private long tournamentId; 
    private String name; 
    private String state; 
    private long startDate; 
    private long endDate; 
    private long balance; 
    private long buyInPrice; 
    private long buyInAmount; 
    private boolean reBuyAllowed; 
    private long reBuyPrice; 
    private long reBuyAmount; 
    private int reBuyCount; 
    private int reBuyLimit; 
    private boolean resetBalanceAfterRebuy; 

    public TournamentInfoDto() {}

    public TournamentInfoDto(long tournamentId,
            String name,
            String state,
            long startDate,
            long endDate,
            long balance,
            long buyInPrice,
            long buyInAmount,
            boolean reBuyAllowed,
            long reBuyPrice,
            long reBuyAmount,
            int reBuyCount,
            int reBuyLimit,
            boolean resetBalanceAfterRebuy) {
        super(true, 0, "");
        this.tournamentId = tournamentId;
        this.name = name;
        this.state = state;
        this.startDate = startDate;
        this.endDate = endDate;
        this.balance = balance;
        this.buyInPrice = buyInPrice;
        this.buyInAmount = buyInAmount;
        this.reBuyAllowed = reBuyAllowed;
        this.reBuyPrice = reBuyPrice;
        this.reBuyAmount = reBuyAmount;
        this.reBuyCount = reBuyCount;
        this.reBuyLimit = reBuyLimit;
        this.resetBalanceAfterRebuy = resetBalanceAfterRebuy;
    }

    public TournamentInfoDto(boolean success, int errorCode, String errorDetails) {
        super(success, errorCode, errorDetails);
    }

    public long getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(long tournamentId) {
        this.tournamentId = tournamentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public long getEndDate() {
        return endDate;
    }

    public void setEndDate(long endDate) {
        this.endDate = endDate;
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public long getBuyInPrice() {
        return buyInPrice;
    }

    public void setBuyInPrice(long buyInPrice) {
        this.buyInPrice = buyInPrice;
    }

    public long getBuyInAmount() {
        return buyInAmount;
    }

    public void setBuyInAmount(long buyInAmount) {
        this.buyInAmount = buyInAmount;
    }

    public boolean isReBuyAllowed() {
        return reBuyAllowed;
    }

    public void setReBuyAllowed(boolean reBuyAllowed) {
        this.reBuyAllowed = reBuyAllowed;
    }

    public long getReBuyPrice() {
        return reBuyPrice;
    }

    public void setReBuyPrice(long reBuyPrice) {
        this.reBuyPrice = reBuyPrice;
    }

    public long getReBuyAmount() {
        return reBuyAmount;
    }

    public void setReBuyAmount(long reBuyAmount) {
        this.reBuyAmount = reBuyAmount;
    }

    public int getReBuyCount() {
        return reBuyCount;
    }

    public void setReBuyCount(int reBuyCount) {
        this.reBuyCount = reBuyCount;
    }

    public int getReBuyLimit() {
        return reBuyLimit;
    }

    public void setReBuyLimit(int reBuyLimit) {
        this.reBuyLimit = reBuyLimit;
    }

    public boolean isResetBalanceAfterRebuy() {
        return resetBalanceAfterRebuy;
    }

    public void setResetBalanceAfterRebuy(boolean resetBalanceAfterRebuy) {
        this.resetBalanceAfterRebuy = resetBalanceAfterRebuy;
    }
}
