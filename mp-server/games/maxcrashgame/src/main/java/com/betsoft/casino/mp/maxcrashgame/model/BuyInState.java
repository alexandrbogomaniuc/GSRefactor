package com.betsoft.casino.mp.maxcrashgame.model;

import java.io.IOException;

import com.betsoft.casino.mp.common.AbstractGameState;
import com.betsoft.casino.mp.common.SharedCrashGameState;
import com.betsoft.casino.mp.model.ISharedGameStateService;
import com.betsoft.casino.mp.model.RoomState;
import com.betsoft.casino.mp.model.room.IRoomInfo;
import com.dgphoenix.casino.common.exception.CommonException;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * State for sending reserved buyIns to GS side before Play or Pause states.
 */
public class BuyInState extends AbstractGameState<AbstractCrashGameRoom, Seat, GameMap, BuyInState> {

    private static final byte VERSION = 0;
    public static final long BUY_IN_STATE_DURATION = 5;

    protected BuyInState() {}

    protected BuyInState(AbstractCrashGameRoom abstractCrashGameRoom) {
        super(abstractCrashGameRoom);
    }

    @Override
    public void init() throws CommonException {
        long roomId = getRoomId();
        gameRoom.lock();
        try {
            SharedCrashGameState crashGameState = getGameStateService().get(roomId, SharedCrashGameState.class);
            getLog().debug("BuyInGameState.init start: crashGameState={}", crashGameState);
            if (crashGameState == null) {
                IRoomInfo roomInfo = getRoomInfo();
                crashGameState = new SharedCrashGameState(roomInfo.getState(), roomId, roomInfo.getRoundId(), 0, 0, null);
                getGameStateService().put(crashGameState);
            }
            if (crashGameState.getState() != RoomState.BUY_IN) {
                crashGameState.setState(getRoomState());
                crashGameState.setMaxCrashData(null);
                getGameStateService().put(crashGameState);
                super.init();
                innerInit();
                crashGameState.setRoundId(getRoomInfo().getRoundId());
                getGameStateService().put(crashGameState);
            } else {
                innerInit();
            }
            getLog().debug("BuyInGameState.init end: crashGameState={}", crashGameState);
        } catch (Exception e) {
            getLog().error("BuyInGameState.init exception {}, message {}, stacktrace {}", e, e.getMessage(), e.getStackTrace());
        } finally {
            gameRoom.unlock();
        }
    }

    protected void setPausedGameState() throws CommonException {
        PlayGameState nextState = new PlayGameState(gameRoom);
        gameRoom.setGameState(new PausedGameState(gameRoom, nextState));
    }

    @Override
    public void onTimer(boolean needClearEnemies) throws CommonException {
        gameRoom.lock();
        try {
            getLog().debug("BuyInState.onTimer");
            gameRoom.processBuyInForReservedBets();
            setPausedGameState();
        } finally {
            gameRoom.unlock();
        }
    }

    protected void innerInit()  throws CommonException {
        if (!getRoom().isTimerStopped()) {
            getRoom().stopTimer();
        }
        gameRoom.setTimerTime(BUY_IN_STATE_DURATION);
        gameRoom.startTimer();
    }

    public RoomState getRoomState() {
        return RoomState.BUY_IN;
    }

    @Override
    public void restoreGameRoom(AbstractCrashGameRoom gameRoom) throws CommonException {
        super.restoreGameRoom(gameRoom);
        gameRoom.lock();
        try {
            init();
        } finally {
            gameRoom.unlock();
        }
    }

    private ISharedGameStateService getGameStateService() {
        return gameRoom.getSharedGameStateService();
    }


    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
    }

    public void read(Kryo kryo, Input input) {
        byte ver = input.readByte();
    }

    public String toString() {
        return "BuyInPlayersGameState";
    }

    @Override
    public boolean isBuyInAllowed(Seat seat) {
        return false;
    }

    @Override
    public boolean isAllowedRemoving() {
        return false;
    }

    @Override
    public boolean isAllowedKick() {
        return false;
    }

    @Override
    public void serializeObject(JsonGenerator gen, SerializerProvider serializers) throws IOException {
        

    }

    @Override
    public BuyInState deserializeObject(JsonParser p, DeserializationContext ctxt) throws IOException {
        return this;
    }
}
