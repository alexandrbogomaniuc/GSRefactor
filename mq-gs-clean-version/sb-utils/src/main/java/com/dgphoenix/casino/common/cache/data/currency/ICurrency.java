package com.dgphoenix.casino.common.cache.data.currency;

/**
 * User: Grien
 * Date: 14.08.2014 18:45
 */
public interface ICurrency {
    String getCode();

    void setCode(String code);

    String getSymbol();

    void setSymbol(String symbol);

    boolean isDefault(long bankId);
}
