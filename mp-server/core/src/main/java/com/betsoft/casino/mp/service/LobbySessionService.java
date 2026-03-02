package com.betsoft.casino.mp.service;

import com.betsoft.casino.mp.model.ILobbySession;
import com.betsoft.casino.mp.model.LobbySession;
import com.betsoft.casino.mp.web.ILobbySocketClient;
import com.betsoft.casino.mp.web.ISocketClient;
import com.abs.casino.common.exception.CommonException;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.query.EntryObject;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.PredicateBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.CloseStatus;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * User: flsh
 * Date: 08.05.18.
 */
@Service
public class LobbySessionService implements ILobbySessionService<LobbySession> {
    private static final Logger LOG = LogManager.getLogger(LobbySessionService.class);
    public static final String LOBBY_SESSION_STORE = "lobbySessionStore";
    private HazelcastInstance hazelcast;
    private IMap<String, LobbySession> sessions;
    //socketClients need for restore transient LobbySession.socketClient
    private ConcurrentHashMap<String, ISocketClient> socketClients = new ConcurrentHashMap<>(512);
    protected final transient Set<ILobbyConnectionClosedListener> lobbyConnectionClosedListener = new HashSet<>(1);

    public LobbySessionService() {
    }

    public LobbySessionService(HazelcastInstance hazelcast) {
        this.hazelcast = hazelcast;
    }

    @PostConstruct
    private void init() {
        LOG.info("init: start");
        sessions = hazelcast.getMap(LOBBY_SESSION_STORE);
        sessions.addIndex("accountId", false);
        LOG.info("init: completed");
    }

    @PreDestroy
    public void shutdown() {
        LOG.info("shutdown");
    }

    @Override
    public void registerCloseLobbyConnectionListener(ILobbyConnectionClosedListener listener) {
        lobbyConnectionClosedListener.add(listener);
    }

    @Override
    public void processCloseLobbyConnection(ILobbySocketClient client) throws CommonException {
        for (ILobbyConnectionClosedListener listener : lobbyConnectionClosedListener) {
            listener.notifyLobbyConnectionClosed(client);
        }
    }

    @Override
    public LobbySession add(LobbySession session) {
        LOG.debug("add: {}", session);
        ISocketClient socketClient = session.getSocketClient();
        if (socketClient != null) {
            socketClients.put(session.getSessionId(), socketClient);
        }
        return sessions.put(session.getSessionId(), session);
    }

    @Override
    public Collection<LobbySession> getByAccountId(long accountId) {
        EntryObject object = new PredicateBuilder().getEntryObject();
        final Predicate predicate = object.get("accountId").equal(accountId);
        return sessions.values(predicate);
    }

    @Override
    public LobbySession get(String sessionId) {
        LobbySession session = sessions.get(sessionId);
        setClient(session);
        return session;
    }

    @Override
    public LobbySession get(long accountId) {
        EntryObject object = new PredicateBuilder().getEntryObject();
        final Predicate predicate = object.get("accountId").equal(accountId);
        Collection<LobbySession> result = sessions.values(predicate);
        if (!result.isEmpty()) {
            LobbySession session = result.iterator().next();
            setClient(session);
            return session;
        }
        return null;
    }

    @Override
    public void remove(String sessionId) {
        LOG.debug("remove: {}", sessionId);
        socketClients.remove(sessionId);
        sessions.delete(sessionId);
    }

    @Override
    public boolean closeConnection(String sessionId) {
        ISocketClient socketClient = socketClients.get(sessionId);
        if (socketClient != null && socketClient.getSession() != null) {
            LOG.debug("closeConnection: {}", sessionId);
            socketClient.getConnection().complete();
            socketClient.getSession().close(CloseStatus.GOING_AWAY).block(Duration.ofSeconds(5L));
            return true;
        }
        return false;
    }

    @Override
    public RoundCompletedNotifyTask createRoundCompletedNotifyTask(String sid, long roomId, long accountId, long balance,
                                                   long kills, long treasures, int rounds, long xp, long xpPrev,
                                                   long xpNext, int level) {
        return new RoundCompletedNotifyTask(sid, roomId, accountId, balance, kills, treasures, rounds, xp, xpPrev,
                xpNext, level);
    }

    public Collection<LobbySession> getAllLobbySessions() {
        return Collections.unmodifiableCollection(sessions.values());
    }

    private void setClient(ILobbySession session) {
        if (session != null) {
            ISocketClient socketClient = socketClients.get(session.getSessionId());
            if (socketClient != null) {
                session.setSocketClient(socketClient);
            }
        }
    }
}
