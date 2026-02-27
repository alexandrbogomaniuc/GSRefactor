package com.abs.casino.gs.biz;

import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.io.Serializable;
import java.util.List;

/**
 * User: flsh
 * Date: 25.08.2009
 */
public class GameHistory implements Serializable {
    @XStreamImplicit
    private List<GameHistoryListEntry> entries;

    public GameHistory() {
    }

    public GameHistory(List<GameHistoryListEntry> entries) {
        this.entries = entries;
    }

    public List<GameHistoryListEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<GameHistoryListEntry> entries) {
        this.entries = entries;
    }
}
