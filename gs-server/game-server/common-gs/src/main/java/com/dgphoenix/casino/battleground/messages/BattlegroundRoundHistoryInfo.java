package com.abs.casino.battleground.messages;

import com.dgphoenix.casino.battleground.messages.BattlegroundRoundHistory;

import java.util.List;
import java.util.Objects;

public class BattlegroundRoundHistoryInfo {
    private List<BattlegroundRoundHistory> mmcRounds;
    private List<BattlegroundRoundHistory> mqcRounds;

    public BattlegroundRoundHistoryInfo(List<BattlegroundRoundHistory> mmcRounds, List<BattlegroundRoundHistory> mqcRounds) {
        this.mmcRounds = mmcRounds;
        this.mqcRounds = mqcRounds;
    }

    public List<BattlegroundRoundHistory> getMmcRounds() {
        return mmcRounds;
    }

    public void setMmcRounds(List<BattlegroundRoundHistory> mmcRounds) {
        this.mmcRounds = mmcRounds;
    }

    public List<BattlegroundRoundHistory> getMqcRounds() {
        return mqcRounds;
    }

    public void setMqcRounds(List<BattlegroundRoundHistory> mqcRounds) {
        this.mqcRounds = mqcRounds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BattlegroundRoundHistoryInfo that = (BattlegroundRoundHistoryInfo) o;
        return Objects.equals(mmcRounds, that.mmcRounds) && Objects.equals(mqcRounds, that.mqcRounds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mmcRounds, mqcRounds);
    }

    @Override
    public String toString() {
        return "BattlegroundRoundHistoryInfo{" +
                "mmcRounds=" + mmcRounds +
                ", mqcRounds=" + mqcRounds +
                '}';
    }
}
