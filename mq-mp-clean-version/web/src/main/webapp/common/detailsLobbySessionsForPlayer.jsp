<%@ page import="com.betsoft.casino.mp.config.WebSocketRouter" %>
<%@ page import="java.util.*" %>
<%@ page import="com.dgphoenix.casino.common.util.string.StringUtils" %>
<%@ page import="org.apache.logging.log4j.LogManager" %>
<%@ page import="org.apache.logging.log4j.Logger" %>
<%@ page import="com.betsoft.casino.mp.service.*" %>
<%@ page import="com.betsoft.casino.mp.web.service.SitOutTask" %>
<%@ page import="com.betsoft.casino.mp.model.LobbySession" %>

<%!
    static final Logger LOG = LogManager.getLogger(SitOutTask.class);
%>

<!DOCTYPE HTML>
<html>
<head>
    <meta charset="utf-8">
    <title>Details Lobby Sessions User Panel</title>
    <style>
        body {
            margin: 0;
            background-color: #dcdcdc;
            font-size: 15px;
        }

        h1 {
            text-align: center;
            color: white;
            background-color: black;
            padding: 0;
            margin: 0;
        }

        table {
            text-align: start;
        }

        td {
            border: 1px solid black;
        }

        th {
            color: white;
            background-color: black;
        }

        .active {
            color: green;
            text-decoration: underline;
        }

        .inactive {
            color: red;
            text-decoration: underline;
        }
    </style>
</head>
<body>
<%
    LobbySessionService lobbySessionService = WebSocketRouter.getApplicationContext().getBean(LobbySessionService.class);

    LOG.debug("detailsLobbySessionsForPlayer.jsp: query={}", request.getQueryString());
    String accountId = request.getParameter("accountId");
    String sid = request.getParameter("SID");
    LOG.debug("detailsLobbySessionsForPlayer.jsp: sid={}", sid);

    Collection<LobbySession> lobbySessions = null;

    if (!StringUtils.isTrimmedEmpty(accountId)) {
        lobbySessions = lobbySessionService.getByAccountId(Long.parseLong(accountId));
    }

    if ((lobbySessions == null || lobbySessions.size() == 0) && (!StringUtils.isTrimmedEmpty(sid))) {
        LobbySession lobbySession = lobbySessionService.get(sid);
        if(lobbySession != null) {
            lobbySessions  = new ArrayList<>();
            lobbySessions.add(lobbySession);
        }
    }
    if ((lobbySessions == null || lobbySessions.size() == 0)) {
        lobbySessions = lobbySessionService.getAllLobbySessions();
    }

    if (lobbySessions == null || lobbySessions.size() == 0) {
        response.getWriter().println("No lobbySessions found");
    } else {


%>
<h1>Details Pending For User Panel</h1>
<hr>
<table>
    <tr>
        <th>MQ Nickname</th>
        <th>Account ID</th>
        <th>External Id</th>
        <th>Room Id</th>
        <th>Game Id</th>
        <th>SID</th>
        <th>Remove Action</th>
    </tr>
    <% for (LobbySession lobbySession : lobbySessions) {%>
    <tr>
        <td><%=lobbySession.getNickname()%></td>
        <td><%=lobbySession.getAccountId()%></td>
        <td><%=lobbySession.getExternalId()%></td>
        <td><%=lobbySession.getRoomId()%></td>
        <td><%=lobbySession.getGameId()%></td>
        <td><%=lobbySession.getSessionId()%></td>
        <td><a href="<%="./removeLobbySessionForPlayer.jsp?SID=" + lobbySession.getSessionId()%>">Remove Lobby Session object</a></td>
    </tr>
    <% }
    }
    %>
</table>
</body>
</html>
