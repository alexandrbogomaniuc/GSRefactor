package com.betsoft.casino.mp.common;

import com.betsoft.casino.mp.model.ISharedGameState;
import com.betsoft.casino.mp.model.RoomState;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Shared game state for crash game.
 */
public class SharedCrashGameState implements ISharedGameState {
    /** version of class, should be increased if added new fields  */
    private static final byte VERSION = 6;
    /** room state  WAIT, PLAY, QUALIFY, CLOSED, PAUSE, BUY_IN  */
    private RoomState state;
    /** roomId  */
    private long roomId;
    /** roundId of current round  */
    private long roundId;
    private long roundStartTime;
    private long roundEndTime;
    /** crash data for current round  */
    private MaxCrashData maxCrashData;
    /** flag indicating that calculation of results of the round have already been calculated on one of the servers.  */
    private boolean calculationFinished;
    private double kilometerMult;
    /** flag indicating that the processing of round results have already been finished in Qualify state  */
    private boolean qualifyRoundResultFinished;
    /** flag indicating that calculation of  results of the round have already been started on one of the servers. */
    private boolean roundResultProcessingStarted;
    private boolean needStartNewRound;
    /** Map of number of observers for all servers. key: serverId, value: number of observers in room on server with serverId  */
    private Map<Integer,Integer> observersMap;

    public SharedCrashGameState(RoomState state, long roomId, long roundId, long roundStartTime, long roundEndTime, MaxCrashData maxCrashData) {
        this.state = state;
        this.roomId = roomId;
        this.roundId = roundId;
        this.roundStartTime = roundStartTime;
        this.roundEndTime = roundEndTime;
        this.maxCrashData = maxCrashData;
        this.calculationFinished = false;
        this.kilometerMult = 0.8;
        this.qualifyRoundResultFinished = true;
        this.observersMap = new HashMap<>();
    }

    public Map<Integer, Integer> getObserversMap() {
        if(observersMap == null){
            observersMap = new HashMap<>();
        }
        return observersMap;
    }

    public void updateNumberObservers(int serverId, int numberObservers) {
        getObserversMap().put(serverId, numberObservers);
    }

    public int getTotalObservers(){
        return getObserversMap().values().stream().reduce(0, Integer::sum);
    }

    public boolean isCalculationFinished() {
        return calculationFinished;
    }

    public void setCalculationFinished(boolean calculationFinished) {
        this.calculationFinished = calculationFinished;
    }

    public boolean isQualifyRoundResultFinished() {
        return qualifyRoundResultFinished;
    }

    public void setQualifyRoundResultFinished(boolean qualifyRoundResultFinished) {
        this.qualifyRoundResultFinished = qualifyRoundResultFinished;
    }

    public SharedCrashGameState() {
    }

    @Override
    public RoomState getState() {
        return state;
    }

    public void setState(RoomState state) {
        this.state = state;
    }

    @Override
    public long getRoomId() {
        return roomId;
    }

    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }

    @Override
    public long getRoundId() {
        return roundId;
    }

    public void setRoundId(long roundId) {
        this.roundId = roundId;
    }

    @Override
    public long getRoundStartTime() {
        return roundStartTime;
    }

    public void setRoundStartTime(long roundStartTime) {
        this.roundStartTime = roundStartTime;
    }

    @Override
    public long getRoundEndTime() {
        return roundEndTime;
    }

    public void setRoundEndTime(long roundEndTime) {
        this.roundEndTime = roundEndTime;
    }

    public MaxCrashData getMaxCrashData() {
        return maxCrashData;
    }

    public void setMaxCrashData(MaxCrashData maxCrashData) {
        this.maxCrashData = maxCrashData;
    }

    public double getKilometerMult() {
        return kilometerMult;
    }

    public void setKilometerMult(double kilometerMult) {
        this.kilometerMult = kilometerMult;
    }

    public boolean isRoundResultProcessingStarted() {
        return roundResultProcessingStarted;
    }

    public void setRoundResultProcessingStarted(boolean roundResultProcessingStarted) {
        this.roundResultProcessingStarted = roundResultProcessingStarted;
    }

    public boolean isNeedStartNewRound() {
        return needStartNewRound;
    }

    public void setNeedStartNewRound(boolean needStartNewRound) {
        this.needStartNewRound = needStartNewRound;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        kryo.writeObjectOrNull(output, state, RoomState.class);
        output.writeLong(roomId, true);
        output.writeLong(roundId, true);
        output.writeLong(roundStartTime, true);
        output.writeLong(roundEndTime, true);
        kryo.writeObjectOrNull(output, maxCrashData, MaxCrashData.class);
        output.writeBoolean(calculationFinished);
        output.writeDouble(kilometerMult);
        output.writeBoolean(qualifyRoundResultFinished);
        output.writeBoolean(roundResultProcessingStarted);
        output.writeBoolean(needStartNewRound);
        kryo.writeObject(output, getObserversMap());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void read(Kryo kryo, Input input) {
        byte version = input.readByte();
        state = kryo.readObjectOrNull(input, RoomState.class);
        roomId = input.readLong(true);
        roundId = input.readLong(true);
        roundStartTime = input.readLong(true);
        roundEndTime = input.readLong(true);
        maxCrashData = kryo.readObjectOrNull(input, MaxCrashData.class);
        if(version > 0) {
            calculationFinished = input.readBoolean();
        }
        if(version > 1) {
            kilometerMult = input.readDouble();
        }
        if (version > 2) {
            qualifyRoundResultFinished = input.readBoolean();
        }
        if (version > 3) {
            roundResultProcessingStarted = input.readBoolean();
        }
        if (version > 4) {
            needStartNewRound = input.readBoolean();
        }
        if (version > 5) {
            observersMap = kryo.readObject(input, HashMap.class);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SharedCrashGameState that = (SharedCrashGameState) o;

        if (roomId != that.roomId) return false;
        if (roundId != that.roundId) return false;
        if (roundStartTime != that.roundStartTime) return false;
        if (roundEndTime != that.roundEndTime) return false;
        if (state != that.state) return false;
        return maxCrashData != null ? maxCrashData.equals(that.maxCrashData) : that.maxCrashData == null;
    }

    @Override
    public int hashCode() {
        int result = state != null ? state.hashCode() : 0;
        result = 31 * result + (int) (roomId ^ (roomId >>> 32));
        result = 31 * result + (int) (roundId ^ (roundId >>> 32));
        result = 31 * result + (int) (roundStartTime ^ (roundStartTime >>> 32));
        result = 31 * result + (int) (roundEndTime ^ (roundEndTime >>> 32));
        result = 31 * result + (maxCrashData != null ? maxCrashData.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", SharedCrashGameState.class.getSimpleName() + "[", "]")
                .add("state=" + state)
                .add("roomId=" + roomId)
                .add("roundId=" + roundId)
                .add("roundStartTime=" + roundStartTime)
                .add("roundEndTime=" + roundEndTime)
                .add("data=" + maxCrashData)
                .add("calculationFinished=" + calculationFinished)
                .add("kilometerMult=" + kilometerMult)
                .add("qualifyRoundResultFinished=" + qualifyRoundResultFinished)
                .add("roundResultProcessingStarted=" + roundResultProcessingStarted)
                .add("needStartNewRound=" + needStartNewRound)
                .add("observersMap=" + getObserversMap())
                .toString();
    }
}
