package com.betsoft.casino.bots.mqb;

import java.io.IOException;
import java.util.Collections;

import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.dgphoenix.casino.common.exception.CommonException;
import com.abs.casino.common.util.string.StringUtils;

/**
 * User: flsh
 * Date: 13.07.2022.
 */
public class MQBApiClient implements IApiClient {
    private static final Logger LOG = LogManager.getLogger(MQBApiClient.class);

    private static final String GET_START_GAME_URL = "getStartGameUrl";
    private static final String GET_BALANCES_URL = "getBalances";
    private static final String LOGOUT_URL = "logout";
    private static final String FINISH_GMA_SESSION_URL = "finishGameSession";
    private final String getMqbSiteBotApiUrl;
    private final String secretMqbAPIKey;
    private final String basicAuthPassword;
    private final RestTemplate restTemplate;

    public MQBApiClient(String getMqbSiteBotApiUrl, String secretMqbAPIKey, String basicAuthPassword) {
        this.getMqbSiteBotApiUrl = getMqbSiteBotApiUrl;
        this.secretMqbAPIKey = secretMqbAPIKey;
        this.basicAuthPassword = basicAuthPassword;
        this.restTemplate = getRestTemplate();

    }

    private RestTemplate getRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        ClientHttpRequestInterceptor interceptor = new ClientHttpRequestInterceptor() {
            @Override
            public ClientHttpResponse intercept(HttpRequest request,
                                                byte[] body,
                                                ClientHttpRequestExecution execution)
                    throws IOException {
                HttpHeaders headers = request.getHeaders();
                headers.set("User-Agent", "curl/8.6.0");
                headers.set("Accept", "*/*");
                return execution.execute(request, body);
            }
        };

        restTemplate.setInterceptors(Collections.singletonList(interceptor));
        return restTemplate;
    }

    public String getMqbSiteBotApiUrl() {
        return this.getMqbSiteBotApiUrl;
    }

    @Override
    public LoginResponse login(String userName, String password, long bankId, long gameId, long buyIn, String currency) {
        LOG.debug("login: userName={}, password=******, bankId={}, gameId={}, buyIn={}, currency={}", userName, bankId, gameId, buyIn, currency);
        HttpEntity<MultiValueMap<String, String>> requestEntity = buildLoginRequestEntity(userName, password, bankId, gameId, buyIn, currency);
        String url = getMqbSiteBotApiUrl + GET_START_GAME_URL;
        LOG.debug("login: userName={}, url={}", userName, url);
        ResponseEntity<LoginResponse> responseEntity = restTemplate.postForEntity(url, requestEntity, LoginResponse.class);
        LoginResponse loginResponse = responseEntity.getBody();
        LOG.debug("login: userName={}, loginResponse={}", userName, loginResponse);
        return loginResponse;
    }

    @Override
    public void logout(String userName, String token) throws CommonException {
        LOG.debug("logout: userName={}, token={}", userName, token);
        HttpEntity<MultiValueMap<String, String>> requestEntity = buildLogoutRequestEntity(token);
        String url = getMqbSiteBotApiUrl + LOGOUT_URL;
        LOG.debug("logout: userName={}, url={}, requestEntity={}", userName, url, requestEntity);
        ResponseEntity<OkResponse> responseEntity = restTemplate.postForEntity(url, requestEntity, OkResponse.class);
        OkResponse response = responseEntity.getBody();
        LOG.debug("logout: userName={}, response={}", userName, response);
        if (response == null || !response.isSuccess()) {
            throw new CommonException("logout failed");
        }
    }

    @Override
    public FinishGameSessionResponse finishGameSession(String userName, String password, String sessionId) throws CommonException {
        LOG.debug("finishGameSession: userName={}, sessionId={}", userName, sessionId);
        HttpEntity<MultiValueMap<String, String>> requestEntity = buildFinishGameSessionRequestEntity(userName, password, sessionId);
        String url = getMqbSiteBotApiUrl + FINISH_GMA_SESSION_URL;
        LOG.debug("finishGameSession: userName={}, sessionId={}, url={}, requestEntity={}", userName, sessionId, url, requestEntity);
        ResponseEntity<FinishGameSessionResponse> responseEntity = restTemplate.postForEntity(url, requestEntity, FinishGameSessionResponse.class);
        FinishGameSessionResponse response = responseEntity.getBody();
        LOG.debug("finishGameSession: userName={}, response={}", userName, response);
        if (response == null) {
            throw new CommonException("finishGameSession failed");
        }
        return response;
    }

    private HttpEntity<MultiValueMap<String, String>> buildLoginRequestEntity(String userName, String password, long bankId, long gameId,
                                                                              long buyIn, String currency) {
        MultiValueMap<String, String> httpBody = new LinkedMultiValueMap<>();
        httpBody.add("userName", userName);
        httpBody.add("password", password);
        httpBody.add("bankId", String.valueOf(bankId));
        httpBody.add("gameId", String.valueOf(gameId));
        httpBody.add("buyIn", String.valueOf(buyIn));
        httpBody.add("currency", currency);

        HttpHeaders httpHeaders = getHeaders(userName + password + bankId + gameId + buyIn + currency + secretMqbAPIKey);

        return new HttpEntity<>(httpBody, httpHeaders);
    }

    @Override
    public GetBalancesResponse getBalance(String externalId, long bankId) throws CommonException {
        LOG.debug("getBalances: externalId={}, bankId={}", externalId, bankId);
        HttpEntity<MultiValueMap<String, String>> requestEntity = buildGetBalanceRequestEntity(externalId, bankId);
        String url = getMqbSiteBotApiUrl + GET_BALANCES_URL;
        LOG.debug("getBalances: externalId={}, url={}, requestEntity={}", externalId, url, requestEntity);
        ResponseEntity<GetBalancesResponse> responseEntity = restTemplate.postForEntity(url, requestEntity, GetBalancesResponse.class);
        GetBalancesResponse getBalancesResponse = responseEntity.getBody();
        LOG.debug("getBalances: externalId={}, getBalancesResponse={}", externalId, getBalancesResponse);
        return getBalancesResponse;
    }

    @Override
    public boolean isFake() {
        return false;
    }

    private HttpEntity<MultiValueMap<String, String>> buildGetBalanceRequestEntity(String externalId, long bankId) {
        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.add("localId", externalId);
        multiValueMap.add("bankId", String.valueOf(bankId));
        return new HttpEntity<>(multiValueMap, getHeaders(externalId + secretMqbAPIKey));
    }

    private HttpEntity<MultiValueMap<String, String>> buildLogoutRequestEntity(String token) {
        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.add("token", token);
        return new HttpEntity<>(multiValueMap, getHeaders(token + secretMqbAPIKey));
    }

    private HttpEntity<MultiValueMap<String, String>> buildFinishGameSessionRequestEntity(String userName, String password, String sid) {
        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.add("userName", userName);
        multiValueMap.add("password", password);
        multiValueMap.add("sid", sid);
        multiValueMap.add("privateRoomId", null);
        return new HttpEntity<>(multiValueMap, getHeaders(sid + secretMqbAPIKey));
    }

    private HttpHeaders getHeaders(String toHash) {
        HmacUtils hashEncoder = new HmacUtils(HmacAlgorithms.HMAC_SHA_256, secretMqbAPIKey);
        String hashValue = hashEncoder.hmacHex(toHash);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        httpHeaders.set("hash", hashValue);
        if (!StringUtils.isTrimmedEmpty(basicAuthPassword)) {
            httpHeaders.set("Authorization", "Basic " + basicAuthPassword);
        }
        httpHeaders.set("User-Agent", "curl/8.6.0");
        return httpHeaders;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MQBApiClient [");
        sb.append("getMqbSiteBotApiUrl='").append(getMqbSiteBotApiUrl).append('\'');
        sb.append(']');
        return sb.toString();
    }
}
