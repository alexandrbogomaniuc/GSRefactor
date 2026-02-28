package com.dgphoenix.casino.common.cache.data.session;

import com.abs.casino.common.cache.data.session.SessionConstants;
import com.dgphoenix.casino.common.cache.IDistributedCacheEntry;
import com.abs.casino.common.util.string.StringIdGenerator;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by ANGeL Date: Sep 17, 2008 Time: 6:37:32 PM
 */
public class SessionInfo implements IDistributedCacheEntry, KryoSerializable {
    private static final byte VERSION = 7;
    private String sessionId;
    private String externalSessionId;
    private long accountId;
    private String host;
    private ClientType clientType;
    private long startTime;
    private Long endTime;
    private volatile long lastActivityTime;
    private int loginServerId;
    private Long gameSessionId;
    private long playedGamesCount;
    private int lastPlayedMode;
    private String secretKey;
    private String lastCloseGameReason;
    private Long lastGameSessionId;
    private long lastRealityCheckTime;
    private long casinoLoginTime;
    private long realityCheckInterval;
    private boolean forceHttps;
    private String privateRoomId;

    public SessionInfo() {
    }

    public SessionInfo(long accountId, ClientType clientType, String sessionId, String host, String externalSessionId,
                       int loginServerId, long startTime, String privateRoomId) {
        this.accountId = accountId;
        this.sessionId = sessionId;
        this.clientType = clientType;
        this.host = host;
        this.externalSessionId = externalSessionId;
        this.loginServerId = loginServerId;

        this.startTime = startTime;
        this.lastActivityTime = startTime;
        this.lastRealityCheckTime = startTime;
        this.casinoLoginTime = startTime;
        this.endTime = null;
        this.gameSessionId = null;
        this.playedGamesCount = 0;
        this.lastPlayedMode = SessionConstants.NO_MODE;
        this.privateRoomId = privateRoomId;
    }

    private SessionInfo(long accountId, String sessionId, ClientType clientType, String host, long startTime,
                        Long endTime, long lastActivityTime, int loginServerId, String externalSessionId,
                        Long gameSessionId, long playedGamesCount, int lastPlayedMode, long casinoLoginTime,
                        long realityCheckInterval, String privateRoomId) {
        super();
        this.accountId = accountId;
        this.sessionId = sessionId;
        this.clientType = clientType;
        this.host = host;
        this.startTime = startTime;
        this.endTime = endTime;
        this.lastActivityTime = lastActivityTime;
        this.loginServerId = loginServerId;
        this.externalSessionId = externalSessionId;
        this.gameSessionId = gameSessionId;
        this.playedGamesCount = playedGamesCount;
        this.lastPlayedMode = lastPlayedMode;
        this.casinoLoginTime = casinoLoginTime;
        this.realityCheckInterval = realityCheckInterval;
        this.privateRoomId = privateRoomId;
    }

    public SessionInfo copy() {
        return new SessionInfo(accountId, sessionId, clientType, host, startTime, endTime, lastActivityTime,
                loginServerId, externalSessionId, gameSessionId,
                playedGamesCount, lastPlayedMode, casinoLoginTime, realityCheckInterval, privateRoomId);
    }

    public String getSecretKey() {
        return secretKey;
    }

    public int getServerId() {
        return StringIdGenerator.extractServerId(sessionId);
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public ClientType getClientType() {
        return clientType;
    }

    public void setClientType(ClientType clientType) {
        this.clientType = clientType;
    }

    public String getExternalSessionId() {
        return externalSessionId;
    }

    public void setExternalSessionId(String externalSessionId) {
        this.externalSessionId = externalSessionId;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getLoginServerId() {
        return loginServerId;
    }

    public void setLoginServerId(int loginServerId) {
        this.loginServerId = loginServerId;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public Long getEndTimeDirty() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public long getLastActivityTime() {
        return lastActivityTime;
    }

    public void setLastActivityTime(long lastActivityTime) {
        this.lastActivityTime = lastActivityTime;
    }

    public Long getGameSessionId() {
        return gameSessionId;
    }

    public void reuse() {
        this.lastActivityTime = System.currentTimeMillis();
        if (gameSessionId != null) {
            this.lastGameSessionId = gameSessionId;
        }
        this.gameSessionId = null;
    }

    public void fireGameSessionClosed(int lastPlayedMode) {
        if (gameSessionId != null) {
            this.lastGameSessionId = gameSessionId;
        }
        this.gameSessionId = null;
        this.playedGamesCount++;
        this.lastPlayedMode = lastPlayedMode;
    }

    public void setGameSessionId(Long gameSessionId) {
        this.gameSessionId = gameSessionId;
        this.lastActivityTime = System.currentTimeMillis();
    }

    public long getPlayedGamesCount() {
        return playedGamesCount;
    }

    public void setPlayedGamesCount(long playedGamesCount) {
        this.playedGamesCount = playedGamesCount;
    }

    public void incrementPlayedGamesCount(long increment) {
        this.playedGamesCount += increment;
    }

    public int getLastPlayedMode() {
        return lastPlayedMode;
    }

    public void setLastPlayedMode(int lastPlayedMode) {
        this.lastPlayedMode = lastPlayedMode;
    }

    public void updateActivity() {
        this.lastActivityTime = System.currentTimeMillis();
    }

    public String getLastCloseGameReason() {
        return lastCloseGameReason;
    }

    public void setLastCloseGameReason(String lastCloseGameReason) {
        this.lastCloseGameReason = lastCloseGameReason;
    }

    public Long getLastGameSessionId() {
        return lastGameSessionId;
    }

    public void setLastGameSessionId(Long lastGameSessionId) {
        this.lastGameSessionId = lastGameSessionId;
    }

    public void setLastRealityCheckTime(long lastRealityCheckTime) {
        this.lastRealityCheckTime = lastRealityCheckTime;
    }

    public long getLastRealityCheckTime() {
        return lastRealityCheckTime;
    }

    public long getCasinoLoginTime() {
        return casinoLoginTime;
    }

    public void setCasinoLoginTime(long casinoLoginTime) {
        this.casinoLoginTime = casinoLoginTime;
    }

    public boolean isForceHttps() {
        return forceHttps;
    }

    public void setForceHttps(boolean forceHttps) {
        this.forceHttps = forceHttps;
    }

    public void calcLastRealityCheckTime(long currentTime, long realityCheckIntervalInSeconds) {
        long playerExternalActiveTime = currentTime - casinoLoginTime;
        long realityCheckIntervalInMills = TimeUnit.SECONDS.toMillis(realityCheckIntervalInSeconds);
        long estimatedLastCheckTime = casinoLoginTime + (playerExternalActiveTime / realityCheckIntervalInMills) * realityCheckIntervalInMills;
        this.lastRealityCheckTime = Math.min(estimatedLastCheckTime, currentTime);
    }

    public long getRealityCheckInterval() {
        return realityCheckInterval;
    }

    public void setRealityCheckInterval(long realityCheckInterval) {
        this.realityCheckInterval = realityCheckInterval;
    }

    public String getPrivateRoomId() {
        return privateRoomId;
    }

    public void setPrivateRoomId(String privateRoomId) {
        this.privateRoomId = privateRoomId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SessionInfo that = (SessionInfo) o;

        if (accountId != that.accountId) return false;
        if (externalSessionId != null ? !externalSessionId.equals(that.externalSessionId) : that.externalSessionId != null)
            return false;
        if (gameSessionId != null ? !gameSessionId.equals(that.gameSessionId) : that.gameSessionId != null)
            return false;
        if (!sessionId.equals(that.sessionId)) return false;
        if (privateRoomId != null ? !privateRoomId.equals(that.privateRoomId) : that.privateRoomId != null)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = sessionId.hashCode();
        result = 31 * result + (externalSessionId != null ? externalSessionId.hashCode() : 0);
        result = 31 * result + (int) (accountId ^ (accountId >>> 32));
        result = 31 * result + (gameSessionId != null ? gameSessionId.hashCode() : 0);
        return result;
    }

    public String toString() {
        return "SessionInfo [" +
                super.toString() +
                ", accountId=" + accountId +
                ", sessionId=" + sessionId +
                ", clientType=" + clientType +
                ", host=" + host +
                ", startTime=" + new Date(startTime) +
                ", endTime=" + (endTime == null ? "null" : new Date(endTime)) +
                ", lastActivityTime=" + new Date(lastActivityTime) +
                ", loginServerId=" + loginServerId +
                ", externalSessionId=" + externalSessionId +
                ", gameSessionId=" + gameSessionId +
                ", playedGamesCount=" + playedGamesCount +
                ", lastPlayedMode=" + lastPlayedMode +
                ", lastCloseGameReason=" + lastCloseGameReason +
                ", lastGameSessionId=" + lastGameSessionId +
                ", lastRealityCheckTime=" + lastRealityCheckTime +
                ", casinoLoginTime=" + casinoLoginTime +
                ", realityCheckInterval=" + realityCheckInterval +
                ", forceHttps=" + forceHttps +
                ", privateRoomId=" + privateRoomId +
                "]";
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeString(sessionId);
        output.writeString(externalSessionId);
        output.writeLong(accountId, true);
        output.writeString(host);
        kryo.writeObject(output, clientType);
        output.writeLong(startTime, true);
        kryo.writeObjectOrNull(output, endTime, Long.class);
        output.writeLong(lastActivityTime, true);
        output.writeInt(loginServerId, true);
        kryo.writeObjectOrNull(output, gameSessionId, Long.class);
        output.writeLong(playedGamesCount, true);
        output.writeInt(lastPlayedMode);
        output.writeString(secretKey);
        output.writeString(lastCloseGameReason);
        kryo.writeObjectOrNull(output, lastGameSessionId, Long.class);
        output.writeLong(lastRealityCheckTime, true);
        output.writeLong(casinoLoginTime, true);
        output.writeLong(realityCheckInterval, true);
        output.writeBoolean(forceHttps);
        output.writeString(privateRoomId);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte ver = input.readByte();
        sessionId = input.readString();
        externalSessionId = input.readString();
        accountId = input.readLong(true);
        host = input.readString();
        clientType = kryo.readObject(input, ClientType.class);
        startTime = input.readLong(true);
        endTime = kryo.readObjectOrNull(input, Long.class);
        lastActivityTime = input.readLong(true);
        loginServerId = input.readInt(true);
        gameSessionId = kryo.readObjectOrNull(input, Long.class);
        playedGamesCount = input.readLong(true);
        lastPlayedMode = input.readInt();
        secretKey = input.readString();
        if (ver >= 1) {
            lastCloseGameReason = input.readString();
        }
        if (ver >= 2) {
            lastGameSessionId = kryo.readObjectOrNull(input, Long.class);
        }
        if (ver >= 3) {
            lastRealityCheckTime = input.readLong(true);
        }
        if (ver >= 4) {
            casinoLoginTime = input.readLong(true);
        }
        if (ver >= 5) {
            realityCheckInterval = input.readLong(true);
        }
        if (ver >= 6) {
            forceHttps = input.readBoolean();
        }
        if (ver >= 7) {
            privateRoomId = input.readString();
        }
    }
}
