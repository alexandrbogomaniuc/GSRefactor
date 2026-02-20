<%@ page import="java.util.*" %>
<%@ page import="com.dgphoenix.casino.common.cache.BankInfoCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.CurrencyCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.currency.Currency" %>
<%@ page import="com.dgphoenix.casino.common.cache.BaseGameCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.game.BaseGameInfo" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.game.BaseGameConstants" %>
<%@ page import="com.dgphoenix.casino.common.cache.BaseGameInfoTemplateCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.bank.BankInfo" %>
<%@ page import="com.dgphoenix.casino.common.util.string.StringUtils" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.game.IBaseGameInfo" %>
<%--
  Created by IntelliJ IDEA.
  User: galanov
  Date: 19.02.14
  Time: 14:40
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>FRB COINS</title>
</head>
<body>
<%
    String value;
    boolean onlyEnabled = request.getParameter("onlyEnabled") != null;
    boolean frbCoinIsNull = request.getParameter("frbCoinIsNull") != null;
    List<Long> bankIds = null;
    List<String> currencies = null;

    value = request.getParameter("bankId");
    if (value != null) {
        StringTokenizer banks = new StringTokenizer(value, "|");
        bankIds = new ArrayList<>();
        while (banks.hasMoreTokens()) {
            bankIds.add(Long.parseLong(banks.nextToken()));
        }
    }

    value = request.getParameter("currencyCode");
    if (value != null) {
        StringTokenizer codes = new StringTokenizer(value, "|");
        currencies = new ArrayList<>();
        while (codes.hasMoreTokens()) {
            currencies.add(codes.nextToken());
        }
    }

    if (bankIds != null) {
        for (long bankId : bankIds) {
            List<Long> enabledFrbGames = new ArrayList<>(BankInfoCache.getInstance().getFrbGames(BankInfoCache.getInstance().getBankInfo(bankId)));
            Collections.sort(enabledFrbGames);

            List<Currency> currencyList = new ArrayList<>();
            if (currencies != null) {
                if (currencies.get(0).toLowerCase().equals("all")) {
                    currencyList.addAll(BankInfoCache.getInstance().getBankInfo(bankId).getCurrencies());
                } else {
                    for (String code : currencies) {
                        currencyList.add(CurrencyCache.getInstance().get(code));
                    }
                }
            } else {
                currencyList.addAll(BankInfoCache.getInstance().getBankInfo(bankId).getCurrencies());
            }
            for (Currency currency : currencyList) {
                response.getWriter().print("<table cellpadding='5' border='2'>");
                response.getWriter().print("<tr><td> Bank: " + bankId + "  " + currency.getCode() + "(" + currency.getSymbol() + ")</td></tr>");
                response.getWriter().write("<tr><td> Name </td><td> Id </td><td> isFRB </td><td> FRB_COIN </td><td> FRB_NUMLINES </td><td> FRB_BPL </td></tr>");

                Map<Long, IBaseGameInfo> games = BaseGameCache.getInstance().getAllGameInfosAsMap(bankId, currency);
                for (Map.Entry<Long, IBaseGameInfo> gameInfoEntry : games.entrySet()) {
                    Long gameId = gameInfoEntry.getKey();
                    IBaseGameInfo bgi = gameInfoEntry.getValue();

                    String frbCoin = bgi.getProperty(BaseGameConstants.KEY_FRB_COIN);
                    frbCoin = frbCoin == null ? "none" : frbCoin;
                    String frbLine = bgi.getProperty(BaseGameConstants.KEY_FRB_DEFAULTNUMLINES);
                    frbLine = frbLine == null ? "none" : frbLine;
                    String frbBet = bgi.getProperty(BaseGameConstants.KEY_FRB_DEFAULTBETPERLINE);
                    frbBet = frbBet == null ? "none" : frbBet;
                    boolean frb = enabledFrbGames.contains(gameId);
                    if (onlyEnabled && !frb) {
                        continue;
                    }
                    if (frbCoinIsNull && !frbCoin.equals("none")) {
                        continue;
                    }
                    response.getWriter().write("<tr><td>" + bgi.getName() + "</td><td>" + bgi.getId() + "</td><td>" + frb + "</td><td>" + frbCoin + "</td><td>" + frbLine + "</td><td>" + frbBet + "</td></tr>");
                }
                response.getWriter().write("</table>" + "<br>");
            }
        }
    }
%>
</body>
</html>
