package com.betsoft.casino.mp.service;

import com.betsoft.casino.mp.exceptions.ServiceNotStartedException;
import com.betsoft.casino.mp.model.GameType;
import com.betsoft.casino.mp.model.IGameRoomSnapshot;
import com.betsoft.casino.mp.model.ISeat;
import com.betsoft.casino.mp.model.room.IRoom;
import com.betsoft.casino.mp.model.room.IRoomInfo;

import java.util.Collection;
import java.util.Map;

/**
 * User: flsh
 * Date: 03.11.17.
 */
public interface IRoomService<ROOM extends IRoom, SNAPSHOT extends IGameRoomSnapshot,
        SEAT extends ISeat, ROOM_INFO extends IRoomInfo> {
    ROOM put(ROOM room) throws ServiceNotStartedException;

    void remove(long id);

    ROOM getRoom(long id) throws ServiceNotStartedException;

    Collection<ROOM> getRooms() throws ServiceNotStartedException;

    Map<Long, ROOM> getRoomsUnmodifiableMap() throws ServiceNotStartedException;

    GameType getType();

    ROOM newInstance(ROOM_INFO roomInfo, ISocketService socketService, IPlayerStatsService playerStatsService,
                     IPlayerQuestsService playerQuestsService,
                     IWeaponService weaponService,
                     IPlayerProfileService playerProfileService,
                     boolean onlyFromSnapshot, IGameConfigService gameConfigService,
                     IActiveFrbSessionService activeFrbSessionService,
                     IActiveCashBonusSessionService activeCashBonusSessionService,
                     ITournamentService tournamentService,
                     IRoomInfoService roomInfoService) throws ServiceNotStartedException;

    void init();

    void shutdown();
}
