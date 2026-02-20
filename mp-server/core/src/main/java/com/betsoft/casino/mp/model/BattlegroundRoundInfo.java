package com.betsoft.casino.mp.model;

import com.betsoft.casino.mp.model.battleground.IBattlegroundRoundInfo;
import com.betsoft.casino.mp.model.battleground.IBgPlace;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.Serializable;
import java.util.List;
import java.util.StringJoiner;

public class BattlegroundRoundInfo implements IBattlegroundRoundInfo, Serializable {
    private static final byte VERSION = 1;

    private long buyIn;
    private long winAmount;
    private long betsSum;
    private long winSum;
    private List<IBgPlace> places;
    private String status;
    private int playersNumber;
    private String winnerName;
    private long roundId;
    private long roundStartDate;
    private String privateRoomId;

    public BattlegroundRoundInfo() {}

    public BattlegroundRoundInfo(long buyIn, long winAmount, long betsSum, long winSum, List<IBgPlace> places,
                                 String status, int playersNumber, String winnerName, long roundId, long roundStartDate, String privateRoomId) {
        this.buyIn = buyIn;
        this.winAmount = winAmount;
        this.betsSum = betsSum;
        this.winSum = winSum;
        this.places = places;
        this.status = status;
        this.playersNumber = playersNumber;
        this.winnerName = winnerName;
        this.roundId = roundId;
        this.roundStartDate = roundStartDate;
        this.privateRoomId = privateRoomId;
    }

    @Override
    public long getBuyIn() {
        return buyIn;
    }

    public void setBuyIn(long buyIn) {
        this.buyIn = buyIn;
    }

    @Override
    public long getWinAmount() {
        return winAmount;
    }

    public void setWinAmount(long winAmount) {
        this.winAmount = winAmount;
    }

    @Override
    public long getBetsSum() {
        return betsSum;
    }

    public void setBetsSum(long betsSum) {
        this.betsSum = betsSum;
    }

    @Override
    public long getWinSum() {
        return winSum;
    }

    public void setWinSum(long winSum) {
        this.winSum = winSum;
    }

    @Override
    public List<IBgPlace> getPlaces() {
        return places;
    }

    public void setPlaces(List<IBgPlace> places) {
        this.places = places;
    }

    @Override
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public int getPlayersNumber() {
        return playersNumber;
    }

    public void setPlayersNumber(int playersNumber) {
        this.playersNumber = playersNumber;
    }

    @Override
    public String getWinnerName() {
        return winnerName;
    }

    public void setWinnerName(String winnerName) {
        this.winnerName = winnerName;
    }

    @Override
    public long getRoundId() {
        return roundId;
    }

    public void setRoundId(long roundId) {
        this.roundId = roundId;
    }

    @Override
    public long getRoundStartDate() {
        return roundStartDate;
    }

    public void setRoundStartDate(long roundStartDate) {
        this.roundStartDate = roundStartDate;
    }

    @Override
    public String getPrivateRoomId() {
        return privateRoomId;
    }

    public void setPrivateRoomId(String privateRoomId) {
        this.privateRoomId = privateRoomId;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeLong(buyIn, true);
        output.writeLong(winAmount, true);
        output.writeLong(betsSum, true);
        output.writeLong(winSum, true);
        kryo.writeClassAndObject(output, places);
        output.writeString(status);
        output.writeInt(playersNumber, true);
        output.writeString(winnerName);
        output.writeLong(roundId, true);
        output.writeLong(roundStartDate, true);
        output.writeString(privateRoomId);
    }


    @SuppressWarnings("unchecked")
    @Override
    public void read(Kryo kryo, Input input) {
        byte version = input.readByte();
        buyIn = input.readLong(true);
        winAmount = input.readLong(true);
        betsSum = input.readLong(true);
        winSum = input.readLong(true);
        places = (List<IBgPlace>) kryo.readClassAndObject(input);
        status = input.readString();
        playersNumber = input.readInt(true);
        winnerName = input.readString();
        roundId = input.readLong(true);
        roundStartDate = input.readLong(true);
        if (version > 0) {
            privateRoomId = input.readString();
        }
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", BattlegroundRoundInfo.class.getSimpleName() + "[", "]")
                .add("buyIn=" + buyIn)
                .add("winAmount=" + winAmount)
                .add("betsSum=" + betsSum)
                .add("winSum=" + winSum)
                .add("places=" + places)
                .add("status='" + status + "'")
                .add("playersNumber=" + playersNumber)
                .add("winnerName='" + winnerName + "'")
                .add("roundId=" + roundId)
                .add("roundStartDate=" + roundStartDate)
                .add("privateRoomId=" + privateRoomId)
                .toString();
    }
}
