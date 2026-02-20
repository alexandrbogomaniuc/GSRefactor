package com.betsoft.casino.mp.service;

import com.betsoft.casino.mp.model.GameType;
import com.betsoft.casino.mp.model.room.IRoom;
import com.hazelcast.spring.context.SpringAware;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import java.io.Serializable;
import java.util.concurrent.Callable;

@SpringAware
public class ObserversCollectTask implements Callable<Integer>, Serializable, ApplicationContextAware {
    private final long roomId;
    private final int gameId;

    private transient ApplicationContext context;
    private static final Logger LOG = LogManager.getLogger(ObserversCollectTask.class);

    public ObserversCollectTask(long roomId, int gameId) {
        this.roomId = roomId;
        this.gameId = gameId;
    }

    public Integer call() {
        int numberObservers = 0;
        if (context != null) {
            LOG.debug("ObserversCollectTask call: {} ", toString());
            try {
                IRoom room;
                GameType byGameId = GameType.getByGameId(gameId);
                if(byGameId != null){
                    IRoomServiceFactory roomServiceFactory = (IRoomServiceFactory) context.getBean("roomServiceFactory");
                    room = roomServiceFactory.getRoomWithoutCreationById(roomId);
                    if (room != null) {
                        numberObservers = room.getObserverCount();
                        LOG.debug("ObserversCollectTask real numberObservers: {} ", numberObservers);
                    }
                }
            } catch (Exception e) {
                LOG.debug("error: " + e.getMessage());
            }
        } else {
            LOG.debug("ObserversCollectTask ApplicationContext not found roomId: {} ", roomId);
        }
        LOG.debug("ObserversCollectTask roomId: {}. gameId: {}, numberObservers: {}", roomId, gameId, numberObservers);
        return numberObservers;
    }


    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.context = context;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ObserversCollectTask [");
        sb.append("roomId=").append(roomId);
        sb.append(", gameId=").append(gameId);
        sb.append(']');
        return sb.toString();
    }
}
