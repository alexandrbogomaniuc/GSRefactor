package com.abs.casino.actions.enter.game;

import com.abs.casino.common.cache.data.game.GameMode;
import com.abs.casino.common.cache.data.session.ClientType;

/**
 * User: flsh
 * Date: 7/22/11
 */
public interface IStartGameForm {
    GameMode getGameMode();

    String getMode();

    String getGameId();

    Integer getBankId();

    String getLang();

    short getSubCasinoId();

    ClientType getClientType();

    /**
     * @return true if forbidden to search active FRB. i.e. if return <code>true</code> FRB can be used only when bonusId are defined
     */
    boolean isNotGameFRB();
}
