package com.abs.casino.sm.login;

import com.dgphoenix.casino.common.cache.data.game.GameMode;

/**
 * User: isirbis
 * Date: 14.10.14
 */
public class GameLoginRequest extends LoginRequest {
    protected GameMode gameMode;
    protected Integer gameId;
    protected boolean needSendGameIdOnAuth = false;

    public GameMode getGameMode() {
        return gameMode;
    }

    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }

    public Integer getGameId() {
        return gameId;
    }

    public void setGameId(Integer gameId) {
        this.gameId = gameId;
    }

    public boolean isNeedSendGameIdOnAuth() {
        return needSendGameIdOnAuth;
    }

    public void setNeedSendGameIdOnAuth(boolean needSendGameIdOnAuth) {
        this.needSendGameIdOnAuth = needSendGameIdOnAuth;
    }

    @Override
    public String toString() {
        return "GameLoginRequest[" +
                "gameMode=" + gameMode +
                ", gameId=" + gameId +
                ", needSendGameIdOnAuth=" + needSendGameIdOnAuth +
                ']' + super.toString();
    }
}
