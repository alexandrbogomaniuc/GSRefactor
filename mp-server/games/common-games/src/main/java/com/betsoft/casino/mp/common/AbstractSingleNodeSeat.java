package com.betsoft.casino.mp.common;

import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.web.IGameSocketClient;

/**
 * User: flsh
 * Date: 18.01.2022.
 */

/**
 * Abstract class for single node games players.
 */
public abstract class AbstractSingleNodeSeat<WEAPON extends IWeapon, PLAYER_ROUND_INFO extends IPlayerRoundInfo, TREASURE extends ITreasure,
        RPI extends IRoomPlayerInfo, S extends ISeat>
        extends AbstractSeat<WEAPON, PLAYER_ROUND_INFO, TREASURE, RPI, S> implements ISingleNodeSeat<WEAPON, PLAYER_ROUND_INFO, TREASURE, RPI, S> {

    /** seatId of player in room */
    protected int number = -1;

    public AbstractSingleNodeSeat() {
        super();
    }

    public AbstractSingleNodeSeat(RPI playerInfo, IGameSocketClient socketClient, double currentRate) {
        super(playerInfo, socketClient, currentRate);
    }

    public AbstractSingleNodeSeat(RPI playerInfo, Money stake) {
        super(playerInfo, stake);
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public void setNumber(int number) {
        this.number = number;
    }

}
