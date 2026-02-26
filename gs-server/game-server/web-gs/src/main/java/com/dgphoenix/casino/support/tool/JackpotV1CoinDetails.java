package com.abs.casino.support.tool;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonInclude
public class JackpotV1CoinDetails {
    private Long coinValue;
    @JsonSerialize(using = DoubleContextualSerializer.class)
    @Precision(precision = 2)
    private Double pcr;
    @JsonSerialize(using = DoubleContextualSerializer.class)
    @Precision(precision = 2)
    private Double bcr;
    @JsonSerialize(using = DoubleContextualSerializer.class)
    @Precision(precision = 2)
    private Double totalJackpot;

    public JackpotV1CoinDetails(Long coinValue, Double pcr, Double bcr, Double totalJackpot) {
        this.coinValue = coinValue;
        this.pcr = pcr;
        this.bcr = bcr;
        this.totalJackpot = totalJackpot;
    }

    public Long getCoinValue() {
        return coinValue;
    }

    public void setCoinValue(Long coinValue) {
        this.coinValue = coinValue;
    }

    public Double getPcr() {
        return pcr;
    }

    public void setPcr(Double pcr) {
        this.pcr = pcr;
    }

    public Double getBcr() {
        return bcr;
    }

    public void setBcr(Double bcr) {
        this.bcr = bcr;
    }

    public Double getTotalJackpot() {
        return totalJackpot;
    }

    public void setTotalJackpot(Double totalJackpot) {
        this.totalJackpot = totalJackpot;
    }
}
