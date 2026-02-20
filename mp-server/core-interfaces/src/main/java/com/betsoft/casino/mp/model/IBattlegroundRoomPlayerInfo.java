package com.betsoft.casino.mp.model;

import com.betsoft.casino.mp.model.battleground.IBattlegroundRoundInfo;
import com.betsoft.casino.mp.model.battleground.IBgPlace;

public interface IBattlegroundRoomPlayerInfo extends IActionRoomPlayerInfo {

    IBattlegroundRoundInfo getBattlegroundRoundInfo();

    void setBattlegroundRoundInfo(IBattlegroundRoundInfo battlegroundRoundInfo);

    IBgPlace createBattlegroundRoundInfo(long buyIn, long winAmount, long betsSum, long winSum, String status,
                                         int playersNumber, String winnerName, long accountId, int rank,
                                         long gameSessionId, long gameScore, long roundId, long roundStartDate,
                                         double ejectPoint, String privateRoomId);

    void setBattlegroundRake(double battlegroundRake);

    double getBattlegroundRake();


    boolean isPrivateRoom();

    void setPrivateRoom(boolean privateRoom);

    boolean isOwner();

    void setOwner(boolean owner);

    String getUserName();

    void setUserName(String userName);
}
