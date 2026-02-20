package com.dgphoenix.casino.common.promo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class MaxBalanceTournamentPlayerDetails implements KryoSerializable, TournamentPlayerDetails {
    private static final byte VERSION = 2;

    private long bankId;
    private String extAccountId;
    private long accountId;
    private long campaignId;
    private String nickname;
    private long currentBalance;
    private long buyInAmount;
    private int reBuyCount;
    private long reBuyAmount;
    private long maxBalance;
    private long timeJoined;
    private long betAmount;

    public MaxBalanceTournamentPlayerDetails() {}

    public MaxBalanceTournamentPlayerDetails(long bankId, String extAccountId, long accountId, long campaignId,
                                             String nickname, long currentBalance, long buyInAmount, int reBuyCount,
                                             long reBuyAmount, long maxBalance, long timeJoined, long betAmount) {
        this.bankId = bankId;
        this.extAccountId = extAccountId;
        this.accountId = accountId;
        this.campaignId = campaignId;
        this.nickname = nickname;
        this.currentBalance = currentBalance;
        this.buyInAmount = buyInAmount;
        this.reBuyCount = reBuyCount;
        this.reBuyAmount = reBuyAmount;
        this.maxBalance = maxBalance;
        this.timeJoined = timeJoined;
        this.betAmount = betAmount;
    }

    public long getBankId() {
        return bankId;
    }

    public void setBankId(long bankId) {
        this.bankId = bankId;
    }

    public String getExtAccountId() {
        return extAccountId;
    }

    public void setExtAccountId(String extAccountId) {
        this.extAccountId = extAccountId;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public long getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(long campaignId) {
        this.campaignId = campaignId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public long getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(long currentBalance) {
        this.currentBalance = currentBalance;
    }

    public long addCurrentBalance(long balance) {
        this.currentBalance += balance;
        return this.currentBalance;
    }

    public long getBuyInAmount() {
        return buyInAmount;
    }

    public void setBuyInAmount(long buyInAmount) {
        this.buyInAmount = buyInAmount;
    }

    public int getReBuyCount() {
        return reBuyCount;
    }

    public void setReBuyCount(int reBuyCount) {
        this.reBuyCount = reBuyCount;
    }

    public int incrementReBuyCount() {
        this.reBuyCount++;
        return this.reBuyCount;
    }

    public long getReBuyAmount() {
        return reBuyAmount;
    }

    public void setReBuyAmount(long reBuyAmount) {
        this.reBuyAmount = reBuyAmount;
    }

    public long addReBuyAmount(long reBuyAmount) {
        this.reBuyAmount += reBuyAmount;
        return this.reBuyAmount;
    }

    public long getMaxBalance() {
        return maxBalance;
    }

    public void setMaxBalance(long maxBalance) {
        this.maxBalance = maxBalance;
    }

    public long getTimeJoined() {
        return timeJoined;
    }

    public void setTimeJoined(long timeJoined) {
        this.timeJoined = timeJoined;
    }

    public long getBetAmount() {
        return betAmount;
    }

    public void setBetAmount(long betAmount) {
        this.betAmount = betAmount;
    }

    public long addBetAmount(long realBetAmount) {
        this.betAmount += realBetAmount;
        return this.betAmount;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeLong(accountId, true);
        output.writeLong(campaignId, true);
        output.writeString(nickname);
        output.writeLong(currentBalance, true);
        output.writeLong(buyInAmount, true);
        output.writeInt(reBuyCount, true);
        output.writeLong(reBuyAmount, true);
        output.writeLong(maxBalance, true);
        output.writeLong(timeJoined, true);
        output.writeLong(bankId, true);
        output.writeString(extAccountId);
        output.writeLong(betAmount, true);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        int version = input.readByte();
        accountId = input.readLong(true);
        campaignId = input.readLong(true);
        nickname = input.readString();
        currentBalance = input.readLong(true);
        buyInAmount = input.readLong(true);
        reBuyCount = input.readInt(true);
        reBuyAmount = input.readLong(true);
        maxBalance = input.readLong(true);
        timeJoined = input.readLong(true);
        if (version > 0) {
            this.bankId = input.readLong(true);
            this.extAccountId = input.readString();
        }
        if (version > 1) {
            this.betAmount = input.readLong(true);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MaxBalanceTournamentPlayerDetails that = (MaxBalanceTournamentPlayerDetails) o;
        return accountId == that.accountId &&
                campaignId == that.campaignId &&
                nickname.equals(that.nickname) &&
                currentBalance == that.currentBalance &&
                buyInAmount == that.buyInAmount &&
                reBuyCount == that.reBuyCount &&
                reBuyAmount == that.reBuyAmount &&
                maxBalance == that.maxBalance &&
                timeJoined == that.timeJoined &&
                bankId == that.bankId &&
                extAccountId.equals(that.extAccountId) &&
                betAmount == that.betAmount;
    }

    @Override
    public int hashCode() {
        int result = (int) (campaignId ^ (campaignId >>> 32));
        result = 31 * result + (int) (accountId ^ (accountId >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "MaxBalanceTournamentPlayerDetails[" +
                "accountId=" + accountId +
                ", bankId=" + bankId +
                ", extAccountId='" + extAccountId + '\'' +
                ", campaignId=" + campaignId +
                ", nickname='" + nickname + '\'' +
                ", currentBalance=" + currentBalance +
                ", buyInAmount=" + buyInAmount +
                ", reBuyCount=" + reBuyCount +
                ", reBuyAmount=" + reBuyAmount +
                ", maxBalance=" + maxBalance +
                ", timeJoined=" + timeJoined +
                ", betAmount=" + betAmount +
                ']';
    }
}
