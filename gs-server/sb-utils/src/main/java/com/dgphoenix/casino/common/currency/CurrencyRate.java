package com.abs.casino.common.currency;

/**
 * User: flsh
 * Date: 19.03.13
 */
public class CurrencyRate {
    private String sourceCurrency;
    private String destinationCurrency;
    private double rate;
    private long updateDate;

    public CurrencyRate() {
    }

    public CurrencyRate(String sourceCurrency, String destinationCurrency, double rate, long updateDate) {
        this.sourceCurrency = sourceCurrency;
        this.destinationCurrency = destinationCurrency;
        this.rate = rate;
        this.updateDate = updateDate;
    }

    public String getSourceCurrency() {
        return sourceCurrency;
    }

    public void setSourceCurrency(String sourceCurrency) {
        this.sourceCurrency = sourceCurrency;
    }

    public String getDestinationCurrency() {
        return destinationCurrency;
    }

    public void setDestinationCurrency(String destinationCurrency) {
        this.destinationCurrency = destinationCurrency;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public long getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(long updateDate) {
        this.updateDate = updateDate;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CurrencyRate)) return false;

        final CurrencyRate currencyRate = (CurrencyRate) o;

        if (destinationCurrency != null
                ? !destinationCurrency.equals(currencyRate.destinationCurrency)
                : currencyRate.destinationCurrency != null)
            return false;
        if (sourceCurrency != null
                ? !sourceCurrency.equals(currencyRate.sourceCurrency)
                : currencyRate.sourceCurrency != null)
            return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = (sourceCurrency != null ? sourceCurrency.hashCode() : 0);
        result = 29 * result + (destinationCurrency != null ? destinationCurrency.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("CurrencyRate");
        sb.append("[sourceCurrency='").append(sourceCurrency).append('\'');
        sb.append(", destinationCurrency='").append(destinationCurrency).append('\'');
        sb.append(", rate=").append(rate);
        sb.append(", updateDate=").append(updateDate);
        sb.append(']');
        return sb.toString();
    }
}
