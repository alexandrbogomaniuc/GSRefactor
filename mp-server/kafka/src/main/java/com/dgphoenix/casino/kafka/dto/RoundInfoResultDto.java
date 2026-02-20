package com.dgphoenix.casino.kafka.dto;

public class RoundInfoResultDto {
    private long accountId;
    private long time;
    private double bet;
    private double payout;
    private String archiveData;
    private BattlegroundRoundInfoDto battlegroundRoundInfo;
    private long roundStartTime;

    public RoundInfoResultDto() {}

    public RoundInfoResultDto(long accountId,
            long time,
            double bet,
            double payout,
            String archiveData,
            BattlegroundRoundInfoDto battlegroundRoundInfo,
            long roundStartTime) {
        super();
        this.accountId = accountId;
        this.time = time;
        this.bet = bet;
        this.payout = payout;
        this.archiveData = archiveData;
        this.battlegroundRoundInfo = battlegroundRoundInfo;
        this.roundStartTime = roundStartTime;
    }

    public long getAccountId() {
        return accountId;
    }

    public long getTime() {
        return time;
    }

    public double getBet() {
        return bet;
    }

    public double getPayout() {
        return payout;
    }

    public String getArchiveData() {
        return archiveData;
    }

    public BattlegroundRoundInfoDto getBattlegroundRoundInfo() {
        return battlegroundRoundInfo;
    }

    public long getRoundStartTime() {
        return roundStartTime;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setBet(double bet) {
        this.bet = bet;
    }

    public void setPayout(double payout) {
        this.payout = payout;
    }

    public void setArchiveData(String archiveData) {
        this.archiveData = archiveData;
    }

    public void setBattlegroundRoundInfo(BattlegroundRoundInfoDto battlegroundRoundInfo) {
        this.battlegroundRoundInfo = battlegroundRoundInfo;
    }

    public void setRoundStartTime(long roundStartTime) {
        this.roundStartTime = roundStartTime;
    }

    @Override
    public String toString() {
        return "RoundInfoResultDto [accountId=" + accountId + ", time=" + time + ", bet=" + bet
                + ", payout=" + payout + ", archiveData=" + archiveData + ", battlegroundRoundInfo="
                + battlegroundRoundInfo + ", roundStartTime=" + roundStartTime + "]";
    }
}
