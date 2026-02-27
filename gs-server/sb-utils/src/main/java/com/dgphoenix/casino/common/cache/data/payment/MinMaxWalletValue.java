package com.abs.casino.common.cache.data.payment;

import com.dgphoenix.casino.common.cache.IDistributedCacheEntry;

import java.util.Date;

/**
 * User: nieky
 * Date: 02.10.13
 * Time: 18:15
 */
public class MinMaxWalletValue implements IDistributedCacheEntry {

    /**
     * date - begin of the month(in long)
     * ie: begin of the Oct 2013 - Tue Oct 01 00:00:00 NOVST 2013, in long: 1380560400000
     */
    private long date;
    private long minBankId;
    private String minExtUserId;
    private long minTransactionId;
    private long minRoundId;
    private long minGameSessionId;
    private String minExternalTransactionId;
    private long minEndTime;

    private long maxBankId;
    private String maxExtUserId;
    private long maxTransactionId;
    private long maxRoundId;
    private long maxGameSessionId;
    private String maxExternalTransactionId;
    private long maxEndTime;

    public MinMaxWalletValue() {
        super();
    }

    public MinMaxWalletValue(long date, long minBankId, String minExtUserId, long minTransactionId, long minRoundId,
                             long minGameSessionId, String minExternalTransactionId, long minEndTime, long maxBankId,
                             String maxExtUserId, long maxTransactionId, long maxRoundId, long maxGameSessionId,
                             String maxExternalTransactionId, long maxEndTime) {
        this.date = date;
        this.minBankId = minBankId;
        this.minExtUserId = minExtUserId;
        this.minTransactionId = minTransactionId;
        this.minRoundId = minRoundId;
        this.minGameSessionId = minGameSessionId;
        this.minExternalTransactionId = minExternalTransactionId;
        this.minEndTime = minEndTime;

        this.maxBankId = maxBankId;
        this.maxExtUserId = maxExtUserId;
        this.maxTransactionId = maxTransactionId;
        this.maxRoundId = maxRoundId;
        this.maxGameSessionId = maxGameSessionId;
        this.maxExternalTransactionId = maxExternalTransactionId;
        this.maxEndTime = maxEndTime;
    }

    public String getMinExtUserId() {
        return minExtUserId;
    }

    public synchronized void setMinExtUserId(String minExtUserId) {
        this.minExtUserId = minExtUserId;
    }

    public long getMinTransactionId() {
        return minTransactionId;
    }

    public void setMinTransactionId(long minTransactionId) {
        this.minTransactionId = minTransactionId;
    }

    public long getMinRoundId() {
        return minRoundId;
    }

    public void setMinRoundId(long minRoundId) {
        this.minRoundId = minRoundId;
    }

    public long getMinGameSessionId() {
        return minGameSessionId;
    }

    public void setMinGameSessionId(long minGameSessionId) {
        this.minGameSessionId = minGameSessionId;
    }

    public String getMinExternalTransactionId() {
        return minExternalTransactionId;
    }

    public void setMinExternalTransactionId(String minExternalTransactionId) {
        this.minExternalTransactionId = minExternalTransactionId;
    }

    public long getMinEndTime() {
        return minEndTime;
    }

    public void setMinEndTime(long minEndTime) {
        this.minEndTime = minEndTime;
    }

    public String getMaxExtUserId() {
        return maxExtUserId;
    }

    public void setMaxExtUserId(String maxExtUserId) {
        this.maxExtUserId = maxExtUserId;
    }

    public long getMaxTransactionId() {
        return maxTransactionId;
    }

    public void setMaxTransactionId(long maxTransactionId) {
        this.maxTransactionId = maxTransactionId;
    }

    public long getMaxRoundId() {
        return maxRoundId;
    }

    public void setMaxRoundId(long maxRoundId) {
        this.maxRoundId = maxRoundId;
    }

    public long getMaxGameSessionId() {
        return maxGameSessionId;
    }

    public void setMaxGameSessionId(long maxGameSessionId) {
        this.maxGameSessionId = maxGameSessionId;
    }

    public String getMaxExternalTransactionId() {
        return maxExternalTransactionId;
    }

    public void setMaxExternalTransactionId(String maxExternalTransactionId) {
        this.maxExternalTransactionId = maxExternalTransactionId;
    }

    public long getMaxEndTime() {
        return maxEndTime;
    }

    public void setMaxEndTime(long maxEndTime) {
        this.maxEndTime = maxEndTime;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public long getMinBankId() {
        return minBankId;
    }

    public void setMinBankId(long minBankId) {
        this.minBankId = minBankId;
    }

    public long getMaxBankId() {
        return maxBankId;
    }

    public void setMaxBankId(long maxBankId) {
        this.maxBankId = maxBankId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MinMaxWalletValue[");
        sb.append("date=").append(new Date(date));
        sb.append(", minBankId=").append(minBankId);
        sb.append(", minExtUserId='").append(minExtUserId).append('\'');
        sb.append(", minTransactionId=").append(minTransactionId);
        sb.append(", minRoundId=").append(minRoundId);
        sb.append(", minGameSessionId=").append(minGameSessionId);
        sb.append(", minExternalTransactionId='").append(minExternalTransactionId).append('\'');
        sb.append(", minEndTime=").append(new Date(minEndTime));
        sb.append(", maxBankId=").append(maxBankId);
        sb.append(", maxExtUserId='").append(maxExtUserId).append('\'');
        sb.append(", maxTransactionId=").append(maxTransactionId);
        sb.append(", maxRoundId=").append(maxRoundId);
        sb.append(", maxGameSessionId=").append(maxGameSessionId);
        sb.append(", maxExternalTransactionId='").append(maxExternalTransactionId).append('\'');
        sb.append(", maxEndTime=").append(new Date(maxEndTime));
        sb.append(']');
        return sb.toString();
    }
}
