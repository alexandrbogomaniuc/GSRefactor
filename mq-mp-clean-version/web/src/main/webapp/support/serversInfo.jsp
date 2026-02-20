<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%@ page import="com.betsoft.casino.mp.config.WebSocketRouter" %>
<%@ page import="com.betsoft.casino.mp.data.service.ServerConfigService" %>
<%@ page import="com.betsoft.casino.mp.service.ServerConfigDto" %>

<%
    ServerConfigService serverConfigService = WebSocketRouter.getApplicationContext().getBean(ServerConfigService.class);
%>

<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>

<html>
<head>
    <script type="text/javascript" src="/support/js/jquery-1.12.4.min.js"></script>
    <style type="text/css">
        .green { background-color: lightgreen; }
        .orange { background-color: orange; }
    </style>
</head>
<body>
<html:errors/>

<div id="serverStates">
    <h2>Servers states</h2>
    <p>Data from server: <%=serverConfigService.getServerId()%></p>
    <table border="1">
        <tr>
            <th>Server id</th>
            <th>Is online?</th>
            <th>Server identifier</th>
            <th>Server IP</th>
            <th>Is master</th>
        </tr>
        <% for (ServerConfigDto srvr : serverConfigService.getConfigs()) { %>
            <tr class="<%= srvr.isOnline() ? "green" : "orange" %>">
                <td><%= srvr.getId() %></td>
                <td><%= srvr.isOnline() ? "Online" : "Offline" %></td>
                <td><%= srvr.getServerIdentifier() %></td>
                <td><%= srvr.getServerIP() %></td>
                <td><%= srvr.isMaster() ? "Master" : "-" %></td>
            </tr>
        <% } %>
    </table>
</div>
</body>
</html>
