package com.betsoft.casino.mp.service;

import java.util.List;
import java.util.Map;

import com.betsoft.casino.mp.model.IRMSRoom;
import com.betsoft.casino.mp.model.IRoundResult;
import com.betsoft.casino.mp.model.ISeat;
import com.betsoft.casino.mp.model.room.IRoom;
import com.abs.casino.common.util.Pair;

public interface IAnalyticsDBClientService {


    List<Map<String, Object>> prepareRoomsPlayers(List<IRMSRoom> trmsRooms, int serverId);
    boolean saveRoomsPlayers(List<Map<String, Object>> roomsPlayersRows);

    List<Map<String, Object>> prepareRoundResult(List<Pair<ISeat, IRoundResult>> seatsRoundResultsPairs, IRoom room);
    List<Map<String, Object>> prepareBattlegroundRoundResults(List<Pair<ISeat, IRoundResult>> seatsRoundResultsPairs, IRoom room);
    boolean saveRoundResults(List<Map<String, Object>> rows);

}
