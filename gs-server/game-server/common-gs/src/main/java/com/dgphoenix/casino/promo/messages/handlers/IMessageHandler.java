package com.abs.casino.promo.messages.handlers;

import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.promo.messages.client.requests.ClientRequest;
import com.dgphoenix.casino.websocket.IWebSocketSessionsController;

/**
 * Created by vladislav on 12/20/16.
 */
public interface IMessageHandler<T extends ClientRequest> {
    void handleMessage(IWebSocketSessionsController webSocketSessionsController, String sessionId, String message)
            throws CommonException;

    String getMessageType();
}
