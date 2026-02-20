package com.betsoft.casino.mp.web.socket;

import com.betsoft.casino.bots.ISender;
import com.betsoft.casino.bots.LocalRoomBot;
import com.betsoft.casino.mp.web.GameSocketClient;
import com.betsoft.casino.mp.web.RequestStatistic;
import com.betsoft.casino.mp.web.handlers.IMessageHandler;
import com.betsoft.casino.utils.ITransportObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.FluxSink;

import java.util.Collection;

public class BotGameClient extends GameSocketClient implements ISender {
    private static final Logger LOG = LogManager.getLogger(BotGameClient.class);
    private final BotGameWebSocketHandler handler;
    private final LocalRoomBot bot;

    public BotGameClient(BotGameWebSocketHandler handler, LocalRoomBot bot) {
        super(null, null, null, null, null, null, null);
        this.handler = handler;
        this.bot = bot;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void send(ITransportObject message) {
        IMessageHandler handle = handler.getHandler(message.getClass());
        if (handle != null) {
            handle.handle(null, message, this);
        } else {
            LOG.error("Handler not defined for {}", message.getClass());
        }
    }

    @Override
    public WebSocketSession getSession() {
        return null;
    }

    @Override
    public void addRequestStatistic(Class requestClass, long lastRequestId, long lastServerInputDate, long lastClientSendDate) {
        
    }

    @Override
    public RequestStatistic getRequestStatistic(Class requestClass) {
        return null;
    }

    @Override
    public Collection<RequestStatistic> getRequestStatistics() {
        return null;
    }

    @Override
    public FluxSink<WebSocketMessage> sendMessage(ITransportObject message) {
        bot.processMessage(message);
        return null;
    }

    @Override
    public FluxSink<WebSocketMessage> sendMessage(WebSocketMessage message) {
        LOG.warn("Message ignored by bot: {}", message.getPayloadAsText());
        return null;
    }

    @Override
    public boolean isBot() {
        return true;
    }
}
