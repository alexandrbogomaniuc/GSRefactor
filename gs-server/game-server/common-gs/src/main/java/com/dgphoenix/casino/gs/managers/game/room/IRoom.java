package com.abs.casino.gs.managers.game.room;

import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.gs.managers.game.core.IGameProcessor;

/**
 * Created
 * Date: 01.12.2008
 * Time: 12:47:42
 */
public interface IRoom extends IGameProcessor {
    long getRoomId();

    String getRoomName();

    void close();

    void reloadBets() throws CommonException;
}
