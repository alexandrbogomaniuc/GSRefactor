package com.dgphoenix.casino.support.tool;

import java.util.List;

public class JackpotV1Details {
    private Double pcrp;
    private Double bcrp;
    private List<JackpotV1CoinDetails> coins;

    public Double getPcrp() {
        return pcrp;
    }

    public void setPcrp(Double pcrp) {
        this.pcrp = pcrp;
    }

    public Double getBcrp() {
        return bcrp;
    }

    public void setBcrp(Double bcrp) {
        this.bcrp = bcrp;
    }

    public List<JackpotV1CoinDetails> getCoins() {
        return coins;
    }

    public void setCoins(List<JackpotV1CoinDetails> coins) {
        this.coins = coins;
    }
}
