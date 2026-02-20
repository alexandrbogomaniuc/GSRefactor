package com.dgphoenix.casino.battleground.messages;

import java.util.Objects;

public class BattlegroundRoundHistory {
    private Integer gameId;
    private String gameName;
    private Long buyIn;
    private Long winPrize;
    private String currencyCode;
    private Integer position;
    private Long time;
    private Long accountId;
    private Long roundId;

    public BattlegroundRoundHistory(Integer gameId, String gameName, Long buyIn, Long winPrize,
                                    String currencyCode, Integer position, Long time, Long accountId, Long roundId) {
        this.gameId = gameId;
        this.gameName = gameName;
        this.buyIn = buyIn;
        this.winPrize = winPrize;
        this.currencyCode = currencyCode;
        this.position = position;
        this.time = time;
        this.accountId = accountId;
        this.roundId = roundId;
    }

    public Integer getGameId() {
        return gameId;
    }

    public void setGameId(Integer gameId) {
        this.gameId = gameId;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public Long getBuyIn() {
        return buyIn;
    }

    public void setBuyIn(Long buyIn) {
        this.buyIn = buyIn;
    }

    public Long getWinPrize() {
        return winPrize;
    }

    public void setWinPrize(Long winPrize) {
        this.winPrize = winPrize;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Long getRoundId() {
        return roundId;
    }

    public void setRoundId(Long roundId) {
        this.roundId = roundId;
    }

    @Override
    public String toString() {
        return "BattlegroundRoundHistory{" +
                "gameId=" + gameId +
                ", gameName='" + gameName + '\'' +
                ", buyIn=" + buyIn +
                ", winPrize=" + winPrize +
                ", currencyCode='" + currencyCode + '\'' +
                ", position=" + position +
                ", time=" + time +
                ", accountId=" + accountId +
                ", roundId=" + roundId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BattlegroundRoundHistory that = (BattlegroundRoundHistory) o;
        return accountId.equals(that.accountId) && roundId.equals(that.roundId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, roundId);
    }
}
