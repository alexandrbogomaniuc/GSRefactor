package com.betsoft.casino.mp.model;

import com.betsoft.casino.utils.ITransportObject;

/**
 * User: flsh
 * Date: 19.05.2020.
 */
public interface IRoundResultNotification extends ITransportObject {
    long getGameId();

    IAward getAward();

    long getNotificationId();

    void setNotificationId(long notificationId);
}
