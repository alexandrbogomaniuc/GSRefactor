package com.dgphoenix.casino.mqb;

import java.util.List;
import java.util.Objects;

public class GameUserHistoryInfo {
    private List<GameUserHistory> mmcHistory;
    private List<GameUserHistory> mqcHistory;

    public GameUserHistoryInfo(List<GameUserHistory> mmcHistory, List<GameUserHistory> mqcHistory) {
        this.mmcHistory = mmcHistory;
        this.mqcHistory = mqcHistory;
    }

    public List<GameUserHistory> getMmcHistory() {
        return mmcHistory;
    }

    public void setMmcHistory(List<GameUserHistory> mmcHistory) {
        this.mmcHistory = mmcHistory;
    }

    public List<GameUserHistory> getMqcHistory() {
        return mqcHistory;
    }

    public void setMqcHistory(List<GameUserHistory> mqcHistory) {
        this.mqcHistory = mqcHistory;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameUserHistoryInfo that = (GameUserHistoryInfo) o;
        return Objects.equals(mmcHistory, that.mmcHistory) && Objects.equals(mqcHistory, that.mqcHistory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mmcHistory, mqcHistory);
    }

    @Override
    public String toString() {
        return "GameUserHistoryInfo{" +
                "mmcHistory=" + mmcHistory +
                ", mqcHistory=" + mqcHistory +
                '}';
    }
}
