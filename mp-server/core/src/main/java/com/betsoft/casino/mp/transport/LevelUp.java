package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.ILevelUp;
import com.betsoft.casino.utils.TObject;

public class LevelUp extends TObject implements ILevelUp {
    private int seatId;
    private int level;

    public LevelUp(long date, int rid, int seatId, int level) {
        super(date, rid);
        this.seatId = seatId;
        this.level = level;
    }

    @Override
    public String toString() {
        return "LevelUp[" +
                "seatId=" + seatId +
                ", level=" + level +
                ", date=" + date +
                ", rid=" + rid +
                ']';
    }
}
