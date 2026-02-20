package com.betsoft.casino.mp.model;

import com.betsoft.casino.mp.model.battleground.IBattlegroundRoundInfo;

/**
 * User: flsh
 * Date: 15.12.2022.
 */
public interface IAddWinRequest {
    String getSessionId();

    long getGameSessionId();

    long getWinAmount();

    long getReturnedBet();

    long getAccountId();

    IPlayerBet getPlayerBet();

    IBattlegroundRoundInfo getBgRoundInfo();

    long getGsRoundId();

    boolean isSitOut();
}
