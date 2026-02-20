package com.betsoft.casino.mp.model.battleground;

import com.betsoft.casino.utils.IServerMessage;
import com.betsoft.casino.utils.ITransportObject;

import java.util.List;
import java.util.Map;

public interface IBattlegroundScoreBoard extends ITransportObject, IServerMessage {
    long getEndTime();
}
