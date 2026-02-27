/**
 * Created
 * Date: 01.12.2008
 * Time: 12:50:28
 */
package com.abs.casino.gs.managers.game.room;

import com.dgphoenix.casino.common.exception.CommonException;
import com.abs.casino.gs.managers.game.engine.GameEngineManager;
import com.abs.casino.gs.managers.game.engine.IGameEngine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RoomManager {
    private static final Logger LOG = LogManager.getLogger(RoomManager.class);
    private static RoomManager instance = new RoomManager();

    public static RoomManager getInstance() {
        return instance;
    }

    private RoomManager() {
    }

    private Map<Long, IRoom> rooms = new HashMap<Long, IRoom>();
    private Set<Long> invalidRooms = new HashSet<Long>();

    public synchronized IRoom registerRoom(long gameId, long roomId, String roomName) throws CommonException {
        if (!rooms.containsKey(roomId)) {
            checkRoom(roomId);

            long bankId = 1l;
            IGameEngine ge = GameEngineManager.getInstance().getGameEngine(bankId, gameId);
            IRoom room = ge.createRoom(roomId, roomName);
            rooms.put(room.getRoomId(), room);
            LOG.info("RoomManager::registerRoom room registered for gameId=" + gameId + " roomId=" + roomId + " roomName=" + roomName);
            return room;
        } else {
            throw new CommonException("Room already exists");
//            return rooms.get(roomId);
        }
    }

    public synchronized IRoom get(long roomId) {
        return rooms.get(roomId);
    }

    public synchronized void unregisterRoom(long roomId) throws CommonException {
        checkRoom(roomId);
        IRoom room = rooms.get(roomId);
        if (room != null) {
            rooms.remove(roomId);
            long bankId = 10l;
            IGameEngine ge = GameEngineManager.getInstance().getGameEngine(bankId, room.getGameId());
            ge.closeRoom(roomId);
            LOG.info("RoomManager::unregisterRoom room unregistered for roomId=" + roomId);
        } else {
            // DO NOTHING
//            throw new RemoteCommonException("Room does not exists");
        }
    }

    private synchronized IRoom reloadRoom(long gameId, long roomId) throws CommonException {
        LOG.info("RoomManager:: reloading room gameId=" + gameId + " roomId=" + roomId);
        if (rooms.containsKey(roomId)) throw new CommonException("Room already exists");
        long bankId = 1l;
        IGameEngine ge = GameEngineManager.getInstance().getGameEngine(bankId, gameId);
        IRoom room = ge.createRoom(roomId, "RELOADED");

        room.reloadBets();

        rooms.put(room.getRoomId(), room);
        LOG.info("RoomManager::reloadRoom room reloaded gameId=" + gameId + " roomId=" + roomId);
        return room;
    }

    public synchronized void addInvalidRoom(long roomId) {
        invalidRooms.add(roomId);
    }

    public synchronized void removeInvalidRoom(long roomId) {
        invalidRooms.remove(roomId);
    }

    public synchronized boolean isInvalid(long roomId) {
        return invalidRooms.contains(roomId);
    }

    private void checkRoom(long roomId) throws CommonException {
        if (isInvalid(roomId)) {
            throw new CommonException("Room " + roomId + " is invalid now");
        }
    }
}
