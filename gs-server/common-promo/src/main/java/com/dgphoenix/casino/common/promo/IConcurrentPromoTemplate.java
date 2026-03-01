package com.abs.casino.common.promo;

import com.dgphoenix.casino.common.promo.DesiredPrize;
import com.dgphoenix.casino.common.promo.IPrize;
import com.dgphoenix.casino.common.promo.IPromoTemplate;

/**
 * Created by vladislav on 3/29/17.
 */
public interface IConcurrentPromoTemplate<T extends IPrize, IPT extends IPromoTemplate> extends IPromoTemplate<T, IPT> {
    boolean processQualifiedConcurrentPrize(long prizeId, DesiredPrize desiredPrize);
}
