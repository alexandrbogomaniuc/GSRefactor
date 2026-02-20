<%@ page import="java.util.Set" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.currency.Currency" %>
<%@ page import="com.dgphoenix.casino.common.cache.BankInfoCache" %>
<%@ page import="com.dgphoenix.casino.gs.persistance.remotecall.RemoteCallHelper" %>
<%@ page import="com.dgphoenix.casino.common.cache.BaseGameCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.game.IBaseGameInfo" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    Set<Long> gameIds = new HashSet<>();
    //gameIds.add(792L);
    //gameIds.add(798L);
    gameIds.add(775L);
    gameIds.add(776L);
    gameIds.add(777L);
    gameIds.add(778L);
    List<Long> bankIds = new ArrayList<>();
    bankIds.add(4379L);
    bankIds.add(4381L);
    bankIds.add(273L);

    boolean enable = true;
    for (Long bankId : bankIds) {
        List<Currency> currencies = BankInfoCache.getInstance().getBankInfo(bankId).getCurrencies();
        for (Currency currency : currencies) {
            for (Long gameId : gameIds) {
                IBaseGameInfo gameInfo = BaseGameCache.getInstance().getGameInfoById(bankId, gameId, currency);
                if (gameInfo != null) {
                    response.getWriter().println((enable ? "Enabling" : "Disabling") + " game: " + gameId + " for bankId: " + bankId + " and currency: " + currency.getCode());
                    response.getWriter().println("<br>");
                    gameInfo.setEnabled(enable);
                    RemoteCallHelper.getInstance().saveAndSendNotification(gameInfo);
                }
            }
        }
    }
    response.getWriter().println("Done");

%>