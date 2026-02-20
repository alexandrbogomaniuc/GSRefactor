package com.betsoft.casino.mp.web.socket;

import org.springframework.web.reactive.socket.WebSocketSession;

/**
 * User: flsh
 * Date: 18.10.18.
 */
public interface ConnectionClosedNotifier {
    void notify(WebSocketSession session);
}
