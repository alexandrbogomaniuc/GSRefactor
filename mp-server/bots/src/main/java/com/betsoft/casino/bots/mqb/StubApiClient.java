package com.betsoft.casino.bots.mqb;

import com.dgphoenix.casino.common.exception.CommonException;

import java.util.ArrayList;

public class StubApiClient implements IApiClient {
    private static final String BG_LAUNCHER = "/battlegroundstartgamev2.do?";
    private static final String TOKEN = "token=";
    private static final String GAME_ID = "gameId=";
    private static final String HOME_URL = "homeUrl=";
    private static final String LANG = "lang=";
    private static final String BUY_IN = "buyIn=";
    private static final String BANK_ID = "bankId=";
    private static final String MQ_PASS = "pass=";
    private final String domainUrl;

    public StubApiClient(String domainUrl) {
        this.domainUrl = domainUrl;
    }

    @Override
    public LoginResponse login(String userName, String password, long bankId, long gameId, long buyIn, String currency) {
        return new LoginResponse("OK", buildBgStartURL(gameId, buyIn, bankId, "homeUrl", userName), 100_000L, userName, "localId");
    }

    public String buildBgStartURL(Long gameId, Long buyIn, Long bankId, String homeUrl, String token) {
        return domainUrl + BG_LAUNCHER +
                TOKEN + token + "&" +
                BANK_ID + bankId + "&" +
                GAME_ID + gameId + "&" +
                LANG + "en" + "&" +
                BUY_IN + buyIn + "&" +
                MQ_PASS + "XYCGX5" + "&" +
                HOME_URL + homeUrl;
    }

    @Override
    public void logout(String userName, String token) {
        //stub ignore
    }

    @Override
    public FinishGameSessionResponse finishGameSession(String userName, String password, String sessionId) throws CommonException {
        return new FinishGameSessionResponse(true, new ArrayList<>());
    }

    @Override
    public GetBalancesResponse getBalance(String externalId, long bankId) {
        return new GetBalancesResponse("OK", 100_100L);
    }

    @Override
    public boolean isFake() {
        return true;
    }

}
