package com.abs.casino.common.promo.feed.tournament;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("record")
public class RoundCountRecord extends TournamentFeedRecord<RoundCountRecord> {
    @XStreamAlias("Rounds")
    private final long rounds;

    public RoundCountRecord(String rank, String playerId, String nickName, long rounds) {
        super(rank, playerId, nickName);
        this.rounds = rounds;
    }

    @Override
    public String getScoreAsString() {
        return String.valueOf(rounds);
    }

    @Override
    public int compareTo(RoundCountRecord o) {
        return (rounds < o.rounds ? -1 : (rounds == o.rounds ? 0 : 1));
    }
}
