package com.betsoft.casino.mp.web.socket;

import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.http.server.reactive.AbstractServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.web.reactive.socket.HandshakeInfo;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.upgrade.ReactorNettyRequestUpgradeStrategy;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.ipc.netty.http.server.HttpServerResponse;

import java.security.Principal;

public class CustomRequestUpgradeStrategy extends ReactorNettyRequestUpgradeStrategy {
    @Override
    public Mono<Void> upgrade(ServerWebExchange exchange, WebSocketHandler handler, String subProtocol) {
        ServerHttpResponse response = exchange.getResponse();
        HttpServerResponse nativeResponse = ((AbstractServerHttpResponse) response).getNativeResponse();
        HandshakeInfo info = getHandshakeInformation(exchange, subProtocol);
        NettyDataBufferFactory bufferFactory = (NettyDataBufferFactory) response.bufferFactory();

        return nativeResponse.sendWebsocket(subProtocol,
                (in, out) -> handler.handle(new CustomWebSocketSession(in, out, info, bufferFactory)));
    }

    private HandshakeInfo getHandshakeInformation(ServerWebExchange exchange, @Nullable String protocol) {
        ServerHttpRequest request = exchange.getRequest();
        Mono<Principal> principal = exchange.getPrincipal();
        return new HandshakeInfo(request.getURI(), request.getHeaders(), principal, protocol);
    }
}
