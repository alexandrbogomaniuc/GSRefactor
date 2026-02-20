package com.dgphoenix.casino.common.promo.feed.tournament;

import com.dgphoenix.casino.common.cache.Identifiable;
import com.dgphoenix.casino.common.promo.TournamentObjective;

import java.util.Date;

/**
 * User: flsh
 * Date: 12.04.17.
 */
public class SummaryTournamentFeed implements Identifiable {
    private long id;
    private String feedURL;
    private String bankName;
    private long startDate;
    private long endDate;
    private String checksum;
    private TournamentObjective type;
    private boolean maskName;

    public SummaryTournamentFeed() {
    }

    public SummaryTournamentFeed(long id, String feedURL, String bankName, long startDate, long endDate,
                                 String checksum, TournamentObjective type, boolean maskName) {
        this.id = id;
        this.feedURL = feedURL;
        this.bankName = bankName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.checksum = checksum;
        this.type = type;
        this.maskName = maskName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFeedURL() {
        return feedURL;
    }

    public void setFeedURL(String feedURL) {
        this.feedURL = feedURL;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public long getEndDate() {
        return endDate;
    }

    public void setEndDate(long endDate) {
        this.endDate = endDate;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public TournamentObjective getType() {
        return type;
    }

    public void setType(TournamentObjective type) {
        this.type = type;
    }

    public boolean isMaskName() {
        return maskName;
    }

    public void setMaskName(boolean maskName) {
        this.maskName = maskName;
    }

    @Override
    public String toString() {
        return "SummaryTournamentFeed[" +
                "id=" + id +
                ", feedURL='" + feedURL + '\'' +
                ", bankName='" + bankName + '\'' +
                ", startDate=" + new Date(startDate) +
                ", endDate=" + new Date(endDate) +
                ", checksum='" + checksum + '\'' +
                ", type=" + type +
                ", maskName=" + maskName +
                ']';
    }
}
