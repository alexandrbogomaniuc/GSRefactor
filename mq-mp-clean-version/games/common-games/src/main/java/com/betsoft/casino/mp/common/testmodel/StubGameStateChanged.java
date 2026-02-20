package com.betsoft.casino.mp.common.testmodel;

import com.betsoft.casino.mp.model.IGameStateChanged;
import com.betsoft.casino.mp.model.RoomState;
import com.betsoft.casino.utils.TObject;

public class StubGameStateChanged extends TObject implements IGameStateChanged {
    private RoomState state;
    private long ttnx; // rough time to next state in seconds
    private long roundId;

    public StubGameStateChanged(long date, RoomState state, long ttnx, long roundId) {
        super(date, SERVER_RID);
        this.state = state;
        this.ttnx = ttnx;
        this.roundId = roundId;
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

    @Override
    public Long getRoundStartTime() {
        return null;
    }

    public void setRoundId(long roundId) {
        this.roundId = roundId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        StubGameStateChanged that = (StubGameStateChanged) o;

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
                ']';
    }
}

