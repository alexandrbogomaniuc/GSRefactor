<%@ page import="com.abs.casino.gs.GameServer" %>
<%@ page import="com.abs.casino.init.ShutdownFilter" %>

<%
    ShutdownFilter shutdownFilter = GameServer.getInstance().getShutdownFilter();
    boolean shutDown = false;
    if (shutdownFilter != null) {
        shutDown = shutdownFilter.isMarkedDown();
    }
    response.getWriter().write(Boolean.toString(shutDown));
%>