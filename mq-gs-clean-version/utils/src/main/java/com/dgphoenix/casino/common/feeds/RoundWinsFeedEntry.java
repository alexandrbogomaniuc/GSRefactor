package com.dgphoenix.casino.common.feeds;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class RoundWinsFeedEntry implements KryoSerializable {

    private static final byte VERSION = 0;

    private long time;
    private long bet;
    private long win;
    private String nickname;
    private long gameId;
    private String currency;

    public RoundWinsFeedEntry() {}

    public RoundWinsFeedEntry(long time, long bet, long win, String nickname, long gameId, String currency) {
        this.time = time;
        this.bet = bet;
        this.win = win;
        this.nickname = nickname;
        this.gameId = gameId;
        this.currency = currency;
    }

    public long getTime() {
        return time;
    }

    public long getBet() {
        return bet;
    }

    public long getWin() {
        return win;
    }

    public String getNickname() {
        return nickname;
    }

    public long getGameId() {
        return gameId;
    }

    public String getCurrency() {
        return currency;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeLong(time, true);
        output.writeLong(bet, true);
        output.writeLong(win, true);
        output.writeLong(gameId, true);
        output.writeString(nickname);
        output.writeString(currency);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        input.readByte();
        time = input.readLong(true);
        bet = input.readLong(true);
        win = input.readLong(true);
        gameId = input.readLong(true);
        nickname = input.readString();
        currency = input.readString();
    }
}
