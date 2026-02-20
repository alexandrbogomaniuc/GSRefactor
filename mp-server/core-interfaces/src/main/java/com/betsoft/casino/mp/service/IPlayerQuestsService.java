package com.betsoft.casino.mp.service;

import com.betsoft.casino.mp.model.IPlayerQuests;
import com.betsoft.casino.mp.model.Money;
import com.betsoft.casino.mp.model.quests.IQuest;
import java.util.Set;

public interface IPlayerQuestsService<PLAYER_QUEST extends IPlayerQuests> {
    PLAYER_QUEST updateQuests(long bankId, long gameId, long accountId, Set<IQuest> quests, Money stake, int mode);
    PLAYER_QUEST updateSpecialModeQuests(long tournamentOrBonusId, long bankId, long gameId,
                                         long accountId, Set<IQuest> quests, Money stake, int mode);

    PLAYER_QUEST load(long bankId, long gameId, long accountId, Money stake, int mode);
    PLAYER_QUEST loadSpecialModeQuests(long tournamentOrBonusId, long bankId, long gameId, long accountId, Money stake,
                                       int mode);

    Set<IQuest> getAllQuests(long bankId, long accountId, int mode, long gameId);
    Set<IQuest> getAllSpecialModeQuests(long tournamentOrBonusId, long bankId, long accountId, long gameId, int mode);

    void removeAllQuests(long gameId);
}
