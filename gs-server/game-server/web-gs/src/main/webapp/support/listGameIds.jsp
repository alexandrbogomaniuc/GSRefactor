<%@ page import="java.util.Collection" %>
<%@ page import="com.abs.casino.common.games.IStartGameHelper" %>
<%@ page import="com.abs.casino.common.games.StartGameHelpers" %>
<%
    Collection<IStartGameHelper> helpers = StartGameHelpers.getInstance().getHelpers();
    for (IStartGameHelper helper : helpers) {
%>
<%=helper.getGameId()%> - <%=helper.getTitle(-1, null)%> <br>
<%
    }
%>
