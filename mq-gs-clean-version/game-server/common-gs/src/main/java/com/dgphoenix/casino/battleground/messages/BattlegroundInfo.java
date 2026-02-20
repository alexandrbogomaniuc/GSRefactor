package com.dgphoenix.casino.battleground.messages;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class BattlegroundInfo implements Serializable {
    private Long gameId;
    private String name;
    private List<Long> buyIns;

    public BattlegroundInfo(Long gameId, String name, List<Long> buyIns) {
        this.gameId = gameId;
        this.name = name;
        this.buyIns = buyIns;
    }

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Long> getBuyIns() {
        return buyIns;
    }

    public void setBuyIns(List<Long> buyIns) {
        this.buyIns = buyIns;
    }

    @Override
    public String toString() {
        return "BattlegroundInfo{" +
                "gameId=" + gameId +
                ", name='" + name + '\'' +
                ", buyIns=" + buyIns +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BattlegroundInfo that = (BattlegroundInfo) o;
        return gameId.equals(that.gameId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gameId);
    }
}
