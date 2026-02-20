package com.betsoft.casino.mp.model.onlineplayer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.Objects;

public class SocketClientInfo implements KryoSerializable {

    private static final byte VERSION = 0;

    private String webSocketSessionId;
    private int serverId;
    private long roomId;
    private long gameId;
    private String gameName;
    private long accountId;
    private String nickname;
    private String externalId;
    private boolean isOwner;
    private String sessionId;
    private int seatNr;
    private boolean isPrivate;
    private boolean isBattleGround;
    private long buyInStake;
    private String currency;
    private long setAt;

    public SocketClientInfo() {
    }

    public SocketClientInfo(String webSocketSessionId, int serverId, long roomId, long gameId, String gameName,
                            long accountId, String nickname, String externalId, boolean isOwner, String sessionId,
                            int seatNr, boolean isPrivate, boolean isBattleGround, long buyInStake, String currency) {

        this(webSocketSessionId, serverId, roomId, gameId, gameName, accountId, nickname, externalId, isOwner, sessionId,
        seatNr, isPrivate, isBattleGround, buyInStake, currency, System.currentTimeMillis());
    }

    public SocketClientInfo(String webSocketSessionId, int serverId, long roomId, long gameId, String gameName,
                            long accountId, String nickname, String externalId, boolean isOwner, String sessionId,
                            int seatNr, boolean isPrivate, boolean isBattleGround, long buyInStake, String currency, long setAt) {

        this.webSocketSessionId = webSocketSessionId;
        this.serverId = serverId;
        this.roomId = roomId;
        this.gameId = gameId;
        this.gameName = gameName;
        this.accountId = accountId;
        this.nickname = nickname;
        this.externalId = externalId;
        this.isOwner = isOwner;
        this.sessionId = sessionId;
        this.seatNr = seatNr;
        this.isPrivate = isPrivate;
        this.isBattleGround = isBattleGround;
        this.buyInStake = buyInStake;
        this.currency = currency;
        this.setAt = setAt;
    }

    public String getWebSocketSessionId() {
        return webSocketSessionId;
    }

    public void setWebSocketSessionId(String webSocketSessionId) {
        this.webSocketSessionId = webSocketSessionId;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public long getRoomId() {
        return roomId;
    }

    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }

    public long getGameId() {
        return gameId;
    }

    public void setGameId(long gameId) {
        this.gameId = gameId;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public boolean isOwner() {
        return isOwner;
    }

    public void setOwner(boolean owner) {
        isOwner = owner;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public int getSeatNr() {
        return seatNr;
    }

    public void setSeatNr(int seatNr) {
        this.seatNr = seatNr;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }

    public boolean isBattleGround() {
        return isBattleGround;
    }

    public void setBattleGround(boolean battleGround) {
        isBattleGround = battleGround;
    }

    public long getBuyInStake() {
        return buyInStake;
    }

    public void setBuyInStake(long buyInStake) {
        this.buyInStake = buyInStake;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public long getSetAt() {
        return setAt;
    }

    public void setSetAt(long setAt) {
        this.setAt = setAt;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeString(webSocketSessionId);
        output.writeInt(serverId);
        output.writeLong(roomId, true);
        output.writeLong(gameId, true);
        output.writeString(gameName);
        output.writeLong(accountId, true);
        output.writeString(nickname);
        output.writeString(externalId);
        output.writeBoolean(isOwner);
        output.writeString(sessionId);
        output.writeInt(seatNr);
        output.writeBoolean(isPrivate);
        output.writeBoolean(isBattleGround);
        output.writeLong(buyInStake, true);
        output.writeString(currency);
        output.writeLong(setAt, true);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        input.readByte();
        //noinspection unchecked
        webSocketSessionId = input.readString();
        serverId = input.readInt();
        roomId = input.readLong(true);
        gameId = input.readLong(true);
        gameName = input.readString();
        accountId = input.readLong(true);
        nickname = input.readString();
        externalId = input.readString();
        isOwner = input.readBoolean();
        sessionId = input.readString();
        seatNr = input.readInt();
        isPrivate = input.readBoolean();
        isBattleGround = input.readBoolean();
        buyInStake = input.readLong(true);
        currency = input.readString();
        setAt = input.readLong(true);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SocketClientInfo)) return false;
        SocketClientInfo that = (SocketClientInfo) o;
        return serverId == that.serverId && roomId == that.roomId && gameId == that.gameId &&
                accountId == that.accountId && isOwner == that.isOwner && seatNr == that.seatNr &&
                isPrivate == that.isPrivate && isBattleGround == that.isBattleGround && buyInStake == that.buyInStake &&
                setAt == that.setAt && Objects.equals(webSocketSessionId, that.webSocketSessionId) &&
                Objects.equals(gameName, that.gameName) && Objects.equals(nickname, that.nickname) &&
                Objects.equals(externalId, that.externalId) && Objects.equals(sessionId, that.sessionId) &&
                Objects.equals(currency, that.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(webSocketSessionId, serverId, roomId, gameId, gameName, accountId, nickname, externalId,
                isOwner, sessionId, seatNr, isPrivate, isBattleGround, buyInStake, currency, setAt);
    }

    @Override
    public String toString() {
        return "SocketClientInfo{" +
                "webSocketSessionId='" + webSocketSessionId + '\'' +
                ", serverId=" + serverId +
                ", roomId=" + roomId +
                ", gameId=" + gameId +
                ", gameName='" + gameName + '\'' +
                ", accountId=" + accountId +
                ", nickname='" + nickname + '\'' +
                ", externalId='" + externalId + '\'' +
                ", isOwner=" + isOwner +
                ", sessionId='" + sessionId + '\'' +
                ", seatNr=" + seatNr +
                ", isPrivate=" + isPrivate +
                ", isBattleGround=" + isBattleGround +
                ", buyInStake=" + buyInStake +
                ", currency='" + currency + '\'' +
                ", setAt=" + setAt +
                '}';
    }
}
