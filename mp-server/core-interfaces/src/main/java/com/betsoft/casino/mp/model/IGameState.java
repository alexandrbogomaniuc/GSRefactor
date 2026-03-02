package com.betsoft.casino.mp.model;

import com.betsoft.casino.mp.model.room.IRoom;
import com.betsoft.casino.mp.model.room.IRoomInfo;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.utils.TInboundObject;
import com.abs.casino.common.cache.JsonSelfSerializable;
import com.abs.casino.common.exception.CommonException;
import com.abs.casino.common.util.ILongIdGenerator;
import com.esotericsoftware.kryo.KryoSerializable;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * User: flsh
 * Date: 14.11.17.
 */

/**
 * Interface for game state for room.
 * @param <GAMEROOM> gameroom
 * @param <SEAT> seat
 * @param <MAP> game map
 */
public interface IGameState<GAMEROOM extends IRoom, SEAT extends ISeat, MAP extends IMap, GS extends IGameState> 
        extends KryoSerializable, JsonSelfSerializable<GS> {
    int UNKNOWN_OR_STOPPED_TIME_TO_NEXT_STATE = -1;

    void init() throws CommonException;

    default IRoomInfo getRoomInfo() {
        return getRoom().getRoomInfo();
    }

    default ILongIdGenerator getIdGenerator() {
        return getRoom().getIdGenerator();
    }


    GAMEROOM getRoom();

    /**
     * Restore game room from snapshot if room was moved to other server or after reboot.
     * @param gameRoom gameroom
     * @throws CommonException   if any unexpected error occur
     */
    void restoreGameRoom(GAMEROOM gameRoom) throws CommonException;

    /**
     * Process sitIn. Not all states are allowed to enter the room
     * @param seat seat of player
     * @throws CommonException   if any unexpected error occur
     */
    default void processSitIn(SEAT seat) throws CommonException {
        throwUnsupportedOperationException("SitIn");
    }

    /**
     * Process sitOut. Not all states are allowed to exit from room
     * @param seat seat of player
     * @throws CommonException   if any unexpected error occur
     */
    void processSitOut(SEAT seat) throws CommonException;

    default void placeMineToMap(SEAT seat, IMineCoordinates mineCoordinates) throws CommonException {
        throwUnsupportedOperationException("placeMineToMap");
    }

    /**
     * Get room state
     * @return {@code RoomState} room state (WAIT, PLAY, QUALIFY,CLOSED, PAUSE, BUY_IN)
     */
    RoomState getRoomState();

    default long getTimeToStart(){
        return 0;
    }

    default PlaySubround getSubround() {
        return PlaySubround.BASE;
    }

    default void nextSubRound() throws CommonException {
        //nop by default
    }

    default long getTimeToNextState() {
        return UNKNOWN_OR_STOPPED_TIME_TO_NEXT_STATE;
    }

    /**
     * Method calls when number of seats changed.
     * @throws CommonException  if any unexpected error occur
     */
    default void firePlayersCountChanged() throws CommonException {
        //nop by default
    }

    /**
     * The method is called when the timer expires.
     * @param needClearEnemies true if you need clear enemies on map.
     * @throws CommonException  if any unexpected error occur
     */
    default void onTimer(boolean needClearEnemies) throws CommonException {
        throw new UnsupportedOperationException();
    }

    default void firePlaySubroundFinished(boolean endGame) throws CommonException {
        //nop, just log
        getLog().warn("May be error, this call allowed only from PlayGameState, current is: {}", this);
    }

    default Logger getLog() {
        return getRoom().getLog();
    }

    default void throwUnsupportedOperationException(String opName) {
        throw new UnsupportedOperationException("Operation not supported: " + opName +
                ". Additional info: tableId=" + getRoom().getId() + ", state " +
                toString());
    }

    default int getCurrentMapId() {
        return getRoom().getMapId();
    }

    default void update() throws CommonException, InterruptedException {
        //nop
    }

    /**
     * Checks if the buyIn is allowed or not.
     * @return true if allowed
     */
    default boolean isBuyInAllowed(SEAT seat) {
        return false;
    }

    default void closeResults(SEAT seat) throws CommonException {
    }

    default long getStartTime() {
        return 0;
    }

    default Map<Long, Integer> getFreezeTimeRemaining() {
        return new HashMap<>();
    }

    /**
     * Checks if removing of room is allowed or not. Is used only for private rooms
     * @return true if allowed
     */
    boolean isAllowedRemoving();

    /**
     * Checks if kicking player from room is allowed or not. Is used only for private rooms
     * @return true if allowed
     */
    boolean isAllowedKick();

    /**
     * Checks if the sitIn to the room is allowed or not.
     * @return true if allowed
     */
    default boolean isSitInAllowed() {
        return false;
    }

    default boolean isBossRound() {
        return false;
    }

    default boolean isRoundWasFinished() {
        return false;
    }

    default boolean isBattlegroundSitOutAllowed() {
        return false;
    }

    default void fireReBuyAccepted(SEAT seat) throws CommonException {
        //nop
    }

    default long getStartRoundTime() {
        return 0;
    }

    default long getEndRoundTime() {
        return 0;
    }

    /**
     * Processing  message of cancel crash multiplier for crash games.
     * @param accountId accountId of player
     * @param crashBetId crashBetId
     * @param senderRequestId serverId
     * @param placeNewBet placeNewBet
     * @param inboundMessage message from client
     * @return {@code ErrorCodes} error code. If cancel was success ErrorCodes.OK
     */
    default int processCancelCrashMultiplier(long accountId, String crashBetId, Integer senderRequestId, boolean placeNewBet,
                                             TInboundObject inboundMessage) {
        return ErrorCodes.CANCEL_BET_NOT_ALLOWED;
    }

    /**
     * Checks if the reconnection for the player is allowed or not.
     * @return true if allowed
     */
    default boolean isReconnectAllowed() {
        return true;
    }
}
