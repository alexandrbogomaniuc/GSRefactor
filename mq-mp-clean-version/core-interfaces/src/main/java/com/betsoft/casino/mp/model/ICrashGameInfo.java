package com.betsoft.casino.mp.model;

import com.betsoft.casino.mp.model.battleground.ITransportObserver;
import com.betsoft.casino.mp.model.onlineplayer.Friend;
import com.betsoft.casino.utils.ITransportObject;

import java.util.List;

public interface ICrashGameInfo extends ITransportObject {
    long getRoomId();

    void addBet(String betId, String name, long amount, boolean auto, double mult, long ejectTime, Double autoPlayMultiplier, boolean isReserved);

    void addBet(String betId, String name, long amount, boolean auto, double mult, long ejectTime, Double autoPlayMultiplier);

    void setState(RoomState state);

    RoomState getState();

    void setCanceledBetAmount(long canceledBetAmount);

    void setCanceledBetAmount(String nickname, long canceledBetAmount);

    void setMaxPlayerProfitInRound(Long maxPlayerProfitInRound);

    void setTotalPlayersProfitInRound(Long totalPlayersProfitInRound);

    void setMaxMultiplier(Double maxMultiplier);

    void setBuyIn(long buyIn);

    void setMaxRoomPlayers(int maxRoomPlayers);

    void setPending(boolean isPending);

    public void setCurrentMult(Double currentMult);

    public void setCrash(Boolean crash);

    void setTimeSpeedMult(double timeSpeedMult);

    void setAllEjectedTime(Long allEjectedTime);

    void setObservers(List<ITransportObserver> observers);

    void setFriends(List<Friend> friends);

    void setOwner(Boolean isOwner);

    void setMinSeats(Short minSeats);
}
