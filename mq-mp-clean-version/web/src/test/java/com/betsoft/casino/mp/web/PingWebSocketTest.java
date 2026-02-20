package com.betsoft.casino.mp.web;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.StandardWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ReplayProcessor;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * User: flsh
 * Date: 12.08.17.
 */

@Ignore
public class PingWebSocketTest {
    private static final Logger LOG = LogManager.getLogger(PingWebSocketTest.class);

/*    @Test
    public void testWebsocketMessage() {
        DataBufferFactory bufferFactory = new DefaultDataBufferFactory();
        //NettyDataBufferFactory bufferFactory = new NettyDataBufferFactory(PooledByteBufAllocator.DEFAULT);
        DataBuffer buffer = bufferFactory.wrap("Qu-Qu".getBytes());
        WebSocketMessage msg =  new WebSocketMessageWrapper(WebSocketMessage.Type.TEXT, buffer);
        System.out.println("msg=" + msg.getPayloadAsText());
        System.out.println("msg=" + msg);

        System.out.println("msg=" + msg.getPayloadAsText());
    }*/

    @Test
    public void ping() throws Exception {
        String path = "/websocket/echo";
        //String path = "/websocket/ping";
        StandardWebSocketClient client = new StandardWebSocketClient();
/*        client.execute(getUrl(path),
                session -> session
                        .send( Flux.just(String.valueOf("ZZZZZZ")).map(session::textMessage)));
*/
        for (int i = 0; i<5; i++) {
            //Flux<String> testMsg = Flux.just(String.valueOf(i));
            final int ii = i;
            Flux<String> testMsg = Flux.range(1, 4).map(index -> "msg-" + index + "_" + ii);
            ReplayProcessor<String> output = ReplayProcessor.create();
            WebSocketHandler handler = new WebSocketHandler() {
                @Override
                public Mono<Void> handle(WebSocketSession session) {
                    return session.send(testMsg.map(session::textMessage))
                            .thenMany(session.receive().take(Duration.ofSeconds(2)).map(WebSocketMessage::getPayloadAsText))
                            .subscribeWith(output)
                            .then();
                }
            };
            client.execute(getUrl(path), handler).block(Duration.ofMillis(1000));
/*
            client.execute(getUrl(path),
                    session -> session
                            .send(testMsg.map(session::textMessage))
                            .thenMany(session.receive().take(2).map(WebSocketMessage::getPayloadAsText))
                            .subscribeWith(output)
                            .then())
                    .block(Duration.ofMillis(1000));
*/
            List<String> outputList = output.collectList().block(Duration.ofMillis(5000));
            //debug("outputList: " + outputList);
        }
    }

    @Test
    public void justSend() throws Exception {

        //String path = "/websocket/echo";
        String path = "/websocket/tick";
        StandardWebSocketClient client = new StandardWebSocketClient();
        //client.execute(getUrl(path), session -> session.send(Flux.just(session.textMessage("qq1")))).block(Duration.ofMillis(1000));
        ReplayProcessor<Object> output = ReplayProcessor.create(50);
        //Flux<Long> messageFlux = Flux.interval(Duration.ofSeconds(1));
        client.execute(getUrl(path), session -> {
            Mono<Void> receiveResult = Mono.empty();
            for(int i = 0; i < 100; i++) {
                //debug("WebSocketHandler: before send");
                //Mono<Void> result = session.send(messageFlux.map(l -> String.format("{ \"value\": %d }", l)).map(session::textMessage));

                Mono<Void> result = session.send(Flux.just(session.textMessage("qq1"), session.textMessage("qq2"),
                        session.textMessage("qq3")));//.delayElement(Duration.ofMillis(2000));

                receiveResult = result.thenMany(session.receive().take(3).map(WebSocketMessage::getPayloadAsText)).
                        subscribeWith(output).then();
/*
            Mono<Void> receiveResult = result.thenMany(session.receive().take(3 ).map(WebSocketMessage::getPayloadAsText)).
                    subscribeWith(output).then();
*/
                //debug("WebSocketHandler: receiveResult = " + receiveResult + ", session=" + session);
            }
            return receiveResult;
        }).block();
        //debug("justSend:  after execute" );
        List<Object> outputList = output.collectList().block();
        debug("outputList: " + outputList);
        //debug("justSend:  completed" );
    }

    //@Test
    public void ping_old() throws Exception {
        int count = 4;
        debug("ping");
        Flux<String> input = Flux.range(1, count).map(index -> "msg-" + index);
        ReplayProcessor<Object> output = ReplayProcessor.create(count);
        WebSocketClient client = new StandardWebSocketClient();
        String path = "/websocket/ping";
        //String path = "/web-mp-casino/websocket/ping";
        client.execute(getUrl(path),
                session -> session
                        .send(input.map(session::textMessage))
                        .thenMany(session.receive().take(count).map(WebSocketMessage::getPayloadAsText))
                        .subscribeWith(output)
                        .then())
                .block(Duration.ofMillis(5000));

        List<String> inputList = input.collectList().block(Duration.ofMillis(5000));
        List<Object> outputList = output.collectList().block(Duration.ofMillis(5000));
        debug("inputList: " + inputList);
        System.out.println("inputList: " + inputList);
        debug("outputList: " + outputList);
        System.out.println("outputList: " + outputList);
        assertEquals(inputList, outputList);
    }

    protected URI getUrl(String path) throws URISyntaxException {
        return new URI("ws://localhost:8080" + path);
    }

    private void debug(String s) {
        LOG.debug(s);
        System.out.println(s);
    }
}
