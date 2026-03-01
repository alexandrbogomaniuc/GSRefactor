package com.abs.casino.common.promo;

import com.abs.casino.common.promo.PromoNotificationType;
import java.util.Set;

/**
 * User: flsh
 * Date: 28.12.16.
 */
public interface IRemotePromoNotifier {
    void sendPromoNotifications(long accountId, String sessionId, long campaignId,
                                Set<PromoNotificationType> notificationsTypes, int serverId);
}
