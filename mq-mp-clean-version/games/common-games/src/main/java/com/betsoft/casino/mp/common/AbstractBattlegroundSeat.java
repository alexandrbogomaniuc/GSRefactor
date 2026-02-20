package com.betsoft.casino.mp.common;

import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.web.IGameSocketClient;

/**
 * Abstract class for action BTG players.
 */
public abstract class AbstractBattlegroundSeat<WEAPON extends IWeapon, PLAYER_ROUND_INFO extends IActionGamePlayerRoundInfo<?, ?, ?>, TREASURE extends ITreasure,
        RPI extends IBattlegroundRoomPlayerInfo, S extends ISeat>
        extends AbstractActionSeat<WEAPON, PLAYER_ROUND_INFO, TREASURE, RPI, S>
        implements IBattlegroundSeat<WEAPON, PLAYER_ROUND_INFO, TREASURE, RPI, S> {

    public AbstractBattlegroundSeat() {
        super();
    }

    public AbstractBattlegroundSeat(RPI playerInfo, Money stake) {
        super(playerInfo, stake);
    }

    public AbstractBattlegroundSeat(RPI playerInfo, IGameSocketClient socketClient, double currentRate) {
        super(playerInfo, socketClient, currentRate);
    }

    @Override
    public RPI getPlayerInfo() {
        return playerInfo;
    }

    @Override
    public boolean isOwner() {
        return playerInfo != null && playerInfo.isOwner();
    }

    @Override
    public void setOwner(boolean isOwner) {
        if (playerInfo != null) {
            playerInfo.setOwner(isOwner);
        }
    }
}
