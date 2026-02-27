package com.dgphoenix.casino.websocket.tournaments;

import com.dgphoenix.casino.common.transport.ITransportObject;
import com.abs.casino.promo.tournaments.messages.BattlegroundInfo;
import com.dgphoenix.casino.websocket.WebSocketMessageCallback;
import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.websocket.api.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class TournamentClient implements ISocketClient {
    private static final Logger LOG = LogManager.getLogger(TournamentClient.class);

    private final Gson gson;
    private final Session session;
    private final long creationTime;
    private String sessionId;
    private String currency;
    private String lang;
    private String cdn;
    private Set<BattlegroundInfo> battlegroundInfos;

    public TournamentClient(Session session, String sessionId, Gson gson) {
        this.session = session;
        this.creationTime = System.currentTimeMillis();
        this.sessionId = sessionId;
        this.gson = gson;
        this.battlegroundInfos = new HashSet<>();
    }

    public Set<BattlegroundInfo> getAvailableBuyIns() {
        return battlegroundInfos;
    }

    public void setAvailableBuyIns(Set<BattlegroundInfo> availableBuyIns) {
        this.battlegroundInfos = availableBuyIns;
    }

    @Override
    public void sendMessage(ITransportObject outbound) {
        if (session.isOpen()) {
            String fullMessage = gson.toJson(outbound);
            LOG.debug("sendMessage: sessionId={}, message={}", sessionId, outbound);
            WebSocketMessageCallback webSocketMessageCallback = new WebSocketMessageCallback(session, sessionId, fullMessage);
            session.getRemote().sendString(fullMessage, webSocketMessageCallback);
        } else {
            LOG.error("sendMessage failed, session is not opened");
        }
    }

    @Override
    public void connect(String sessionId, String currency, String lang, String cdn) {
        this.sessionId = sessionId;
        this.currency = currency;
        this.lang = lang;
        this.cdn = cdn;
    }

    @Override
    public String getCurrency() {
        return currency;
    }

    @Override
    public String getLang() {
        return lang;
    }

    @Override
    public String getCdn() {
        return cdn;
    }

    @Override
    public boolean isConnected() {
        return this.sessionId != null;
    }

    @Override
    public String getSessionId() {
        return sessionId;
    }

    @Override
    public void close() {
        session.close();
    }

    @Override
    public void close(CloseStatus closeStatus) {
        session.close(closeStatus);
    }

    @Override
    public void close(int statusCode, String reason) {
        session.close(statusCode, reason);
    }

    @Override
    public void disconnect() throws IOException {
        session.disconnect();
    }

    @Override
    public long getIdleTimeout() {
        return session.getIdleTimeout();
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return session.getLocalAddress();
    }

    @Override
    public WebSocketPolicy getPolicy() {
        return session.getPolicy();
    }

    @Override
    public String getProtocolVersion() {
        return session.getProtocolVersion();
    }

    @Override
    public RemoteEndpoint getRemote() {
        return session.getRemote();
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return session.getRemoteAddress();
    }

    @Override
    public UpgradeRequest getUpgradeRequest() {
        return session.getUpgradeRequest();
    }

    @Override
    public UpgradeResponse getUpgradeResponse() {
        return session.getUpgradeResponse();
    }

    @Override
    public boolean isOpen() {
        return session.isOpen();
    }

    @Override
    public boolean isSecure() {
        return session.isSecure();
    }

    @Override
    public void setIdleTimeout(long ms) {
        session.setIdleTimeout(ms);
    }

    @Override
    public SuspendToken suspend() {
        return session.suspend();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TournamentClient that = (TournamentClient) o;
        return creationTime == that.creationTime &&
                Objects.equals(gson, that.gson) &&
                Objects.equals(session, that.session) &&
                Objects.equals(sessionId, that.sessionId) &&
                Objects.equals(currency, that.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gson, session, creationTime, sessionId, currency);
    }

    @Override
    public String toString() {
        return "TournamentClient{" +
                "gson=" + gson +
                ", session=" + session +
                ", creationTime=" + creationTime +
                ", sessionId='" + sessionId + '\'' +
                ", currency='" + currency + '\'' +
                ", battlegroundInfos='" + battlegroundInfos + '\'' +
                '}';
    }
}
