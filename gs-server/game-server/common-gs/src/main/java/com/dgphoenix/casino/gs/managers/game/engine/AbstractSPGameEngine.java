package com.abs.casino.gs.managers.game.engine;

import com.abs.casino.common.exception.CommonException;
import com.abs.casino.gs.managers.game.core.AbstractGameProcessor;
import com.abs.casino.gs.managers.game.room.IRoom;

/**
 * Created
 * Date: 01.12.2008
 * Time: 17:55:24
 */
public abstract class AbstractSPGameEngine extends AbstractGameProcessor implements IGameEngine {
    protected AbstractSPGameEngine(long gameId) {
        super(gameId);
    }

    public void init() throws CommonException {
    }

    public void destroy() throws CommonException {
    }

    public IRoom createRoom(long roomId, String roomName) throws CommonException {
        throw new CommonException("Not suported");
    }

    public IRoom getRoom(long roomId) throws CommonException {
        throw new CommonException("Not suported");
    }

    public void closeRoom(long roomId) throws CommonException {
        throw new CommonException("Not suported");
    }

    public IRoom reloadRoom(long roomId) throws CommonException {
        throw new CommonException("Not suported");
    }
}
