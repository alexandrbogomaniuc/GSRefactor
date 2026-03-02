<%@ page import="com.abs.casino.cassandra.persist.CassandraAccountInfoPersister" %>
<%@ page import="com.abs.casino.common.cache.data.account.AccountInfo" %>
<%@ page import="com.abs.casino.common.util.string.StringUtils" %>
<%@ page import="com.abs.casino.cassandra.persist.CassandraLasthandPersister" %>
<%@ page import="com.abs.casino.common.cache.BaseGameCache" %>
<%@ page import="com.abs.casino.cassandra.CassandraPersistenceManager" %>
<%@ page import="com.abs.casino.common.util.ApplicationContextHelper" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
            .getBean("persistenceManager", CassandraPersistenceManager.class);
    CassandraAccountInfoPersister accountInfoPersister = persistenceManager.getPersister(CassandraAccountInfoPersister.class);
    CassandraLasthandPersister lasthandPersister = persistenceManager.getPersister(CassandraLasthandPersister.class);
    String s = request.getParameter("accountId");
    if (!StringUtils.isTrimmedEmpty(s)) {
        long accountId = Long.parseLong(s);
        AccountInfo accountInfo = accountInfoPersister.getById(accountId);
        if (accountInfo == null) {
            response.getWriter().write("Account not found");
            return;
        }
        response.getWriter().print("accountInfo=" + accountInfo + "<br>");
        for (Long gameId : BaseGameCache.getInstance().getAllGamesSet(accountInfo.getBankId(), accountInfo.getCurrency())) {
            String lastHands = lasthandPersister.get(accountInfo.getId(), gameId, null, null);
            if (!StringUtils.isTrimmedEmpty(lastHands)) {
                response.getWriter().print("GameId:" + gameId + ", LastHand" + ":" + lastHands + "<br>");
            }
        }
    }
    response.getWriter().print("\n<br>");
    response.flushBuffer();
%>