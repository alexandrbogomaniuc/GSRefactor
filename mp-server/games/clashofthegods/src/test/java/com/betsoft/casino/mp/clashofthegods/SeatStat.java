package com.betsoft.casino.mp.clashofthegods;

import com.betsoft.casino.mp.model.ICurrency;
import com.betsoft.casino.mp.clashofthegods.model.PlayerRoundInfo;

import java.util.HashSet;
import java.util.Set;

public class SeatStat {
    long seatId;
    ICurrency currency;
    Set<PlayerRoundInfo> rounds = new HashSet<>();


    public SeatStat(long seatId, ICurrency currency) {
        this.seatId = seatId;
        this.currency = currency;
    }


    public void updateStat(PlayerRoundInfo roundInfo) {
        rounds.add(roundInfo);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SeatStat seatStat = (SeatStat) o;

        return seatId == seatStat.seatId;
    }

    public Set<PlayerRoundInfo> getRounds() {
        return rounds;
    }

    @Override
    public int hashCode() {
        return (int) (seatId ^ (seatId >>> 32));
    }


    public void logStats() {
        double tbet = 0;
        double twin = 0;
        for (PlayerRoundInfo round : rounds) {
            System.out.println(round.getRTPStatData(843));
        }
    }

}
