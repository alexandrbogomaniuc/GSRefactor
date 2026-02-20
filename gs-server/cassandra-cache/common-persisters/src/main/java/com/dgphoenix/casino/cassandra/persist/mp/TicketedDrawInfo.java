package com.dgphoenix.casino.cassandra.persist.mp;

public class TicketedDrawInfo {
    private long id;
    private long startDate;
    private long endDate;
    private long updateDate;

    public TicketedDrawInfo(long id, long startDate, long endDate, long updateDate) {
        this.id = id;
        this.startDate = startDate;
        this.endDate = endDate;
        this.updateDate = updateDate;
    }

    public long getId() {
        return id;
    }

    public long getStartDate() {
        return startDate;
    }

    public long getEndDate() {
        return endDate;
    }

    public long getUpdateDate() {
        return updateDate;
    }
}
