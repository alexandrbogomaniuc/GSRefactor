package com.betsoft.casino.bots;

import com.betsoft.casino.mp.web.IConfigurableWebSocketHandler;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.StandardWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ReplayProcessor;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.List;

/**
 * User: flsh
 * Date: 01.12.2022.
 */
@Ignore
public class LongMessageTest {
    private static final Logger LOG = LogManager.getLogger(LongMessageTest.class);
    private static final String TEST_URL = "wss://gs1-mp-beta.discreetgaming.com/websocket/echo";

    @Test
    public void sendLongMessage() throws Exception {
        sendMessageToEcho(new ReactorNettyWebSocketClient(), 100000);
    }

    private void sendMessageToEcho(WebSocketClient client, int length) throws Exception {
        for (int i = 0; i < 5; i++) {
            Flux<String> testMsg = Flux.range(1, 1).map(index -> getLongMessage(length));
            ReplayProcessor<String> output = ReplayProcessor.create();
            WebSocketHandler handler = new IConfigurableWebSocketHandler() {
                @Override
                public Mono<Void> handle(WebSocketSession session) {
                    try {
                        changeDefaultConfig(session, false, LOG);
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        LOG.error("Reflective Operation Exception", e);
                        e.printStackTrace();
                    }

                    return session.send(testMsg.map(session::textMessage))
                            .thenMany(session.receive().take(Duration.ofMillis(1000)).map(WebSocketMessage::getPayloadAsText))
                            .subscribeWith(output)
                            .then();
                }
            };
            client.execute(getUrl(), handler).block();
            debug("outputList: " + output.collectList().block());
        }
    }

    protected URI getUrl() throws URISyntaxException {
        return new URI(TEST_URL);
    }

    private String getLongMessage(int length) {
        return RandomStringUtils.random(length, true, true);
    }

    private void debug(String s) {
        //LOG.debug(s);
        System.out.println(s);
    }
}
