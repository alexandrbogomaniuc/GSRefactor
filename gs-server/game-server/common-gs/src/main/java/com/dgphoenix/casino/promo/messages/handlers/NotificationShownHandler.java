package com.dgphoenix.casino.promo.messages.handlers;

import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.promo.messages.client.requests.NotificationShown;
import com.dgphoenix.casino.common.promo.messages.server.notifications.prizes.PrizeWonNotification;
import com.dgphoenix.casino.common.promo.messages.server.responses.NotificationShownResponse;
import com.dgphoenix.casino.gs.GameServerComponentsHelper;
import com.dgphoenix.casino.promo.IPromoMessagesDispatcher;
import com.dgphoenix.casino.websocket.IWebSocketSessionsController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * @deprecated It is the old version of {@link NotificationsShownProcessor}
 * Left only for backward capability. Should be removed in near future.
 */
public class NotificationShownHandler extends AbstractMessageHandler<NotificationShown> {
    private static final Logger LOG = LogManager.getLogger(NotificationShownHandler.class);

    public NotificationShownHandler() {
        super(NotificationShown.class);
    }

    @Override
    protected void processMessage(IWebSocketSessionsController webSocketSessionsController, String sessionId,
                                  NotificationShown notificationShown) throws CommonException {
        String notificationId = notificationShown.getNotificationId();
        String[] keyArgs = notificationId.split("_");
        String type = keyArgs[0];

        if (PrizeWonNotification.PRIZE_WON_NOTIFICATION_PREFIX.equals(type)) {
            long campaignId = Long.valueOf(keyArgs[1]);
            long prizeId = Long.valueOf(keyArgs[2]);
            IPromoMessagesDispatcher messagesDispatcher = GameServerComponentsHelper
                    .getPromoMessagesDispatcher();
            Map<Long, Collection<Long>> prizesByCampaigns = new HashMap<>();
            prizesByCampaigns.put(campaignId, Collections.singleton(prizeId));
            messagesDispatcher.markPrizesAsNotifiedAbout(sessionId, prizesByCampaigns);

            NotificationShownResponse notificationShownResponse = new NotificationShownResponse();
            notificationShownResponse.setRequestId(notificationShown.getId());
            webSocketSessionsController.sendMessage(sessionId, notificationShownResponse);
        } else {
            LOG.warn("Unknown notification type, type = {}", type);
        }
    }
}
