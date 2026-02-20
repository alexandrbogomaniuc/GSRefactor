package com.dgphoenix.casino.common.promo;

import com.dgphoenix.casino.common.exception.CommonException;

import java.util.Map;
import java.util.Set;

/**
 * User: flsh
 * Date: 5.12.2020.
 */
public interface INetworkPromoCampaign<E extends INetworkPromoEvent> {
    E getCurrentEvent();

    Set<E> getEvents();

    Set<E> addEvent(E event) throws CommonException;

    boolean isSingleClusterPromo();

    Map<String, LocalizationTitles> getLocalizationTitles();

    LocalizationTitles getLocalizationTitle(String lang);

    void addLocalizationTitle(String lang, LocalizationTitles title);

    long getTotalPrizePool();
}
