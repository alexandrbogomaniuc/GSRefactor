package com.dgphoenix.casino.unj.api;

import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.Serializable;

/**
 * User: flsh
 * Date: 03.04.13
 */
public class ContributionResult implements Serializable {
    public static final ContributionResult EMPTY_RESULT = new ContributionResult();
    public static final ContributionResult SHUTDOWN_RESULT = new ContributionResult(true);
    private final double contributionAmount;
    private final double contributionReseedAmount;
    //bet in NJP baseCurrency
    private final double bet;
    private final double betInPlayerCurrency;
    private final Long jackpotId;
    private boolean shutdown;
    private final double summaryContributionInPlayerCurrency;
    private double summaryContributionInEUR;

    public ContributionResult() {
        bet = 0;
        betInPlayerCurrency = 0;
        contributionAmount = 0;
        contributionReseedAmount = 0;
        jackpotId = null;
        shutdown = false;
        summaryContributionInPlayerCurrency = 0;
        summaryContributionInEUR = 0;
    }

    public ContributionResult(boolean shutdown) {
        this();
        this.shutdown = shutdown;
    }

    //this constructor required only for compatibility with old unj_common.jar used on AAMS;
    // must ne removed after certification new UNJ
    public ContributionResult(double bet, double contributionAmount, double contributionReseedAmount, Long jackpotId) {
        this.bet = bet;
        this.betInPlayerCurrency = 0;
        this.contributionAmount = contributionAmount;
        this.contributionReseedAmount = contributionReseedAmount;
        this.summaryContributionInPlayerCurrency = 0;
        this.jackpotId = jackpotId;
        this.shutdown = false;
    }

    public ContributionResult(double bet, double betInPlayerCurrency, double contributionAmount, double contributionReseedAmount,
                              double summaryContributionInPlayerCurrency, Long jackpotId) {
        this.bet = bet;
        this.betInPlayerCurrency = betInPlayerCurrency;
        this.contributionAmount = contributionAmount;
        this.contributionReseedAmount = contributionReseedAmount;
        this.summaryContributionInPlayerCurrency = summaryContributionInPlayerCurrency;
        this.jackpotId = jackpotId;
        this.shutdown = false;
    }

    public ContributionResult(double bet, double betInPlayerCurrency, double contributionAmount,
                              double contributionReseedAmount, double summaryContributionInPlayerCurrency,
                              Long jackpotId, boolean shutdown) {
        this(bet, betInPlayerCurrency, contributionAmount, contributionReseedAmount, summaryContributionInPlayerCurrency, jackpotId);
        this.shutdown = shutdown;
    }

    public double getBet() {
        return bet;
    }

    public double getBetInPlayerCurrency() {
        return betInPlayerCurrency;
    }

    public double getSummaryContribution() {
        return contributionAmount + contributionReseedAmount;
    }

    public double getContributionAmount() {
        return contributionAmount;
    }

    public double getContributionReseedAmount() {
        return contributionReseedAmount;
    }

    public Long getJackpotId() {
        return jackpotId;
    }

    public boolean isShutdown() {
        return shutdown;
    }

    public double getSummaryContributionInPlayerCurrency() {
        return summaryContributionInPlayerCurrency;
    }

    public double getSummaryContributionInEUR() {
        return summaryContributionInEUR;
    }

    public void setSummaryContributionInEUR(double summaryContributionInEUR) {
        this.summaryContributionInEUR = summaryContributionInEUR;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ContributionResult that = (ContributionResult) o;

        if (Double.compare(that.contributionAmount, contributionAmount) != 0) return false;
        if (Double.compare(that.contributionReseedAmount, contributionReseedAmount) != 0) return false;
        if (Double.compare(that.bet, bet) != 0) return false;
        if (that.shutdown != shutdown) return false;
        return !(jackpotId != null ? !jackpotId.equals(that.jackpotId) : that.jackpotId != null);

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(contributionAmount);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(contributionReseedAmount);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(bet);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (jackpotId != null ? jackpotId.hashCode() : 0);
        result = 31 * result + (shutdown ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).
                append("bet", bet).
                append("betInPlayerCurrency", betInPlayerCurrency).
                append("contributionAmount", contributionAmount).
                append("contributionReseedAmount", contributionReseedAmount).
                append("summaryContributionInPlayerCurrency", summaryContributionInPlayerCurrency).
                append("summaryContributionInEUR", summaryContributionInEUR).
                append("jackpotId", jackpotId).
                append("shutdown", shutdown).
                toString();
    }
}
