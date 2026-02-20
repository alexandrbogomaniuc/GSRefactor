package com.dgphoenix.casino.kafka.dto;

import java.util.List;

public class BattlegroundInfoDto {
    private long gameId; 
    private String icon; 
    private String rules; 
    private List<Long> buyIns; 
    private double rake; 

    public BattlegroundInfoDto() {}

    public BattlegroundInfoDto(long gameId,
            String icon,
            String rules,
            List<Long> buyIns,
            double rake) {
        super();
        this.gameId = gameId;
        this.icon = icon;
        this.rules = rules;
        this.buyIns = buyIns;
        this.rake = rake;
    }

    public long getGameId() {
        return gameId;
    }

    public void setGameId(long gameId) {
        this.gameId = gameId;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public java.lang.String getRules() {
        return rules;
    }

    public void setRules(String rules) {
        this.rules = rules;
    }

    public java.util.List<Long> getBuyIns() {
        return buyIns;
    }

    public void setBuyIns(java.util.List<Long> buyIns) {
        this.buyIns = buyIns;
    }

    public double getRake() {
        return rake;
    }

    public void setRake(double rake) {
        this.rake = rake;
    }

}
