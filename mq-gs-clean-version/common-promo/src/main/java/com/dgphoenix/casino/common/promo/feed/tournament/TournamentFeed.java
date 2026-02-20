package com.dgphoenix.casino.common.promo.feed.tournament;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vladislav on 12/27/16.
 */
@XStreamAlias("dataset")
public class TournamentFeed {
    @XStreamImplicit
    private final List<ITournamentFeedRecord> records = new ArrayList<ITournamentFeedRecord>();

    public void addRecord(ITournamentFeedRecord record) {
        records.add(record);
    }

    public int getSize() {
        return records.size();
    }

    public List<ITournamentFeedRecord> getRecords() {
        return records;
    }
}
