package com.betsoft.casino.mp.service;

import com.betsoft.casino.mp.model.bots.dto.BotLogInResult;
import com.betsoft.casino.mp.model.bots.dto.BotLogOutResult;
import com.betsoft.casino.mp.model.bots.dto.BotStatusResult;
import com.betsoft.casino.mp.model.bots.dto.BotsMap;
import com.abs.casino.common.exception.CommonException;

/**
 * User: flsh
 * Date: 14.07.2022.
 */
public interface IBotServiceClient {
    BotLogInResult logIn(int botServerId, long botId, String userName, String password, long bankId, long gameId, long buyIn, String botNickname,
                          long roomId, String lang, String enterLobbyWsUrl, String openRoomWSUrl, long expiresAt, double shootsRate, double bulletsRate) throws CommonException;

    BotStatusResult getStatusForNewBot(String userName, String password, String botNickName, long bankId, long gameId) throws CommonException;

    BotStatusResult getStatus(long botId, String sessionId, String botNickname, long roomId) throws CommonException;

    BotStatusResult confirmNextRoundBuyIn(long botId, String sessionId, String botNickname, long roomId, long roundId) throws CommonException;

    BotLogOutResult logOut(long botId, String sessionId, String botNickname, long roomId) throws CommonException;

    boolean isBotServiceEnabled();

    void removeBot(long botId, String botNickName, long roomId) throws CommonException;

    String getDetailBotInfo(long botId, String botNickName) throws CommonException;

    BotsMap getBotsMap() throws CommonException;
}
