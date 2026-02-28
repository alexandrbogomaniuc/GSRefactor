package com.abs.casino.kafka.dto;

public class CurrencyRateDto {
    private String sourceCurrency;
    private String destinationCurrency;
    private double rate;
    private long updateDate;

    public CurrencyRateDto() {}

    public CurrencyRateDto(String sourceCurrency,
            String destinationCurrency,
            double rate,
            long updateDate) {
        this.sourceCurrency = sourceCurrency;
        this.destinationCurrency = destinationCurrency;
        this.rate = rate;
        this.updateDate = updateDate;
    }

    public String getSourceCurrency() {
        return sourceCurrency;
    }

    public String getDestinationCurrency() {
        return destinationCurrency;
    }

    public double getRate() {
        return rate;
    }

    public long getUpdateDate() {
        return updateDate;
    }

    public void setSourceCurrency(String sourceCurrency) {
        this.sourceCurrency = sourceCurrency;
    }

    public void setDestinationCurrency(String destinationCurrency) {
        this.destinationCurrency = destinationCurrency;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public void setUpdateDate(long updateDate) {
        this.updateDate = updateDate;
    }
}
