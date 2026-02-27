package com.abs.casino.common.promo.ai;

public interface IPromoFeedPersister {

    void persist(long promoId, long time, String feed);
    String get(long promoId, long time);
}
