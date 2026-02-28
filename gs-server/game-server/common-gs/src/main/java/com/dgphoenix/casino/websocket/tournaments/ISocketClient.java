package com.abs.casino.websocket.tournaments;

import com.abs.casino.common.transport.ITransportObject;
import org.eclipse.jetty.websocket.api.Session;

public interface ISocketClient extends Session {
    String getSessionId();

    void sendMessage(ITransportObject outbound);

    void connect(String sessionId, String currency, String lang, String cdn);

    boolean isConnected();

    String getCurrency();

    String getLang();

    String getCdn();
}
