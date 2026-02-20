package com.betsoft.casino.mp.model;

import java.util.Collection;

/**
 * User: flsh
 * Date: 21.05.2020.
 */
public interface IRoomTemplateService<T extends IRoomTemplate> {
    T put(T template);

    void remove(Long id);

    Collection<T> getAll();

    T get(Long id);

    T getForBankOrDefault(long bankId, GameType gameType, MoneyType moneyType, boolean battlegroundMode);

    Collection<T> getForBankOrDefault(Long bankId, MoneyType moneyType);

    T getMostSuitable(Long bankId, Money stake, MoneyType moneyType, GameType gameType);

    Collection<T> getDefault(MoneyType moneyType);
}
