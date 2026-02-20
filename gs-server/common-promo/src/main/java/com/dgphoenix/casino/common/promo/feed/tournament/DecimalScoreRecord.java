package com.dgphoenix.casino.common.promo.feed.tournament;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("record")
public class DecimalScoreRecord extends TournamentFeedRecord<DecimalScoreRecord> {
    @XStreamAlias("Score")
    private final String score;

    public DecimalScoreRecord(String rank, String playerId, String nickName, String score) {
        super(rank, playerId, nickName);
        this.score = score;
    }

    @Override
    public String getScoreAsString() {
        return score;
    }

    @Override
    public int compareTo(DecimalScoreRecord o) {
        double thisScore = 0;
        double otherScore = 0;
        try {
            thisScore = Double.parseDouble(score);
            otherScore = Double.parseDouble(o.score);
        } catch (NumberFormatException e) {
            //nop, impossible
        }
        return Double.compare(thisScore, otherScore);
    }
}
