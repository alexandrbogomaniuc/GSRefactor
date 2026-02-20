<%@ page import="com.betsoft.casino.mp.config.WebSocketRouter" %>
<%@ page import="java.util.*" %>
<%@ page import="com.betsoft.casino.mp.web.service.SitOutTask" %>
<%@ page import="org.apache.logging.log4j.LogManager" %>
<%@ page import="org.apache.logging.log4j.Logger" %>
<%@ page import="com.betsoft.casino.mp.data.service.ServerConfigService" %>
<%@ page import="com.dgphoenix.casino.kafka.dto.*" %>
<%@ page import="com.betsoft.casino.mp.model.GameType" %>
<%@ page import="com.betsoft.casino.mp.web.service.SocketService" %>
<%@ page import="com.dgphoenix.casino.common.util.string.StringUtils" %>
<%@ page import="com.betsoft.casino.mp.web.service.RoomServiceFactory" %>

<%!
    static final Logger LOG = LogManager.getLogger(SitOutTask.class);
%>


<!DOCTYPE HTML>
<html>
<head>
    <meta charset="utf-8">
    <title>All Servers Rooms</title>
    <style>
        body {
            margin: 0;
            background-color: #dcdcdc;
            font-size: 25px;
        }

        h1 {
            text-align: center;
            color: white;
            background-color: black;
            padding: 0;
            margin: 0;
        }

        table {
            text-align: center;
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
    SocketService socketService = WebSocketRouter.getApplicationContext().getBean(SocketService.class);
    RoomServiceFactory roomServiceFactory = WebSocketRouter.getApplicationContext().getBean(RoomServiceFactory.class);
    ServerConfigService serverConfigService = WebSocketRouter.getApplicationContext().getBean(ServerConfigService.class);

    LOG.debug("listRoomsOnServers.jsp: query={}", request.getQueryString());
    String gameIdAsString = request.getParameter("gameId");
    Long gameId = null;
    if(!StringUtils.isTrimmedEmpty(gameIdAsString)) {
        try {
            gameId = Long.parseLong(gameIdAsString);
        } catch (Exception exception) {
            LOG.debug("listRoomsOnServers.jsp: exception to parse gameId={} to long value", gameIdAsString, exception);
        }
    }
    int localServerId = serverConfigService.getServerId();

    List<GetServerRunningRoomsResponse> responsesFromServers = new ArrayList<>();
    try {
        Map<Long, RunningRoomDto> runningRoomsDtoMap =
                roomServiceFactory.getLocalServerRunningRooms(gameId);

        GetServerRunningRoomsResponse serverRunningRoomsResponseFromLocalServer =
                new GetServerRunningRoomsResponse(localServerId, runningRoomsDtoMap);

        responsesFromServers.add(serverRunningRoomsResponseFromLocalServer);
    } catch (Exception exception) {
        LOG.debug("listRoomsOnServers.jsp: exception to get serverRunningRoomsResponse for local server", exception);
    }

    try {
        List<GetServerRunningRoomsResponse> responsesFromRemoteServers = socketService.getAllRemoteServersRunningRooms(gameId);
        responsesFromServers.addAll(responsesFromRemoteServers);
    } catch (Exception exception) {
        LOG.debug("listRoomsOnServers.jsp: exception to get serverRunningRoomsResponse from remote servers", exception);
    }
%>

<h1>All Server Running Rooms</h1>
<h3>All Socket Client Infos</h3>
<hr>
<table id="serversTable">
        <tr>
            <th>Server ID</th>
            <th>Rooms</th>
        </tr>

        <% for (GetServerRunningRoomsResponse serverRunningRooms : responsesFromServers) {
            if(serverRunningRooms != null && serverRunningRooms.getRunningRoomsDtoMap() != null) {
                int serverId = serverRunningRooms.getServerId();
                Map<Long, RunningRoomDto> runningRoomDtoMap = new TreeMap<>(serverRunningRooms.getRunningRoomsDtoMap());
        %>
        <tr>
            <td><%=serverId%></td>
            <td>
                <table id="<%="gamesTable" + serverId%>">
                    <tr>
                        <th>RoomId</th>
                        <th>GameId</th>
                        <th>Observers</th>
                        <th>IsPrivate</th>
                    </tr>
                    <% for (Map.Entry<Long, RunningRoomDto> mapEntry : runningRoomDtoMap.entrySet()) {
                        RunningRoomDto runningRoomDto = mapEntry.getValue();
                        int roomGameId = runningRoomDto.getGameId();
                        GameType gameType = GameType.getByGameId(roomGameId);
                        Set<String> observers = runningRoomDto.getObservers();
                        int observersCount = observers != null ? observers.size() : 0;
                    %>
                    <tr>
                        <td><%=runningRoomDto.getRoomId()%></td>
                        <td><%=roomGameId + " : " + (gameType != null ? gameType.name() : "Unknown")%></td>
                        <td><%=observersCount + " : " + observers%></td>
                        <td><%=runningRoomDto.isPrivate()%></td>
                    </tr>
                    <%
                    }%>
                </table>
            </td>
        </tr>
        <%  }
        } %>
</table>
</body>
</html>
