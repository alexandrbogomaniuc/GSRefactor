package com.dgphoenix.casino.common.promo.feed.tournament;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("record")
public class ScoreRecord extends TournamentFeedRecord<ScoreRecord> {
    @XStreamAlias("Score")
    private final long score;

    public ScoreRecord(String rank, String playerId, String nickName, long score) {
        super(rank, playerId, nickName);
        this.score = score;
    }

    @Override
    public String getScoreAsString() {
        return String.valueOf(score);
    }

    @Override
    public int compareTo(ScoreRecord o) {
        return (score < o.score ? -1 : (score == o.score ? 0 : 1));
    }
}
