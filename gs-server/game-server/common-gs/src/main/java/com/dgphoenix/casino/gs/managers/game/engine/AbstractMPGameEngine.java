package com.dgphoenix.casino.gs.managers.game.engine;

import com.dgphoenix.casino.common.cache.data.game.GameMode;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.gs.managers.dblink.IDBLink;
import com.dgphoenix.casino.gs.managers.game.core.AbstractGameProcessor;
import com.dgphoenix.casino.gs.managers.game.room.IRoom;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created
 * Date: 01.12.2008
 * Time: 17:55:36
 */
public abstract class AbstractMPGameEngine extends AbstractGameProcessor implements IGameEngine {
    private Map<Long, IRoom> rooms;

    protected AbstractMPGameEngine(long gameId) {
        super(gameId);
        rooms = new HashMap<Long, IRoom>();
    }

    public void init() throws CommonException {
    }

    public void destroy() throws CommonException {
        for (Long roomId : rooms.keySet()) {
            closeRoom(roomId);
        }
    }

    @Override
    public Map<String, String> processCommand(String cmd, Long accountId, HttpServletRequest request,
                                              HttpServletResponse response)
            throws IOException, ServletException, CommonException {
        throw new CommonException("Not suported");
    }

    public IRoom createRoom(long roomId, String roomName) throws CommonException {
        IRoom room = createRoomInstance(roomId, roomName);
        rooms.put(roomId, room);
        return room;
    }

    public IRoom getRoom(long roomId) throws CommonException {
        return rooms.get(roomId);
    }

    public void closeRoom(long roomId) throws CommonException {
        IRoom room = rooms.get(roomId);
        if (room != null) {
            rooms.remove(roomId);
            room.close();
        }
    }

    public IRoom reloadRoom(long roomId) throws CommonException {
        return createRoomInstance(roomId, "reloaded");
    }

    abstract protected IRoom createRoomInstance(long roomId, String roomName) throws CommonException;

    protected IDBLink createDBLink_internal(long accountId, GameMode mode) throws CommonException {
        throw new CommonException("Not supported");
    }
}
