package com.dgphoenix.casino.common.promo.feed.tournament;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("record")
public class BetSumRecord extends TournamentFeedRecord<BetSumRecord> {
    @XStreamAlias("BetSum")
    private final double betSum;

    public BetSumRecord(String rank, String playerId, String nickName, long betSumInCents) {
        super(rank, playerId, nickName);
        this.betSum = (double) betSumInCents / 100;
    }

    @Override
    public String getScoreAsString() {
        return String.valueOf(betSum);
    }

    @Override
    public int compareTo(BetSumRecord o) {
        return Double.compare(betSum, o.betSum);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        BetSumRecord that = (BetSumRecord) o;

        return Double.compare(that.betSum, betSum) == 0;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        long temp;
        temp = Double.doubleToLongBits(betSum);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
