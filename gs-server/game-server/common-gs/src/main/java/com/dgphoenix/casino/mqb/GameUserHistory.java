package com.abs.casino.mqb;

import java.util.Objects;

public class GameUserHistory {
    private Long accountId;
    private Long gameId;
    private String gameName;
    private Long bet;
    private Long win;
    private String currencyCode;
    private Long date;

    public GameUserHistory(Long accountId, Long gameId, String gameName, Long bet, Long win, String currencyCode, Long date) {
        this.accountId = accountId;
        this.gameId = gameId;
        this.gameName = gameName;
        this.bet = bet;
        this.win = win;
        this.currencyCode = currencyCode;
        this.date = date;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public Long getBet() {
        return bet;
    }

    public void setBet(Long bet) {
        this.bet = bet;
    }

    public Long getWin() {
        return win;
    }

    public void setWin(Long win) {
        this.win = win;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameUserHistory that = (GameUserHistory) o;
        return accountId.equals(that.accountId) && gameId.equals(that.gameId) && gameName.equals(that.gameName) && date.equals(that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, gameId, gameName, date);
    }

    @Override
    public String toString() {
        return "GameUserHistory{" +
                "accountId=" + accountId +
                ", gameId='" + gameId + '\'' +
                ", gameName='" + gameName + '\'' +
                ", bet=" + bet +
                ", win=" + win +
                ", currencyCode='" + currencyCode + '\'' +
                ", date=" + date +
                '}';
    }
}
