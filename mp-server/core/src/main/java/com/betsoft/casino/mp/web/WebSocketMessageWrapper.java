package com.betsoft.casino.mp.web;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.web.reactive.socket.WebSocketMessage;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * User: flsh
 * Date: 23.11.17.
 */
public class WebSocketMessageWrapper extends WebSocketMessage {
    private String message;

    public WebSocketMessageWrapper(Type type, DataBuffer payload) {
        super(type, payload);
        ByteBuffer byteBuffer = payload.asByteBuffer();
        message = new String(byteBuffer.array(), StandardCharsets.UTF_8);
    }

    public WebSocketMessageWrapper(String message, DataBuffer payload) {
        super(WebSocketMessage.Type.TEXT, payload);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return message;
    }
}
