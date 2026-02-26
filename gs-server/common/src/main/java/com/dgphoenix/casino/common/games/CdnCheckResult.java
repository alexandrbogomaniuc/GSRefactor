package com.abs.casino.common.games;

public class CdnCheckResult {

    public static final int MAX_LOAD_TIME = 10000;

    private String cdnUrl;
    private int loadTime;
    private long lastUpdateTime;

    public CdnCheckResult(String cdnUrl, int loadTime, long lastUpdateTime) {
        this.cdnUrl = cdnUrl;
        this.loadTime = loadTime;
        this.lastUpdateTime = lastUpdateTime;
    }

    public String getCdnUrl() {
        return cdnUrl;
    }

    public int getLoadTime() {
        return loadTime;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }
}
