package com.abs.casino.common.promo.messages.server.responses;

import com.dgphoenix.casino.common.promo.messages.server.notifications.PromoNotification;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.gs.singlegames.tools.cbservtools.response.ServerMessage;
import com.dgphoenix.casino.gs.singlegames.tools.cbservtools.response.ServerResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vladislav on 12/13/16.
 */
public class GetPromoNotificationsResponse extends ServerResponse {
    private static final String NOTIFICATIONS = "NOTIFICATIONS";

    private final List<PromoNotification> notifications;

    public GetPromoNotificationsResponse(List<PromoNotification> notifications) {
        this.notifications = notifications;
    }

    public List<PromoNotification> getNotifications() {
        return notifications;
    }

    @Override
    public String httpFormat() {
        StringBuilder responseBuilder = new StringBuilder();
        responseBuilder.append(NOTIFICATIONS).append(ServerMessage.VALUE_DELIMITER);

        List<String> messages = new ArrayList<String>(notifications.size());
        for (PromoNotification notification : notifications) {
            messages.add(notification.httpFormat());
        }
        String serverMessagesAsString = ServerMessage.ELEMENTS_JOINER.join(messages);
        responseBuilder.append(StringUtils.encodeUriComponent(serverMessagesAsString));

        return responseBuilder.toString();
    }

    @Override
    public String toString() {
        return "GetPromoMessagesResponse{" +
                super.toString() +
                "notifications=" + notifications +
                '}';
    }
}
