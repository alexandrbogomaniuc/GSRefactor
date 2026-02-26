package com.abs.casino.web.history;

import java.io.Serializable;

/**
 * User: flsh
 * Date: 17.04.2009
 */
public class GameHistoryListEntry implements Serializable {
    private long sessionId;
    private String gameName;
    private String localizedGameName;
    private String startDate;
    private String endDate;
    private String income;
    private String payout;
    private String revenue;
    private String historyUrl;
    private String online;

    public GameHistoryListEntry() {
    }

    public long getSessionId() {
        return sessionId;
    }

    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getLocalizedGameName() {
        return localizedGameName;
    }

    public void setLocalizedGameName(String localizedGameName) {
        this.localizedGameName = localizedGameName;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getIncome() {
        return income;
    }

    public void setIncome(String income) {
        this.income = income;
    }

    public String getPayout() {
        return payout;
    }

    public void setPayout(String payout) {
        this.payout = payout;
    }

    public String getRevenue() {
        return revenue;
    }

    public void setRevenue(String revenue) {
        this.revenue = revenue;
    }

    public String getHistoryUrl() {
        return historyUrl;
    }

    public void setHistoryUrl(String historyUrl) {
        this.historyUrl = historyUrl;
    }

    public String getOnline() {
        return online;
    }

    public void setOnline(String online) {
        this.online = online;
    }
}
