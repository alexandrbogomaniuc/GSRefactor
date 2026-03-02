<%@ page import="com.abs.casino.common.cache.BankInfoCache" %>
<%@ page import="com.abs.casino.common.cache.BaseGameCache" %>
<%@ page import="com.abs.casino.common.cache.data.currency.Currency" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.StringTokenizer" %>
<%@ page import="java.util.TreeSet" %>
<%@ page import="com.abs.casino.common.cache.BaseGameInfoTemplateCache" %>
<%@ page import="com.abs.casino.common.cache.data.game.BaseGameInfoTemplate" %>
<%@ page import="com.abs.casino.common.util.CollectionUtils" %>
<%@ page import="com.abs.casino.common.cache.data.game.IBaseGameInfo" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    response.setContentType("text");
    //bankId=XXX;<GAME ID="23" NAME="Five Draw Poker" IMAGEURL="" LANGUAGES="en,no,se,nl,it,fi,es,fr,de,pt"></GAME>
    String value;
    Set<Long> bankIds = null;
    Set<Long> gameIds = null;

    value = request.getParameter("bankId");
    if (value != null) {
        StringTokenizer banks = new StringTokenizer(value, ",.;:|-+");
        bankIds = new TreeSet<Long>();
        while (banks.hasMoreTokens()) {
            bankIds.add(Long.parseLong(banks.nextToken()));
        }
    }

    value = request.getParameter("gameId");
    if (value != null) {
        StringTokenizer games = new StringTokenizer(value, ",.;:|-+");
        gameIds = new TreeSet<Long>();
        while (games.hasMoreTokens()) {
            gameIds.add(Long.parseLong(games.nextToken()));
        }
    }

    for (long bankId : bankIds) {
        Currency defaultCurrency = BankInfoCache.getInstance().getBankInfo(bankId).getDefaultCurrency();
        for (long gameId : gameIds) {
            IBaseGameInfo bgi = BaseGameCache.getInstance().getGameInfoShared(bankId, gameId, defaultCurrency);
            if (bgi != null) {
                BaseGameInfoTemplate template = BaseGameInfoTemplateCache.getInstance().getBaseGameInfoTemplateById(gameId);
                String title = template.getTitle() != null ? template.getTitle() : template.getGameName();
                String langs = CollectionUtils.listOfStringsToString(bgi.getLanguages());
                response.getWriter().println(String.format("bankId=%d;<GAME ID=\"%d\" NAME=\"%s\" IMAGEURL=\"\" LANGUAGES=\"%s\"></GAME>", bankId, gameId, title, langs));
            }
        }
    }

%>