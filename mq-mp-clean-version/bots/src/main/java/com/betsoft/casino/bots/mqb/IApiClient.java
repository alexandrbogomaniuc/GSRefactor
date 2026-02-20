package com.betsoft.casino.bots.mqb;

import com.dgphoenix.casino.common.exception.CommonException;

public interface IApiClient {
    LoginResponse login(String userName, String password, long bankId, long gameId, long buyIn, String currency);
    void logout(String userName, String token) throws CommonException;
    FinishGameSessionResponse finishGameSession(String userName, String password, String sessionId) throws CommonException;
    GetBalancesResponse getBalance(String externalId, long bankId) throws CommonException;
    boolean isFake();
}
