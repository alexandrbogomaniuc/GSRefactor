package com.dgphoenix.casino.support;

/**
 * User: flsh
 * Date: 26.07.13
 */
public class GameSessionHistory {
    private long gameSessionId;
    private long gameId;
    private String data;

    public GameSessionHistory() {
    }

    public GameSessionHistory(long gameSessionId, long gameId, String data) {
        this.gameSessionId = gameSessionId;
        this.gameId = gameId;
        this.data = data;
    }

    public long getGameSessionId() {
        return gameSessionId;
    }

    public void setGameSessionId(long gameSessionId) {
        this.gameSessionId = gameSessionId;
    }

    public long getGameId() {
        return gameId;
    }

    public void setGameId(long gameId) {
        this.gameId = gameId;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GameSessionHistory [");
        sb.append("gameSessionId=").append(gameSessionId);
        sb.append(", gameId=").append(gameId);
        sb.append(", data='").append(data).append('\'');
        sb.append(']');
        return sb.toString();
    }
}
