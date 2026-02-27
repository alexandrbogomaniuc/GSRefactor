package com.abs.casino.promo.exception;

public class UnsupportedCurrencyException extends RuntimeException {
    private final String currency;

    public UnsupportedCurrencyException(String currency) {

        super("Currency not found: " + currency);
        this.currency = currency;
    }

    public String getCurrency() {
        return currency;
    }
}
