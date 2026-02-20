<%@ page import="com.betsoft.casino.mp.config.WebSocketRouter" %>
<%@ page import="com.betsoft.casino.mp.service.*" %>
<%@ page import="com.betsoft.casino.mp.web.service.SitOutTask" %>
<%@ page import="org.apache.logging.log4j.LogManager" %>
<%@ page import="org.apache.logging.log4j.Logger" %>
<%@ page import="org.springframework.context.ApplicationContext" %>
<%@ page import="java.util.Collection" %>
<%@ page import="java.util.stream.Collectors" %>
<%@ page import="java.util.Comparator" %>
<%@ page import="com.dgphoenix.casino.common.util.string.StringUtils" %>
<%@ page import="com.betsoft.casino.mp.model.RoomTemplate" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%!
    static final Logger LOG = LogManager.getLogger(SitOutTask.class);
%>

<!DOCTYPE HTML>
<html>
<head>
    <meta charset="utf-8">
    <title>Details Pending For User Panel</title>
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

    LOG.debug("getRoomTemplates.jsp: query={}", request.getQueryString());
    String gameIdAsString = request.getParameter("gameId");
    long gameId = -1;
    if(!StringUtils.isTrimmedEmpty(gameIdAsString)) {
        try {
            gameId = Long.parseLong(gameIdAsString);
        } catch (Exception exception) {
            LOG.debug("getRoomTemplates.jsp: exception to parse gameId={} to long value", gameIdAsString, exception);
        }

    }
    LOG.debug("getRoomTemplates.jsp: gameId={}", gameId);

    response.getWriter().println("gameId=" + gameId);

    ApplicationContext appContext = WebSocketRouter.getApplicationContext();

    RoomTemplateService roomTemplateService = appContext.getBean(RoomTemplateService.class);

    Collection<RoomTemplate>  roomTemplates = roomTemplateService.getAll();

    if (roomTemplates != null && !roomTemplates.isEmpty()) {
        roomTemplates = roomTemplates.stream()
                .sorted(Comparator.comparingLong(RoomTemplate::getId))
                .collect(Collectors.toList());
    }

    if (roomTemplates == null) {
        response.getWriter().println("roomTemplates is null");
    } else {
%>
<hr>
<h1>Details roomTemplates</h1>
<hr>
<table>
    <tr>
        <th>Room Template Id</th><th>Game</th><th>RoomTemplate</th>
    </tr>
    <% for (RoomTemplate roomTemplate : roomTemplates) {
        if(gameId == -1 || gameId == roomTemplate.getGameType().getGameId()) {%>
    <tr>
        <td><%=roomTemplate.getId()%></td>
        <td><%=roomTemplate.getGameType().getGameId() + ":" + roomTemplate.getGameType().name()%></td>
        <td><%=roomTemplate%></td>
    </tr>
    <%      }
        }
    }
    %>
</table>

</body>
</html>
