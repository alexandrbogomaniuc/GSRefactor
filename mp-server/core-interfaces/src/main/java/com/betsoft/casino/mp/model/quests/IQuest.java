package com.betsoft.casino.mp.model.quests;

import com.abs.casino.common.cache.Identifiable;

/**
 * User: flsh
 * Date: 19.05.2020.
 */
public interface IQuest extends Identifiable {
    void setId(long id);

    int getType();

    void setType(int type);

    long getRoomCoin();

    boolean isNeedReset();

    long getCollectedAmount();

    void setCollectedAmount(long collectedAmount);

    IQuestProgress getProgress();

    IQuestPrize getQuestPrize();

    String getName();

    void setName(String name);

    void setNeedReset(boolean reset);
}
