package com.betsoft.casino.mp.web;

import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.service.ILobbySessionService;
import com.betsoft.casino.mp.service.ISocketService;

/**
 * User: flsh
 * Date: 21.12.2021.
 */
public interface ILobbySocketClient<SOCKET_SERVICE extends ISocketService, LOBBY_SESSION_SERVICE extends ILobbySessionService>  extends ISocketClient {
    void startBalanceUpdater(SOCKET_SERVICE socketService, int serverId, String sessionId,
                             LOBBY_SESSION_SERVICE lobbySessionService);

    void stopBalanceUpdater();

    void startTouchSession(SOCKET_SERVICE socketService, int serverId, String sessionId);

    void stopTouchSession();

    GameType getGameType();

    void setGameType(GameType gameType);

    MoneyType getMoneyType();

    void setMoneyType(MoneyType moneyType);

    IPlayerInfo getPlayerInfo();

    void setPlayerInfo(IPlayerInfo playerInfo);

    String getLang();

    void setLang(String lang);

    String getNickname();

    void setNickname(String nickname);

    void setLoggedIn(boolean loggedIn);

    boolean isPrivateRoom();

    void setPrivateRoom(boolean privateRoom);
}
