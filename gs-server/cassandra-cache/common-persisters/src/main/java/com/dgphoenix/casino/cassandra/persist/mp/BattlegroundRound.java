package com.abs.casino.cassandra.persist.mp;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class BattlegroundRound implements KryoSerializable {
    private static final byte VERSION = 1;
    private int gameId;
    private String gameName;
    private long buyIn;
    private long roundId;
    private long roomId;
    private long dateTime;
    private String status;
    private long gameScore;
    private int players;
    private Integer finalRank;
    private String winnerName;
    private Long winnerPot;
    private long gameSessionId;

    public BattlegroundRound() {}

    public BattlegroundRound(int gameId, String gameName, Long buyIn, long roundId, long roomId, long dateTime,
                             String status, long gameScore, int players, Integer finalRank, String winnerName,
                             Long winnerPot, long gameSessionId) {
        this.gameId = gameId;
        this.gameName = gameName;
        this.buyIn = buyIn;
        this.roundId = roundId;
        this.roomId = roomId;
        this.dateTime = dateTime;
        this.status = status;
        this.gameScore = gameScore;
        this.players = players;
        this.finalRank = finalRank;
        this.winnerName = winnerName;
        this.winnerPot = winnerPot;
        this.gameSessionId = gameSessionId;
    }

    public static byte getVERSION() {
        return VERSION;
    }

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public long getBuyIn() {
        return buyIn;
    }

    public void setBuyIn(long buyIn) {
        this.buyIn = buyIn;
    }

    public long getRoundId() {
        return roundId;
    }

    public void setRoundId(long roundId) {
        this.roundId = roundId;
    }

    public long getRoomId() {
        return roomId;
    }

    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }

    public long getDateTime() {
        return dateTime;
    }

    public void setDateTime(long dateTime) {
        this.dateTime = dateTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getGameScore() {
        return gameScore;
    }

    public void setGameScore(long gameScore) {
        this.gameScore = gameScore;
    }

    public int getPlayers() {
        return players;
    }

    public void setPlayers(int players) {
        this.players = players;
    }

    public Integer getFinalRank() {
        return finalRank;
    }

    public void setFinalRank(Integer finalRank) {
        this.finalRank = finalRank;
    }

    public String getWinnerName() {
        return winnerName;
    }

    public void setWinnerName(String winnerName) {
        this.winnerName = winnerName;
    }

    public Long getWinnerPot() {
        return winnerPot;
    }

    public void setWinnerPot(Long winnerPot) {
        this.winnerPot = winnerPot;
    }

    public long getGameSessionId() {
        return gameSessionId;
    }

    public void setGameSessionId(long gameSessionId) {
        this.gameSessionId = gameSessionId;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeInt(gameId, true);
        output.writeString(gameName);
        output.writeLong(buyIn, true);
        output.writeLong(roundId, true);
        output.writeLong(roomId, true);
        output.writeLong(dateTime, true);
        output.writeString(status);
        output.writeLong(gameScore, true);
        output.writeInt(players, true);
        kryo.writeObjectOrNull(output, finalRank, Integer.class);
        output.writeString(winnerName);
        kryo.writeObjectOrNull(output, winnerPot, Long.class);
        output.writeLong(gameSessionId, true);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte version = input.readByte();
        gameId = input.readInt(true);
        gameName = input.readString();
        buyIn = input.readLong(true);
        roundId = input.readLong(true);
        roomId = input.readLong(true);
        dateTime = input.readLong(true);
        status = input.readString();
        gameScore = input.readLong(true);
        players = input.readInt(true);
        finalRank = kryo.readObjectOrNull(input, Integer.class);
        winnerName = input.readString();
        winnerPot = kryo.readObjectOrNull(input, Long.class);
        gameSessionId = input.readLong(true);
    }

    @Override
    public String toString() {
        return "BattlegroundRound{" +
                "gameId=" + gameId +
                ", gameName='" + gameName + '\'' +
                ", buyIn=" + buyIn +
                ", roundId=" + roundId +
                ", roomId=" + roomId +
                ", dateTime=" + dateTime +
                ", status='" + status + '\'' +
                ", gameScore=" + gameScore +
                ", players=" + players +
                ", finalRank=" + finalRank +
                ", winnerName='" + winnerName + '\'' +
                ", winnerPot=" + winnerPot +
                ", gameSessionId=" + gameSessionId +
                '}';
    }
}
