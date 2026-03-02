package com.abs.casino.gs.socket.mq;

import com.abs.casino.battleground.messages.RoomWasDeactivated;
import com.abs.casino.common.cache.data.bank.BankInfo;
import com.abs.casino.common.util.string.StringUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.apache.logging.log4j.LogManager;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class DeactivatedRoomNotificationTask implements Runnable{
    private static final org.apache.logging.log4j.Logger LOG = LogManager.getLogger(DeactivatedRoomNotificationTask.class);
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private String privateRoomId;
    private String reason;
    private BankInfo bankInfo;
    private final RestTemplate restTemplate;

    public DeactivatedRoomNotificationTask(String privateRoomId, String reason, BankInfo bankInfo) {
        this.privateRoomId = privateRoomId;
        this.reason = reason;
        this.bankInfo = bankInfo;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public void run() {
        try {
            if (privateRoomId == null || reason == null) {
                return;
            }
            String url = bankInfo.getNotificationRoomWasDeactivatedUrl();
            String passKey = bankInfo.getNotificationRoomWasDeactivatedAuthPass();
            if (url == null || passKey == null) {
                LOG.warn("For notification url and passkey params are required");
                return;
            }
            String hostAuthCredential = bankInfo.getHostAuthCredential();
            RoomWasDeactivated roomWasDeactivated = new RoomWasDeactivated(privateRoomId, reason);
            LOG.info("Request to url:{} bankId:{} privateRoomId:{} reason:{}", url, bankInfo.getId(), privateRoomId, reason);
            ResponseEntity<String> response = restTemplate.postForEntity(url, buildEntity(roomWasDeactivated, passKey,reason, passKey, hostAuthCredential), String.class);
            LOG.info("Response from url:{} bankId:{} is:{}", url, bankInfo.getId(), response);
        } catch (Exception e) {
            LOG.error("Can't send Deactivated Room Notification privateRoomId={}", privateRoomId, e);
        }
    }

    private HttpEntity<String> buildEntity(RoomWasDeactivated info, String privateRoomId, String reason, String passKey, String authCredential) {
        return new HttpEntity<>(gson.toJson(info), getHeaders(buildHash(privateRoomId, reason, passKey), authCredential));
    }

    private HttpHeaders getHeaders(String hash, String authCredential) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.set("hash", hash);
        if (!StringUtils.isTrimmedEmpty(authCredential)) {
            httpHeaders.set("Authorization", authCredential);
        }
        return httpHeaders;
    }

    private String buildHash(String privateRoomId, String reason, String passKey) {
        HmacUtils hashEncoder = new HmacUtils(HmacAlgorithms.HMAC_SHA_256, passKey);
        String sb = privateRoomId +
                reason +
                passKey;
        return hashEncoder.hmacHex(sb);
    }
}
