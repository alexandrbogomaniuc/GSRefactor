package com.abs.casino.kafka.dto;

import com.dgphoenix.casino.kafka.dto.PlaceDto;

import java.util.List;

public class BattlegroundRoundInfoDto {
    private long buyIn;
    private long winAmount;
    private long betsSum;
    private long winSum;
    private List<PlaceDto> places;
    private String status;
    private int playersNumber;
    private String winnerName;
    private long roundId;
    private long roundStartDate;
    private String privateRoomId;

    public BattlegroundRoundInfoDto() {}

    public BattlegroundRoundInfoDto(long buyIn,
            long winAmount,
            long betsSum,
            long winSum,
            List<PlaceDto> places,
            String status,
            int playersNumber,
            String winnerName,
            long roundId,
            long roundStartDate,
            String privateRoomId) {
        super();
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

    public long getBuyIn() {
        return buyIn;
    }

    public long getWinAmount() {
        return winAmount;
    }

    public long getBetsSum() {
        return betsSum;
    }

    public long getWinSum() {
        return winSum;
    }

    public List<PlaceDto> getPlaces() {
        return places;
    }

    public String getStatus() {
        return status;
    }

    public int getPlayersNumber() {
        return playersNumber;
    }

    public String getWinnerName() {
        return winnerName;
    }

    public long getRoundId() {
        return roundId;
    }

    public long getRoundStartDate() {
        return roundStartDate;
    }

    public String getPrivateRoomId() {
        return privateRoomId;
    }

    public void setBuyIn(long buyIn) {
        this.buyIn = buyIn;
    }

    public void setWinAmount(long winAmount) {
        this.winAmount = winAmount;
    }

    public void setBetsSum(long betsSum) {
        this.betsSum = betsSum;
    }

    public void setWinSum(long winSum) {
        this.winSum = winSum;
    }

    public void setPlaces(List<PlaceDto> places) {
        this.places = places;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setPlayersNumber(int playersNumber) {
        this.playersNumber = playersNumber;
    }

    public void setWinnerName(String winnerName) {
        this.winnerName = winnerName;
    }

    public void setRoundId(long roundId) {
        this.roundId = roundId;
    }

    public void setRoundStartDate(long roundStartDate) {
        this.roundStartDate = roundStartDate;
    }

    public void setPrivateRoomId(String privateRoomId) {
        this.privateRoomId = privateRoomId;
    }


}
