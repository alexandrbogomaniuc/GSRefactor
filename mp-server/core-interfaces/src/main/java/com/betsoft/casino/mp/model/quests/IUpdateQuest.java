package com.betsoft.casino.mp.model.quests;

import com.betsoft.casino.utils.IServerMessage;
import com.betsoft.casino.utils.ITransportObject;

/**
 * User: flsh
 * Date: 25.05.2020.
 */
public interface IUpdateQuest<Q extends IQuest> extends ITransportObject, IServerMessage {
    Q getQuest();

    void setQuest(Q quest);

    long getSourceEnemyId();

    void setSourceEnemyId(long sourceEnemyId);
}
