package com.abs.casino.cache;

import com.dgphoenix.casino.common.cache.IDistributedCacheEntry;

public class GameList implements IDistributedCacheEntry {
    private long bankId;
    private String version;
    private String showTestingGame;
    private Boolean isHttpsRequest;
    private long lastAccess;
    private long lastUpdate;
    private String feed;

    public GameList(long bankId, String version, String showTestingGame, Boolean isHttpsRequest, long lastAccess,
                    long lastUpdate, String feed) {
        this.bankId = bankId;
        this.version = version;
        this.showTestingGame = showTestingGame;
        this.isHttpsRequest = isHttpsRequest;
        this.lastAccess = lastAccess;
        this.lastUpdate = lastUpdate;
        this.feed = feed;
    }

    public long getBankId() {
        return bankId;
    }

    public void setBankId(long bankId) {
        this.bankId = bankId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getFeed() {
        return feed;
    }

    public void setFeed(String feed) {
        this.feed = feed;
    }

    public void setLastAccess(long lastAccess) {
        this.lastAccess = lastAccess;
    }

    @Override
    public String toString() {
        return "GameList{" +
                "bankId=" + bankId +
                ", version='" + version + '\'' +
                ", showTestingGame='" + showTestingGame + '\'' +
                ", isHttpsRequest=" + isHttpsRequest +
                ", feedSize='" + feed.length() + '\'' +
                '}';
    }
}
