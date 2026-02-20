package com.betsoft.casino.mp.web.socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.*;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.function.BiFunction;

/**
 * User: flsh
 * Date: 25.10.17.
 */
@Component
public class TickWebSocketHandler implements WebSocketHandler {
    private static final Logger LOG = LogManager.getLogger(TickWebSocketHandler.class);
    private Flux<Long> messageFlux;
    private Flux<String> messageFlux2;

    /**
     * Here we prepare a Flux that will emit a message every second
     */
    @PostConstruct
    private void init() {
        LOG.debug("init: PostConstruct");
        messageFlux = Flux.interval(Duration.ofSeconds(1));
    }


    @Override
    public Mono<Void> handle(final WebSocketSession session) {
        LOG.debug("handle: " + session + ", messageFlux=" + messageFlux2 + ", this=" + this);
/*        Flux<WebSocketMessage> receiveFlux = session.receive();
        receiveFlux.subscribe(msg -> {
            LOG.debug("msg=" + msg);
        }, error -> {
            LOG.debug("error=" + error);
        }, () -> LOG.debug("subscribe completed"), subscription -> {LOG.debug("subscription=" + subscription);});*/

        Flux<WebSocketMessage> receiveFlux = session.receive().doOnNext(message -> {
            String text = "Message from client '" + session.getId() + "': " + message.getPayloadAsText();
            LOG.info(text);
        });

        if(messageFlux2 == null) {
            messageFlux2 = Flux.generate(() -> 0, (BiFunction<Integer, SynchronousSink<String>, Integer>) (index, sink) -> {
                String s = "" + session.getId() + "=" + index;
                sink.next(s);
                LOG.debug("handle:apply " + session + ", s=" + s );
                if(index > 10) {
                    sink.complete();
                }
                return ++index;
            }).delayElements(Duration.ofMillis(200));
        }
        //return session.send(messageFlux2.map(l -> String.format("{ \"value\": %s }", l)).map(session::textMessage));
        Flux<WebSocketMessage> senderFlux = messageFlux2.map(l -> String.format("{ \"value\": %s }", l)).map(session::textMessage);
        return session.send(Flux.merge(senderFlux, receiveFlux));
    }

    /**
     * On each new client session, send the message flux to the client.
     * Spring subscribes to the flux and send every new flux event to the WebSocketSession object
     * @param session
     * @return Mono<Void>
     */
    //@Override
    public Mono<Void> handle1(WebSocketSession session) {
        LOG.debug("handle: " + session + ", messageFlux=" + messageFlux + ", this=" + this);
        if(messageFlux == null) {
            messageFlux = Flux.interval(Duration.ofSeconds(1));
        }
        return session.send(
                messageFlux
                        .map(l -> String.format("{ \"value\": %d }", l)) //transform to json
                        .map(session::textMessage)); // map to Spring WebSocketMessage of type text
    }

}
