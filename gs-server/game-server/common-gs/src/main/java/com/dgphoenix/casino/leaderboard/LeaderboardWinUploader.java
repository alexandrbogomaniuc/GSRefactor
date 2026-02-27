package com.dgphoenix.casino.leaderboard;

import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.exception.TransportException;
import com.abs.casino.common.mp.LeaderboardResult;
import com.abs.casino.common.mp.LeaderboardResultWrapper;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.abs.casino.common.util.web.HttpClientConnection;
import com.dgphoenix.casino.gs.managers.payment.wallet.WalletProtocolFactory;
import com.google.gson.Gson;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class LeaderboardWinUploader {
    private static final Logger LOG = LogManager.getLogger(LeaderboardWinUploader.class);
    private static final String RESPONSE_OK = "OK";
    private static final String SEPARATOR = "|";

    private static final Gson gson = new Gson();

    public boolean upload(long bankId, long leaderboardId, String result) {
        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
        String url = bankInfo.getLeaderboardResultsUrl();
        if (!StringUtils.isTrimmedEmpty(url)) {
            String secretKey = WalletProtocolFactory.getInstance().isWalletBank(bankInfo.getId())
                    ? bankInfo.getAuthPassword()
                    : bankInfo.getCTPassKey();
            if (!StringUtils.isTrimmedEmpty(secretKey)) {
                String hash = DigestUtils
                        .sha256Hex(secretKey + SEPARATOR + bankInfo.getExternalBankId() + SEPARATOR + leaderboardId);
                if (bankInfo.shouldUseLegacyLeaderboardResultsFormat()) {
                    return sendInLegacyFormat(bankInfo, leaderboardId, hash, result, url);
                } else {
                    return send(bankInfo, hash, result, url);
                }
            } else {
                LOG.info("Key for bank {} is empty, leaderboard result was not send", bankInfo.getId());
                return false;
            }
        } else {
            LOG.info("Leaderboard results was not send as API was not configured for bank " + bankInfo.getId());
        }
        return true;
    }

    private boolean sendInLegacyFormat(BankInfo bankInfo, long leaderboardId, String hash, String result, String url) {
        Map<String, String> params = new HashMap<>();
        params.put("bankId", bankInfo.getExternalBankId());
        params.put("leaderboardId", "" + leaderboardId);
        params.put("hash", hash);
        params.put("result", result);
        try {
            LOG.info("Sending leaderboard results request to " + url + " with params: " + params);
            String response = HttpClientConnection.newInstance().doRequest(url, params, true, false);
            LOG.info("Leaderboard results was send, response: '{}'", response);
            return response.trim().equals(RESPONSE_OK);
        } catch (TransportException e) {
            LOG.error("Failed to send Leaderboard results for bank " + bankInfo.getId(), e);
            return false;
        }
    }

    private boolean send(BankInfo bankInfo, String hash, String json, String url) {
        LeaderboardResult result = gson.fromJson(json, LeaderboardResult.class);
        LeaderboardResultWrapper wrapper = new LeaderboardResultWrapper(
                bankInfo.getExternalBankId(),
                result.getLeaderboardId(),
                result.getTransactionId(),
                hash,
                result);
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            String response = HttpClientConnection.newInstance()
                    .doRequest(url, gson.toJson(wrapper), true, false, headers, false).toString().trim();
            LOG.info("Leaderboard results was send, response: '" + response + "'");
            return response.equals(RESPONSE_OK) || isOkJson(response);
        } catch (TransportException e) {
            LOG.error("Failed to send Leaderboard results for bank " + bankInfo.getId(), e);
            return false;
        }
    }

    private boolean isOkJson(String response) {
        try {
            Result result = gson.fromJson(response, Result.class);
            return RESPONSE_OK.equals(result.getResult());
        } catch (Throwable e) {
            return false;
        }
    }

    private class Result {
        private String result;

        public String getResult() {
            return result;
        }
    }
}
