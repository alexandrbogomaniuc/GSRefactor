package com.betsoft.casino.mp.common.testmodel;

import com.betsoft.casino.mp.model.ICurrency;

public class StubCurrency implements ICurrency {
    private String code;
    private String symbol;

    public StubCurrency() {
    }

    public StubCurrency(String code, String symbol) {
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
