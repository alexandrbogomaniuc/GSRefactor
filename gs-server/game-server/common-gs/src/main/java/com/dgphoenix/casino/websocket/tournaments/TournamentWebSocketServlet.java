package com.abs.casino.websocket.tournaments;

import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

public class TournamentWebSocketServlet extends WebSocketServlet {

    @Override
    public void configure(WebSocketServletFactory webSocketServletFactory) {
        webSocketServletFactory.register(TournamentWebSocket.class);
    }
}
