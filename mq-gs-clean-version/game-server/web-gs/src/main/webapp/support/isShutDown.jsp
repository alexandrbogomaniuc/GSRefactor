<%@ page import="com.dgphoenix.casino.gs.GameServer" %>
<%@ page import="com.dgphoenix.casino.init.ShutdownFilter" %>

<%
    ShutdownFilter shutdownFilter = GameServer.getInstance().getShutdownFilter();
    boolean shutDown = false;
    if (shutdownFilter != null) {
        shutDown = shutdownFilter.isMarkedDown();
    }
    response.getWriter().write(Boolean.toString(shutDown));
%>