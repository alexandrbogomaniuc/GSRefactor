package com.abs.casino.common.cache.data.bet;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * User: flsh
 * Date: 07.03.17.
 */
@XStreamAlias("BetInfo")
public class ShortBetInfo implements KryoSerializable, Comparable<ShortBetInfo> {
    private static final byte VERSION = 1;

    @XStreamAsAttribute
    @XStreamAlias("userId")
    private String extUserId;

    @XStreamOmitField
    private long accountId;

    @XStreamOmitField
    private int bankId;

    @XStreamAsAttribute
    @XStreamAlias("gameId")
    private long gameId;

    @XStreamAsAttribute
    @XStreamAlias("sessionId")
    private long sessionId;

    @XStreamAsAttribute
    @XStreamAlias("roundId")
    private long roundId;

    @XStreamAsAttribute
    @XStreamAlias("bet")
    private long bet;

    @XStreamAsAttribute
    @XStreamAlias("win")
    private long win;

    @XStreamAsAttribute
    @XStreamAlias("balance")
    private long balance;

    @XStreamAsAttribute
    @XStreamAlias("time")
    private long time;

    @XStreamOmitField
    private String currency;

    public ShortBetInfo() {
    }

    public ShortBetInfo(String extUserId, long accountId, int bankId, long gameId, long sessionId, long roundId,
                        long bet, long win, long balance, long time, String currency) {
        this.extUserId = extUserId;
        this.accountId = accountId;
        this.bankId = bankId;
        this.gameId = gameId;
        this.sessionId = sessionId;
        this.roundId = roundId;
        this.bet = bet;
        this.win = win;
        this.balance = balance;
        this.time = time;
        this.currency = currency;
    }

    public String getExtUserId() {
        return extUserId;
    }

    public void setExtUserId(String extUserId) {
        this.extUserId = extUserId;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public int getBankId() {
        return bankId;
    }

    public void setBankId(int bankId) {
        this.bankId = bankId;
    }

    public long getGameId() {
        return gameId;
    }

    public void setGameId(long gameId) {
        this.gameId = gameId;
    }

    public long getSessionId() {
        return sessionId;
    }

    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }

    public long getRoundId() {
        return roundId;
    }

    public void setRoundId(long roundId) {
        this.roundId = roundId;
    }

    public long getBet() {
        return bet;
    }

    public void setBet(long bet) {
        this.bet = bet;
    }

    public long getWin() {
        return win;
    }

    public void setWin(long win) {
        this.win = win;
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    @Override
    public int compareTo(ShortBetInfo betInfo) {
        return getTime() < betInfo.getTime() ? 1 : (getTime() > betInfo.getTime() ? -1 : 0);
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeString(extUserId);
        output.writeLong(accountId, true);
        output.writeInt(bankId, true);
        output.writeLong(gameId, true);
        output.writeLong(sessionId, true);
        output.writeLong(roundId, true);
        output.writeLong(bet, true);
        output.writeLong(win, true);
        output.writeLong(balance, true);
        output.writeLong(time, true);
        output.writeString(currency);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte ver = input.readByte();
        extUserId = input.readString();
        accountId = input.readLong(true);
        bankId = input.readInt(true);
        gameId = input.readLong(true);
        sessionId = input.readLong(true);
        roundId = input.readLong(true);
        bet = input.readLong(true);
        win = input.readLong(true);
        balance = input.readLong(true);
        time = input.readLong(true);
        if (ver > 0) {
            currency = input.readString();
        }
    }

    @Override
    public String toString() {
        return "ShortBetInfo[" +
                "extUserId='" + extUserId + '\'' +
                ", accountId=" + accountId +
                ", bankId=" + bankId +
                ", gameId=" + gameId +
                ", sessionId='" + sessionId + '\'' +
                ", roundId=" + roundId +
                ", bet=" + bet +
                ", win=" + win +
                ", balance=" + balance +
                ", time=" + time +
                ", currency='" + currency + '\'' +
                ']';
    }
}
