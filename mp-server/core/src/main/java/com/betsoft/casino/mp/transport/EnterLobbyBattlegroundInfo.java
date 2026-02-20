package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.IEnterLobbyBattlegroundInfo;

import java.io.Serializable;
import java.util.List;
import java.util.StringJoiner;

/**
 * User: flsh
 * Date: 22.07.2021.
 */
public class EnterLobbyBattlegroundInfo implements IEnterLobbyBattlegroundInfo, Serializable {
    private List<Long> buyIns;
    private Long alreadySeatRoomId;
    private String startGameUrl;
    private String roomState;
    private boolean buyInConfirmed;

    public EnterLobbyBattlegroundInfo(List<Long> buyIns, Long alreadySeatRoomId, String startGameUrl, String roomState,
                                      boolean buyInConfirmed) {
        this.buyIns = buyIns;
        this.alreadySeatRoomId = alreadySeatRoomId;
        this.startGameUrl = startGameUrl;
        this.roomState = roomState;
        this.buyInConfirmed = buyInConfirmed;
    }

    @Override
    public List<Long> getBuyIns() {
        return buyIns;
    }

    @Override
    public void setBuyIns(List<Long> buyIns) {
        this.buyIns = buyIns;
    }

    @Override
    public Long getAlreadySeatRoomId() {
        return alreadySeatRoomId;
    }

    @Override
    public void setAlreadySeatRoomId(Long alreadySeatRoomId) {
        this.alreadySeatRoomId = alreadySeatRoomId;
    }

    @Override
    public String getStartGameUrl() {
        return startGameUrl;
    }

    @Override
    public void setStartGameUrl(String startGameUrl) {
        this.startGameUrl = startGameUrl;
    }

    @Override
    public String getRoomState() {
        return roomState;
    }

    @Override
    public void setRoomState(String roomState) {
        this.roomState = roomState;
    }

    @Override
    public boolean isBuyInConfirmed() {
        return buyInConfirmed;
    }

    @Override
    public void setBuyInConfirmed(boolean buyInConfirmed) {
        this.buyInConfirmed = buyInConfirmed;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", EnterLobbyBattlegroundInfo.class.getSimpleName() + "[", "]")
                .add("buyIns=" + buyIns)
                .add("alreadySeatRoomId=" + alreadySeatRoomId)
                .add("startGameUrl='" + startGameUrl + "'")
                .add("roomState='" + roomState + "'")
                .add("buyInConfirmed=" + buyInConfirmed)
                .toString();
    }
}
