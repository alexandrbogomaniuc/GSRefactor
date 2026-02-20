package com.dgphoenix.casino.common.promo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.HashMap;
import java.util.Map;

/**
 * User: flsh
 * Date: 12.01.17.
 */
public class TournamentMemberRank implements KryoSerializable, Comparable<TournamentMemberRank> {
    private static final byte VERSION = 1;

    private long campaignId;
    //score depend from tournament objectives and may be roundsCount, betSum, bonusCount, iconsCount ant etc
    private long score;
    private long accountId;
    private long bankId;
    private String extAccountId;
    private String nickName;
    private long roundsCount;
    private long betSum;
    private long winSum;
    private long bonusCount;
    private Map<Long, RoundStat> roundStats = new HashMap<Long, RoundStat>();

    //next field may be used for resolve possible conflicts with same rank
    private long saveTime;
    private long playerEnterTime;

    public TournamentMemberRank() {
    }

    public TournamentMemberRank(long score, String nickname, String extAccountId) {
        this.score = score;
        this.nickName = nickname;
        this.extAccountId = extAccountId;
    }

    public TournamentMemberRank(long campaignId, long score, long accountId, long bankId, String extAccountId,
                                String nickName, long roundsCount, long betSum, long winSum, long bonusCount,
                                long saveTime, long playerEnterTime) {
        this.campaignId = campaignId;
        this.score = score;
        this.accountId = accountId;
        this.bankId = bankId;
        this.extAccountId = extAccountId;
        this.nickName = nickName;
        this.roundsCount = roundsCount;
        this.betSum = betSum;
        this.winSum = winSum;
        this.bonusCount = bonusCount;
        this.saveTime = saveTime;
        this.playerEnterTime = playerEnterTime;
    }

    public long getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(long campaignId) {
        this.campaignId = campaignId;
    }

    public long getScore() {
        return score;
    }

    public void setScore(long score) {
        this.score = score;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
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

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public long getRoundsCount() {
        return roundsCount;
    }

    public void setRoundsCount(long roundsCount) {
        this.roundsCount = roundsCount;
    }

    public long getBetSum() {
        return betSum;
    }

    public void setBetSum(long betSum) {
        this.betSum = betSum;
    }

    public long getWinSum() {
        return winSum;
    }

    public void setWinSum(long winSum) {
        this.winSum = winSum;
    }

    public long getBonusCount() {
        return bonusCount;
    }

    public void setBonusCount(long bonusCount) {
        this.bonusCount = bonusCount;
    }

    public long getSaveTime() {
        return saveTime;
    }

    public long getPlayerEnterTime() {
        return playerEnterTime;
    }

    public Map<Long, RoundStat> getRoundStats() {
        return roundStats;
    }

    public void setRoundStats(Map<Long, RoundStat> roundStats) {
        this.roundStats = roundStats;
    }

    public RoundStat getRoundStat(long gameId) {
        return roundStats.get(gameId);
    }

    public void addRoundStat(long gameId, RoundStat roundStat) {
        roundStats.put(gameId, roundStat);
    }

    public void removeRoundStat(long gameId) {
        roundStats.remove(gameId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TournamentMemberRank that = (TournamentMemberRank) o;

        if (campaignId != that.campaignId) return false;
        if (score != that.score) return false;
        if (accountId != that.accountId) return false;
        if (roundsCount != that.roundsCount) return false;
        if (betSum != that.betSum) return false;
        if (bonusCount != that.bonusCount) return false;
        if (saveTime != that.saveTime) return false;
        return playerEnterTime == that.playerEnterTime;

    }

    @Override
    public int hashCode() {
        int result = (int) (campaignId ^ (campaignId >>> 32));
        result = 31 * result + (int) (accountId ^ (accountId >>> 32));
        return result;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeLong(campaignId, true);
        output.writeLong(score, true);
        output.writeLong(accountId, true);
        output.writeLong(bankId, true);
        output.writeString(extAccountId);
        output.writeString(nickName);
        output.writeLong(roundsCount, true);
        output.writeLong(betSum, true);
        output.writeLong(winSum, true);
        output.writeLong(bonusCount, true);
        output.writeLong(saveTime, true);
        output.writeLong(playerEnterTime, true);
        kryo.writeClassAndObject(output, roundStats);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void read(Kryo kryo, Input input) {
        byte ver = input.readByte();
        campaignId = input.readLong(true);
        score = input.readLong(true);
        accountId = input.readLong(true);
        bankId = input.readLong(true);
        extAccountId = input.readString();
        nickName = input.readString();
        roundsCount = input.readLong(true);
        betSum = input.readLong(true);
        winSum = input.readLong(true);
        bonusCount = input.readLong(true);
        saveTime = input.readLong(true);
        playerEnterTime = input.readLong(true);
        if (ver > 0) {
            roundStats = (Map<Long, RoundStat>) kryo.readClassAndObject(input);
        }
    }

    @Override
    public String toString() {
        return "TournamentMemberRank[" +
                "campaignId=" + campaignId +
                ", score=" + score +
                ", accountId=" + accountId +
                ", bankId=" + bankId +
                ", extAccountId='" + extAccountId + '\'' +
                ", nickName='" + nickName + '\'' +
                ", roundsCount=" + roundsCount +
                ", betSum=" + betSum +
                ", winSum=" + winSum +
                ", bonusCount=" + bonusCount +
                ", saveTime=" + saveTime +
                ", playerEnterTime=" + playerEnterTime +
                ']';
    }

    @Override
    public int compareTo(TournamentMemberRank rank) {
        if (score == rank.score) {
            return saveTime < rank.saveTime ? -1 : (saveTime == rank.saveTime ? 0 : 1);
        }
        return score < rank.score ? 1 : -1;
    }
}
