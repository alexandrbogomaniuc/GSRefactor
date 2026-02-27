package com.abs.casino.common.promo.messages.server.notifications.prizes;

import com.dgphoenix.casino.common.promo.messages.server.notifications.PromoNotification;

/**
 * Created by vladislav on 1/24/17.
 */
public abstract class PrizeWonNotification extends PromoNotification {
    public static final String PRIZE_WON_NOTIFICATION_PREFIX = "prize";

    public void setIdentifiers(long prizeId, long promoId) {
        String id = composeId(PRIZE_WON_NOTIFICATION_PREFIX, promoId, prizeId);
        super.setId(id);
        super.setPromoId(promoId);
    }
}
