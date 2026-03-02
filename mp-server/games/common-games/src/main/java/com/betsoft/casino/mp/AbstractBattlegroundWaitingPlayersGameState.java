package com.betsoft.casino.mp;

import com.betsoft.casino.mp.common.AbstractSingleNodeWaitingPlayerGameState;
import com.betsoft.casino.mp.model.IActionGameSeat;
import com.betsoft.casino.mp.model.IBattlegroundRoom;
import com.betsoft.casino.mp.model.IGameState;
import com.betsoft.casino.mp.model.IMap;
import com.betsoft.casino.mp.model.RoomState;
import com.abs.casino.common.exception.CommonException;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;

public abstract class AbstractBattlegroundWaitingPlayersGameState<GAMEROOM extends IBattlegroundRoom, SEAT extends IActionGameSeat, MAP extends IMap, GS extends IGameState>
        extends AbstractSingleNodeWaitingPlayerGameState<GAMEROOM, SEAT, MAP, GS> {
    private static final byte VERSION = 0;
    public static final long START_NEW_ROUND_PAUSE_FOR_BG = 1000;
    protected long timeToStart;
    protected long possibleTimeToStart;

    public AbstractBattlegroundWaitingPlayersGameState() {
        super();
    }

    public AbstractBattlegroundWaitingPlayersGameState(GAMEROOM gameRoom) {
        super(gameRoom);
    }


    @Override
    public boolean isBuyInAllowed(SEAT seat) {
        return getRoomInfo().isBattlegroundMode() && seat.getAmmoAmount() <= 0;
    }

    @Override
    public void firePlayersCountChanged() throws CommonException {
        gameRoom.lock();
        try {

            getLog().debug("firePlayersCountChanged: (AbstractBattleground): Room State={}", gameRoom.getState());

            if (gameRoom.getState().equals(RoomState.WAIT)) {
                short realSeatsCount = gameRoom.getRealConfirmedSeatsCount();
                getLog().debug("firePlayersCountChanged: (AbstractBattleground): realSeatsCount={}, minSeats={}, needStartNewRound={}" +
                                ", timeToStart before: {}",
                        realSeatsCount, gameRoom.getMinSeats(), needStartNewRound, timeToStart);

                if ((realSeatsCount == 0 && timeToStart < Long.MAX_VALUE) || timeToStart == 0) {
                    if (gameRoom.getSeatsCount() > 0) {
                        //case for all players not make rebuy
                        possibleTimeToStart = getExpectedBattlegroundModeStartTime();
                        timeToStart = possibleTimeToStart;
                        getLog().debug("firePlayersCountChanged: (AbstractBattleground): wait for BG rebuy, set timeToStart={}", timeToStart);
                    } else {
                        timeToStart = Long.MAX_VALUE;
                        possibleTimeToStart = getExpectedBattlegroundModeStartTime();
                        getLog().debug("firePlayersCountChanged: (AbstractBattleground): reset timeToStart: {} ", timeToStart);
                    }
                }

                if (realSeatsCount > 0 && timeToStart == Long.MAX_VALUE) {
                    timeToStart = getExpectedBattlegroundModeStartTime();
                    getLog().debug("firePlayersCountChanged: (AbstractBattleground): first seater in battle: timeToStart: {} ", timeToStart);
                }

                if (timeToStart != Long.MAX_VALUE) {
                    if (!gameRoom.isTimerStopped()) {
                        getLog().debug("firePlayersCountChanged: (AbstractBattleground): stopTimer");
                        gameRoom.stopTimer();
                    }
                    gameRoom.setTimerTime(START_NEW_ROUND_PAUSE_FOR_BG);
                    gameRoom.startTimer();
                    getLog().debug("firePlayersCountChanged: (AbstractBattleground): startTimer");
                }

            } else {
                getLog().debug("firePlayersCountChanged: (AbstractBattleground):  game room is in PLAY state, not need any action");
            }
        } finally {
            gameRoom.unlock();
        }
    }

    private long getExpectedBattlegroundModeStartTime() {
        long currentTime = System.currentTimeMillis();
        long waitTime = TimeUnit.SECONDS.toMillis(10);
        return currentTime + waitTime;
    }

    public void onTimer(boolean needClearEnemies) throws CommonException {
        gameRoom.lock();
        try {
            short seatsCount = gameRoom.getRealConfirmedSeatsCount();
            if (System.currentTimeMillis() >= possibleTimeToStart) {
                possibleTimeToStart = getExpectedBattlegroundModeStartTime();
            }
            boolean allSeatsOccupied = seatsCount >= gameRoom.getMaxSeats();
            boolean timeIsOver = System.currentTimeMillis() >= timeToStart;
            boolean readyStartRound = seatsCount >= gameRoom.getMinSeats() && (allSeatsOccupied || timeIsOver);
            if (readyStartRound) {
                gameRoom.removeSeatsWithPendingOperations();
                gameRoom.sitOutAllPlayersWithoutConfirmedRebuy();
                setPlayGameState();
            } else {
                if (timeIsOver) {
                    getLog().debug("onTimer, found case for reBuy failed, need cancel BG and return money " +
                            "seatsCount={}, timeToStart={}", seatsCount, timeToStart);
                    gameRoom.removeSeatsWithPendingOperations();
                    gameRoom.convertBulletsToMoney();
                    //cancel BG and return all money
                }
                gameRoom.stopTimer();
                gameRoom.setTimerTime(START_NEW_ROUND_PAUSE_FOR_BG);
                gameRoom.startTimer();
            }
        } finally {
            gameRoom.unlock();
        }
    }

    @Override
    public long getTimeToStart() {
        if (timeToStart == Long.MAX_VALUE) {
            return possibleTimeToStart;
        }
        return timeToStart == 0 ? getExpectedBattlegroundModeStartTime() : timeToStart;
    }

    public void setTimeToStart(long timeToStart) {
        this.timeToStart = timeToStart;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeBoolean(needStartNewRound);
        output.writeLong(timeToStart, true);
        output.writeLong(possibleTimeToStart, true);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte ver = input.readByte();
        needStartNewRound = input.readBoolean();
        timeToStart = input.readLong(true);
        possibleTimeToStart = input.readLong(true);
    }

    @Override
    public void serializeObject(JsonGenerator gen, SerializerProvider serializers) throws IOException {
        

        gen.writeBooleanField("isNeedStartNewRound", isNeedStartNewRound());
        gen.writeNumberField("timeToStart", timeToStart);
        gen.writeNumberField("possibleTimeToStart", possibleTimeToStart);


    }

    @Override
    public GS deserializeObject(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);

        needStartNewRound = node.get("needStartNewRound").booleanValue();
        timeToStart = node.get("timeToStart").longValue();
        possibleTimeToStart = node.get("possibleTimeToStart").longValue();

        return getDeserializer();
    }

    protected abstract GS getDeserializer();

    @Override
    public String toString() {
        return new StringJoiner(", ", AbstractBattlegroundWaitingPlayersGameState.class.getSimpleName() + "[", "]")
                .add("possibleTimeToStart=" + possibleTimeToStart)
                .add("needStartNewRound=" + needStartNewRound)
                .toString();
    }
}
