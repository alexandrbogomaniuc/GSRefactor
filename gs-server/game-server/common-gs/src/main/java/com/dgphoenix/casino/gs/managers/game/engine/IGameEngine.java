package com.abs.casino.gs.managers.game.engine;

import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.dgphoenix.casino.common.exception.CommonException;
import com.abs.casino.gs.managers.dblink.IDBLink;
import com.abs.casino.gs.managers.game.core.IGameProcessor;
import com.abs.casino.gs.managers.game.room.IRoom;

/**
 * Created
 * Date: 28.11.2008
 * Time: 15:41:58
 */
public interface IGameEngine extends IGameProcessor {
    void init() throws CommonException;

    void destroy() throws CommonException;

    IRoom createRoom(long roomId, String roomName) throws CommonException;

    IRoom getRoom(long roomId) throws CommonException;

    void closeRoom(long roomId) throws CommonException;

    IRoom reloadRoom(long roomId) throws CommonException;

    IDBLink recreateDBLink(GameSession gameSession, AccountInfo accountInfo, boolean putToCache) throws CommonException;
}
