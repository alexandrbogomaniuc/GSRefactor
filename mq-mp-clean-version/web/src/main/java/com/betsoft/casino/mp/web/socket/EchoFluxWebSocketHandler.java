package com.betsoft.casino.mp.web.socket;

import com.betsoft.casino.mp.web.IConfigurableWebSocketHandler;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reactivestreams.Subscription;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * User: flsh
 * Date: 12.08.17.
 * see https://streamdata.io/blog/using-spring-web-flux-as-a-java-client-of-streamdata-io/
 */
@SuppressWarnings("NullableProblems")
@Component
public class EchoFluxWebSocketHandler implements IConfigurableWebSocketHandler {
    private static final Logger LOG = LogManager.getLogger(EchoFluxWebSocketHandler.class);

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        LOG.warn("handler: {}", session);
        try {
            changeDefaultConfig(session, LOG);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            LOG.error("Reflective Operation Exception", e);
        }
        Flux<WebSocketMessage> message = session.receive();
        message.subscribe(new BaseSubscriber<WebSocketMessage>() {
            @Override
            protected void hookOnSubscribe(Subscription subscription) {
                LOG.debug("hookOnSubscribe={}", subscription);
                super.hookOnSubscribe(subscription);
            }

            @Override
            protected void hookOnNext(WebSocketMessage value) {
                LOG.debug("hookOnNext={}", value.getPayloadAsText());
                super.hookOnNext(value);
            }

            @Override
            protected void hookOnComplete() {
                LOG.debug("hookOnComplete={}", session.getHandshakeInfo());
                super.hookOnComplete();
            }

            @Override
            protected void hookOnError(Throwable throwable) {
                LOG.debug("hookOnError: ", throwable);
                super.hookOnError(throwable);
            }
        });
        String sendedMessage = System.currentTimeMillis() % 2 == 0 ? "small" : RandomStringUtils.random(100000, true, true);
        return session.send(Flux.just(session.textMessage(sendedMessage)));
    }
}
