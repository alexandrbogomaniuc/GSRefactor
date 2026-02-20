<%@ page import="com.dgphoenix.casino.common.util.string.StringUtils" %>
<%@ page import="com.dgphoenix.casino.common.cache.LoadBalancerCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.server.ServerInfo" %>
<%@ page import="com.dgphoenix.casino.gs.GameServer" %>
<%
    String sessionId = request.getParameter("sessionId");
    String roundId = "-1";
    if (request.getParameter("roundId") != null)
        roundId = request.getParameter("roundId");

    if (!StringUtils.isTrimmedEmpty(sessionId)) {
        long serverId = GameServer.getInstance().getServerId();
        ServerInfo serverInfo = LoadBalancerCache.getInstance().getServerInfoById((int) serverId);
        String url = "http://" + serverInfo.getHost() + "/gamehistoryFS.do?SESSID=" + sessionId + "&ROUNDID=" + roundId;
        response.sendRedirect(url);
    }
%>