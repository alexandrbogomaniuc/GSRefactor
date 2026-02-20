package com.dgphoenix.casino.common.promo.feed.tournament;

import com.thoughtworks.xstream.annotations.XStreamAlias;

abstract class TournamentFeedRecord<T> implements ITournamentFeedRecord<T> {
    @XStreamAlias("Rank")
    private final String rank;
    @XStreamAlias("PlayerID")
    private final String playerId;
    @XStreamAlias("Nickname")
    private final String nickName;

    TournamentFeedRecord(String rank, String playerId, String nickName) {
        this.rank = rank;
        this.playerId = playerId;
        this.nickName = nickName;
    }

    @Override
    public String getRank() {
        return rank;
    }

    @Override
    public String getPlayerId() {
        return playerId;
    }

    @Override
    public String getNickName() {
        return nickName;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TournamentFeedRecord [");
        sb.append("rank='").append(rank).append('\'');
        sb.append(", playerId='").append(playerId).append('\'');
        sb.append(", nickName='").append(nickName).append('\'');
        sb.append(", score='").append(getScoreAsString()).append('\'');
        sb.append(']');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TournamentFeedRecord<?> that = (TournamentFeedRecord<?>) o;

        if (!rank.equals(that.rank)) return false;
        if (!playerId.equals(that.playerId)) return false;
        return nickName.equals(that.nickName);
    }

    @Override
    public int hashCode() {
        int result = rank.hashCode();
        result = 31 * result + playerId.hashCode();
        result = 31 * result + nickName.hashCode();
        return result;
    }
}
