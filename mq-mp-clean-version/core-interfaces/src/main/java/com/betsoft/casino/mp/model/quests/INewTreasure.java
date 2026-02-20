package com.betsoft.casino.mp.model.quests;

import com.betsoft.casino.utils.IServerMessage;
import com.betsoft.casino.utils.ITransportObject;

/**
 * User: flsh
 * Date: 25.05.2020.
 */
public interface INewTreasure extends ITransportObject, IServerMessage {
    long getId();

    void setId(int id);

    long getEnemyId();

    void setEnemyId(int enemyId);

    long getCompletedQuestId();

    void setCompletedQuestId(long completedQuestId);

    long getQuestId();

    void setQuestId(long questId);
}
