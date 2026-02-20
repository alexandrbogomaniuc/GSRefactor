package com.betsoft.casino.mp.common;

import com.betsoft.casino.mp.model.IGameState;
import com.betsoft.casino.mp.model.IMap;
import com.betsoft.casino.mp.model.ISeat;
import com.betsoft.casino.mp.model.RoomState;
import com.betsoft.casino.mp.model.room.IRoom;
import com.dgphoenix.casino.common.exception.CommonException;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * User: flsh
 * Date: 14.02.19.
 */
@SuppressWarnings("rawtypes")
/**
 * The state of the room before the start of the round.
 */
public abstract class AbstractWaitingPlayersGameState<GAMEROOM extends IRoom, SEAT extends ISeat, MAP extends IMap, GS extends IGameState>
        extends AbstractGameState<GAMEROOM, SEAT, MAP, GS> {
    private static final byte VERSION = 0;

    public static final long START_NEW_ROUND_PAUSE = 2000;

    protected AbstractWaitingPlayersGameState() {
        super();
    }

    protected AbstractWaitingPlayersGameState(GAMEROOM gameRoom) {
        super(gameRoom);
    }

    /**
     * Moves to next state (play game state)
     * @throws CommonException if any unexpected error occur
     */
    protected abstract void setPlayGameState() throws CommonException;

    /**
     * Prepares and starts the round
     * @throws CommonException if any unexpected error occur
     */
    @Override
    public void init() throws CommonException {
        boolean isLocked = false;
        if (!gameRoom.isLocked()) {
            gameRoom.lock();
            isLocked = true;
        }
        try {
            super.init();
            if (getRoom() != null) { //may be null on serialization
                innerInit();
                //need set new roundId on this state for prevent infinity replaying snapshots
                initNewRound();
            }
        } finally {
            if (isLocked) {
                gameRoom.unlock();
            }
        }
    }

    protected void innerInit() throws CommonException {

        if (getRoom().shutdownRoomIfEmpty()) {
            return;
        }

        short seatsCountRequiredForStart = getSeatsCountRequiredForStart();
        short minSeats = getRoom().getMinSeats();

        getLog().debug("innerInit: (Abstract): seatsCountRequiredForStart={},  minSeats={} for RoomInfo={}",
                seatsCountRequiredForStart, minSeats, gameRoom.getRoomInfo());

        if (seatsCountRequiredForStart < minSeats) {
            getLog().debug("innerInit: (Abstract): call setTimerStartNewRoundTime() for RoomInfo={}", gameRoom.getRoomInfo());
            setTimerStartNewRoundTime();
        } else {
            getLog().debug("innerInit: (Abstract): call firePlayersCountChanged() for RoomInfo={}", gameRoom.getRoomInfo());
            firePlayersCountChanged();
        }
    }

    protected short getSeatsCountRequiredForStart() {
        return getRoom().getRealSeatsCount();
    }

    protected abstract boolean isNeedStartNewRound();
    protected abstract void setNeedStartNewRound(boolean needStartNewRound);

    protected void setTimerStartNewRoundTime() throws CommonException {
        long time = getStartNewRoundPause();
        getRoom().setTimerTime(time);
        getRoom().startTimer();
    }

    private void initNewRound() {
        getRoom().updateRoomInfo(roomInfo -> {
            Class<?> klass = getRoom().getTOFactoryService().getClassForRoundId();
            long nextRoundId = getRoom().getIdGenerator().getNext(klass);
            roomInfo.startNewRound(nextRoundId);
            getLog().debug("Start new round with roundId={}", nextRoundId);
        });
    }

    @Override
    public void restoreGameRoom(GAMEROOM gameRoom) throws CommonException {
        super.restoreGameRoom(gameRoom);

        getLog().debug("restoreGameRoom: gameRoom={}", gameRoom);

        gameRoom.lock();
        try {
            init();
            if (gameRoom.isTimerStopped() && isNeedStartNewRound()) {
                getLog().debug("restoreGameRoom: startTimer for gameRoom={}", gameRoom);
                setTimerStartNewRoundTime();
            }
        } finally {
            gameRoom.unlock();
        }
    }

    @Override
    public void firePlayersCountChanged() throws CommonException {
        boolean locked = false;

        try {
            locked = gameRoom.tryLock(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            getLog().warn("firePlayersCountChanged: (Abstract): roomId={}, cannot lock: {}", getRoomId(), e.getMessage());
        }

        if (!locked) {
            getLog().error("firePlayersCountChanged: (Abstract): roomId={}, cannot obtain room lock, just exit", getRoomId());
            return;
        }

        getLog().debug("firePlayersCountChanged: (Abstract): roomId={}, Room State={}", getRoomId(), gameRoom.getState());

        try {

            if (gameRoom.getState().equals(RoomState.WAIT)) {

                short seatsCount = getSeatsCountRequiredForStart();
                getLog().debug("firePlayersCountChanged: (Abstract): roomId={}, seatsCount={}, minSeats={}, needStartNewRound={}",
                        getRoomId(), seatsCount, getRoom().getMinSeats(), isNeedStartNewRound());

                if (seatsCount >= getRoom().getMinSeats()) {

                    if (!isNeedStartNewRound()) {

                        if (!getRoom().isTimerStopped()) {
                            getLog().debug("firePlayersCountChanged: (Abstract): roomId={}, stopTimer", getRoomId());
                            getRoom().stopTimer();
                        }

                        setNeedStartNewRound(true);

                        getLog().debug("firePlayersCountChanged: (Abstract): roomId={}, call setTimerStartNewRoundTime()", getRoomId());
                        setTimerStartNewRoundTime();

                        getLog().debug("firePlayersCountChanged: (Abstract): roomId={}, startTimer", getRoomId());
                    }
                }
            } else {
                getLog().debug("firePlayersCountChanged: (Abstract): roomId={}, game room is in PLAY state, not need any action", getRoomId());
            }
        } finally {
            gameRoom.unlock();
        }
    }

    public void onTimer(boolean needClearEnemies) throws CommonException {

        short seatsCount = getSeatsCountRequiredForStart();
        //remove after debug
        getLog().debug("onTimer: (Abstract): roomId={}, seatsCount={}, seatsCount={}, needStartNewRound={}",
                getRoomId(), getRoom().getSeatsCount(), seatsCount, isNeedStartNewRound());

        if (seatsCount >= getRoom().getMinSeats() && isNeedStartNewRound()) {

            setPlayGameState();

        } else {
            getLog().debug("onTimer: (Abstract): roomId={}, stop timer and call  setTimerStartNewRoundTime()", getRoomId());
            getRoom().stopTimer();
            setTimerStartNewRoundTime();
        }
    }

    @Override
    public boolean isSitInAllowed() {
        return true;
    }

    @Override
    public void processSitIn(SEAT seat) throws CommonException {
        getLog().debug("processSitIn: (Abstract): seat={}, call firePlayersCountChanged()", seat);
        firePlayersCountChanged();
    }

    @Override
    public void processSitOut(SEAT seat) throws CommonException {
        getLog().debug("processSitOut: (Abstract): seat={}, call firePlayersCountChanged()", seat);
        super.processSitOut(seat);
        firePlayersCountChanged();
    }

    @Override
    public void fireReBuyAccepted(SEAT seat) throws CommonException {
        getLog().debug("fireReBuyAccepted: (Abstract): seat={}, call firePlayersCountChanged()", seat);
        firePlayersCountChanged();
    }

    @Override
    public RoomState getRoomState() {
        return RoomState.WAIT;
    }

    @Override
    public boolean isBattlegroundSitOutAllowed() {
        return true;
    }

    protected long getStartNewRoundPause() {
        return START_NEW_ROUND_PAUSE;
    }

    @Override
    public boolean isAllowedRemoving() {
        return true;
    }

    @Override
    public boolean isAllowedKick() {
        return true;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeBoolean(isNeedStartNewRound());
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte ver = input.readByte();
        setNeedStartNewRound(input.readBoolean());
    }

    @Override
    public void serializeObject(JsonGenerator gen, SerializerProvider serializers) throws IOException {
        

        gen.writeBooleanField("isNeedStartNewRound", isNeedStartNewRound());


    }

    @Override
    public GS deserializeObject(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);

        setNeedStartNewRound(node.get("isNeedStartNewRound").booleanValue());

        return getDeserializer();
    }

    protected abstract GS getDeserializer();

    @Override
    public String toString() {
        return "WaitingPlayersGameState [needStartNewRound=" + isNeedStartNewRound() + ']';
    }

}
