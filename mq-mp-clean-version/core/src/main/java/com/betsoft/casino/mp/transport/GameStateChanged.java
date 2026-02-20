package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.IGameStateChanged;
import com.betsoft.casino.mp.model.RoomState;
import com.betsoft.casino.utils.TObject;

/**
 * User: flsh
 * Date: 11.06.17.
 */
public class GameStateChanged extends TObject implements IGameStateChanged {
    private RoomState state;
    private long ttnx; // rough time to next state in seconds
    private long roundId;
    //only for PLAY state
    private Long roundStartTime;

    public GameStateChanged(long date, RoomState state, long ttnx, long roundId, Long roundStartTime) {
        super(date, SERVER_RID);
        this.state = state;
        this.ttnx = ttnx;
        this.roundId = roundId;
        this.roundStartTime = roundStartTime;
    }

    @Override
    public RoomState getState() {
        return state;
    }

    public void setState(RoomState state) {
        this.state = state;
    }

    @Override
    public long getTtnx() {
        return ttnx;
    }

    public void setTtnx(long ttnx) {
        this.ttnx = ttnx;
    }

    @Override
    public long getRoundId() {
        return roundId;
    }

    public void setRoundId(long roundId) {
        this.roundId = roundId;
    }

    @Override
    public Long getRoundStartTime() {
        return roundStartTime;
    }

    public void setRoundStartTime(Long roundStartTime) {
        this.roundStartTime = roundStartTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        GameStateChanged that = (GameStateChanged) o;

        if (ttnx != that.ttnx) return false;
        return state == that.state;

    }

    @Override
    public String toString() {
        return "GameStateChanged[" +
                "rid==" + rid +
                ", date=" + date +
                ", state=" + state +
                ", ttnx=" + ttnx +
                ", roundId=" + roundId +
                ", roundStartTime=" + roundStartTime +
                ']';
    }
}
