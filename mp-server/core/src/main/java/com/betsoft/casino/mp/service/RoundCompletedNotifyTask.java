package com.betsoft.casino.mp.service;

import com.betsoft.casino.mp.model.LobbySession;
import com.betsoft.casino.mp.transport.BalanceUpdated;
import com.betsoft.casino.mp.transport.Stats;
import com.betsoft.casino.mp.web.ISocketClient;
import com.hazelcast.spring.context.SpringAware;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.io.Serializable;
import java.util.Collection;

/**
 * User: flsh
 * Date: 28.08.18.
 * This task required for notify lobby
 */
@SpringAware
public class RoundCompletedNotifyTask implements Runnable, Serializable, ApplicationContextAware {
    private static final Logger LOG = LogManager.getLogger(RoundCompletedNotifyTask.class);
    private String sid;
    private long roomId;
    private long accountId;
    private long balance;
    private long kills;
    private long treasures;
    private int rounds;
    private long xp;
    private long xpPrev;
    private long xpNext;
    private int level;
    private transient ApplicationContext context;

    public RoundCompletedNotifyTask(String sid, long roomId, long accountId, long balance,
                                    long kills, long treasures, int rounds, long xp, long xpPrev, long xpNext,
                                    int level) {
        this.sid = sid;
        this.roomId = roomId;
        this.accountId = accountId;
        this.balance = balance;
        this.kills = kills;
        this.treasures = treasures;
        this.rounds = rounds;
        this.xp = xp;
        this.xpPrev = xpPrev;
        this.xpNext = xpNext;
        this.level = level;
    }

    @Override
    public void run() {
        LOG.debug("run: " + toString());
        LobbySessionService lobbySessionService = context.getBean("lobbySessionService", LobbySessionService.class);
        LobbySession session = lobbySessionService.get(sid);
        sendMessages(session);
        Collection<LobbySession> lobbySessions = lobbySessionService.getByAccountId(accountId);
        for (LobbySession lobbySession : lobbySessions) {
            sendMessages(lobbySession);
        }
    }

    private void sendMessages(LobbySession session) {
        ISocketClient socketClient = session == null ? null : session.getSocketClient();
        if (socketClient != null) {
            socketClient.sendMessage(new BalanceUpdated(System.currentTimeMillis(), balance, 0));
            socketClient.sendMessage(new Stats(System.currentTimeMillis(), kills, treasures, rounds, xp,
                    xpPrev, xpNext, level));
            socketClient.setLastUpdatedBalance(balance);
        } else {
            LOG.debug("run: LobbySession not found for sid={}", sid);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.context = context;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RoundCompletedNotifyTask [");
        sb.append("roomId=").append(roomId);
        sb.append(", sid=").append(sid);
        sb.append(", accountId=").append(accountId);
        sb.append(", balance=").append(balance);
        sb.append(']');
        return sb.toString();
    }
}
