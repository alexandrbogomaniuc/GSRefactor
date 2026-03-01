package com.abs.casino.common.promo;

import com.abs.casino.common.promo.IPrize;
import com.abs.casino.common.promo.IPromoTemplate;

/**
 * User: flsh
 * Date: 10.12.2020.
 */
public interface INetworkPromoEventTemplate<P extends IPrize, IPT extends INetworkPromoEventTemplate> extends IPromoTemplate<P, IPT> {
    long getBuyInPrice();

    long getBuyInAmount();

    long getPrize();

    boolean isReBuyEnabled();

    long getReBuyPrice();

    long getReBuyAmount();

    int getReBuyLimit();

    long getCutOffTime();

    long getIconId();

    boolean isResetBalance();
}
