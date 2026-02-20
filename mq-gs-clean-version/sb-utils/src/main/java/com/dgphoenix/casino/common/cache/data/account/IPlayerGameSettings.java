package com.dgphoenix.casino.common.cache.data.account;

import com.dgphoenix.casino.common.cache.data.bank.ICoin;
import com.dgphoenix.casino.common.cache.data.bank.ILimit;

import java.util.List;

/**
 * User: Grien
 * Date: 17.08.2014 18:26
 */
public interface IPlayerGameSettings {
    List<? extends ICoin> getCoins();

    ILimit getLimit();

    Integer getDefCoin();
}
