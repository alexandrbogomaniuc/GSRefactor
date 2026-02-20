package com.betsoft.casino.mp.model;

/**
 * User: flsh
 * Date: 18.01.2022.
 */
public interface IMultiNodeSeat<WEAPON extends IWeapon, PLAYER_ROUND_INFO extends IPlayerRoundInfo, TREASURE extends ITreasure, RPI extends IRoomPlayerInfo, S extends ISeat>
        extends ISeat<WEAPON, PLAYER_ROUND_INFO, TREASURE, RPI, S> {
    long getRoomId();
    void setLastActivityDate(long date);
    long getLastActivityDate();
    int getActualVersion();
    int incrementActualVersion();
    int getActiveServerId();
}
