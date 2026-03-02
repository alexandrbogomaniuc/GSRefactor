package com.betsoft.casino.mp.web.service;

import com.betsoft.casino.mp.common.AbstractGameRoom;
import com.betsoft.casino.mp.data.persister.TournamentSessionPersister;
import com.betsoft.casino.mp.model.IRoomPlayerInfo;
import com.betsoft.casino.mp.model.ITournamentSession;
import com.betsoft.casino.mp.model.LobbySession;
import com.betsoft.casino.mp.model.TournamentSession;
import com.betsoft.casino.mp.model.room.IRoom;
import com.betsoft.casino.mp.service.ITransportObjectsFactoryService;
import com.betsoft.casino.mp.service.LobbySessionService;
import com.betsoft.casino.mp.service.RoomPlayerInfoService;
import com.betsoft.casino.mp.web.ISocketClient;
import com.abs.casino.cassandra.CassandraPersistenceManager;
import com.hazelcast.spring.context.SpringAware;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.io.Serializable;
import java.util.List;

/**
 * User: flsh
 * Date: 18.08.2020.
 */
@SpringAware
public class ChangeTournamentStateTask implements Runnable, Serializable, ApplicationContextAware {
    private final long tournamentId;
    private final String state;
    private final String oldState;

    private transient ApplicationContext context;
    private static final Logger LOG = LogManager.getLogger(ChangeTournamentStateTask.class);

    public ChangeTournamentStateTask(long tournamentId, String state, String oldState) {
        this.tournamentId = tournamentId;
        this.state = state;
        this.oldState = oldState;
    }

    public void run() {
        LOG.debug("run: tournamentId={}, state={}", tournamentId, state);
        if (context == null) {
            LOG.error("Context is not injected");
        }
        RoomPlayerInfoService playerInfoService = context.getBean("playerInfoService", RoomPlayerInfoService.class);
        CassandraPersistenceManager persistenceManager = context
                .getBean("persistenceManager", CassandraPersistenceManager.class);
        TournamentSessionPersister sessionsPersister = persistenceManager.
                getPersister(TournamentSessionPersister.class);
        RoomServiceFactory roomServiceFactory = context.getBean("roomServiceFactory", RoomServiceFactory.class);
        ITransportObjectsFactoryService toFactoryService =
                (ITransportObjectsFactoryService) context.getBean("transportObjectsFactoryService");
        LobbySessionService lobbySessionService = context.getBean("lobbySessionService", LobbySessionService.class);
        List<ITournamentSession> sessions = sessionsPersister.getByTournament(tournamentId);
        LOG.debug("found sessions: {}", sessions.size());
        for (ITournamentSession tournamentSession : sessions) {
            long accountId = tournamentSession.getAccountId();
            try {
                IRoomPlayerInfo roomPlayerInfo = playerInfoService.get(accountId);
                boolean isSeater = false;
                if (roomPlayerInfo != null) {
                    long roomId = roomPlayerInfo.getRoomId();
                    @SuppressWarnings("rawtypes")
                    IRoom room = roomServiceFactory.getRoomWithoutCreationById(roomId);
                    if (room != null) {
                        @SuppressWarnings("rawtypes")
                        AbstractGameRoom gameRoom = (AbstractGameRoom) room;
                        ITournamentSession activeTournamentSession = roomPlayerInfo.getTournamentSession();
                        LOG.debug("activeTournamentSession: {}, new status: {} ", activeTournamentSession, state);

                        if (activeTournamentSession != null && activeTournamentSession.getTournamentId() == tournamentId) {
                            TournamentSession changedSession = new TournamentSession(activeTournamentSession.getAccountId(),
                                    activeTournamentSession.getTournamentId(), activeTournamentSession.getName(),
                                    state, activeTournamentSession.getStartDate(), activeTournamentSession.getEndDate(),
                                    activeTournamentSession.getBalance(),
                                    activeTournamentSession.getBuyInPrice(), activeTournamentSession.getBuyInAmount(),
                                    activeTournamentSession.isReBuyAllowed(), activeTournamentSession.getReBuyPrice(),
                                    activeTournamentSession.getReBuyAmount(), activeTournamentSession.getReBuyCount(),
                                    activeTournamentSession.getReBuyLimit(),
                                    activeTournamentSession.isResetBalanceAfterRebuy());
                            gameRoom.changeTournamentState(changedSession);
                            isSeater = true;
                        } else {
                            LOG.debug("Tournament status changed, but seater not play this tournament");
                        }
                    } else {
                        LOG.debug("found roomPlayerInfo, but room not found on this server");
                    }
                }
                if (!isSeater) {
                    LOG.debug("Seater not found, need save new state");
                    tournamentSession.setState(state);
                    sessionsPersister.persist(tournamentSession);
                }
                LobbySession lobbySession = lobbySessionService.get(accountId);
                LOG.debug("process for accountId={}, lobbySession={}", accountId, lobbySession);
                if (lobbySession != null && lobbySession.getTournamentSession() != null &&
                        lobbySession.getTournamentSession().getTournamentId() == tournamentId) {
                    if(!isSeater) {
                        lobbySession.getTournamentSession().setState(state);
                        lobbySessionService.add(lobbySession);
                    }
                    ISocketClient lobbySocketClient = lobbySession.getSocketClient();
                    if (lobbySocketClient != null && !oldState.equalsIgnoreCase(state)) {
                        LOG.debug("found lobbySocketClient, send ChangeTournamentStateTask");
                        lobbySocketClient.sendMessage(toFactoryService.createTournamentStateChangedMessage(
                                tournamentId, oldState, state, ""));
                    }
                }
            } catch (Exception e) {
                LOG.debug("ChangeTournamentStateTask error, accountId={}", accountId, e);
            }
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.context = context;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ChangeTournamentStateTask[");
        sb.append("tournamentId=").append(tournamentId);
        sb.append(", state='").append(state).append('\'');
        sb.append(", oldState='").append(oldState).append('\'');
        sb.append(", context=").append(context);
        sb.append(']');
        return sb.toString();
    }
}
