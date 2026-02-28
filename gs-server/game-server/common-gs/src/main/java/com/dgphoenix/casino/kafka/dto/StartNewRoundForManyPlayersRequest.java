package com.abs.casino.kafka.dto;

import com.dgphoenix.casino.kafka.dto.RoundPlayerDto;

import com.dgphoenix.casino.kafka.dto.KafkaRequest;

import java.util.List;

public class StartNewRoundForManyPlayersRequest implements KafkaRequest {
    private List<RoundPlayerDto> roundPlayers;
    private long roomId;
    private long roomRoundId;
    private long roundStartDate;
    private boolean battlegroundRoom;
    private long stakeOrBuyInAmount;

    public StartNewRoundForManyPlayersRequest() {}

    public StartNewRoundForManyPlayersRequest(List<RoundPlayerDto> roundPlayers,
            long roomId,
            long roomRoundId,
            long roundStartDate,
            boolean battlegroundRoom,
            long stakeOrBuyInAmount) {
        this.roundPlayers = roundPlayers;
        this.roomId = roomId;
        this.roomRoundId = roomRoundId;
        this.roundStartDate = roundStartDate;
        this.battlegroundRoom = battlegroundRoom;
        this.stakeOrBuyInAmount = stakeOrBuyInAmount;
    }

    public List<RoundPlayerDto> getRoundPlayers() {
        return roundPlayers;
    }

    public long getRoomId() {
        return roomId;
    }

    public long getRoomRoundId() {
        return roomRoundId;
    }

    public long getRoundStartDate() {
        return roundStartDate;
    }

    public boolean isBattlegroundRoom() {
        return battlegroundRoom;
    }

    public long getStakeOrBuyInAmount() {
        return stakeOrBuyInAmount;
    }

    public void setRoundPlayers(List<RoundPlayerDto> roundPlayers) {
        this.roundPlayers = roundPlayers;
    }

    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }

    public void setRoomRoundId(long roomRoundId) {
        this.roomRoundId = roomRoundId;
    }

    public void setRoundStartDate(long roundStartDate) {
        this.roundStartDate = roundStartDate;
    }

    public void setBattlegroundRoom(boolean battlegroundRoom) {
        this.battlegroundRoom = battlegroundRoom;
    }

    public void setStakeOrBuyInAmount(long stakeOrBuyInAmount) {
        this.stakeOrBuyInAmount = stakeOrBuyInAmount;
    }
}
