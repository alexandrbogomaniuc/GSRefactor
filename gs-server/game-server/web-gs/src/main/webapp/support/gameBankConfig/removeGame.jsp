<%@ page import="com.dgphoenix.casino.cassandra.persist.CassandraBaseGameInfoTemplatePersister" %>
<%@ page import="com.dgphoenix.casino.common.cache.BankInfoCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.BaseGameCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.BaseGameInfoTemplateCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.bank.BankInfo" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.currency.Currency" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.game.BaseGameInfo" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.game.BaseGameInfoTemplate" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.game.GameMode" %>
<%@ page import="com.dgphoenix.casino.common.exception.CommonException" %>
<%@ page import="com.abs.casino.gs.persistance.remotecall.RefreshConfigCall" %>
<%@ page import="com.abs.casino.gs.persistance.remotecall.RemoteCallHelper" %>
<%@ page import="java.io.IOException" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.game.IBaseGameInfo" %>
<%@ page import="com.dgphoenix.casino.cassandra.CassandraPersistenceManager" %>
<%@ page import="com.dgphoenix.casino.common.util.ApplicationContextHelper" %>
<%@ page import="com.dgphoenix.casino.common.util.string.StringUtils" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>


<html>
<head>
    <title></title>
</head>
<body>

<%!
    private final CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
            .getBean("persistenceManager", CassandraPersistenceManager.class);
    private final CassandraBaseGameInfoTemplatePersister baseGameInfoTemplatePersister =
            persistenceManager.getPersister(CassandraBaseGameInfoTemplatePersister.class);

    String getParameter(HttpServletRequest request, String key) throws IOException {
        String result = request.getParameter(key);
        if (result == null || result.equals("null") || result.equals("")) return null;

        return result.trim();
    }

    String[] getArrayParameter(HttpServletRequest request, String key) throws IOException {
        return request.getParameterValues(key);
    }

    Long[] convertStringArrayToLongArray(String[] array) {
        Long[] result = new Long[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = Long.parseLong(array[i]);
        }

        return result;
    }

    void deleteGame(long bankId, long gameId, Currency currency) throws IOException, CommonException {
        BaseGameCache.getInstance().remove(bankId, gameId, currency);
        BaseGameInfoTemplate template = BaseGameInfoTemplateCache.getInstance().getBaseGameInfoTemplateById(gameId);

        //if (template != null)
        //{
        //    if (template.isJackpotGame() && !template.isJackpot3Game())
        //    {
        //        if (JackPotCache.getInstance().isExist(bankId, gameId)) {
        //            JackPotCache.getInstance().remove((int)bankId, gameId);
        //        }
        //    }

        //    if (template.isJackpot3Game())
        //    {
        //        Jackpot3Manager jackpot3Manager = Jackpot3Managers.get(GameMode.REAL);
        //        jackpot3Manager.delete(bankId, gameId, currency);
        //    }
        //}
    }
%>


<%
    String strGameIds = getParameter(request, "game_ids");
    String strBankIds = getParameter(request, "bank_ids");

    String strCheckTemplate = getParameter(request, "check_template");
    String strCheckFull = getParameter(request, "check_full");
    Long[] array_banks = null;
    if (!StringUtils.isTrimmedEmpty(strBankIds)) {
        String[] strArrayBankIds = strBankIds.replaceAll(", ", " ").replaceAll(",", " ").split(" ");
        array_banks = convertStringArrayToLongArray(strArrayBankIds);
    } else if (!StringUtils.isTrimmedEmpty(strCheckFull)) {
        array_banks = BankInfoCache.getInstance().getBankIds().toArray(new Long[0]);
    }

    if (strGameIds == null) {
        response.getWriter().write("<b>Game ID list is empty</b><br/>");
        return;
    }
    if (array_banks == null) {
        response.getWriter().write("<b>Bank ID list is empty and 'Full delete' not checked</b><br/>");
        return;
    }

    String[] strArrayGameIds = strGameIds.replaceAll(", ", " ").replaceAll(",", " ").split(" ");
    Long[] array_games = convertStringArrayToLongArray(strArrayGameIds);

    for (long bankId : array_banks) {
        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);

        response.getWriter().write("<b>BANK: " + bankId + (bankInfo == null ? " not found" : "") + "</b><br>");

        if (bankInfo != null) {
            for (Currency currency : bankInfo.getCurrencies()) {
                for (long gameId : array_games) {
                    IBaseGameInfo gameInfo = BaseGameCache.getInstance().getGameInfoShared(bankId, gameId, currency);

                    if (gameInfo != null) {
                        BaseGameInfoTemplate template = BaseGameInfoTemplateCache.getInstance().getBaseGameInfoTemplateById(gameId);

                        String title = (template != null) ? template.getTitle() : gameInfo.getName();
                        response.getWriter().write("<b>delete BaseGameInfo:</b> " + title + "(" + gameId + ")   " + currency.getCode() + "<br>");
                        deleteGame(bankId, gameId, currency);
                    }
                }
            }
        }
    }

    response.getWriter().write("<hr>");

    if (strCheckTemplate != null && strCheckTemplate.equals("on")) {
        for (long gameId : array_games) {
            BaseGameInfoTemplate template = BaseGameInfoTemplateCache.getInstance().getBaseGameInfoTemplateById(gameId);

            if (template != null) {
                response.getWriter().write("<b>delete Template:</b> " + template.getTitle() + "(" + gameId + ")" + "<br>");
                BaseGameInfoTemplateCache.getInstance().remove(String.valueOf(gameId));
                baseGameInfoTemplatePersister.remove(gameId);

                RemoteCallHelper.getInstance().sendRefreshConfigCallToAllServers(new RefreshConfigCall(
                        BaseGameInfoTemplateCache.class.getCanonicalName(), String.valueOf(gameId)));
            }
        }
    }
%>

</body>
</html>