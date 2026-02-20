package com.betsoft.casino.mp.transport;

import com.betsoft.casino.utils.TObject;

public class LevelUpLobby extends TObject {
    private int seatId;
    private int level;
    private long xp;
    private long xpPrev;
    private long xpNext;

    public LevelUpLobby(long date, int rid, int seatId, int level, long xp, long xpPrev, long xpNext) {
        super(date, rid);
        this.seatId = seatId;
        this.level = level;
        this.xp = xp;
        this.xpPrev = xpPrev;
        this.xpNext = xpNext;
    }

    @Override
    public String toString() {
        return "LevelUp[" +
                "seatId=" + seatId +
                ", level=" + level +
                ", date=" + date +
                ", rid=" + rid +
                ", xp=" + xp +
                ", xpPrev=" + xpPrev +
                ", xpNext=" + xpNext +
                ']';
    }
}
