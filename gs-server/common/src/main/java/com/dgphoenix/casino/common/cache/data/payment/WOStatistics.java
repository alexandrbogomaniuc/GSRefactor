package com.abs.casino.common.cache.data.payment;

import com.dgphoenix.casino.common.cache.IDistributedCacheEntry;

import java.util.Map;

public class WOStatistics implements IDistributedCacheEntry {

    private static final long serialVersionUID = 8869145956368226780L;
    private long date = 0;
    private long totalNumDebitTransactions = 0;
    private long totalValueDebitTransactions = 0;
    private long totalNumCreditTransactions = 0;
    private long totalValueCreditTransactions = 0;


    public WOStatistics() {
        super();
    }

    public WOStatistics(
            long date,
            long totalNumDebitTransactions,
            long totalValueDebitTransactions,
            long totalNumCreditTransactions,
            long totalValueCreditTransactions,
            Map<String, String> properties) {
        super();
        this.date = date;
        this.totalNumDebitTransactions = totalNumDebitTransactions;
        this.totalValueDebitTransactions = totalValueDebitTransactions;
        this.totalNumCreditTransactions = totalNumCreditTransactions;
        this.totalValueCreditTransactions = totalValueCreditTransactions;
    }


    //light copy constructor
    public WOStatistics(
            long date,
            long totalNumDebitTransactions,
            long totalValueDebitTransactions,
            long totalNumCreditTransactions,
            long totalValueCreditTransactions) {
        super();
        this.date = date;
        this.totalNumDebitTransactions = totalNumDebitTransactions;
        this.totalValueDebitTransactions = totalValueDebitTransactions;
        this.totalNumCreditTransactions = totalNumCreditTransactions;
        this.totalValueCreditTransactions = totalValueCreditTransactions;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public long getTotalNumDebitTransactions() {
        return totalNumDebitTransactions;
    }

    public void setTotalNumDebitTransactions(long totalNumDebitTransactions) {
        this.totalNumDebitTransactions = totalNumDebitTransactions;
    }

    public void addTotalNumDebitTransactions(long totalNumDebitTransactions) {
        this.totalNumDebitTransactions += totalNumDebitTransactions;
    }

    public long getTotalValueDebitTransactions() {
        return totalValueDebitTransactions;
    }

    public void setTotalValueDebitTransactions(long totalValueDebitTransactions) {
        this.totalValueDebitTransactions = totalValueDebitTransactions;
    }

    public void addTotalValueDebitTransactions(long totalValueDebitTransactions) {
        this.totalValueDebitTransactions += totalValueDebitTransactions;
    }

    public long getTotalNumCreditTransactions() {
        return totalNumCreditTransactions;
    }

    public void setTotalNumCreditTransactions(long totalNumCreditTransactions) {
        this.totalNumCreditTransactions = totalNumCreditTransactions;
    }

    public void addTotalNumCreditTransactions(long totalNumCreditTransactions) {
        this.totalNumCreditTransactions += totalNumCreditTransactions;
    }

    public long getTotalValueCreditTransactions() {
        return totalValueCreditTransactions;
    }

    public void setTotalValueCreditTransactions(long totalValueCreditTransactions) {
        this.totalValueCreditTransactions = totalValueCreditTransactions;
    }

    public void addTotalValueCreditTransactions(long totalValueCreditTransactions) {
        this.totalValueCreditTransactions += totalValueCreditTransactions;
    }

    @Override
    public String toString() {
        return "WOStatistics [date=" + date
                + ", totalNumDebitTransactions=" + totalNumDebitTransactions
                + ", totalValueDebitTransactions="
                + totalValueDebitTransactions + ", totalNumCreditTransactions="
                + totalNumCreditTransactions
                + ", totalValueCreditTransactions="
                + totalValueCreditTransactions + "]";
    }


}
