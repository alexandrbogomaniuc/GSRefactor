package com.betsoft.casino.mp.common.testmodel;

import com.betsoft.casino.mp.model.GameType;
import com.betsoft.casino.mp.model.ISeat;
import com.betsoft.casino.mp.web.IGameSocketClient;
import com.betsoft.casino.mp.web.IMessageSerializer;
import com.betsoft.casino.mp.web.RequestStatistic;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.FluxSink;

import java.util.Collection;
import java.util.Objects;

public class StubGameSocketClient implements IGameSocketClient {
    private static final Logger LOG = LogManager.getLogger(StubGameSocketClient.class);
    private Long accountId;
    private Long bankId;
    private Long roomId;
    private int seatNumber = -1;
    private Integer lastRequestId;
    private String sessionId;
    private long enterDate = -1;
    private int serverId;
    private ISeat seat;
    private boolean isKicked;

    private Logger logger = LOG;

    private boolean disconnected = false;
    private GameType gameType;
    private boolean privateRoom;
    private boolean isOwner;

    public StubGameSocketClient(Long accountId, Long bankId, String webSocketSessionId, FluxSink<WebSocketMessage> connection,
                                IMessageSerializer serializer, GameType gameType) {
        this.accountId = accountId;
        this.bankId = bankId;
        this.gameType = gameType;
    }

    @Override
    public FluxSink<WebSocketMessage> getConnection() {
        return null;
    }

    @Override
    public IMessageSerializer getSerializer() {
        return null;
    }

    @Override
    public String getWebSocketSessionId() {
        return null;
    }

    @Override
    public WebSocketSession getSession() {
        return null;
    }

    @Override
    public Long getAccountId() {
        return accountId;
    }

    @Override
    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    @Override
    public Long getBankId() {
        return bankId;
    }

    @Override
    public void setBankId(Long bankId) {
        this.bankId = bankId;
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
                LOG.error("setLastRequestId: bad new requestId=" + lastRequestId + ", current=" + this.lastRequestId);
            } else {
                this.lastRequestId = lastRequestId;
            }
        }
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
    public void addRequestStatistic(Class requestClass, long lastRequestId, long lastServerInputDate, long lastClientSendDate) {

    }

    @Override
    public RequestStatistic getRequestStatistic(Class requestClass) {
        return null;
    }

    @Override
    public Collection<RequestStatistic> getRequestStatistics() {
        return null;
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
    public long getEnterDate() {
        return enterDate;
    }

    @Override
    public void setEnterDate(long enterDate) {
        this.enterDate = enterDate;
    }

    @Override
    public ISeat getSeat() {
        return seat;
    }

    @Override
    public void setSeat(ISeat seat) {
        this.seat = seat;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StubGameSocketClient that = (StubGameSocketClient) o;
        return Objects.equals(getWebSocketSessionId(), that.getWebSocketSessionId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getWebSocketSessionId());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GameSocketClient [");
        sb.append("webSocketSessionId='").append(getWebSocketSessionId()).append('\'');
        sb.append(", accountId=").append(accountId);
        sb.append(", bankId=").append(bankId);
        sb.append(", roomId=").append(roomId);
        sb.append(", seatNumber=").append(seatNumber);
        sb.append(", lastRequestId=").append(lastRequestId);
        sb.append(", sessionId='").append(sessionId).append('\'');
        sb.append(", serverId=").append(serverId);
        sb.append(", enterDate=").append(enterDate);
        sb.append(", gameType.gameId=").append(gameType == null ? "null" : gameType.getGameId());
        sb.append(", privateRoom=").append(privateRoom);
        sb.append(", isOwner=").append(isOwner);
        sb.append(']');
        return sb.toString();
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
        return false;
    }

    @Override
    public String getNickname() {
        return null;
    }

    @Override
    public void setNickname(String nickname) {

    }

    @Override
    public int getRid() {
        return -1;
    }

    @Override
    public boolean isBot() {
        return false;
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
    public void setPrivateRoom(boolean privateRoom) {
        this.privateRoom = privateRoom;
    }

    @Override
    public boolean isPrivateRoom() {
        return this.privateRoom;
    }

    @Override
    public void setOwner(boolean owner) {
        this.isOwner = owner;
    }

    @Override
    public boolean isOwner() {
        return isOwner;
    }

    public void setKicked(boolean isKicked) {
        this.isKicked = isKicked;
    }

    public boolean isKicked() {
        return isKicked;
    }

}
