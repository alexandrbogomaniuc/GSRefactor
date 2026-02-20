package com.dgphoenix.casino.common.promo.feed.tournament;

public interface ITournamentFeedRecord<T> extends Comparable<T> {
    String getRank();

    String getPlayerId();

    String getNickName();

    String getScoreAsString();
}
