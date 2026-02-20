package com.dgphoenix.casino.common.promo;

public interface IPromoCountryRestrictionService {
    boolean isCountryAllowed(String ip, long promoId);
}
