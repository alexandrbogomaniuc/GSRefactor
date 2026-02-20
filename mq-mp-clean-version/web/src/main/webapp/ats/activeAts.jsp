<%@ page import="com.betsoft.casino.mp.config.WebSocketRouter" %>
<%@ page import="com.betsoft.casino.mp.model.bots.BotConfigInfo" %>
<%@ page import="java.util.*" %>
<%@ page import="com.betsoft.casino.mp.model.bots.ActiveBot" %>
<%@ page import="com.betsoft.casino.mp.model.IRoomPlayerInfo" %>
<%@ page import="com.betsoft.casino.mp.service.*" %>
<%@ page import="com.betsoft.casino.mp.model.room.IRoomInfo" %>
<%@ page import="com.betsoft.casino.mp.model.GameType" %>
<%@ page import="java.math.BigDecimal" %>
<%@ page import="com.dgphoenix.casino.common.util.Pair" %>
<%@ page import="static com.betsoft.casino.utils.DateTimeUtils.toHumanReadableFormat" %>
<%@ page import="java.util.stream.Collectors" %>
<%@ page import="com.betsoft.casino.mp.model.bots.dto.SimpleBot" %>

<!DOCTYPE HTML>
<html>
<head>
    <meta charset="utf-8">
    <title>Active Ats Admin Panel</title>
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

    BotManagerService botManagerService = WebSocketRouter.getApplicationContext().getBean(BotManagerService.class);
    BotConfigInfoService botConfigInfoService = WebSocketRouter.getApplicationContext().getBean(BotConfigInfoService.class);

    Map<Long, Pair<Integer, Long>> botsRequiredInShootingRooms = botManagerService.getBotsRequiredInShootingRooms();
    Collection<ActiveBot> activeBots = botManagerService.getAllActiveBots();
    Collection<SimpleBot> botsMap = botManagerService.getBotsMap();
    botsMap = botsMap
            .stream()
            .sorted(Comparator.comparing(SimpleBot::getId))
            .collect(Collectors.toList());

    try {
        String sortBy = request.getParameter("sortBy") == null ? "" : request.getParameter("sortBy");
        if(sortBy != null) {
            switch (sortBy.toLowerCase()) {

                case "accountid":
                    activeBots = activeBots
                            .stream()
                            .sorted(Comparator.comparingLong(ActiveBot::getAccountId))
                            .collect(Collectors.toList());
                    break;

                case "roomid":
                    activeBots = activeBots
                            .stream()
                            .sorted(
                                    Comparator.comparingLong(ActiveBot::getRoomId)
                                            .thenComparingLong(ActiveBot::getAccountId)
                            )
                            .collect(Collectors.toList());
                    break;

                case "gameid":
                    activeBots = activeBots
                            .stream()
                            .sorted(
                                    Comparator.comparingLong(ActiveBot::getGameId)
                                            .thenComparingLong(ActiveBot::getRoomId)
                                            .thenComparingLong(ActiveBot::getAccountId)
                            )
                            .collect(Collectors.toList());
                    break;

                case "botid":
                default:
                    activeBots = activeBots
                            .stream()
                            .sorted(Comparator.comparingLong(ActiveBot::getBotId))
                            .collect(Collectors.toList());
                    break;
            }
        }
    } catch (Exception e) {

    }
%>
<h1>Bots Active Statuses</h1>
<h3>Ats Service</h3>
    <input type="checkbox" id="botServiceEnabled" name="botServiceEnabled" <%=botManagerService.isBotServiceEnabled() ? "checked" : ""%> disabled>
    <label for="botServiceEnabled">Ats Service Enabled</label>
<hr>
<h3>Bots Required In Shooting Rooms Panel</h3>
<table id="botsRequiredInShootingRooms">
    <tr>
        <th>RoomId</th>
        <th>NumberOfBotsRequired</th>
        <th>ExpiresAt</th>
    </tr>
    <% for (Map.Entry entry : botsRequiredInShootingRooms.entrySet()) {%>
    <tr>
        <td><%=entry.getKey()%></td>
        <td><%=((Pair<Integer, Long>)entry.getValue()).getKey()%></td>
        <td><%=toHumanReadableFormat(((Pair<Integer, Long>)entry.getValue()).getValue(), "yyyy-MM-dd HH:mm:ss.SSS")%></td>
    </tr>
    <% } %>
</table>
<hr>

<h3>BotManagerService: activeBots</h3>
<hr>
<table id="activeBots">
    <tr>
        <th><a href="<%="activeAts.jsp?sortBy=botId"%>">BotId</a></th>
        <th><a href="<%="activeAts.jsp?sortBy=accountId"%>">AccountId</a></th>
        <th>MQ Nickname</th>
        <th><a href="<%="activeAts.jsp?sortBy=roomId"%>">RoomId</a></th>
        <th><a href="<%="activeAts.jsp?sortBy=gameId"%>">GameId</a></th>
        <th>Status</th>
        <th>In Game</th>
        <th>ExpiresAt</th>
        <th>MQC</th>
        <th>MMC</th>
        <th>Stub/Fake ATS</th>
        <th>Details and pending</th>
    </tr>
    <%

        for (ActiveBot activeBot : activeBots) {

        BotConfigInfo botConfig = botConfigInfoService.get(activeBot.getBotId());

        IRoomPlayerInfo roomPlayer = null;
        Collection<IRoomPlayerInfo> roomPlayers = roomPlayerInfoService.getByNickname(botConfig.getMqNickname());
        if (roomPlayers != null) {
            Iterator<IRoomPlayerInfo> iterator = roomPlayers.iterator();
            if (iterator.hasNext()) {
                roomPlayer = roomPlayers.iterator().next();
            }
        }

        Long roomId = null;
        if (roomPlayer != null) {
            roomId = roomPlayer.getRoomId();
        }
        if (roomId == null) {
            roomId = activeBot.getRoomId();
        }

        GameType gameType = null;
        if (roomId != null) {
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
            gameType = roomInfo.getGameType();
        }
    %>
    <tr>
        <td><%=botConfig.getId()%></td>
        <td><%=activeBot.getAccountId()%></td>
        <td><%=botConfig.getMqNickname()%></td>
        <td><%=roomId%></td>
        <td><%=gameType != null ? gameType.getGameId() : ""%></td>
        <td <%=botConfig.isActive() ? "class=active" : "class=inactive"%>><%=botConfig.isActive() ? "ON" : "OFF"%></td>
        <td><%=gameType != null ? gameType.name() : ""%></td>
        <td><%=toHumanReadableFormat(activeBot.getExpiresAt(), "yyyy-MM-dd HH:mm:ss.SSS")%></td>
        <td><%=String.format("%,.2f", BigDecimal.valueOf(botConfig.getMqcBalance()).divide(BigDecimal.valueOf(100)))%></td>
        <td><%=String.format("%,.2f", BigDecimal.valueOf(botConfig.getMmcBalance()).divide(BigDecimal.valueOf(100)))%></td>
        <td><%=botConfig.isFake() ? "YES" : ""%>
        </td>
        <td>
            <% if (roomPlayer != null) { %>
                <%="Account=" + roomPlayer.getId() + ";" + "BankId=" + roomPlayer.getBankId() + ";" + "BuyIn=" + roomPlayer.getRoundBuyInAmount() + ";" + (roomPlayer.isPendingOperation() ? roomPlayer.getLastOperationInfo() : "NO")%>
                <%  if (roomPlayer.isPendingOperation()) { %>
                    <a href="<%="../common/removePendingForPlayer.jsp?accountId=" + roomPlayer.getId()%>">Remove Pending in RoomPlayerInfo object</a>
                <% } %>
            <% } else { %>
                <%="NULL: NO"%>
            <% } %>
        </td>
    </tr>
    <% } %>
</table>
<hr>

<h3>BotService: botsMap</h3>
<hr>
<table id="botsMap">
    <tr>
        <th>BotId</th>
        <th>Type</th>
        <th>MQ Nickname</th>
        <th>RoomId</th>
        <th>BankId</th>
        <th>GameId</th>
        <th>ServerId</th>
        <th>ExpiresAt</th>
        <th>BotState</th>
        <th>Token</th>
        <th>Url</th>
        <th>Sid</th>
    </tr>
    <%
        for (SimpleBot tBot : botsMap) {
    %>
    <tr>
        <td><%=tBot.getId()%></td>
        <td>LOBBY</td>
        <td><%=tBot.getNickname()%></td>
        <td><%=tBot.getRoomId()%></td>
        <td><%=tBot.getBankId()%></td>
        <td><%=tBot.getGameId()%></td>
        <td><%=tBot.getServerId()%></td>
        <td><%=toHumanReadableFormat(tBot.getExpiresAt(), "yyyy-MM-dd HH:mm:ss.SSS")%></td>
        <td><%=tBot.getBotState()%></td>
        <td><%=tBot.getToken()%></td>
        <td><%=tBot.getUrl()%></td>
        <td><%=tBot.getSid()%></td>
    </tr>
    <% if(tBot.getRoomBot() != null) {
       %>
    <tr>
        <td><%=tBot.getRoomBot().getId()%></td>
        <td>ROOM</td>
        <td><%=tBot.getRoomBot().getNickname()%></td>
        <td><%=tBot.getRoomBot().getRoomId()%></td>
        <td><%=tBot.getRoomBot().getBankId()%></td>
        <td><%=tBot.getRoomBot().getGameId()%></td>
        <td><%=tBot.getRoomBot().getServerId()%></td>
        <td><%=toHumanReadableFormat(tBot.getRoomBot().getExpiresAt(), "yyyy-MM-dd HH:mm:ss.SSS")%></td>
        <td><%=tBot.getRoomBot().getBotState()%></td>
        <td><%=tBot.getRoomBot().getToken()%></td>
        <td><%=tBot.getRoomBot().getUrl()%></td>
        <td><%=tBot.getRoomBot().getSid()%></td>
    </tr>
    <%}
       } %>

</table>

</body>
</html>
