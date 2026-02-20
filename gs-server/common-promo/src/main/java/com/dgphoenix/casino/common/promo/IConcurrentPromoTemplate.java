package com.dgphoenix.casino.common.promo;

/**
 * Created by vladislav on 3/29/17.
 */
public interface IConcurrentPromoTemplate<T extends IPrize, IPT extends IPromoTemplate> extends IPromoTemplate<T, IPT> {
    boolean processQualifiedConcurrentPrize(long prizeId, DesiredPrize desiredPrize);
}
