<%@ page import="com.betsoft.casino.mp.config.WebSocketRouter" %>
<%@ page import="com.betsoft.casino.mp.model.bots.BotConfigInfo" %>
<%@ page import="java.util.*" %>
<%@ page import="com.betsoft.casino.mp.model.bots.ActiveBot" %>
<%@ page import="com.betsoft.casino.mp.model.IRoomPlayerInfo" %>
<%@ page import="java.util.stream.Collectors" %>
<%@ page import="com.betsoft.casino.mp.service.*" %>
<%@ page import="com.betsoft.casino.mp.model.room.IRoomInfo" %>
<%@ page import="com.betsoft.casino.mp.model.GameType" %>
<%@ page import="java.math.BigDecimal" %>

<!DOCTYPE HTML>
<html>
<head>
    <meta charset="utf-8">
    <title>Ats Admin Panel</title>
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

    BotConfigInfoService botConfigInfoService = WebSocketRouter.getApplicationContext().getBean(BotConfigInfoService.class);
    BotManagerService botManagerService = WebSocketRouter.getApplicationContext().getBean(BotManagerService.class);
    Collection<BotConfigInfo> allBotConfigs = botConfigInfoService.getAll();

    try {
        String sortBy = request.getParameter("sortBy") == null ? "" : request.getParameter("sortBy");
        if(sortBy != null) {
            switch (sortBy.toLowerCase()) {
                case "nickname":
                    allBotConfigs = allBotConfigs.stream()
                            .sorted(Comparator.comparing(BotConfigInfo::getMqNickname))
                            .collect(Collectors.toList());
                    break;

                case "mqc":
                    allBotConfigs = allBotConfigs.stream()
                            .sorted(Comparator.comparing(BotConfigInfo::getMqcBalance))
                            .collect(Collectors.toList());
                    break;

                case "mmc":
                    allBotConfigs = allBotConfigs.stream()
                            .sorted(Comparator.comparing(BotConfigInfo::getMmcBalance))
                            .collect(Collectors.toList());
                    break;
                case "id":
                default:
                    allBotConfigs = allBotConfigs.stream()
                            .sorted(Comparator.comparing(BotConfigInfo::getId))
                            .collect(Collectors.toList());
                    break;
            }
        }
    } catch (Exception e) {

    }
%>
<h1>Ats Monitor Panel</h1>
<h3>MQ Battlegrounds Ats Monitor</h3>
<hr>
<table>
    <tr>
        <th><a href="<%="monitor.jsp?sortBy=id"%>">Id</a></th>
        <th><a href="<%="monitor.jsp?sortBy=nickname"%>">MQ Nickname</a></th>
        <th>Status</th>
        <th>In Game</th>
        <th><a href="<%="monitor.jsp?sortBy=mqc"%>">MQC</a></th>
        <th><a href="<%="monitor.jsp?sortBy=mmc"%>">MMC</a></th>
        <th>Stub/Fake ATS</th>
        <th>Details and pendings</th>
    </tr>
    <% for (BotConfigInfo bot : allBotConfigs) {
        ActiveBot activeBot = botManagerService.findActiveBotByBotId(bot.getId());

        IRoomPlayerInfo roomPlayer =  null;
        Collection<IRoomPlayerInfo> roomPlayers =  roomPlayerInfoService.getByNickname(bot.getMqNickname());
        if(roomPlayers != null) {
            Iterator<IRoomPlayerInfo> iterator = roomPlayers.iterator();
            if(iterator.hasNext()) {
                roomPlayer = roomPlayers.iterator().next();
            }
        }

        Long roomId = null;
        if(roomPlayer != null) {
            roomId = roomPlayer.getRoomId();
        }
        if(roomId == null && activeBot != null) {
            roomId = activeBot.getRoomId();
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
            gameType = roomInfo.getGameType();
        }
    %>
    <tr>
        <td><%=bot.getId()%></td>
        <td><%=bot.getMqNickname()%></td>
        <td <%=bot.isActive() ? "class=active" : "class=inactive"%>><%=bot.isActive() ? "ON" : "OFF"%></td>
        <td><%=roomId != null ? "roomId: " + roomId : ""%>
            <%=gameType != null ? "game: " + gameType.getGameId() + ": " + gameType.name() : ""%></td>
        <td><%=String.format("%,.2f", BigDecimal.valueOf(bot.getMqcBalance()).divide(BigDecimal.valueOf(100)))%></td>
        <td><%=String.format("%,.2f", BigDecimal.valueOf(bot.getMmcBalance()).divide(BigDecimal.valueOf(100)))%></td>
        <td><%=bot.isFake() ? "YES" : ""%></td>
        <td><%= roomPlayer != null && activeBot == null ? "STUCK ":""%>
            <%= roomPlayer != null ?
                    "Account=" + roomPlayer.getId() + ";" +
                            "BankId=" + roomPlayer.getBankId() + ";" +
                            "BuyIn=" + roomPlayer.getRoundBuyInAmount() + ";" +
                            (roomPlayer.isPendingOperation() ? roomPlayer.getLastOperationInfo() : "NO")
                :
                "NULL: NO" %>
        </td>
    </tr>
    <% } %>
</table>
</body>
</html>
