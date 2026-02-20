package com.betsoft.casino.mp.model;

import java.util.Currency;

/**
 * User: flsh
 * Date: 19.05.2020.
 */
public interface IPlayerInfo <CURRENCY extends ICurrency, PLAYER_STATS extends IPlayerStats>{
    Long getBankId();

    void setBankId(Long bankId);

    Long getAccountId();

    void setAccountId(Long accountId);

    String getExternalId();

    void setExternalId(String externalId);

    String getUserName();

    void setUserName(String userName);

    CURRENCY getCurrency() ;

    void setCurrency(CURRENCY currency);

    PLAYER_STATS getStats();

    void setStats(PLAYER_STATS stats);

    boolean isGuest();

    void setGuest(boolean guest);

    boolean isShowRefreshBalanceButton();

    void setShowRefreshBalanceButton(boolean showRefreshBalanceButton);
}
