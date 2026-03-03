package com.betsoft.casino.mp.service;

import com.betsoft.casino.mp.model.GameType;
import com.betsoft.casino.mp.model.room.IRoom;
import com.betsoft.casino.mp.model.room.IRoomInfo;
import com.abs.casino.common.exception.CommonException;

import java.util.Collection;

/**
 * User: flsh
 * Date: 17.01.2022.
 */
@SuppressWarnings("rawtypes")
public interface IRoomServiceFactory {
    IRoomInfo getRoomInfo(long roomId);

    IRoom put(IRoom room) throws CommonException;

    Collection<IRoom> getRooms(GameType type) throws CommonException;

    IRoomInfoService getRoomInfoService(GameType type, Boolean isPrivate);

    IRoom getRoom(GameType type, long id) throws CommonException;

    IRoom getRoomWithoutCreation(GameType type, long id) throws CommonException;

    IRoom getRoomWithoutCreationById(long id) throws CommonException;

    Collection<IRoom> getAllRooms() throws CommonException;

    Collection<IRoom> getAllActiveRooms() throws CommonException;

    IServerConfigService getServerConfigService();

    void repairRoomsOnDownServer(Integer downServerId);

}
