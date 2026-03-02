<%@ page import="java.io.StringWriter" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="com.abs.casino.common.cache.data.bank.SubCasino" %>
<%@ page import="com.abs.casino.common.cache.SubCasinoCache" %>
<%@ page import="com.abs.casino.common.cache.data.bank.BankInfo" %>
<%@ page import="com.abs.casino.common.cache.BankInfoCache" %>
<%@ page import="com.abs.casino.common.cache.data.currency.Currency" %>
<%@ page import="com.abs.casino.common.cache.data.game.IBaseGameInfo" %>
<%@ page import="com.abs.casino.common.cache.BaseGameCache" %>
<%@ page import="com.abs.casino.common.cache.data.game.BaseGameInfo" %>
<%@ page import="com.abs.casino.common.util.string.StringUtils" %>
<%@ page import="com.abs.casino.gs.persistance.remotecall.RemoteCallHelper" %>
<%@ page import="com.abs.casino.common.exception.CommonException" %>
<%@ page import="java.util.List" %>
<%@ page import="com.google.common.base.Splitter" %>
<%--
  Author: svvedenskiy
  Date: 4/26/21

  The script allows you invalidate all BaseGameInfo with specified gameId.

  Usage:
        <host>/support/templateManager/invalidateBgi.jsp?gameId=841&propsForRemove=REPOSITORY_FILE|DEVELOPMENT_VERSION
--%>

<%
    try {
        writer = response.getWriter();
        gameId = null;
        propsForRemove = null;
        if (!parseParams(request)) {
            return;
        }
        for (SubCasino subCasino : SubCasinoCache.getInstance().getAllObjects().values()) {
            for (Long bankId : subCasino.getBankIds()) {
                BankInfo bank = BankInfoCache.getInstance().getBankInfo(bankId);
                if (bank == null || !bank.isEnabled()) {
                    continue;
                }
                for (Currency currency : bank.getCurrencies()) {
                    IBaseGameInfo game = BaseGameCache.getInstance().getGameInfoShared(bankId, gameId, currency);
                    if (!(game instanceof BaseGameInfo)) {
                        continue;
                    }
                    boolean changed = false;
                    if (propsForRemove != null) {
                        for (String property : propsForRemove) {
                            if (!StringUtils.isTrimmedEmpty(game.getProperty(property))) {
                                game.removeProperty(property);
                                changed = true;
                            }
                        }
                    }
                    if (changed) {
                        save(game);
                        LOG(bankId + " " + currency.getCode());
                    }
                }
            }
        }

    } catch (Throwable ex) {
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        response.getWriter().write(sw.toString());
    }
%>

<%!
    PrintWriter writer;
    Long gameId = null;
    List<String> propsForRemove = null;

    private boolean parseParams(HttpServletRequest request) {
        String gameIdStr = request.getParameter("gameId");
        if (StringUtils.isTrimmedEmpty(gameIdStr)) {
            printUsage();
            return false;
        }
        try {
            gameId = Long.parseLong(gameIdStr);
        } catch (NumberFormatException nfe) {
            LOG("Parameter 'gameId' has wrong format");
            LOG();
            printUsage();
            return false;
        }
        String propsForRemoveStr = request.getParameter("propsForRemove");
        if (!StringUtils.isTrimmedEmpty(propsForRemoveStr)) {
            propsForRemove = Splitter.on("|").omitEmptyStrings().splitToList(propsForRemoveStr);
        }
        return true;
    }

    private void printUsage() {
        LOG("The script allows you invalidate all BaseGameInfo with specified gameId.");
        LOG();
        LOG("Usage:");
        LOG("        <host>/support/templateManager/invalidateBgi.jsp?gameId=841&propsForRemove=REPOSITORY_FILE|DEVELOPMENT_VERSION");
    }

    private void save(IBaseGameInfo game) throws CommonException {
        RemoteCallHelper.getInstance().saveAndSendNotification(game);
    }

    void LOG(String str) {
        writer.write(str + "<br/>");
        writer.flush();
    }

    void LOG() {
        LOG("");
    }
%>
