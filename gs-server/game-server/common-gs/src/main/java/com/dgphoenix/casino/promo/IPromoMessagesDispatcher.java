package com.dgphoenix.casino.promo;

import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.promo.PromoNotificationType;
import com.dgphoenix.casino.common.promo.Status;
import com.abs.casino.websocket.IWebSocketSessionsListener;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Created by vladislav on 12/7/16.
 */
public interface IPromoMessagesDispatcher extends IWebSocketSessionsListener {
    void sendPromoNotifications(String sessionId, long campaignId, Set<PromoNotificationType> notificationsTypes);

    void sendPromoNotificationsAsync(String sessionId, long campaignId, Set<PromoNotificationType> notificationsTypes);

    void markPrizesAsNotifiedAbout(String sessionId, Map<Long, Collection<Long>> prizesIdsByCampaigns)
            throws CommonException;

    void notifyPromoSessionIsOpen(String sessionId, Set<Long> activeCampaigns) throws CommonException;

    void notifyPromoCampaignStatusChanged(long campaignId, Status newStatus);

    void notifyPromoCampaignCreated(long promoCampaignId);
}
