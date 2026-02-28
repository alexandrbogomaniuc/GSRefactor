package com.dgphoenix.casino.common.web;

import com.abs.casino.common.web.ResultType;

import java.util.List;

public class JsonResultForLeaderboardUrls {
    private final String error;
    private final ResultType result;
    private final List<String> urls;

    private JsonResultForLeaderboardUrls(ResultType result, List<String> urls, String error) {
        this.result = result;
        this.urls = urls;
        this.error = error;
    }

    public static JsonResultForLeaderboardUrls createSuccessResult(List<String> urls) {
        return new JsonResultForLeaderboardUrls(ResultType.OK, urls, null);
    }

    public static JsonResultForLeaderboardUrls createErrorResult(String error) {
        return new JsonResultForLeaderboardUrls(ResultType.ERROR, null, error);
    }

    public String getError() {
        return error;
    }

    public String getResult() {
        return result.toString().toLowerCase();
    }

    public List<String> getUrls() {
        return urls;
    }
}
