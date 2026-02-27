package com.abs.casino.common.mp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TicketedDrawFeed implements Serializable {
    private long id;
    private String cluster;
    private boolean finished;
    private List<TicketedDrawFeedEntry> players;

    public TicketedDrawFeed(long id, String cluster, boolean finished) {
        this.id = id;
        this.cluster = cluster;
        this.finished = finished;
        this.players = new ArrayList<TicketedDrawFeedEntry>();
    }

    public TicketedDrawFeed(long id, String cluster, boolean finished, List<TicketedDrawFeedEntry> players) {
        this.id = id;
        this.cluster = cluster;
        this.finished = finished;
        this.players = players;
    }

    public void addPlayer(long bankId, String bankName, long accountId, long tickets, String nickname) {
        players.add(new TicketedDrawFeedEntry(bankId, bankName, accountId, tickets, nickname));
    }

    public long getId() {
        return id;
    }

    public String getCluster() {
        return cluster;
    }

    public boolean isFinished() {
        return finished;
    }

    public List<TicketedDrawFeedEntry> getPlayers() {
        return players;
    }

    @Override
    public String toString() {
        return "TicketedDrawFeed{" +
                "id=" + id +
                ", cluster='" + cluster + '\'' +
                ", finished=" + finished +
                ", players=" + players +
                '}';
    }
}
