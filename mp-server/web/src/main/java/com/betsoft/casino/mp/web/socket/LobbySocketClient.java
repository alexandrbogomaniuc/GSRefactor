package com.betsoft.casino.mp.web.socket;

import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.service.ILobbySessionService;
import com.betsoft.casino.mp.service.ISocketService;
import com.betsoft.casino.mp.transport.BalanceUpdated;
import com.betsoft.casino.mp.transport.Error;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.mp.web.AbstractWebSocketClient;
import com.betsoft.casino.mp.web.ILobbySocketClient;
import com.betsoft.casino.mp.web.IMessageSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

public class LobbySocketClient extends AbstractWebSocketClient implements ILobbySocketClient {
    private static final Logger LOG = LogManager.getLogger(LobbySocketClient.class);
    private IPlayerInfo playerInfo;
    private String nickname;

    private boolean loggedIn;
    private int serverId;
    private String sessionId;
    private String lang = "en";

    private static final Duration BALANCE_UPDATE_PERIOD = Duration.ofSeconds(10);
    private static final Duration TOUCH_SESSION_PERIOD = Duration.ofMinutes(2);
    private Disposable touchSession;
    private Disposable touchRequest;
    private Disposable balanceUpdater;
    private GameType gameType;
    private MoneyType moneyType;
    private Long lastUpdatedBalance;
    /** is true for private rooms */
    private boolean privateRoom;
    private WebSocketSession session;

    public LobbySocketClient(WebSocketSession session, String webSocketSessionId, FluxSink<WebSocketMessage> connection, IMessageSerializer serializer) {
        super(webSocketSessionId, connection, serializer);
        this.session = session;
    }

    @Override
    public WebSocketSession getSession() {
        return session;
    }

    @Override
    public void setLastUpdatedBalance(Long lastUpdatedBalance) {
        this.lastUpdatedBalance = lastUpdatedBalance;
    }

    @Override
    public void startBalanceUpdater(ISocketService socketService, int serverId, String sessionId,
                                    ILobbySessionService lobbySessionService) {
        LOG.debug("startBalanceUpdater: serverId={}, sessionId={}", serverId, sessionId);
        balanceUpdater = Flux
                .interval(BALANCE_UPDATE_PERIOD)
                .subscribe(i -> {
                            ILobbySession session = lobbySessionService.get(sessionId);
                            if (session != null && (session.getActiveCashBonusSession() != null ||
                                    session.getTournamentSession() != null)) {
                                long balance = session.getBalance();
                                sendMessage(new BalanceUpdated(System.currentTimeMillis(), balance, 0));
                                lastUpdatedBalance = balance;
                            } else if(session != null) {
                                String mode = session.getMoneyType().name();
                                socketService.getBalance(serverId, sessionId, mode)
                                        .doOnSuccess(balance -> {
                                            LOG.debug("Success update balance, nickName={}, sid={}, balance={}",
                                                    nickname, sessionId, balance);
                                            ILobbySession lobbySession = lobbySessionService.get(sessionId);
                                            if(lobbySession == null) {
                                                LOG.warn("Cannot lobbySession, sessionId={}", sessionId);
                                            } else if (lastUpdatedBalance == null || !lastUpdatedBalance.equals(balance)
                                                    || lobbySession.getBalance() != balance) {
                                                lobbySession.setBalance(balance);
                                                lobbySessionService.add(lobbySession);
                                                sendMessage(new BalanceUpdated(System.currentTimeMillis(), balance, 0));
                                                lastUpdatedBalance = balance;
                                            }
                                        })
                                        .doOnError(error -> {
                                            LOG.error("Failed to update balance, nickName={}, sid={}", nickname, sessionId, error);
                                        })
                                        .subscribeOn(Schedulers.elastic())
                                        .subscribe();
                            }
                        }
                );
    }

    @Override
    public void stopBalanceUpdater() {
        if (balanceUpdater != null) {
            try {
                balanceUpdater.dispose();
            } catch (Exception e) {
                LOG.error("stopBalanceUpdater: balanceUpdater", e);
                throw e;
            }
        }
    }

    @Override
    public void startTouchSession(ISocketService socketService, int serverId, String sessionId) {
        touchSession = Flux
                .interval(TOUCH_SESSION_PERIOD)
                .subscribe(i -> touchRequest = socketService
                        .touchSession(sessionId)
                        .doOnSuccess(sessionExist -> {
                            if (!sessionExist) {
                                LOG.error("Failed to touch session: {}, session expired, stop touch", sessionId);
                                stopTouchSession();
                                sendMessage(new Error(ErrorCodes.INVALID_SESSION,
                                        "Session not found", System.currentTimeMillis()));
                            }
                        })
                        .doOnError(this::handleSessionError)
                        .subscribe());
    }

    @Override
    public void stopTouchSession() {
        Exception exception = null;

        if (touchRequest != null) {
            try {
                touchRequest.dispose();
            } catch (Exception e) {
                LOG.error("stopTouchSession: touchRequest", e);
                exception = e;
            }
        }

        if (touchSession != null) {
            try {
                touchSession.dispose();
            } catch (Exception e) {
                LOG.error("stopTouchSession: touchSession", e);
                if (exception == null) {
                    exception = e;
                }
            }
        }

        if (exception != null) {
            throw new RuntimeException("Exception occurred during stopTouchSession", exception);
        }
    }

    private void handleSessionError(Throwable e) {
        LOG.error("Failed to touch session", e);
    }

    @Override
    public IPlayerInfo getPlayerInfo() {
        return playerInfo;
    }

    @Override
    public void setPlayerInfo(IPlayerInfo playerInfo) {
        this.playerInfo = playerInfo;
    }

    @Override
    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    @Override
    public boolean isLoggedIn() {
        return loggedIn;
    }

    @Override
    public Long getAccountId() {
        return playerInfo == null ? null : playerInfo.getAccountId();
    }

    @Override
    public Long getBankId() {
        return playerInfo == null ? null : playerInfo.getBankId();
    }

    @Override
    public int getRid() {
        return -1;
    }

    @Override
    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    @Override
    public boolean isPrivateRoom() {
        return this.privateRoom;
    }

    @Override
    public void setPrivateRoom(boolean privateRoom) {
        this.privateRoom = privateRoom;
    }

    @Override
    public int getServerId() {
        return serverId;
    }

    @Override
    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    @Override
    public String getSessionId() {
        return sessionId;
    }

    @Override
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public GameType getGameType() {
        return gameType;
    }

    @Override
    public void setGameType(GameType gameType) {
        this.gameType = gameType;
    }

    @Override
    public MoneyType getMoneyType() {
        return moneyType;
    }

    @Override
    public void setMoneyType(MoneyType moneyType) {
        this.moneyType = moneyType;
    }

    @Override
    public String getLang() {
        return lang;
    }

    @Override
    public void setLang(String lang) {
        this.lang = lang;
    }

    @Override
    public Logger getLog() {
        return LOG;
    }

    @Override
    public boolean isBot() {
        return false;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LobbySocketClient [");
        sb.append("playerInfo=").append(playerInfo);
        sb.append(", nickname='").append(nickname).append('\'');
        sb.append(", loggedIn=").append(loggedIn);
        sb.append(", serverId=").append(serverId);
        sb.append(", gameType=").append(gameType);
        sb.append(", moneyType=").append(moneyType);
        sb.append(", privateRoom=").append(privateRoom);
        sb.append(", sessionId='").append(sessionId).append('\'');
        sb.append(", lang=").append(lang);
        sb.append(']');
        return sb.toString();
    }
}
