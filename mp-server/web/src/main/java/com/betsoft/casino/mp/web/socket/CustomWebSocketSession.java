package com.betsoft.casino.mp.web.socket;

import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.web.reactive.socket.HandshakeInfo;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.adapter.ReactorNettyWebSocketSession;
import reactor.core.publisher.Flux;
import reactor.ipc.netty.http.websocket.WebsocketInbound;
import reactor.ipc.netty.http.websocket.WebsocketOutbound;

public class CustomWebSocketSession extends ReactorNettyWebSocketSession {
    public CustomWebSocketSession(WebsocketInbound inbound, WebsocketOutbound outbound, HandshakeInfo info, NettyDataBufferFactory bufferFactory) {
        super(inbound, outbound, info, bufferFactory);
    }

    @Override
    public Flux<WebSocketMessage> receive() {
        return getDelegate().getInbound()
                .aggregateFrames(2 * DEFAULT_FRAME_MAX_SIZE)
                .receiveFrames()
                .map(this::toMessage);
    }
}
