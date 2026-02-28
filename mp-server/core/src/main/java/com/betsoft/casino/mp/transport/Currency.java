package com.betsoft.casino.mp.transport;

import com.abs.casino.common.cache.data.currency.ICurrency;

import java.io.Serializable;

public class Currency implements ICurrency, Serializable {
    private String code;
    private String symbol;

    public Currency() {
    }

    public Currency(String code, String symbol) {
        this.code = code;
        this.symbol = symbol;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String getSymbol() {
        return symbol;
    }

    @Override
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    @Override
    public boolean isDefault(long bankId) {
        return false;
    }

    @Override
    public String toString() {
        return "Currency{" +
                "code='" + code + '\'' +
                ", symbol='" + symbol + '\'' +
                '}';
    }
}
