package com.betsoft.casino.mp.payment;

import com.esotericsoftware.kryo.KryoSerializable;

/**
 * User: flsh
 * Date: 11.08.2022.
 */
public interface IPendingOperation extends KryoSerializable {
    long getAccountId();

    PendingOperationType getOperationType();

    String getSessionId();

    long getGameSessionId();

    long getRoomId();

    long getCreateDate();

    long getGameId();

    long getBankId();

    String getOperationId();
}
