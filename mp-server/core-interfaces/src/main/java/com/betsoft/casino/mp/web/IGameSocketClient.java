package com.betsoft.casino.mp.web;

import com.betsoft.casino.mp.model.GameType;
import com.betsoft.casino.mp.model.ISeat;
import com.dgphoenix.casino.common.web.statistics.IntervalStatistics;
import org.apache.logging.log4j.Logger;

/**
 * User: flsh
 * Date: 19.05.2020.
 */
public interface IGameSocketClient extends ISocketClient {

    Long getRoomId();

    void setRoomId(Long roomId);

    void setAccountId(Long accountId);

    void setBankId(Long bankId);

    int getSeatNumber();

    void setSeatNumber(int seatNumber);

    Integer getLastRequestId();

    void setLastRequestId(Integer lastRequestId);

    void setSessionId(String sessionId);

    void setServerId(int serverId);

    long getEnterDate();

    void setEnterDate(long enterDate);

    void setSeat(ISeat seat);

    ISeat getSeat();

    GameType getGameType();

    void setGameType(GameType gameType);

    void setDisconnected();

    void setLog(Logger logger);

    void setPrivateRoom(boolean privateRoom);

    boolean isPrivateRoom();

    void setOwner(boolean owner);

    boolean isOwner();

    void setKicked(boolean isKicked);

    boolean isKicked();

    String getNickname();

    void setNickname(String nickname);

    default IntervalStatistics getLatencyStatistic() {
        return null;
    }

    default IntervalStatistics getPingLatencyStatistic() {
        return null;
    }

}
