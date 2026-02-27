package com.abs.casino.promo.messages.handlers;

import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.promo.messages.client.requests.ClientRequest;
import com.abs.casino.websocket.IWebSocketSessionsController;
import com.google.gson.Gson;

/**
 * Created by vladislav on 2/14/17.
 */
public abstract class AbstractMessageHandler<T extends ClientRequest> implements IMessageHandler<T> {
    private final Gson gson = new Gson();
    private final Class<T> classToken;

    protected AbstractMessageHandler(Class<T> classToken) {
        this.classToken = classToken;
    }

    @Override
    public void handleMessage(IWebSocketSessionsController webSocketSessionsController, String sessionId, String message)
            throws CommonException {
        T actualMessage = gson.fromJson(message, classToken);
        processMessage(webSocketSessionsController, sessionId, actualMessage);
    }

    protected abstract void processMessage(IWebSocketSessionsController webSocketSessionsController, String sessionId,
                                           T message) throws CommonException;

    @Override
    public String getMessageType() {
        return classToken.getSimpleName();
    }
}
