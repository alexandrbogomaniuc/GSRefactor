package com.betsoft.casino.mp.service;

import com.betsoft.casino.mp.model.GameType;
import com.betsoft.casino.mp.model.room.IRoom;
import com.betsoft.casino.mp.web.GameSocketClient;
import com.betsoft.casino.mp.web.IGameSocketClient;
import com.hazelcast.spring.context.SpringAware;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;

@SpringAware
public class ObserverClientListCollectTask implements Callable<Collection>, Serializable, ApplicationContextAware {
    private final long roomId;
    private final int gameId;

    private transient ApplicationContext context;
    private static final Logger LOG = LogManager.getLogger(ObserverClientListCollectTask.class);

    public ObserverClientListCollectTask(long roomId, int gameId) {
        this.roomId = roomId;
        this.gameId = gameId;
    }

    public Collection call() {
        Collection<IGameSocketClient> observers = new ArrayList<>();
        if (context != null) {
            LOG.debug("ObserverClientListCollectTask call: {} ", toString());
            try {
                IRoom room;
                GameType gameType = GameType.getByGameId(gameId);

                if(gameType != null){

                    IRoomServiceFactory roomServiceFactory = (IRoomServiceFactory) context.getBean("roomServiceFactory");
                    room = roomServiceFactory.getRoomWithoutCreation(gameType, roomId);

                    if (room != null) {

                        LOG.debug("ObserverClientListCollectTask real observers: {} ", room.getObservers());

                        if(room.getObservers() != null && !room.getObservers().isEmpty()) {

                            for(Object realObserver : room.getObservers()) {

                                if(realObserver!= null && realObserver instanceof GameSocketClient) {

                                    GameSocketClient gameSocketClient = (GameSocketClient)realObserver;

                                    IGameSocketClient observer = new GameSocketClient(
                                            gameSocketClient.getSession(),
                                            gameSocketClient.getAccountId(),
                                            gameSocketClient.getBankId(),
                                            gameSocketClient.getWebSocketSessionId(),
                                            null,
                                            gameSocketClient.getSerializer(),
                                            gameSocketClient.getGameType()
                                    );

                                    observers.add(observer);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                LOG.debug("error: " + e.getMessage());
            }
        } else {
            LOG.debug("ObserverClientListCollectTask ApplicationContext not found roomId: {} ", roomId);
        }
        LOG.debug("ObserverClientListCollectTask roomId: {}. gameId: {}, observers: {}", roomId, gameId, observers);
        return observers;
    }


    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.context = context;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ObserverClientListCollectTask [");
        sb.append("roomId=").append(roomId);
        sb.append(", gameId=").append(gameId);
        sb.append(']');
        return sb.toString();
    }
}
