package com.dgphoenix.casino.common.promo.messages.client.requests;

/**
 * @deprecated It is the old version of {@link com.dgphoenix.casino.common.promo.messages.client.requests.NotificationsShown}
 * Left only for backward capability. Should be removed in near future.
 */
public class NotificationShown extends ClientRequest {
    private String notificationId;

    public NotificationShown() {
    }

    public NotificationShown(long id, String notificationId) {
        super(id);
        this.notificationId = notificationId;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    @Override
    public String toString() {
        return "NotificationShown[" +
                super.toString() +
                ", notificationId='" + notificationId + '\'' +
                ']';
    }
}
