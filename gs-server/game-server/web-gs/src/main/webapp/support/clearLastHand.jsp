<%@ page import="com.abs.casino.cassandra.CassandraPersistenceManager" %>
<%@ page import="com.abs.casino.cassandra.persist.CassandraLasthandPersister" %>
<%@ page import="com.abs.casino.common.cache.data.bonus.BonusSystemType" %>
<%@ page import="com.abs.casino.common.util.ApplicationContextHelper" %>
<%@ page import="com.abs.casino.common.util.logkit.ThreadLog" %>
<%@ page import="com.abs.casino.common.util.string.StringUtils" %>
<%--
User: vladislav
Date: 19/05/15
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%
    CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
            .getBean("persistenceManager", CassandraPersistenceManager.class);
    CassandraLasthandPersister lasthandPersister = persistenceManager.getPersister(CassandraLasthandPersister.class);
    ThreadLog.warn("clearLastHand.jsp:: Called from IP:" + request.getRemoteAddr() + ". Request:: " + request.getQueryString());

    Long accountId;
    try {
        accountId = Long.parseLong(request.getParameter("accountId"));
    } catch (NumberFormatException e) {
        response.getWriter().write("Invalid accountId");
        ThreadLog.warn("clearLastHand.jsp::", e);
        return;
    }

    Long gameId;
    try {
        gameId = Long.parseLong(request.getParameter("gameId"));
    } catch (NumberFormatException e) {
        response.getWriter().write("Invalid gameId");
        ThreadLog.warn("clearLastHand.jsp::", e);
        return;
    }

    String bonusIdAsString = request.getParameter("bonusId");
    if (!StringUtils.isTrimmedEmpty(bonusIdAsString)) {
        long bonusId;
        try {
            bonusId = Long.parseLong(bonusIdAsString);
        } catch (Exception e) {
            response.getWriter().write("Invalid bonusId");
            ThreadLog.warn("clearLastHand.jsp::", e);
            return;
        }

        String bonusTypeAsString = request.getParameter("bonusType");
        if (!StringUtils.isTrimmedEmpty(bonusTypeAsString)) {
            try {
                BonusSystemType bonusSystemType = BonusSystemType.valueOf(bonusTypeAsString);

                String lastHand = lasthandPersister.get(accountId, gameId, bonusId, bonusSystemType);
                if (!StringUtils.isTrimmedEmpty(lastHand)) {
                    lasthandPersister.delete(accountId, gameId, bonusId, bonusSystemType);
                    response.getWriter().write("last hand cleared");
                    ThreadLog.warn("clearLastHand.jsp:: last hand cleared: accountId=" + accountId + ", gameId=" + gameId +
                            ", bonusId=" + bonusId + ", bonusSystemType=" + bonusSystemType);
                } else {
                    response.getWriter().write("last hand not found");
                    ThreadLog.warn("clearLastHand.jsp:: last hand not found: accountId=" + accountId + ", gameId=" + gameId +
                            ", bonusId=" + bonusId + ", bonusSystemType=" + bonusSystemType);
                }

            } catch (IllegalArgumentException e) {
                response.getWriter().write("Invalid bonusType");
                ThreadLog.warn("clearLastHand.jsp::", e);
            }

        } else {
            response.getWriter().write("Need bonusType");
            ThreadLog.warn("clearLastHand.jsp:: request have bonusId, but bonusType not found.");
        }

    } else {
        String lastHand = lasthandPersister.get(accountId, gameId, null, null);
        if (!StringUtils.isTrimmedEmpty(lastHand)) {
            lasthandPersister.delete(accountId, gameId, null, null);
            response.getWriter().write("last hand cleared");
            ThreadLog.warn("clearLastHand.jsp:: last hand cleared: accountId=" + accountId + ", gameId=" + gameId);
        } else {
            response.getWriter().write("last hand not found");
            ThreadLog.warn("clearLastHand.jsp:: last hand not found: accountId=" + accountId + ", gameId=" + gameId);
        }
    }

%>