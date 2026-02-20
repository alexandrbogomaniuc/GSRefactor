package com.betsoft.casino.mp.common;

import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.room.IRoom;
import com.betsoft.casino.mp.model.room.IRoomInfo;
import com.dgphoenix.casino.common.exception.CommonException;

/**
 * User: flsh
 * Date: 14.02.19.
 */
public abstract class AbstractGameState<GAMEROOM extends IRoom, SEAT extends ISeat, MAP extends IMap, GS extends IGameState>
        implements IGameState<GAMEROOM, SEAT, MAP, GS> {

    protected transient GAMEROOM gameRoom;

    protected AbstractGameState() {
    }

    protected AbstractGameState(GAMEROOM gameRoom) {
        this.gameRoom = gameRoom;
    }

    @Override
    public void init() throws CommonException {
        if (getRoom() != null && getRoom().getRoomInfo() != null) { //may be null on serialization
            getRoom().updateRoomInfo(roomInfo -> {
                IRoomInfo roomRoomInfo = getRoom().getRoomInfo();
                RoomState oldState = roomRoomInfo.getState();
                roomRoomInfo.setState(getRoomState());
                getLog().debug("init: persist roomInfo, after update state, oldState={}, new state={}", oldState,
                        roomInfo.getState());
            });
        }
    }

    protected long getRoomId() {
        return getRoom().getId();
    }

    /**
     * Get seat number of player in room
     * @param seat seat of player
     * @return int seat number
     */
    protected int getSeatNumber(SEAT seat) {
        //noinspection unchecked
        return getRoom().getSeatNumber(seat);
    }

    public void processSitOut(SEAT seat) throws CommonException {
        if (seat != null) {
            seat.setWantSitOut(true);
        }
    }

    @Override
    public GAMEROOM getRoom() {
        return gameRoom;
    }

    public MAP getMap() {
        return (MAP) getRoom().getMap();
    }

    @Override
    public void restoreGameRoom(GAMEROOM gameRoom) throws CommonException {
        this.gameRoom = gameRoom;
    }

    @Override
    public long getTimeToNextState() {
        return UNKNOWN_OR_STOPPED_TIME_TO_NEXT_STATE;
    }

    protected long getCurrentTime() {
        return getRoom().getCurrentTime();
    }

    protected boolean isFRB() {
        return getRoom().getRoomInfo().getMoneyType() == MoneyType.FRB;
    }
}
