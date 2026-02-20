package com.betsoft.casino.mp.common;

import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.room.IRoom;
import com.betsoft.casino.mp.model.room.IRoomInfo;
import com.dgphoenix.casino.common.exception.CommonException;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * User: flsh
 * Date: 14.02.19.
 */

/**
 * Class for calculation of results after play state.
 * @param <GAMEROOM> gameroom
 * @param <SEAT> seat
 * @param <MAP> game map
 */
public abstract class AbstractQualifyGameState<GAMEROOM extends IRoom, SEAT extends ISeat, MAP extends IMap, GS extends IGameState>
        extends AbstractGameState<GAMEROOM, SEAT, MAP, GS> {

    private static final byte VERSION = 0;
    private static final int QUALIFY_PAUSE = 10000;

    private int lastUsedMapId;
    private int pauseTime;
    private long startRoundTime;
    private long endRoundTime;
    protected Map<Integer, Boolean> skipResults = new HashMap<>(6);

    public AbstractQualifyGameState() {
        super();
    }

    public AbstractQualifyGameState(GAMEROOM gameRoom, int lastUsedMapId, int pauseTime, long startRoundTime, long endRoundTime) {
        super(gameRoom);
        this.lastUsedMapId = lastUsedMapId;
        this.pauseTime = pauseTime + getQualifyPauseTime();
        this.startRoundTime = startRoundTime;
        this.endRoundTime = endRoundTime;
    }

    public int getQualifyPauseTime() {
        return QUALIFY_PAUSE;
    }

    /**
     * Moves to next state (waiting game state)
     * @throws CommonException if any unexpected error occur
     */
    public abstract void setWaitingPlayersGameState() throws CommonException;

    /**
     * Method for calculations results after play game state.
     * @throws CommonException if any unexpected error occur
     */
    @Override
    public void init() throws CommonException {
        gameRoom.sendChanges(gameRoom.getTOFactoryService().createChangeMap(getCurrentTime(), gameRoom.getNextMapId(),
                PlaySubround.BASE.name()));
        gameRoom.lock();
        try {
            IRoomInfo roomInfo = getRoomInfo();
            if (!roomInfo.isBattlegroundMode()) {
                gameRoom.removeSeatsWithPendingOperations();
                gameRoom.updateStats();
            }
            gameRoom.sendRoundResults();
            gameRoom.resetRoundResults();
            gameRoom.convertBulletsToMoney();
            gameRoom.getMap().removeAllEnemies();
            gameRoom.resetRoundResults();
        } catch (Exception e) {
            getLog().error("QualifyGameState: init error", e);
        } finally {
            try {
                gameRoom.setTimerTime(getPauseTime());
                gameRoom.startTimer();
            } finally {
                gameRoom.unlock();
            }
        }
    }

    public int getPauseTime() {
        return pauseTime;
    }


    @Override
    public void restoreGameRoom(GAMEROOM gameRoom) throws CommonException {
        super.restoreGameRoom(gameRoom);
        gameRoom.setTimerTime(pauseTime);
        gameRoom.startTimer();
    }

    @Override
    public void onTimer(boolean needRemoveEnemies) throws CommonException {
        gameRoom.lock();
        try {
            gameRoom.removeDisconnectedObservers();
            gameRoom.removeAllEnemies();
            gameRoom.toggleMap();

            setWaitingPlayersGameState();

            IChangeMap changeMap = gameRoom.getTOFactoryService().createChangeMap(
                    getCurrentTime(),
                    gameRoom.getMapId(),
                    PlaySubround.BASE.name());

            gameRoom.sendChanges(changeMap);

        } finally {
            gameRoom.unlock();
        }
    }

    @Override
    public RoomState getRoomState() {
        return RoomState.QUALIFY;
    }

    @Override
    public int getCurrentMapId() {
        return lastUsedMapId;
    }

    @Override
    public long getTimeToNextState() {
        return pauseTime / 1000;
    }

    @Override
    public boolean isBattlegroundSitOutAllowed() {
        return true;
    }

    @Override
    public long getStartRoundTime() {
        return startRoundTime;
    }

    @Override
    public long getEndRoundTime() {
        return endRoundTime;
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
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeInt(lastUsedMapId, true);
        output.writeInt(pauseTime, true);
        output.writeLong(startRoundTime, true);
        output.writeLong(endRoundTime, true);
        kryo.writeClassAndObject(output, skipResults);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void read(Kryo kryo, Input input) {
        input.readByte();
        lastUsedMapId = input.readInt(true);
        pauseTime = input.readInt(true);
        startRoundTime = input.readLong(true);
        endRoundTime = input.readLong(true);
        skipResults = (Map<Integer, Boolean>) kryo.readClassAndObject(input);
    }

    @Override
    public void serializeObject(JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeNumberField("lastUsedMapId", lastUsedMapId);
        gen.writeNumberField("pauseTime", pauseTime);
        gen.writeNumberField("startRoundTime", startRoundTime);
        gen.writeNumberField("endRoundTime", endRoundTime);
        serializeMapField(gen, "skipResults", skipResults, new TypeReference<Map<Integer,Boolean>>() {});
    }

    @Override
    public GS deserializeObject(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectMapper om = (ObjectMapper) p.getCodec();
        JsonNode node = p.getCodec().readTree(p);

        lastUsedMapId = node.get("lastUsedMapId").intValue();
        pauseTime = node.get("pauseTime").intValue();
        startRoundTime = node.get("startRoundTime").longValue();
        endRoundTime = node.get("endRoundTime").longValue();
        skipResults = om.convertValue(node.get("skipResults"), new TypeReference<Map<Integer, Boolean>>() {});

        return getDeserializer();
    }

    protected abstract GS getDeserializer();

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("QualifyGameState [");
        sb.append("lastUsedMapId=").append(lastUsedMapId);
        sb.append(", pauseTime=").append(pauseTime);
        sb.append(", skipResults=").append(skipResults);
        sb.append(']');
        return sb.toString();
    }

}
