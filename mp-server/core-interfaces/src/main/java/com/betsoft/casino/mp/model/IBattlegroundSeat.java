package com.betsoft.casino.mp.model;

public interface IBattlegroundSeat<WEAPON extends IWeapon, PLAYER_ROUND_INFO extends IActionGamePlayerRoundInfo<?, ?, ?>, TREASURE extends ITreasure,
        RPI extends IBattlegroundRoomPlayerInfo, S extends ISeat>
        extends IActionGameSeat<WEAPON, PLAYER_ROUND_INFO, TREASURE, RPI, S> {

    @Override
    RPI getPlayerInfo();
}
