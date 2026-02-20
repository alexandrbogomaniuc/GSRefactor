package com.betsoft.casino.mp.payment;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * User: flsh
 * Date: 11.08.2022.
 */
public abstract class AbstractPendingOperation implements IPendingOperation {
    private static final byte VERSION = 1;
    protected long accountId;
    private String sessionId;
    protected long gameSessionId;
    protected long roomId;
    private long createDate;
    private long gameId;
    private long bankId;

    protected AbstractPendingOperation() {

    }

    protected AbstractPendingOperation(long accountId, String sessionId, long gameSessionId, long roomId, long gameId, long bankId) {
        this.accountId = accountId;
        this.sessionId = sessionId;
        this.gameSessionId = gameSessionId;
        this.roomId = roomId;
        this.createDate = System.currentTimeMillis();
        this.gameId = gameId;
        this.bankId = bankId;
    }

    @Override
    public long getAccountId() {
        return accountId;
    }

    @Override
    public String getSessionId() {
        return sessionId;
    }

    @Override
    public long getGameSessionId() {
        return gameSessionId;
    }

    @Override
    public long getRoomId() {
        return roomId;
    }

    @Override
    public long getCreateDate() {
        return createDate;
    }

    @Override
    public long getGameId() {
        return gameId;
    }

    @Override
    public long getBankId() {
        return bankId;
    }

    public String getOperationId(){
        return  "" + accountId + gameSessionId + roomId;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeLong(accountId, true);
        output.writeString(sessionId);
        output.writeLong(gameSessionId, true);
        output.writeLong(roomId, true);
        output.writeLong(createDate, true);
        output.writeLong(gameId, true);
        output.writeLong(bankId, true);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte version = input.readByte();
        accountId = input.readLong(true);
        sessionId = input.readString();
        gameSessionId = input.readLong(true);
        roomId = input.readLong(true);
        createDate = input.readLong(true);
        if (version > 0) {
            gameId = input.readLong(true);
            bankId = input.readLong(true);
        }
    }

    @Override
    public String toString() {
        return ", accountId=" + accountId +
                ", sessionId='" + sessionId + '\'' +
                ", gameSessionId=" + gameSessionId +
                ", roomId=" + roomId +
                ", gameId=" + gameId +
                ", bankId=" + bankId +
                ", createDate=" + createDate;
    }
}
