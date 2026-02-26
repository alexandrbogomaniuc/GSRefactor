<%@ page import="java.util.Collection" %>
<%@ page import="com.abs.casino.common.games.StartGameHelpers" %>
<%@ page import="com.dgphoenix.casino.common.games.IStartGameHelper" %>
<%
    Collection<IStartGameHelper> helpers = StartGameHelpers.getInstance().getHelpers();
    for (IStartGameHelper helper : helpers) {
%>
<%=helper.getGameId()%> - <%=helper.getTitle(-1, null)%> <br>
<%
    }
%>
