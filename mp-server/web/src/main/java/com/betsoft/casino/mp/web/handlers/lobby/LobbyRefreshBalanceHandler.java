package com.betsoft.casino.mp.web.handlers.lobby;

import com.betsoft.casino.mp.model.LobbySession;
import com.betsoft.casino.mp.service.LobbySessionService;
import com.betsoft.casino.mp.transport.BalanceUpdated;
import com.betsoft.casino.mp.transport.RefreshBalance;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.mp.web.handlers.MessageHandler;
import com.betsoft.casino.mp.web.service.LobbyManager;
import com.betsoft.casino.mp.web.service.SocketService;
import com.betsoft.casino.mp.web.ILobbySocketClient;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * User: flsh
 * Date: 10.08.18.
 */
@Component
public class LobbyRefreshBalanceHandler extends MessageHandler<RefreshBalance, ILobbySocketClient> {
    private static final Logger LOG = LogManager.getLogger(LobbyRefreshBalanceHandler.class);
    private SocketService socketService;

    public LobbyRefreshBalanceHandler(Gson gson, LobbySessionService lobbySessionService, LobbyManager lobbyManager,
                                      SocketService socketService) {
        super(gson, lobbySessionService, lobbyManager);
        this.socketService = socketService;
    }

    @Override
    public void handle(WebSocketSession session, RefreshBalance message, ILobbySocketClient client) {
        long now = System.currentTimeMillis();
        LobbySession tmpSession = lobbySessionService.get(client.getSessionId());
        if (tmpSession == null) {
            LOG.warn("lobbySession is null, client={}", client);
        } else if (tmpSession.getActiveCashBonusSession() != null || tmpSession.getTournamentSession() != null) {
            client.sendMessage(new BalanceUpdated(getCurrentTime(), message.getRid(),
                    tmpSession.getBalance(), 0), message);
        } else if (client.getGameType().isCrashGame()) {
            client.sendMessage(new BalanceUpdated(getCurrentTime(), message.getRid(), tmpSession.getBalance(), 0), message);
            StatisticsManager.getInstance().updateRequestStatistics("LobbyRefreshBalanceHandler: fast for crash",
                    System.currentTimeMillis() - now, client.getSessionId() + ":" + client.getRid());
        } else {
            getBalance(client)
                    .doOnSuccess(balance -> {
                        LOG.debug("Success update balance={}", balance);
                        LobbySession lobbySession = lobbySessionService.get(client.getSessionId());
                        if (lobbySession == null) {
                            LOG.warn("lobbySession is null, client={}", client);
                        } else {
                            lobbySession.setBalance(balance);
                            lobbySessionService.add(lobbySession);
                        }
                        client.sendMessage(new BalanceUpdated(getCurrentTime(), message.getRid(), balance, 0), message);
                    })
                    .doOnError((Throwable error) -> {
                        LOG.error("Failed to RefreshBalance, client{}", client, error);
                        sendErrorMessage(client, ErrorCodes.INTERNAL_ERROR, "RefreshBalance failed: " + error.getMessage(),
                                message.getRid());
                    })
                    .subscribeOn(Schedulers.elastic())
                    .subscribe()
            ;
        }
    }

    protected Mono<Long> getBalance(ILobbySocketClient client) {
        return socketService.getBalance(client.getServerId(), client.getSessionId(),
                client.getMoneyType() == null ? null : client.getMoneyType().name());
    }


    @Override
    public Logger getLog() {
        return LOG;
    }
}
