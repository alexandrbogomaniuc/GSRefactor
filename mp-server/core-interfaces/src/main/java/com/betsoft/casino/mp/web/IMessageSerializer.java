package com.betsoft.casino.mp.web;

import com.betsoft.casino.utils.ITransportObject;
import com.betsoft.casino.utils.TObject;
import org.springframework.web.reactive.socket.WebSocketMessage;

/**
 * User: flsh
 * Date: 03.11.17.
 */
public interface IMessageSerializer {
    TObject deserialize(WebSocketMessage message);

    WebSocketMessage serialize(ITransportObject obj);
}
