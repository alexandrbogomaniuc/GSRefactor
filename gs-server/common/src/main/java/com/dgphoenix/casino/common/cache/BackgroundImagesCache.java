package com.abs.casino.common.cache;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BackgroundImagesCache {
    private static final Logger LOG = LogManager.getLogger(BackgroundImagesCache.class);

    private final Map<Long, Boolean> cache = new ConcurrentHashMap<>();
    private final RestTemplate restTemplate = new RestTemplate();

    public BackgroundImagesCache() {
        restTemplate.setErrorHandler(new ErrorHandler());
    }

    public boolean contains(long gameId) {
        return cache.containsKey(gameId);
    }

    public boolean get(long gameId) {
        return cache.get(gameId);
    }

    public void invalidate() {
        cache.clear();
    }

    public boolean load(long gameId, String lobbyUrl) {
        String url = lobbyUrl + "/images/backgrounds/" + gameId + "/bg.jpg";
        boolean result;

        LOG.info("Checking background at {}", url);
        try {
            result = restTemplate.exchange(url, HttpMethod.HEAD, HttpEntity.EMPTY, Void.class)
                    .getStatusCode()
                    .is2xxSuccessful();
        } catch (RestClientException e) {
            LOG.error("Network error, result is not cached", e);
            return false;
        }

        LOG.info("Caching check result for {}: {}", url, result);
        cache.put(gameId, result);
        return result;
    }

    @Override
    public String toString() {
        return "BackgroundImagesCache{" +
                "cache=" + cache +
                '}';
    }

    private static class ErrorHandler extends DefaultResponseErrorHandler {

        @Override
        protected boolean hasError(HttpStatus statusCode) {
            if (HttpStatus.NOT_FOUND.equals(statusCode)) {
                return false;
            }
            return super.hasError(statusCode);
        }

        @Override
        public void handleError(ClientHttpResponse response) throws IOException {
            if (!HttpStatus.NOT_FOUND.equals(response.getStatusCode())) {
                super.handleError(response);
            }
        }
    }
}
