<%@ page import="com.betsoft.casino.mp.config.WebSocketRouter" %>
<%@ page import="com.betsoft.casino.mp.service.*" %>
<%@ page import="com.betsoft.casino.mp.web.service.SitOutTask" %>
<%@ page import="org.apache.logging.log4j.LogManager" %>
<%@ page import="org.apache.logging.log4j.Logger" %>
<%@ page import="org.springframework.context.ApplicationContext" %>
<%@ page import="java.util.Collection" %>
<%@ page import="com.betsoft.casino.mp.model.room.IRoomInfo" %>
<%@ page import="java.util.stream.Collectors" %>
<%@ page import="java.util.Comparator" %>
<%@ page import="com.dgphoenix.casino.common.util.string.StringUtils" %>
<%@ page import="com.betsoft.casino.mp.model.room.IRoom" %>
<%@ page import="com.betsoft.casino.mp.web.service.RoomServiceFactory" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="com.betsoft.casino.mp.model.ISeat" %>

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

    LOG.debug("getRoomInfos.jsp: query={}", request.getQueryString());
    String gameIdAsString = request.getParameter("gameId");
    long gameId = -1;
    if(!StringUtils.isTrimmedEmpty(gameIdAsString)) {
        try {
            gameId = Long.parseLong(gameIdAsString);
        } catch (Exception exception) {
            LOG.debug("getRoomInfos.jsp: exception to parse gameId={} to long value", gameIdAsString, exception);
        }

    }
    LOG.debug("getRoomInfos.jsp: gameId={}", gameId);

    response.getWriter().println("gameId=" + gameId);

    ApplicationContext appContext = WebSocketRouter.getApplicationContext();

    RoomServiceFactory roomServiceFactory = appContext.getBean(RoomServiceFactory.class);

    AbstractRoomInfoService multiNodeRoomInfoService = appContext.getBean(MultiNodeRoomInfoService.class);
    AbstractRoomInfoService multiNodePrivateRoomInfoService =  appContext.getBean(MultiNodePrivateRoomInfoService.class);

    Collection<IRoomInfo>  multiNodeRoomInfos = multiNodeRoomInfoService.getAllRooms();
    Collection<IRoomInfo>  multiNodePrivateRoomInfos = multiNodePrivateRoomInfoService.getAllRooms();

    if (multiNodeRoomInfos != null && !multiNodeRoomInfos.isEmpty()) {
        multiNodeRoomInfos = multiNodeRoomInfos.stream()
                .sorted(Comparator.comparingLong(IRoomInfo::getId))
                .collect(Collectors.toList());
    }

    if (multiNodePrivateRoomInfos != null && !multiNodePrivateRoomInfos.isEmpty()) {
        multiNodePrivateRoomInfos = multiNodePrivateRoomInfos.stream()
                .sorted(Comparator.comparingLong(IRoomInfo::getId))
                .collect(Collectors.toList());
    }

    if (multiNodeRoomInfos == null) {
        response.getWriter().println("multiNodeRoomInfos is null");
    } else {
%>
<hr>
<h1>Details multiNodeRoomInfos for Opened(Running) Rooms</h1>
<hr>
<table>
    <tr>
        <th>Room Id</th><th>Game</th><th>RoomInfo</th>
    </tr>
    <% for (IRoomInfo roomInfo : multiNodeRoomInfos) {
        IRoom room = null;
        if(gameId == -1 || gameId == roomInfo.getGameType().getGameId()) {
            List<ISeat> seats = null;

            if(roomServiceFactory != null) {
                room = roomServiceFactory.getRoom(roomInfo.getGameType(), roomInfo.getId());
                seats = room.getAllSeats();
            }

            if(seats == null) {
                seats = new ArrayList<>();
            }

            if(room != null) {
    %>
    <tr>
        <td><%=roomInfo.getId()%></td>
        <td><%=roomInfo.getGameType().getGameId() + ": " + roomInfo.getGameType().name() + ":seats=" + seats.size()%></td>
        <td><%=roomInfo%>
            <br/>
            <%
                for(ISeat seat :seats) {
            %>
            <br/>
            <%= seat %>
            <br/>
            <%
                }
            %>
        </td>
    </tr>
    <%      }
        }
    }
    }
    %>
</table>

<%
    if (multiNodePrivateRoomInfos == null) {
        response.getWriter().println("multiNodePrivateRoomInfos is null");
    } else {
%>
<hr>
<h1>Details multiNodePrivateRoomInfos for Opened(Running) Rooms</h1>
<hr>
<table>
    <tr>
        <th>Room Id</th>
        <th>Game</th>
        <th>RoomInfo</th>
    </tr>
    <% for (IRoomInfo roomInfo : multiNodePrivateRoomInfos) {
        IRoom room = null;
        if(gameId == -1 || gameId == roomInfo.getGameType().getGameId()) {
            List<ISeat> seats = null;
            if(roomServiceFactory != null) {
                room = roomServiceFactory.getRoom(roomInfo.getGameType(), roomInfo.getId());
                seats = room.getAllSeats();
            }
            if(seats == null) {
                seats = new ArrayList<>();
            }
            if(room != null) {
    %>
    <tr>
        <td><%=roomInfo.getId()%></td>
        <td><%=roomInfo.getGameType().getGameId() + ": " + roomInfo.getGameType().name() + ":seats=" + seats.size()%></td>
        <td><%=roomInfo%>
            <br/>
            <%
                for(ISeat seat :seats) {
            %>
            <br/>
            <%= seat %>
            <br/>
            <%
                }
            %>
        </td>
    </tr>
    <%      }
        }
    }
    }
    %>
</table>

</body>
</html>
