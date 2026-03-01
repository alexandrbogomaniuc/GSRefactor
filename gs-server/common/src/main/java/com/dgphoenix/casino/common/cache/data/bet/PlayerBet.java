package com.abs.casino.common.cache.data.bet;

import com.abs.casino.common.cache.IDistributedCacheEntry;
import com.abs.casino.common.cache.Identifiable;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import java.util.Date;

@XStreamAlias("Bet")
public class PlayerBet implements IDistributedCacheEntry, Identifiable, Comparable<PlayerBet>, KryoSerializable {
    private static final byte VERSION = 3;
    private static final String UNJ_WIN = "UNJ_WIN";
    private static final String UNJ_NAME = "UNJ_NAME";

    @XStreamAsAttribute
    @XStreamAlias("id")
    private long id;

    @XStreamAsAttribute
    @XStreamAlias("r")
    private long roundId;

    @XStreamAsAttribute
    @XStreamAlias("s")
    private int gameStateId;

    @XStreamAsAttribute
    @XStreamAlias("d")
    private String data;

    @XStreamAsAttribute
    @XStreamAlias("sd")
    private String servletData;

    @XStreamAsAttribute
    @XStreamAlias("b")
    private long bet;

    @XStreamAsAttribute
    @XStreamAlias("w")
    private long win;

    @XStreamAsAttribute
    @XStreamAlias("ba")
    private long balance;

    @XStreamAsAttribute
    @XStreamAlias("t")
    private long time;

    @XStreamAsAttribute
    @XStreamAlias("ad")
    private byte[] archiveAdditionalData;

    @XStreamOmitField
    private String extBetId;

    public PlayerBet() {
    }

    public PlayerBet(long id, long roundId, int gameStateId, String data, String servletData, long bet, long win,
                     long balance, byte[] archiveAdditionalData, long time) {
        this.id = id;
        this.roundId = roundId;
        this.gameStateId = gameStateId;
        this.data = data == null ? " " : data;
        this.servletData = servletData == null ? " " : servletData;
        this.bet = bet;
        this.win = win;
        this.balance = balance;
        this.time = time;
        this.archiveAdditionalData = archiveAdditionalData;
    }

    //use from PlayeBetPersistenceManager
    public void update(int gameStateId, String data, String servletData, long bet, long win,
                       long balance, byte[] archiveAdditionalData) {
        this.gameStateId = gameStateId;
        this.data = data == null ? " " : data;
        this.servletData = servletData == null ? " " : servletData;
        this.bet = bet;
        this.win = win;
        this.balance = balance;
        this.archiveAdditionalData = archiveAdditionalData;
    }

    public long getId() {
        return id;
    }

    public long getRoundId() {
        return roundId;
    }

    public int getGameStateId() {
        return gameStateId;
    }

    public String getData() {
        return data;
    }

    public String getServletData() {
        return servletData;
    }

    public long getBet() {
        return bet;
    }

    public long getWin() {
        return win;
    }

    public long getBalance() {
        return balance;
    }

    public long getTime() {
        return time;
    }

    public byte[] getArchiveAdditionalData() {
        return archiveAdditionalData;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setRoundId(long roundId) {
        this.roundId = roundId;
    }

    public void setGameStateId(int gameStateId) {
        this.gameStateId = gameStateId;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void setServletData(String servletData) {
        this.servletData = servletData;
    }

    public void setBet(long bet) {
        this.bet = bet;
    }

    public void setWin(long win) {
        this.win = win;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setArchiveAdditionalData(byte[] archiveAdditionalData) {
        this.archiveAdditionalData = archiveAdditionalData;
    }

    public String getExtBetId() {
        return extBetId;
    }

    public void setExtBetId(String extBetId) {
        this.extBetId = extBetId;
    }

    @Override
    public int compareTo(PlayerBet o) {
        return o == null ? -1 : Long.compare(id, o.id);
    }

    public String toString() {
        return "PlayerBet [" +
                "id=" + id +
                ", roundId=" + roundId +
                ", gameStateId=" + gameStateId +
                ", data='" + data + "'" +
                ", servletData='" + servletData + "'" +
                ", bet=" + bet +
                ", win=" + win +
                ", balance=" + balance +
                ", time=" + new Date(time) +
                ", archiveAdditionalData='" + archiveAdditionalData + "'" +
                ", extBetId=" + extBetId +
                "]";
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeLong(id, true);
        output.writeLong(roundId, true);
        output.writeInt(gameStateId);
        output.writeString(data);
        output.writeString(servletData);
        output.writeLong(bet, true);
        output.writeLong(win, true);
        output.writeLong(balance, true);
        output.writeLong(time, true);
        if (archiveAdditionalData == null || archiveAdditionalData.length == 0) {
            output.writeInt(0, true);
        } else {
            output.writeVarInt(archiveAdditionalData.length, true);
            output.writeBytes(archiveAdditionalData);
        }
        output.writeString(extBetId);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte ver = input.readByte();
        id = input.readLong(true);
        roundId = input.readLong(true);
        gameStateId = input.readInt();
        data = input.readString();
        servletData = input.readString();
        bet = input.readLong(true);
        win = input.readLong(true);
        balance = input.readLong(true);
        time = input.readLong(true);
        int aADLength = input.readInt(true);
        if (aADLength > 0) {
            archiveAdditionalData = input.readBytes(aADLength);
        } else {
            archiveAdditionalData = null;
        }
        if (ver > 2) {
            extBetId = input.readString();
        }
    }
}