package com.betsoft.casino.mp.model.battleground;

import com.esotericsoftware.kryo.KryoSerializable;

import java.util.List;

/**
 * User: flsh
 * Date: 17.07.2021.
 */
public interface IBattlegroundRoundInfo extends KryoSerializable {
    long getBuyIn();

    long getWinAmount();

    long getBetsSum();

    long getWinSum();

    List<IBgPlace> getPlaces();

    void setPlaces(List<IBgPlace> places);

    String getStatus();

    int getPlayersNumber();

    String getWinnerName();

    long getRoundId();

    long getRoundStartDate();

    String getPrivateRoomId();
}
