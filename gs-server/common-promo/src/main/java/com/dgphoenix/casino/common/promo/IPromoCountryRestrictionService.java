package com.abs.casino.common.promo;

public interface IPromoCountryRestrictionService {
    boolean isCountryAllowed(String ip, long promoId);
}
