package com.abs.casino.common.cache.data.account;

import com.abs.casino.common.cache.Identifiable;
import com.abs.casino.common.cache.data.currency.ICurrency;

/**
 * User: flsh
 * Date: 14.04.17.
 */
public interface IAccountInfo extends Identifiable {
    public static final String BIRTH_DATE = "BIRTH_DATE";
    public static final String GENDER = "GENDER";

    long getId();

    //is getBankId() alias, used for resolve conflict between SB (long bankId) and GP3 (int bankId)
    long getSystemId();

    Long getAccountUseId();

    long getBalance();

    String getEmail();

    String getExternalId();

    long getFreeBalance();

    boolean isGuest();

    boolean isLocked();

    String getNickName();

    String getPassword();

    long getRegisterTime();

    String getSessionKey();

    void setSessionKey(String sessionKey);

    String getFirstName();

    String getLastName();

    ICurrency getCurrency();
}
