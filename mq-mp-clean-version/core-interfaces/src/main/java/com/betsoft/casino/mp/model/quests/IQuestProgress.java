package com.betsoft.casino.mp.model.quests;

import java.util.List;

/**
 * User: flsh
 * Date: 19.05.2020.
 */
public interface IQuestProgress<TP extends ITreasureProgress> {
    List<TP> getTreasures();

    void setTreasures(List<TP> treasures);

    void resetProgress();

    void decreaseProgress();
}
