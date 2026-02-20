package com.betsoft.casino.mp.service;

import com.betsoft.casino.mp.model.IMultiNodeSeat;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * User: flsh
 * Date: 20.01.2022.
 */
@SuppressWarnings("rawtypes")
public interface IMultiNodeSeatService {
    String ID_DELIMITER = "+";
    Collection<IMultiNodeSeat> getAllRoomSeats();
    <SEAT extends IMultiNodeSeat> List<SEAT> getRoomSeats(long roomId, Class<SEAT> requiredType);
    IMultiNodeSeat getSeat(long roomId, long accountId);
    void put(IMultiNodeSeat playerInfo);
    void removeAll(long roomId);
    void remove(long roomId, long accountId);
    void remove(IMultiNodeSeat seat);
    default String getKey(long roomId, long accountId) {
        return roomId + ID_DELIMITER + accountId;
    }
    default String getKey(IMultiNodeSeat seat) {
        return getKey(seat.getRoomId(), seat.getAccountId());
    }
    int seatsCount(long roomId);
}
