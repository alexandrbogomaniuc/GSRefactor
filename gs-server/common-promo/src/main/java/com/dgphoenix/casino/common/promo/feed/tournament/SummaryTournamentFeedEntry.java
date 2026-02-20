package com.dgphoenix.casino.common.promo.feed.tournament;

import com.dgphoenix.casino.common.util.string.StringUtils;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * User: flsh
 * Date: 14.11.2019.
 */
@XStreamAlias("player")
public class SummaryTournamentFeedEntry implements Comparable<SummaryTournamentFeedEntry>, ISummaryTournamentFeedEntry {
    private String bankName;
    private String nickName;
    private String score;
    @XStreamOmitField
    private ITournamentFeedRecord record;

    public SummaryTournamentFeedEntry(String bankName,
                                      ITournamentFeedRecord record) {
        this.bankName = bankName;
        this.nickName = !StringUtils.isTrimmedEmpty(record.getNickName()) ? record.getNickName() : record.getPlayerId();
        this.score = record.getScoreAsString();
        this.record = record;
    }

    public String getBankName() {
        return bankName;
    }

    public String getNickName() {
        return nickName;
    }

    @Override
    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getScore() {
        return score;
    }

    public ITournamentFeedRecord getRecord() {
        return record;
    }

    @Override
    public int compareTo(SummaryTournamentFeedEntry o) {
        //noinspection unchecked
        return record.compareTo(o.record);
    }
}
