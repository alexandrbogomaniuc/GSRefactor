package com.dgphoenix.casino.common.promo.messages.client.requests;

import java.util.Arrays;

/**
 * Created by vladislav on 12/6/16.
 */
public class NotificationsShown extends ClientRequest {
    private String[] notificationsIds;

    public NotificationsShown() {
    }

    public NotificationsShown(long id, String[] notificationsIds) {
        super(id);
        this.notificationsIds = notificationsIds;
    }

    public String[] getNotificationsIds() {
        return notificationsIds;
    }

    public void setNotificationsIds(String[] notificationsIds) {
        this.notificationsIds = notificationsIds;
    }

    @Override
    public String toString() {
        return "NotificationsShown[" +
                super.toString() +
                ", notificationsIds='" + Arrays.toString(notificationsIds) + '\'' +
                ']';
    }
}
