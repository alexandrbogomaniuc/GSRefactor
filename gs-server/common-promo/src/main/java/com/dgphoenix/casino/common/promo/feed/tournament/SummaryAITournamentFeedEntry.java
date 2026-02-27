package com.abs.casino.common.promo.feed.tournament;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("player")
public class SummaryAITournamentFeedEntry implements Comparable<SummaryAITournamentFeedEntry>, ISummaryTournamentFeedEntry {

    private String nickname;
    private String score;

    public SummaryAITournamentFeedEntry(String nickname, String score) {
        this.nickname = nickname;
        this.score = score;
    }

    @Override
    public String getBankName() {
        return null;
    }

    @Override
    public ITournamentFeedRecord getRecord() {
        return null;
    }

    @Override
    public String getNickName() {
        return nickname;
    }

    @Override
    public void setNickName(String nickname) {
        this.nickname = nickname;
    }

    public String getScore() {
        return score;
    }

    @Override
    public int compareTo(SummaryAITournamentFeedEntry o) {
        return score.compareTo(o.score);
    }
}
