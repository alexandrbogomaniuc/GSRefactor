package com.dgphoenix.casino.common.promo;

import com.dgphoenix.casino.common.cache.Identifiable;
import com.abs.casino.common.util.DatePeriod;
import com.esotericsoftware.kryo.KryoSerializable;

import java.util.Map;
import java.util.Set;

/**
 * User: flsh
 * Date: 16.11.16.
 */
public interface IPromoCampaign extends Identifiable, KryoSerializable {
    IPromoTemplate getTemplate();

    String getName();

    EnterType getEnterType();

    DatePeriod getActionPeriod();

    Status getStatus();

    void setStatus(Status status);

    Set<Long> getBankIds();

    Set<IPrize> getPrizePool();

    IPrize getPrize(long prizeId);

    Set<Long> getGameIds();

    boolean isActual(long gameId);

    boolean showNotifications(long gameId);

    String getBaseCurrency();

    //key is bankId
    Map<Long, String> getPromoDetailURLs();

    String getPromoDetailURL(Long bankId);

    PlayerIdentificationType getPlayerIdentificationType();

    void setPlayerIdentificationType(PlayerIdentificationType playerIdentificationType);

    boolean isNetworkPromoCampaign();
}
