package com.betsoft.casino.mp.transport;

import com.betsoft.casino.utils.TInboundObject;

public class CloseRoundResultNotification extends TInboundObject {

    private long notificationId;

    public CloseRoundResultNotification(long date, int rid, long notificationId) {
        super(date, rid);
        this.notificationId = notificationId;
    }

    public long getNotificationId() {
        return notificationId;
    }

    @Override
    public String toString() {
        return "CloseRoundResultNotification[" +
                "notificationId=" + notificationId +
                ", date=" + date +
                ", rid=" + rid +
                ']';
    }
}
