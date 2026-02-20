package com.dgphoenix.casino.web.history;

import com.dgphoenix.casino.common.cache.data.bank.BankInfo;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

import static com.dgphoenix.casino.common.util.string.StringUtils.isTrimmedEmpty;

public class GameHistoryURLBuilder {
    private static final String BANK_ID = "bankId";
    private static final String GAME_ID = "gameId";
    private static final String LANG = "lang";

    private StringBuilder urlBuilder = new StringBuilder(100);
    private boolean specificBank = true;

    public GameHistoryURLBuilder(HttpServletRequest request, BankInfo bankInfo, String sessionId) {
        String baseUrl = request.getScheme() + "://" + request.getServerName();
        urlBuilder.append(baseUrl);
        specificBank = false;
        urlBuilder.append("/gamehistory.do?");
        urlBuilder.append(GameHistoryListAction.SESSION_ID).append("=").append(sessionId);
    }

    public static GameHistoryURLBuilder create(HttpServletRequest request, BankInfo bankInfo, String sessionId) {
        return new GameHistoryURLBuilder(request, bankInfo, sessionId);
    }

    public GameHistoryURLBuilder passRequestParameters(HttpServletRequest request) {
        if (!specificBank) {
            Map<String, String[]> parameters = request.getParameterMap();
            for (Map.Entry<String, String[]> entry : parameters.entrySet()) {
                String pName = entry.getKey();
                if (pName.equals(GameHistoryListAction.SESSION_ID)) {
                    continue;
                }
                String pValue = entry.getValue()[0];
                if (!isTrimmedEmpty(pValue)) {
                    urlBuilder.append("&").append(pName).append("=").append(pValue);
                }
            }
        }
        return this;
    }

    public GameHistoryURLBuilder addBankId(long bankId) {
        urlBuilder.append("&").append(BANK_ID).append("=").append(bankId);
        return this;
    }

    public GameHistoryURLBuilder addGameId(long gameId) {
        urlBuilder.append("&").append(GAME_ID).append("=").append(gameId);
        return this;
    }

    public GameHistoryURLBuilder addLang(String lang) {
        urlBuilder.append("&").append(LANG).append("=").append(lang);
        return this;
    }

    public String build() {
        return urlBuilder.toString();
    }
}
