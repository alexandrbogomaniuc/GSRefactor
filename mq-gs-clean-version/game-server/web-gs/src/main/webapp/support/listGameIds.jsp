<%@ page import="java.util.Collection" %>
<%@ page import="com.dgphoenix.casino.common.games.IStartGameHelper" %>
<%@ page import="com.dgphoenix.casino.common.games.StartGameHelpers" %>
<%
    Collection<IStartGameHelper> helpers = StartGameHelpers.getInstance().getHelpers();
    for (IStartGameHelper helper : helpers) {
%>
<%=helper.getGameId()%> - <%=helper.getTitle(-1, null)%> <br>
<%
    }
%>
