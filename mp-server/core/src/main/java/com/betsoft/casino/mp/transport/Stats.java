package com.betsoft.casino.mp.transport;

import com.betsoft.casino.utils.TObject;

public class Stats extends TObject {
    private long kills;
    private long treasures;
    private int rounds;
    private long xp;
    private long xpPrev;
    private long xpNext;
    private int level;

    public Stats(long date, long kills, long treasures, int rounds, long xp, long xpPrev, long xpNext, int level) {
        super(date, SERVER_RID);
        this.kills = kills;
        this.treasures = treasures;
        this.rounds = rounds;
        this.xp = xp;
        this.xpPrev = xpPrev;
        this.xpNext = xpNext;
        this.level = level;
    }

    @Override
    public String toString() {
        return "Stats[" +
                "kills=" + kills +
                ", treasures=" + treasures +
                ", rounds=" + rounds +
                ", xp=" + xp +
                ", xpPrev=" + xpPrev +
                ", xpNext=" + xpNext +
                ", level=" + level +
                ", date=" + date +
                ", rid=" + rid +
                ']';
    }
}
