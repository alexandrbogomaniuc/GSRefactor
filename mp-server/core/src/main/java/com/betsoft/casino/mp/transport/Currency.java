package com.betsoft.casino.mp.transport;

import com.abs.casino.common.cache.data.currency.ICurrency;
import com.betsoft.casino.mp.model.ICurrency;

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
    public String getSymbol() {
        return symbol;
    }

    @Override
    public String toString() {
        return "Currency{" +
                "code='" + code + '\'' +
                ", symbol='" + symbol + '\'' +
                '}';
    }
}
