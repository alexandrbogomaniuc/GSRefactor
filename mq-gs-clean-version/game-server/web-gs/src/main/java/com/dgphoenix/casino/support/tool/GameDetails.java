package com.dgphoenix.casino.support.tool;

import com.dgphoenix.casino.common.cache.data.game.GameVariableType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameDetails {
    private String gameName;
    private Long gameId;
    private String group;
    private String variableType;
    private List<Long> coins;
    private String defCoin;
    private Integer maxLimit;
    private Integer minLimit;
    private Boolean fromMasterBank = false;
    private Boolean fromDefaultInfo = false;
    private Map<String, String> properties = new HashMap<>();
    private JackpotV1Details jackpotV1Details;

    public Long getGameId() {
        return gameId;
    }

    public String getGroup() {
        return group;
    }

    public String getVariableType() {
        return variableType;
    }

    public String getGameName() {
        return gameName;
    }

    public List<Long> getCoins() {
        return coins;
    }

    public Boolean getFromDefaultInfo() {
        return fromDefaultInfo;
    }

    public Boolean getFromMasterBank() {
        return fromMasterBank;
    }

    public String getDefCoin() {
        return defCoin;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public Integer getMaxLimit() {
        return maxLimit;
    }

    public Integer getMinLimit() {
        return minLimit;
    }

    public JackpotV1Details getJackpotV1Details() {
        return jackpotV1Details;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public void setVariableType(GameVariableType variableType) {
        this.variableType = variableType.toString();
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public void setCoins(List<Long> coins) {
        this.coins = coins;
    }

    public void setFromDefaultInfo(Boolean fromDefaultInfo) {
        this.fromDefaultInfo = fromDefaultInfo;
    }

    public void setFromMasterBank(Boolean fromMasterBank) {
        this.fromMasterBank = fromMasterBank;
    }

    public void setDefCoin(String defCoin) {
        this.defCoin = defCoin;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public void addProperties(String key, String value) {
        this.properties.put(key, value);
    }

    public void setMaxLimit(Integer maxLimit) {
        this.maxLimit = maxLimit;
    }

    public void setMinLimit(Integer minLimit) {
        this.minLimit = minLimit;
    }

    public void setJackpotV1Details(JackpotV1Details jackpotV1Details) {
        this.jackpotV1Details = jackpotV1Details;
    }
}