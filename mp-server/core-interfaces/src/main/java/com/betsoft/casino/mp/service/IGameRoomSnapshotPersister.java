package com.betsoft.casino.mp.service;

import com.betsoft.casino.mp.model.IGameRoomSnapshot;

/**
 * User: flsh
 * Date: 28.09.18.
 */
public interface IGameRoomSnapshotPersister {

    void persist(IGameRoomSnapshot snapshot);

    IGameRoomSnapshot get(long roomId, long roundId);
}
