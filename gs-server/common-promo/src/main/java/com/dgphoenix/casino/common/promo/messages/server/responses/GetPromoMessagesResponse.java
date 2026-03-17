package com.abs.casino.common.promo.messages.server.responses;

import com.abs.casino.common.promo.messages.server.notifications.PromoNotification;
import com.abs.casino.common.util.string.StringUtils;
import com.abs.casino.gs.singlegames.tools.cbservtools.response.ServerMessage;
import com.abs.casino.gs.singlegames.tools.cbservtools.response.ServerResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * @deprecated It is the old version of {@link com.abs.casino.common.promo.messages.server.responses.GetPromoNotificationsResponse}
 * Left only for backward capability. Should be removed in near future.
 */
public class GetPromoMessagesResponse extends ServerResponse {
    private static final String MESSAGES = "MESSAGES";

    private final List<PromoNotification> notifications;

    public GetPromoMessagesResponse(List<PromoNotification> notifications) {
        this.notifications = notifications;
    }

    @Override
    public String httpFormat() {
        StringBuilder responseBuilder = new StringBuilder();
        responseBuilder.append(MESSAGES).append(ServerMessage.VALUE_DELIMITER);

        List<String> messages = new ArrayList<String>(notifications.size());
        for (PromoNotification notification : notifications) {
            messages.add(notification.oldHttpFormat());
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
