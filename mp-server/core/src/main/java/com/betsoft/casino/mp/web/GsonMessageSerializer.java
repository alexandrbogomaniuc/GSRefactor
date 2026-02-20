package com.betsoft.casino.mp.web;

import com.betsoft.casino.mp.transport.Ping;
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

/**
 * User: flsh
 * Date: 03.11.17.
 */
public class GsonMessageSerializer implements IMessageSerializer {
    private static final Logger LOG = LogManager.getLogger(GsonMessageSerializer.class);
    private final Gson gson;
    private final DataBufferFactory bufferFactory;

    private static final Ping ping = new Ping();

    public GsonMessageSerializer(Gson gson) {
        this.gson = gson;
        //todo: try bufferFactory = new DefaultDataBufferFactory(true);
        this.bufferFactory = new DefaultDataBufferFactory();
    }

    @Override
    public TObject deserialize(WebSocketMessage message) {
        String text = message.getPayloadAsText();
        // TODO: remove after going away from tomcat
        // Temporary workaround for tomcat client bug:
        // Ping message payload is always passed to application as a text message
        if ("ping".equals(text)) {
            LOG.warn("Invalid ping message type");
            return ping;
        }
        TObject tObject;
        try {
            tObject = gson.fromJson(text, TObject.class);
        } catch (RuntimeException e) {
            LOG.debug("Cannot parse message: {}", text, e);
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
}
