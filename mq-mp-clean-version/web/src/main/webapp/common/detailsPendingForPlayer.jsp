<%@ page import="com.betsoft.casino.mp.config.WebSocketRouter" %>
<%@ page import="java.util.*" %>
<%@ page import="com.betsoft.casino.mp.model.IRoomPlayerInfo" %>
<%@ page import="com.dgphoenix.casino.common.util.string.StringUtils" %>
<%@ page import="org.apache.logging.log4j.LogManager" %>
<%@ page import="org.apache.logging.log4j.Logger" %>
<%@ page import="com.betsoft.casino.mp.service.*" %>
<%@ page import="com.betsoft.casino.mp.model.room.IRoomInfo" %>
<%@ page import="com.betsoft.casino.mp.model.GameType" %>
<%@ page import="com.betsoft.casino.mp.payment.IPendingOperation" %>

<%!
    static final Logger LOG = LogManager.getLogger(RoomPlayerInfoService.class);
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
    RoomPlayerInfoService roomPlayerInfoService = WebSocketRouter.getApplicationContext().getBean(RoomPlayerInfoService.class);

    AbstractRoomInfoService multiNodeRoomInfoService = WebSocketRouter.getApplicationContext().getBean(MultiNodeRoomInfoService.class);
    AbstractRoomInfoService singleNodeRoomInfoService = WebSocketRouter.getApplicationContext().getBean(SingleNodeRoomInfoService.class);
    AbstractRoomInfoService bgPrivateRoomInfoService =  WebSocketRouter.getApplicationContext().getBean(BGPrivateRoomInfoService.class);
    AbstractRoomInfoService multiNodePrivateRoomInfoService =  WebSocketRouter.getApplicationContext().getBean(MultiNodePrivateRoomInfoService.class);

    PendingOperationService pendingOperationService = WebSocketRouter.getApplicationContext().getBean(PendingOperationService.class);

    LOG.debug("detailsPendingForPlayer.jsp: query={}", request.getQueryString());
    String accountId = request.getParameter("accountId");
    Long accountIdLong = null;
    String sid = request.getParameter("SID");
    String gameSessionId = request.getParameter("gameSessionId");
    LOG.debug("detailsPendingForPlayer.jsp: accountId={}, gameSessionId={}, sid={}", accountId, gameSessionId, sid);


    Collection<IRoomPlayerInfo> roomPlayerInfos = null;
    Collection<IPendingOperation> pendingOperations = new ArrayList<>();

    if (!StringUtils.isTrimmedEmpty(accountId)) {
        accountIdLong = Long.parseLong(accountId);
        IRoomPlayerInfo socketClientInfo = roomPlayerInfoService.get(accountIdLong);
        if(socketClientInfo != null) {
            roomPlayerInfos = new ArrayList<>();
            roomPlayerInfos.add(socketClientInfo);
        }
    }

    if ((roomPlayerInfos == null || roomPlayerInfos.isEmpty()) && (!StringUtils.isTrimmedEmpty(sid))) {
        roomPlayerInfos = roomPlayerInfoService.getBySessionId(sid);
    }

    if ((roomPlayerInfos == null || roomPlayerInfos.isEmpty()) && (!StringUtils.isTrimmedEmpty(gameSessionId))) {
        roomPlayerInfos = roomPlayerInfoService.getByGameSessionId(Long.parseLong(gameSessionId));
    }

    if (roomPlayerInfos != null && !roomPlayerInfos.isEmpty() && accountIdLong == null) {
        IRoomPlayerInfo roomPlayerInfo = roomPlayerInfos.stream().findFirst().orElse(null);
        accountIdLong = roomPlayerInfo.getId();
    }

    if(accountIdLong != null) {
        IPendingOperation pendingOperation = pendingOperationService.get(accountIdLong);
        if(pendingOperation != null) {
            pendingOperations.add(pendingOperation);
        }
        accountId = String.valueOf(accountIdLong);
    } else {
        pendingOperations = pendingOperationService.getAll();
    }
%>

<h1>Details Pending Operation from pendingOperationService for accountId=<%=accountId%></h1>
<hr>
<table>
    <tr>
        <th>AccountId</th>
        <th>pendingOperation</th>
        <th>Details and Pending</th>
    </tr>
    <%
        for(IPendingOperation pendingOperation: pendingOperations) {
    %>
    <tr>
        <td><%=pendingOperation.getAccountId()%></td>
        <td><%=pendingOperation%></td>
        <td>
            <a href="<%="./removePendingOperationForPlayer.jsp?accountId=" + pendingOperation.getAccountId()%>">Remove Pending Operation in pendingOperationService</a>
        </td>
    </tr>
    <%
        }
    %>
</table>

<br/>

<hr>
<h1>Details Pending from (roomPlayerInfos)</h1>
<hr>
<%
    if (roomPlayerInfos == null || roomPlayerInfos.isEmpty()) {
%>
No roomPlayerInfos found
<%
} else {
%>
<table>
    <tr>
        <th>Id</th>
        <th>MQ Nickname</th>
        <th>In Game</th>
        <th>Details and Pending</th>
    </tr>
    <% for (IRoomPlayerInfo socketClientInfo : roomPlayerInfos) {

        Long roomId = null;
        if(socketClientInfo != null) {
            roomId = socketClientInfo.getRoomId();
        }

        GameType gameType = null;
        if(roomId != null) {
            IRoomInfo roomInfo = null;
            if (multiNodeRoomInfoService != null) {
                roomInfo = multiNodeRoomInfoService.getRoom(roomId);
            }
            if (roomInfo == null && singleNodeRoomInfoService != null) {
                roomInfo = singleNodeRoomInfoService.getRoom(roomId);
            }
            if (roomInfo == null && bgPrivateRoomInfoService != null) {
                roomInfo = bgPrivateRoomInfoService.getRoom(roomId);
            }
            if (roomInfo == null && multiNodePrivateRoomInfoService != null) {
                roomInfo = multiNodePrivateRoomInfoService.getRoom(roomId);
            }
            gameType = roomInfo.getGameType();
        }
    %>
    <tr>
        <td><%=socketClientInfo.getId()%></td>
        <td><%=socketClientInfo.getNickname()%></td>
        <td><%=roomId != null ? "roomId: " + roomId : ""%>
            <%=gameType != null ? "game: " + gameType.getGameId() + ": " + gameType.name() : ""%></td>
        <td><% if(socketClientInfo != null) { %>
            <%="Account=" + socketClientInfo.getId() + ";" + "BankId=" + socketClientInfo.getBankId() + ";" +
                    "BuyIn=" + socketClientInfo.getRoundBuyInAmount() + ";" +
                    (socketClientInfo.isPendingOperation() ? socketClientInfo.getLastOperationInfo() : "NO")%>

            <%if(socketClientInfo.isPendingOperation()) { %>
            <a href="<%="./removePendingForPlayer.jsp?accountId=" + socketClientInfo.getId()%>">Remove Pending in RoomPlayerInfo object</a></td>
        <% } %>

        <% } else { %>
        <%="NULL: NO"%>
        <% }  %>
        </td>
    </tr>
    <% }
    }
    %>

</table>
</body>
</html>
