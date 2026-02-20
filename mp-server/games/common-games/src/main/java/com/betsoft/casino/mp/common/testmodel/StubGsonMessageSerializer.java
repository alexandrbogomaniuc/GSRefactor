package com.betsoft.casino.mp.common.testmodel;

import com.betsoft.casino.mp.web.IMessageSerializer;
import com.betsoft.casino.utils.ITransportObject;
import com.betsoft.casino.utils.TObject;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.web.reactive.socket.WebSocketMessage;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class StubGsonMessageSerializer implements IMessageSerializer {
    private static final Logger LOG = LogManager.getLogger(StubGsonMessageSerializer.class);
    private final Gson gson;
    private final DataBufferFactory bufferFactory;

    public StubGsonMessageSerializer(Gson gson) {
        this.gson = gson;
        //todo: try bufferFactory = new DefaultDataBufferFactory(true);
        this.bufferFactory = new DefaultDataBufferFactory();
    }

    @Override
    public TObject deserialize(WebSocketMessage message) {
        String text = message.getPayloadAsText();
        // TODO: remove after going away from tomcat
        TObject tObject;
        try {
            tObject = gson.fromJson(text, TObject.class);
        } catch (JsonParseException e) {
            LOG.debug("Cannot parse message: " + text, e);
            throw e;
        }
        return tObject;
    }

    @Override
    public WebSocketMessageWrapper serialize(ITransportObject obj) {
        String msg = gson.toJson(obj);
        DataBuffer buffer = bufferFactory.wrap(msg.getBytes());

        return new WebSocketMessageWrapper(msg, buffer);
    }

    class WebSocketMessageWrapper extends WebSocketMessage {
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

}

