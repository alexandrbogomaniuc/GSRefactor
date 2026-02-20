package com.betsoft.casino.mp.web.service;

import com.betsoft.casino.mp.model.LobbySession;
import com.betsoft.casino.mp.service.ILobbySessionService;
import com.betsoft.casino.mp.service.LobbySessionService;
import com.hazelcast.spring.context.SpringAware;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.io.Serializable;
import java.util.Collection;

@SpringAware
public class CloseConnectionTask implements Runnable, Serializable, ApplicationContextAware {

    private static final Logger LOG = LogManager.getLogger(CloseConnectionTask.class);

    private final long accountId;
    private final String currentSessionId;

    private transient ApplicationContext context;

    public CloseConnectionTask(long accountId, String currentSessionId) {
        this.accountId = accountId;
        this.currentSessionId = currentSessionId;
    }

    @Override
    public void run() {
        LOG.debug("run: {}", this);
        ILobbySessionService lobbySessionService = context.getBean("lobbySessionService", LobbySessionService.class);
        Collection<LobbySession> playerLobbySessions = lobbySessionService.getByAccountId(accountId);
        for (LobbySession lobbySession : playerLobbySessions) {
            if (!lobbySession.getSessionId().equals(currentSessionId)) {

                boolean closeConnectionResult = lobbySessionService.closeConnection(lobbySession.getSessionId());
                LOG.debug("close connection sid: {} result: {}", lobbySession.getSessionId(), closeConnectionResult);
            }
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.context = context;
    }

    @Override
    public String toString() {
        return "CloseConnectionTask{" +
                "accountId=" + accountId +
                ", currentSessionId='" + currentSessionId + '\'' +
                ", context=" + context +
                '}';
    }
}
