<%@ page import="com.betsoft.casino.mp.config.WebSocketRouter" %>
<%@ page import="java.util.*" %>
<%@ page import="java.util.stream.Collectors" %>
<%@ page import="com.betsoft.casino.mp.service.*" %>
<%@ page import="com.betsoft.casino.mp.model.onlineplayer.SocketClientInfo" %>
<%@ page import="static com.betsoft.casino.utils.DateTimeUtils.toHumanReadableFormat" %>

<!DOCTYPE HTML>
<html>
<head>
    <meta charset="utf-8">
    <title>All Socket Client Infos Admin Panel</title>
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
    RoomPlayersMonitorService roomPlayersMonitorService = WebSocketRouter.getApplicationContext().getBean(RoomPlayersMonitorService.class);

    Map<String, SocketClientInfo> allSocketClientInfos = roomPlayersMonitorService.getMapSocketClientInfos();

    try {
        String sortBy = request.getParameter("sortBy") == null ? "" : request.getParameter("sortBy");
        if(sortBy != null) {
            switch (sortBy.toLowerCase()) {
                case "nickname":
                    allSocketClientInfos = allSocketClientInfos
                            .entrySet()
                            .stream()
                            .sorted(Comparator.comparing(entry -> entry.getValue().getNickname()))
                            .collect(Collectors.toMap(
                                    Map.Entry::getKey,
                                    Map.Entry::getValue,
                                    (oldValue, newValue) -> oldValue, // In case of duplicate keys
                                    LinkedHashMap::new // Preserve insertion order
                            ));
                    break;

                case "roomid":
                    allSocketClientInfos = allSocketClientInfos
                            .entrySet()
                            .stream()
                            .sorted(
                                    Map.Entry.comparingByValue(
                                            Comparator.comparingLong(SocketClientInfo::getRoomId)
                                                    .thenComparing(SocketClientInfo::getNickname)
                                    )
                            )
                            .collect(Collectors.toMap(
                                    Map.Entry::getKey,
                                    Map.Entry::getValue,
                                    (oldValue, newValue) -> oldValue, // In case of duplicate keys
                                    LinkedHashMap::new // Preserve insertion order
                            ));
                    break;

                case "gameid":
                    allSocketClientInfos = allSocketClientInfos
                            .entrySet()
                            .stream()
                            .sorted(
                                    Map.Entry.comparingByValue(
                                            Comparator.comparingLong(SocketClientInfo::getGameId)
                                                    .thenComparingLong(SocketClientInfo::getRoomId)
                                                    .thenComparing(SocketClientInfo::getNickname)
                                    )
                            )
                            .collect(Collectors.toMap(
                                    Map.Entry::getKey,
                                    Map.Entry::getValue,
                                    (oldValue, newValue) -> oldValue, // In case of duplicate keys
                                    LinkedHashMap::new // Preserve insertion order
                            ));
                    break;

                case "accountid":
                default:
                    allSocketClientInfos = allSocketClientInfos
                            .entrySet()
                            .stream()
                            .sorted(Comparator.comparing(entry -> entry.getValue().getAccountId()))
                            .collect(Collectors.toMap(
                                    Map.Entry::getKey,
                                    Map.Entry::getValue,
                                    (oldValue, newValue) -> oldValue, // In case of duplicate keys
                                    LinkedHashMap::new // Preserve insertion order
                            ));
                    break;
            }
        }
    } catch (Exception e) {

    }
%>
<h1>All Socket Client Infos Admin Panel</h1>
<h3>All Socket Client Infos</h3>
<hr>
<table id="sciTable">

        <tr>
            <th><a href="<%="listSocketClientInfos.jsp?sortBy=accountId"%>">accountId</a></th>
            <th><a href="<%="listSocketClientInfos.jsp?sortBy=nickname"%>">nickname</a></th>
            <th>serverId</th>
            <th><a href="<%="listSocketClientInfos.jsp?sortBy=roomId"%>">roomId</a></th>
            <th><a href="<%="listSocketClientInfos.jsp?sortBy=gameId"%>">gameId</a></th>
            <th>gameName</th>
            <th>isOwner</th>
            <th>seatNr</th>
            <th>isPrivate</th>
            <th>isBattleGround</th>
            <th>buyInStake</th>
            <th>currency</th>
            <th>setAt</th>
            <th>externalId</th>
            <th>socketId</th>
            <th>sessionId</th>
            <th>Remove</th>
        </tr>

        <% for (Map.Entry<String, SocketClientInfo> sci : allSocketClientInfos.entrySet()) { %>

        <tr>
            <td><%=sci.getValue().getAccountId()%></td>
            <td><%=sci.getValue().getNickname()%></td>
            <td><%=sci.getValue().getServerId()%></td>
            <td><%=sci.getValue().getRoomId()%></td>
            <td><%=sci.getValue().getGameId()%></td>
            <td><%=sci.getValue().getGameName()%></td>
            <td><%=sci.getValue().isOwner()%></td>
            <td><%=sci.getValue().getSeatNr()%></td>
            <td><%=sci.getValue().isPrivate()%></td>
            <td><%=sci.getValue().isBattleGround()%></td>
            <td><%=sci.getValue().getBuyInStake()%></td>
            <td><%=sci.getValue().getCurrency()%></td>
            <td><%=toHumanReadableFormat(sci.getValue().getSetAt(), "yyyy-MM-dd HH:mm:ss.SSS")%></td>
            <td><%=sci.getValue().getExternalId()%></td>
            <td><%=sci.getKey()%></td>
            <td><%=sci.getValue().getSessionId()%></td>
            <td></td>
        </tr>
        <% } %>
</table>
</body>
</html>
