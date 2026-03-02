<%@ page import="com.abs.casino.common.cache.BankInfoCache" %>
<%@ page import="com.abs.casino.common.cache.BaseGameCache" %>
<%@ page import="com.abs.casino.common.cache.data.bank.BankInfo" %>
<%@ page import="com.abs.casino.common.cache.data.bank.Limit" %>
<%@ page import="com.abs.casino.common.cache.data.currency.Currency" %>
<%@ page import="com.abs.casino.common.cache.data.game.BaseGameInfo" %>
<%@ page import="com.abs.casino.common.cache.data.game.GameVariableType" %>
<%@ page import="com.abs.casino.gs.persistance.remotecall.RemoteCallHelper" %>
<%@ page import="java.math.BigDecimal" %>
<%@ page import="java.math.RoundingMode" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.abs.casino.common.cache.data.game.IBaseGameInfo" %>
<%

    String bank = request.getParameter("bankId");
    long bankId;
    if (bank != null) {
        bankId = Long.parseLong(bank);
    } else {
        response.getWriter().print("unknown bankId");
        return;
    }

    BaseGameCache gameCache = BaseGameCache.getInstance();
    BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
    Map<String, String> properties;

    for (Currency currency : bankInfo.getCurrencies()) {

        Map<Long, IBaseGameInfo> games = gameCache.getAllGameInfosAsMap(bankInfo.getId(), currency);
        for (Map.Entry<Long, IBaseGameInfo> gameInfoEntry : games.entrySet()) {
            Long gameId = gameInfoEntry.getKey();
            try {
                IBaseGameInfo resultGame = gameInfoEntry.getValue();
                if (resultGame.getVariableType() == GameVariableType.LIMIT) {

                    String gameName = resultGame.getName().toLowerCase();

                    if (gameName.contains("roulet")) {

                        Limit limit = resultGame.getLimit() != null ? (Limit) resultGame.getLimit() : bankInfo.getLimit();
                        int maxValue = new BigDecimal((double) (limit.getMaxValue() / 100)).setScale(0,
                                RoundingMode.HALF_UP).intValue();

                        properties = new HashMap<>();
                        properties.put("MAX_BET_1", (maxValue / 20 < 1) ? "1" : Integer.toString(maxValue / 20)); // Straight up	1/20 of high limit
                        properties.put("MAX_BET_2", (maxValue / 20 < 1) ? "1" : Integer.toString(maxValue / 20)); // Split Bet	1/20 of high limit
                        properties.put("MAX_BET_3", (maxValue / 20 < 1) ? "1" : Integer.toString(maxValue / 20)); // Street Bet	1/20 of high limit
                        properties.put("MAX_BET_4", (maxValue / 10 < 1) ? "1" : Integer.toString(maxValue / 10)); // Corner Bet	1/10 of high limit
                        properties.put("MAX_BET_5", (maxValue / 20 < 1) ? "1" : Integer.toString(maxValue / 20)); // First five	1/20 of high limit
                        properties.put("MAX_BET_6", (maxValue / 5 < 1) ? "1" : Integer.toString(maxValue / 5)); // Line Bet	1/5 of high limit
                        properties.put("MAX_BET_12", Integer.toString(maxValue / 2)); // Dozen Bet/Column Bet	1/2 of high limit
                        properties.put("MAX_BET_18", Integer.toString(maxValue)); // Red/Black, Even/Odd, Low/High Bets	of high limit
                        for (Map.Entry<String, String> property : properties.entrySet()) {
                            resultGame.setProperty(property.getKey(), property.getValue());
                        }
                        response.getWriter().write("resultGame = " + resultGame + "<br><br><br>");
                    }

                    RemoteCallHelper.getInstance().saveAndSendNotification(resultGame);
                }
            } catch (Exception e) {
                response.getWriter().write("not supported: " + gameId + "<br><br><br>");
            }
        }
    }
%>
