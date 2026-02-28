<%@ page import="com.abs.casino.common.util.web.HttpClientConnectionStatistics" %>
<%@ page import="com.dgphoenix.casino.gs.GameServer" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.Collections" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    List<Long> hours = Collections.emptyList();//WalletClientConnectionCallbackHandler.getInstance().getSortedByHour();
%>
<html>
<head>

    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta http-equiv="Expires" content="Tue, 01 Jan 1980 1:00:00 GMT">
    <meta http-equiv="Cache-Control" content="no-cache">
    <meta http-equiv="Pragma" content="no-cache">
    <title>Wallet http statistics, gs<%=GameServer.getInstance().getServerId()%>
    </title>
</head>
<body>
<table border="1">
    <tr>
        <td>Date</td>
        <td>Timeout</td>
        <td>Empty responce</td>
        <td>Unclassified errors</td>
        <td>Http error 503</td>
        <td>Http error 500</td>
        <td>Unclassified Http error</td>
        <td>Success</td>
        <td>Login error [game sessions limit]</td>
        <td>Long requests [>10sec.]</td>
    </tr>
    <%
        for (Long hour : hours) {
//            HttpClientConnectionStatistics stat = WalletClientConnectionCallbackHandler.getInstance().get(hour);
    %>
    <tr>
        <td><%=(new Date(hour))%>
        </td>
        <%--<td><%=stat.getTimeouts().get()%></td>--%>
        <%--<td><%=stat.getEmptyResponces().get()%></td>--%>
        <%--<td><%=stat.getUnclassifiedErrors().get()%></td>--%>
        <%--<td><%=stat.getError503().get()%></td>--%>
        <%--<td><%=stat.getError500().get()%></td>--%>
        <%--<td><%=stat.getUnclassifiedErrors().get()%></td>--%>
        <%--<td><%=stat.getSuccess().get()%></td>--%>
        <%--<td><%=stat.getLoginErrorByGameSessionsLimit().get()%></td>--%>
        <%--<td><%=stat.getLongRequests().get()%></td>--%>
    </tr>
    <%
        }
    %>
</table>
</body>
</html>