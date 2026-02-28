<%@ page import="com.dgphoenix.casino.cassandra.persist.CassandraSubCasinoGroupPersister" %>
<%@ page import="com.dgphoenix.casino.common.cache.BankInfoCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.BaseGameCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.SubCasinoCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.SubCasinoGroupCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.bank.BankInfo" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.bank.SubCasinoGroup" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.currency.Currency" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.game.BaseGameInfo" %>
<%@ page import="com.dgphoenix.casino.common.exception.CommonException" %>
<%@ page import="com.abs.casino.common.util.logkit.ThreadLog" %>
<%@ page import="com.dgphoenix.casino.common.util.string.StringUtils" %>
<%@ page import="com.abs.casino.gs.persistance.remotecall.RefreshConfigCall" %>
<%@ page import="com.abs.casino.gs.persistance.remotecall.RemoteCallHelper" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.regex.Pattern" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.game.IBaseGameInfo" %>
<%@ page import="com.abs.casino.common.cache.data.game.ImmutableBaseGameInfoWrapper" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.bank.SubCasino" %>
<%@ page import="com.dgphoenix.casino.cassandra.CassandraPersistenceManager" %>
<%@ page import="com.dgphoenix.casino.common.util.ApplicationContextHelper" %>
<%--
  Created by IntelliJ IDEA.
  User: user
  Date: 01.06.15
  Time: 20:05
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Lang tools</title>
</head>
<body>
<%!
    private static final Pattern PATTERN = Pattern.compile("[,]");

    //static SubCasinoGroupCache cache = SubCasinoGroupCache.getInstance();
    static {
        try {
            if (SubCasinoGroupCache.getInstance().getObject("default") == null) {
                RemoteCallHelper.getInstance().saveAndSendNotification(new SubCasinoGroup("default"));
            }
        } catch (CommonException ex) {
        }
    }
%>

<%
    CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
            .getBean("persistenceManager", CassandraPersistenceManager.class);
    CassandraSubCasinoGroupPersister subCasinoGroupPersister =
            persistenceManager.getPersister(CassandraSubCasinoGroupPersister.class);

    String lastAction = "";
    boolean isError = false;

    String deleteName = request.getParameter("delete");
    if (!StringUtils.isTrimmedEmpty(deleteName) && !"default".equals(deleteName)) {
        subCasinoGroupPersister.remove(deleteName);
        SubCasinoGroupCache.getInstance().remove(deleteName);
        RemoteCallHelper.getInstance().sendRefreshConfigCallToAllServers(new RefreshConfigCall(
                SubCasinoGroupCache.class.getCanonicalName(), deleteName));

    }

    if (request.getParameter("add") != null) {
        String name = request.getParameter("name");
        if (!StringUtils.isTrimmedEmpty(name)) {
            try {
                name = name.replaceAll("\\<[^>]*>", "").replaceAll(" ", "");
                RemoteCallHelper.getInstance().saveAndSendNotification(new SubCasinoGroup(name));
                lastAction = "Group adding: " + name;
            } catch (CommonException ex) {
                lastAction = "error: " + ex.getMessage();
                isError = true;
            }
        }
    }

    String curName = request.getParameter("group");
    if (curName == null) {
        curName = "default";
    }
    SubCasinoGroup curGroup = SubCasinoGroupCache.getInstance().getObject(curName);

    String subcasinoIds = request.getParameter("subcasinoIds");
    if (!StringUtils.isTrimmedEmpty(subcasinoIds)) {
        if (request.getParameter("add") != null) {
            for (String sid : PATTERN.split(subcasinoIds)) {
                long id = -1;
                try {
                    id = Long.parseLong(sid);
                } catch (NumberFormatException ex) {
                    response.getWriter().println("Wrong subCasinoId " + sid + "<br>");
                    break;
                }
                if (SubCasinoCache.getInstance().get(id) != null) {
                    curGroup.addSubCasino(id);
                    response.getWriter().println("Add subcasino: " + sid + "<br>");
                } else {
                    response.getWriter().println("Wrong subcasinoId: " + sid + "<br>");
                }
            }
        } else if (request.getParameter("remove") != null) {
            for (String sid : PATTERN.split(subcasinoIds)) {
                long id = -1;
                try {
                    id = Long.parseLong(sid);
                } catch (NumberFormatException ex) {
                    response.getWriter().println("Wrong subCasinoId " + sid + "<br>");
                    break;
                }
                curGroup.removeSubCasino(id);
            }
        }
    }

    if (request.getParameter("addlangs") != null) {
        if (curGroup.getSubCasinoList() != null) {
            for (Long id : curGroup.getSubCasinoList()) {
                for (Long bankId : SubCasinoCache.getInstance().get(id).getBankIds()) {
                    BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
                    if (bankInfo == null) {
                        ThreadLog.warn("langTools.jsp::bankInfo for id=" + bankId + " is null, subCasinoId=" + id);
                        continue;
                    }
                    for (Currency currency : bankInfo.getCurrencies()) {
                        String[] gameList = PATTERN.split(request.getParameter("gameIds"));
                        if (gameList.length <= 0 || StringUtils.isTrimmedEmpty(gameList)) {
                            response.getWriter().println("Game list is empty <br>");
                            break;
                        }
                        for (String sGameId : gameList) {
                            long gameId = -1;
                            try {
                                gameId = Long.parseLong(sGameId);
                            } catch (NumberFormatException ex) {
                                response.getWriter().println("Wrong gameId " + sGameId + "<br>");
                                break;
                            }
                            IBaseGameInfo gameInfo = BaseGameCache.getInstance().getGameInfoShared(bankId, gameId,
                                    currency);
                            if (gameInfo != null) {
                                if (gameInfo instanceof ImmutableBaseGameInfoWrapper) {
                                    continue;
                                }
                                gameInfo.removeAllLanguages();
                                String[] langs = PATTERN.split(request.getParameter("langs"));
                                if (StringUtils.isTrimmedEmpty(langs)) {
                                    response.getWriter().println(
                                            "Use default lang en bankId: " + bankId + " gameId:" + gameId
                                                    + " currency:" + currency.getCode() + "<br>");
                                    langs = new String[]{"en"};
                                }
                                for (String lang : langs) {
                                    gameInfo.addLanguage(lang);
                                }
                                try {
                                    RemoteCallHelper.getInstance().saveAndSendNotification(gameInfo);
                                } catch (CommonException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                response.getWriter().println(
                                        "Game not found bankId: " + bankId + " gameId:" + gameId
                                                + " currency:" + currency.getCode() + "<br>");
                            }

                        }
                    }
                }

            }
        }
    }


    Set<String> names = SubCasinoGroupCache.getInstance().getAllObjects().keySet();
%>
<div style="color: <%=isError ? "#ff0000" : "#00008b"%>"><%=lastAction%>
</div>
<%
    for (String name : names) {
        if (!curName.equals(name)) {
%>
<a href="/support/langTools.jsp?group=<%=name%>"><%=name%>
</a>
<% } else {%>
<a><%=name%>
</a>
<% }
}%>

<% if (!curName.equals("default")) {%>
<form action="/support/langTools.jsp" method="post">
    <input type="hidden" name="delete" value="<%=curName%>">
    <input type="submit" value="delete group">
</form>
<%}%>
<form action="/support/langTools.jsp?group=<%=curName%>" method="post">
    <input type="hidden" name="add">
    <input type=text name="name">
    <input type="submit" value="add group">
</form>

<label>SubCasinos:</label>
<% if (curGroup.getSubCasinoList() != null)
    for (Long id : curGroup.getSubCasinoList()) {
        SubCasino subCasino = SubCasinoCache.getInstance().get(id);
        String label = subCasino != null ? subCasino.getName() : id.toString();%>
<div><%=id + ":" + label%>
</div>
<% } %>
<br>
<label>Add subCasinoIds:</label>
<form action="/support/langTools.jsp?group=<%=curName%>" method="post">
    <input type="hidden" name="add">
    <input type=text name="subcasinoIds">
    <input type="submit" value="add subCasinoIds">
</form>

<label>Remove subCasinoIds:</label>
<form action="/support/langTools.jsp?group=<%=curName%>" method="post">
    <input type="hidden" name="remove">
    <input type=text name="subcasinoIds">
    <input type="submit" value="remove subCasinoIds">
</form>

<label>Set langs:</label>
<form action="/support/langTools.jsp?group=<%=curName%>" method="post">
    <input type="hidden" name="addlangs">
    <label>Games:</label>
    <input type=text name="gameIds">
    <label>Langs (en by default):</label>
    <input type=text name="langs">
    <input type="submit" value="add langs">
</form>

</body>
</html>
