package com.dgphoenix.casino.gs.managers.payment.transfer.processor;

import com.dgphoenix.casino.account.AccountManager;
import com.abs.casino.battleground.messages.RoomStartedInfo;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.gs.managers.payment.IStartGameProcessor;
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

import java.util.concurrent.TimeUnit;

public class MQBStartGameProcessor implements IStartGameProcessor {
    private static final Logger LOG = LogManager.getLogger(MQBStartGameProcessor.class);
    private static final long TIME_OUT = TimeUnit.SECONDS.toMillis(5);

    private final BattlegroundService battlegroundService;
    private final AccountManager accountManager;
    private final RestTemplate restTemplate;
    private final BankInfoCache bankInfoCache;
    private final Gson gson;

    public MQBStartGameProcessor() {
        this.battlegroundService = ApplicationContextHelper.getApplicationContext().getBean(BattlegroundService.class);
        this.accountManager = ApplicationContextHelper.getApplicationContext().getBean(AccountManager.class);
        this.restTemplate = new RestTemplate();
        SimpleClientHttpRequestFactory requestFactory = (SimpleClientHttpRequestFactory) restTemplate.getRequestFactory();
        requestFactory.setConnectTimeout((int) TIME_OUT);
        requestFactory.setReadTimeout((int) TIME_OUT);
        this.bankInfoCache = BankInfoCache.getInstance();
        this.gson = new Gson();
    }

    @Override
    public void process(GameSession gameSession, AccountInfo accountInfo, SessionInfo sessionInfo) throws CommonException {
        try {
            if (sessionInfo == null || gameSession == null || !battlegroundService.isBattlegroundGame(gameSession.getGameId())) {
                return;
            }
            BankInfo bankInfo = bankInfoCache.getBankInfo(gameSession.getBankId());
            String url = bankInfo.getNotificationStartGameProcessorUrl();
            String passKey = bankInfo.getNotificationStartGameAuthPass();
            if (url == null || passKey == null) {
                LOG.warn("For MQBCloseGameProcessor url and passkey params are required");
                return;
            }
            String hostAuthCredential = bankInfo.getHostAuthCredential();
            String privateRoomId = battlegroundService.getBattlegroundPrivateRoomIdIfExist(gameSession);
            if (StringUtils.isTrimmedEmpty(privateRoomId)) {
                LOG.info("No find btg private room for gameSession: {}", gameSession.getId());
                return;
            }
            RoomStartedInfo roomStartedInfo = new RoomStartedInfo(sessionInfo.getSessionId(), privateRoomId, accountManager.getExtId(gameSession.getAccountId()), gameSession.getStartTime());
            LOG.info("Request to url:{} bankId:{} is:{}", url, accountInfo.getBankId(), roomStartedInfo);
            ResponseEntity<String> response = restTemplate.postForEntity(url, buildEntity(roomStartedInfo, passKey, hostAuthCredential), String.class);
            LOG.info("Response from url:{} bankId:{} is:{}", url, accountInfo.getBankId(), response);
        } catch (Exception e) {
            LOG.debug("Enable process start game processor", e);
        }
    }

    private HttpEntity<String> buildEntity(RoomStartedInfo info, String passKey, String authCredential) {
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

    private String buildHash(RoomStartedInfo info, String passKey) {
        HmacUtils hashEncoder = new HmacUtils(HmacAlgorithms.HMAC_SHA_256, passKey);
        String sb = info.getSid() +
                info.getPrivateRoomId() +
                info.getUserId() +
                info.getStartTime() +
                passKey;

        return hashEncoder.hmacHex(sb);
    }
}
