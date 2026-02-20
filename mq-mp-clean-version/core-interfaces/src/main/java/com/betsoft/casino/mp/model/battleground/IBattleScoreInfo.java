package com.betsoft.casino.mp.model.battleground;

import java.io.Serializable;

public interface IBattleScoreInfo extends Serializable {

    int getSeatId();

    long getWinAmount();

    long getBetAmount();

    boolean isKing();
}
