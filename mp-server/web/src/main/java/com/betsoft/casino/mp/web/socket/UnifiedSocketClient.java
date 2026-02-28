package com.betsoft.casino.mp.web.socket;

import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.service.ILobbySessionService;
import com.betsoft.casino.mp.service.ISocketService;
import com.betsoft.casino.mp.service.LobbySessionService;
import com.betsoft.casino.mp.transport.BalanceUpdated;
import com.betsoft.casino.mp.transport.Error;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.mp.web.AbstractWebSocketClient;
import com.betsoft.casino.mp.web.IGameSocketClient;
import com.betsoft.casino.mp.web.ILobbySocketClient;
import com.betsoft.casino.mp.web.IMessageSerializer;
import com.betsoft.casino.mp.web.service.SocketService;
import com.abs.casino.common.web.statistics.IntervalStatistics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

/**
 * User: flsh
 * Date: 21.12.2021.
 */
public class UnifiedSocketClient extends AbstractWebSocketClient implements ILobbySocketClient, IGameSocketClient {
    private static final Logger LOG = LogManager.getLogger(UnifiedSocketClient.class);

    /** info of player */
    private IPlayerInfo playerInfo;
    /** player nickname */
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
    /** current roomId of room   */
    private Long roomId;
    /** seatNumber of player, isn't used for multinode rooms */
    private int seatNumber = -1;
    private Integer lastRequestId;
    private long enterDate = -1;
    private boolean disconnected = false;
    private boolean privateRoom;
    /** true if player is owner of room, for private rooms only */
    private boolean isOwner;
    private boolean isKicked;
    private Logger logger = LOG;
    private WebSocketSession session;

    private IntervalStatistics latencyStatistic;
    private IntervalStatistics pingLatencyStatistic;

    public UnifiedSocketClient(WebSocketSession session, String webSocketSessionId, FluxSink<WebSocketMessage> connection, IMessageSerializer serializer) {
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

    public Long getLastUpdatedBalance() {
        return lastUpdatedBalance;
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

                    } else if (session != null) {

                        String mode = session.getMoneyType().name();

                        socketService.getBalance(serverId, sessionId, mode)
                                .doOnSuccess(balance -> {

                                    LOG.debug("Success update balance, nickName={}, sid={}, balance={}",
                                            nickname, sessionId, balance);

                                    ILobbySession lobbySession = lobbySessionService.get(sessionId);

                                    if (lobbySession == null) {

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
                });
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
        if (touchRequest != null) {
            try {
                touchRequest.dispose();
            } catch (Exception e) {
                LOG.error("stopTouchSession: touchRequest", e);
                throw e;
            }
        }
        if (touchSession != null) {
            try {
                touchSession.dispose();
            } catch (Exception e) {
                LOG.error("stopTouchSession: touchSession", e);
                throw e;
            }
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
    public void setAccountId(Long accountId) {
        //nop, need only for compatibility with IGameSocketClient
    }

    @Override
    public Long getBankId() {
        return playerInfo == null ? null : playerInfo.getBankId();
    }

    @Override
    public void setBankId(Long bankId) {
        //nop, need only for compatibility with IGameSocketClient
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
    public void setOwner(boolean isOwner) {
        this.isOwner = isOwner;
    }

    @Override
    public boolean isOwner() {
        return isOwner;
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
        return logger;
    }

    @Override
    public void setLog(Logger logger) {
        this.logger = logger;
    }

    @Override
    public boolean isBot() {
        return false;
    }

    @Override
    public Long getRoomId() {
        return roomId;
    }

    @Override
    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    @Override
    public int getSeatNumber() {
        return seatNumber;
    }

    @Override
    public void setSeatNumber(int seatNumber) {
        this.seatNumber = seatNumber;
    }

    @Override
    public Integer getLastRequestId() {
        return lastRequestId;
    }

    @Override
    public void setLastRequestId(Integer lastRequestId) {
        if (lastRequestId != null) {
            if (this.lastRequestId != null && this.lastRequestId > lastRequestId) {
                LOG.error("setLastRequestId: bad new requestId={}, current={}", lastRequestId, this.lastRequestId);
            } else {
                this.lastRequestId = lastRequestId;
            }
        }
    }

    @Override
    public long getEnterDate() {
        return enterDate;
    }

    @Override
    public void setEnterDate(long enterDate) {
        this.enterDate = enterDate;
    }

    //todo: remove seat from IGameSocketClient
    @Override
    public void setSeat(ISeat seat) {
        //nop
    }

    //todo: remove seat from IGameSocketClient
    @Override
    public ISeat getSeat() {
        return null;
    }

    @Override
    public void setDisconnected() {
        this.disconnected = true;
    }

    @Override
    public boolean isDisconnected() {
        return disconnected;
    }

    @Override
    public boolean isSingleConnectionClient() {
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UnifiedSocketClient [");
        sb.append("playerInfo=").append(playerInfo);
        sb.append(", nickname='").append(nickname).append('\'');
        sb.append(", loggedIn=").append(loggedIn);
        sb.append(", serverId=").append(serverId);
        sb.append(", gameType=").append(gameType);
        sb.append(", moneyType=").append(moneyType);
        sb.append(", sessionId='").append(sessionId).append('\'');
        sb.append(", lang=").append(lang);
        sb.append(", roomId=").append(roomId);
        sb.append(", seatNumber=").append(seatNumber);
        sb.append(", lastRequestId=").append(lastRequestId);
        sb.append(", enterDate=").append(enterDate);
        sb.append(", disconnected=").append(isDisconnected());
        sb.append(", privateRoom=").append(privateRoom);
        sb.append(", isOwner=").append(isOwner);
        sb.append(", isKicked=").append(isKicked);
        sb.append(']');
        return sb.toString();
    }


    public void setKicked(boolean isKicked) {
        this.isKicked = isKicked;
    }

    public boolean isKicked() {
        return isKicked;
    }

    @Override
    public IntervalStatistics getLatencyStatistic() {
        if (latencyStatistic == null) {
            this.latencyStatistic = new IntervalStatistics(
                    "Session id: " + session != null ? session.getId() : "'session is null'"
                    + "; GameType: " + getGameType() != null ? getGameType().name() : "null"
                    + "; Nickname: " + getNickname());
        }
        return latencyStatistic;
    }

    @Override
    public IntervalStatistics getPingLatencyStatistic() {
        if (pingLatencyStatistic == null) {
            this.pingLatencyStatistic = new IntervalStatistics(
                    "PING Session id: " + session != null ? session.getId() : "'session is null'"
                            + "; GameType: " + getGameType() != null ? getGameType().name() : "null"
                            + "; Nickname: " + getNickname());
        }
        return pingLatencyStatistic;
    }

}
