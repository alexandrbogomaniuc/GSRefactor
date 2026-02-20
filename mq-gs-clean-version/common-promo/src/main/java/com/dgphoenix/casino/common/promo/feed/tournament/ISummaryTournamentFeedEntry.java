package com.dgphoenix.casino.common.promo.feed.tournament;

public interface ISummaryTournamentFeedEntry {

    String getBankName();

    String getNickName();

    void setNickName(String nickName);

    String getScore();

    ITournamentFeedRecord getRecord();
}
