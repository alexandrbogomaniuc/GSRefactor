package com.betsoft.casino.mp.service;

import com.betsoft.casino.mp.model.IRoundResultNotification;

public interface INotificationService {
    void addRoundResultNotification(long accountId, long gameId, IRoundResultNotification notification);
}
