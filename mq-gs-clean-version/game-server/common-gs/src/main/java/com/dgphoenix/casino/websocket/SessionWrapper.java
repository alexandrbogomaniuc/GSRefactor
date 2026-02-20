package com.dgphoenix.casino.websocket;

import com.dgphoenix.casino.common.util.string.StringUtils;
import com.google.common.base.Splitter;
import org.apache.commons.collections.CollectionUtils;
import org.eclipse.jetty.websocket.api.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by vladislav on 1/25/17.
 */
public class SessionWrapper implements Session {
    private static final String SESSION_ID = "sessionId";
    private static final String ACTIVE_PROMO_IDS = "activePromoIds";
    private static final Splitter ACTIVE_PROMO_IDS_SPLITTER = Splitter.on("|").omitEmptyStrings();

    private final Session session;
    private final long creationTime;

    public SessionWrapper(Session session) {
        this.session = session;
        this.creationTime = System.currentTimeMillis();
    }

    public static SessionWrapper of(Session session) {
        return new SessionWrapper(session);
    }

    public String getPlayerSessionId() {
        return getRequestParam(SESSION_ID);
    }

    public Set<Long> getActivePromoIds() {
        Set<Long> activePromoIds = new HashSet<>();
        String activePromoIdsAsString = getRequestParam(ACTIVE_PROMO_IDS);
        if (!StringUtils.isTrimmedEmpty(activePromoIdsAsString)) {
            Iterable<String> activePromoIdsAsStrings = ACTIVE_PROMO_IDS_SPLITTER.split(activePromoIdsAsString);
            for (String promoIdAsString : activePromoIdsAsStrings) {
                activePromoIds.add(Long.valueOf(promoIdAsString));
            }
        }
        return activePromoIds;
    }

    private String getRequestParam(String paramName) {
        Map<String, List<String>> parametersMap = session.getUpgradeRequest().getParameterMap();
        List<String> values = parametersMap.get(paramName);
        String value = null;
        if (CollectionUtils.isNotEmpty(values)) {
            value = values.get(0);
        }
        return value;
    }

    public long getCreationTime() {
        return creationTime;
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

        SessionWrapper that = (SessionWrapper) o;

        return session.equals(that.session);
    }

    @Override
    public int hashCode() {
        return session.hashCode();
    }
}
