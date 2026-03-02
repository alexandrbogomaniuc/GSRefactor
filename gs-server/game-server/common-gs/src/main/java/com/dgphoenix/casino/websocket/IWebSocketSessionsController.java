package com.abs.casino.websocket;

import com.abs.casino.common.exception.CommonException;
import com.abs.casino.gs.singlegames.tools.cbservtools.response.ServerMessage;

/**
 * Created by vladislav on 12/1/16.
 */
public interface IWebSocketSessionsController {
    void sendMessage(String sessionId, ServerMessage message);

    void openSession(SessionWrapper session, String sessionId) throws CommonException;

    void closeSession(SessionWrapper session, String sessionId, int status, String reason);

    void closeWebSocketForPlayerSession(String sessionId);

    void processMessage(String sessionId, String message) throws CommonException;

    void registerSessionsListener(IWebSocketSessionsListener sessionsListener);
}
