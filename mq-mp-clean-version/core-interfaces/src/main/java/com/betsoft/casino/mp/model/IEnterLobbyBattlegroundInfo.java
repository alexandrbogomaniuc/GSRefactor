package com.betsoft.casino.mp.model;

import java.util.List;

public interface IEnterLobbyBattlegroundInfo {

    List<Long> getBuyIns();

    void setBuyIns(List<Long> buyIns);

    Long getAlreadySeatRoomId();

    void setAlreadySeatRoomId(Long alreadySeatRoomId);

    String getStartGameUrl();

    void setStartGameUrl(String startGameUrl);

    String getRoomState();

    void setRoomState(String roomState);

    boolean isBuyInConfirmed();

    void setBuyInConfirmed(boolean buyInConfirmed);
}
