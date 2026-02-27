package com.dgphoenix.casino.gs.managers.payment.transfer.processor;

import com.abs.casino.battleground.messages.GameRoundEntry;
import com.abs.casino.battleground.messages.MPGameSessionCloseInfo;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.session.ClientType;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.gs.managers.ICloseGameProcessor;
import com.dgphoenix.casino.gs.socket.mq.BattlegroundService;
import com.google.gson.Gson;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

public class MQBCloseGameProcessor implements ICloseGameProcessor {
    private static final Logger LOG = LogManager.getLogger(MQBCloseGameProcessor.class);

    private final BattlegroundService battlegroundService;
    private final RestTemplate restTemplate;
    private final BankInfoCache bankInfoCache;
    private final Gson gson;

    public MQBCloseGameProcessor() {
        this.battlegroundService = ApplicationContextHelper.getApplicationContext().getBean(BattlegroundService.class);
        this.restTemplate = new RestTemplate();
        SimpleClientHttpRequestFactory requestFactory = (SimpleClientHttpRequestFactory) restTemplate.getRequestFactory();
        requestFactory.setConnectTimeout((int) TIME_OUT);
        requestFactory.setReadTimeout((int) TIME_OUT);
        this.bankInfoCache = BankInfoCache.getInstance();
        this.gson = new Gson();
    }

    @Override
    public void process(GameSession gameSession, AccountInfo accountInfo, ClientType clientType) throws CommonException {
        if (gameSession == null || !battlegroundService.isBattlegroundGame(gameSession.getGameId())) {
            return;
        }
        BankInfo bankInfo = bankInfoCache.getBankInfo(gameSession.getBankId());
        String url = bankInfo.getNotificationCloseGameProcessorUrl();
        String passKey = bankInfo.getNotificationCloseGameAuthPass();
        if (url == null || passKey == null) {
            LOG.warn("For MQBCloseGameProcessor url and passkey params are required");
            return;
        }
        String hostAuthCredential = bankInfo.getHostAuthCredential();
        MPGameSessionCloseInfo mpGameSessionCloseInfo = battlegroundService.getParticipationNicknamesByGameSession(gameSession);
        LOG.info("Request to url:{} bankId:{} is:{}", url, accountInfo.getBankId(), mpGameSessionCloseInfo);
        ResponseEntity<String> response = restTemplate.postForEntity(url, buildEntity(mpGameSessionCloseInfo, passKey, hostAuthCredential), String.class);
        LOG.info("Response from url:{} bankId:{} is:{}", url, accountInfo.getBankId(), response);
    }

    private HttpEntity<String> buildEntity(MPGameSessionCloseInfo info, String passKey, String authCredential) {
        return new HttpEntity<>(gson.toJson(info), getHeaders(buildHash(info, passKey), authCredential));
    }

    private HttpHeaders getHeaders(String hash, String authCredential) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.set("hash", hash);
        httpHeaders.set("User-Agent", "Apache-HttpClient/4.5.13");
        if (!StringUtils.isTrimmedEmpty(authCredential)) {
            httpHeaders.set("Authorization", authCredential);
        }
        return httpHeaders;
    }

    private String buildHash(MPGameSessionCloseInfo request, String passKey) {
        HmacUtils hashEncoder = new HmacUtils(HmacAlgorithms.HMAC_SHA_256, passKey);
        StringBuilder sb = new StringBuilder();
        sb.append(request.getGameId());
        sb.append(request.getGameSessionId());
        sb.append(request.getSid());
        sb.append(request.getPrivateRoomId());
        if (request.getGameRounds() != null) {
            for (GameRoundEntry gameRound : request.getGameRounds()) {
                sb.append(gameRound.getRoundId());
                if (gameRound.getUsers() != null) {
                    for (String user : gameRound.getUsers()) {
                        sb.append(user);
                    }
                }
                sb.append(gameRound.getStartTime());
                sb.append(gameRound.getEndTime());
            }
        }
        sb.append(request.getUserId());
        sb.append(request.getStartTime());
        sb.append(request.getEndTime());
        sb.append(passKey);

        return hashEncoder.hmacHex(sb.toString());
    }
}
