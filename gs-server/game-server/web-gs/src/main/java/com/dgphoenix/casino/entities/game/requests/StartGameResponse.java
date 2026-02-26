package com.abs.casino.entities.game.requests;

import com.dgphoenix.casino.common.cache.data.game.IBaseGameInfo;

import java.util.List;

/**
 * User: isirbis
 * Date: 30.09.14
 */
public class StartGameResponse {
    Long gameSessionId;
    List<String> additionalParams;
    String status;
    String description;
    IBaseGameInfo gameInfo;
    Long bonusId;

    public StartGameResponse() {

    }

    public StartGameResponse(Long gameSessionId, List<String> additionalParams,
                             String status, String description) {
        this.gameSessionId = gameSessionId;
        this.additionalParams = additionalParams;
        this.status = status;
        this.description = description;
    }

    public void setGameSessionId(Long gameSessionId) {
        this.gameSessionId = gameSessionId;
    }

    public Long getGameSessionId() {
        return this.gameSessionId;
    }

    public void setAdditionalParams(List<String> additionalParams) {
        this.additionalParams = additionalParams;
    }

    public List<String> getAdditionalParams() {
        return this.additionalParams;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public IBaseGameInfo getGameInfo() {
        return gameInfo;
    }

    public void setGameInfo(IBaseGameInfo gameInfo) {
        this.gameInfo = gameInfo;
    }

    public Long getBonusId() {
        return bonusId;
    }

    public void setBonusId(Long bonusId) {
        this.bonusId = bonusId;
    }
}
