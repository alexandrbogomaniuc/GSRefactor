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
<%@ page import="static com.betsoft.casino.mp.model.bots.BotConfigInfo.MMC_BankId" %>
<%@ page import="static com.betsoft.casino.mp.model.bots.BotConfigInfo.MQC_BankId" %>

<!DOCTYPE HTML>
<html>
<head>
    <meta charset="utf-8">
    <title>All Ats Admin Panel</title>
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

    boolean isBotServiceEnabled = botConfigInfoService.isBotServiceEnabled();
    Collection<BotConfigInfo> botConfigInfos = botConfigInfoService.getAll();

    try {
        String sortBy = request.getParameter("sortBy") == null ? "" : request.getParameter("sortBy");
        if(sortBy != null) {
            switch (sortBy.toLowerCase()) {
                case "nickname":
                    botConfigInfos = botConfigInfos.stream()
                            .sorted(Comparator.comparing(BotConfigInfo::getMqNickname))
                            .collect(Collectors.toList());
                    break;

                case "mqc":
                    botConfigInfos = botConfigInfos.stream()
                            .sorted(Comparator.comparing(BotConfigInfo::getMqcBalance))
                            .collect(Collectors.toList());
                    break;

                case "mmc":
                    botConfigInfos = botConfigInfos.stream()
                            .sorted(Comparator.comparing(BotConfigInfo::getMmcBalance))
                            .collect(Collectors.toList());
                    break;
                case "id":
                default:
                    botConfigInfos = botConfigInfos.stream()
                            .sorted(Comparator.comparing(BotConfigInfo::getId))
                            .collect(Collectors.toList());
                    break;
            }
        }
    } catch (Exception e) {

    }
%>
<h1>All Ats Admin Panel</h1>
<hr>
<h3>Ats Service Admin</h3>
<hr>
<form>
    <input type="checkbox" id="botServiceEnabled" name="botServiceEnabled" <%=isBotServiceEnabled ? "checked" : ""%>>
    <label for="botServiceEnabled">Ats Service Enabled</label>
    <input type="button" value="Apply" onclick="setBotServiceEnabled()">
</form>
<hr>
<h3>MQ Battlegrounds Ats Admin</h3>
<hr>
<form action="addAts.jsp?action=add" method="post">
    <input type="submit" value="Add new Ats">
</form>
<form>
    <input type="text" name="usernameSearch">
    <input type="submit" value="Search">
</form>
<hr>
<table id="atsTable">
        <tr>
            <th>
                <form>
                    <input type="button" value="Sync Balance" onclick="syncFunc()">
                    <input type="checkbox" onclick="toggleCheckboxes(this, 'atsTable', 0)">
                </form>
            </th>
            <th>
                <form>
                    <input type="button" value="Sync 6274" onclick="syncFunc6274()">
                    <input type="checkbox" onclick="toggleCheckboxes(this, 'atsTable', 1)">
                </form>
            </th>
            <th>
                <form>
                    <input type="button" value="Sync 6275" onclick="syncFunc6275()">
                    <input type="checkbox" onclick="toggleCheckboxes(this, 'atsTable', 2)">
                </form>
            </th>
            <th><a href="<%="listAts.jsp?sortBy=id"%>">Id</a></th>
            <th><a href="<%="listAts.jsp?sortBy=nickname"%>">MQ Nickname</a></th>
            <th>
                <form>
                    <input type="button" value="Sync Active" onclick="syncFuncActive()">
                    <input type="checkbox" onclick="toggleCheckboxes(this, 'atsTable', 5)">
                </form>
            </th>
            <th>
                <form>
                    <input type="button" value="Sync MBC" onclick="syncFuncMBC()">
                    <input type="checkbox" onclick="toggleCheckboxes(this, 'atsTable', 6)">
                </form>
            </th>
            <th>
                <form>
                    <input id="SHOOT_RATE_VALUE" type="text" value="{867=0.0, 856=1.0, 862=1.0}">
                    <br/>
                    <input type="button" value="Sync Shoots Rates" onclick="syncShootsRates()">
                    <input type="checkbox" onclick="toggleCheckboxes(this, 'atsTable', 7)">
                </form>
            </th>
            <th>
                <form>
                    <input id="BULLET_RATE_VALUE" type="text" value="{867=0.9, 856=1.0, 862=1.0}">
                    <br/>
                    <input type="button" value="Sync Bullets Rates" onclick="syncBulletsRates()">
                    <input type="checkbox" onclick="toggleCheckboxes(this, 'atsTable', 8)">
                </form>
            </th>
            <th>
                <form>
                    <input id="ROOM_VALUE_6274" type="text" value="[100, 200]">
                    <br/>
                    <input type="button" value="Sync Room Value 6274" onclick="syncRoomValues6274()">
                    <input type="checkbox" onclick="toggleCheckboxes(this, 'atsTable', 9)">
                </form>
            </th>
            <th>
                <form>
                    <input id="ROOM_VALUE_6275" type="text" value="[100, 200]">
                    <br/>
                    <input type="button" value="Sync Room Value 6275" onclick="syncRoomValues6275()">
                    <input type="checkbox" onclick="toggleCheckboxes(this, 'atsTable', 10)">
                </form>
            </th>
            <th>Edit</th>
            <th>Details</th>
            <th>In Game</th>
            <th><a href="<%="listAts.jsp?sortBy=mqc"%>">MQC</a></th>
            <th><a href="<%="listAts.jsp?sortBy=mmc"%>">MMC</a></th>
            <th>Stub/Fake ATS</th>
            <th>Details and pendings</th>
            <th>Remove</th>
        </tr>
        <% for (BotConfigInfo botConfigInfo : botConfigInfos) {
            ActiveBot activeBot = botManagerService.findActiveBotByBotId(botConfigInfo.getId());

            IRoomPlayerInfo roomPlayer =  null;
            Collection<IRoomPlayerInfo> roomPlayers =  roomPlayerInfoService.getByNickname(botConfigInfo.getMqNickname());
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
            <td><input id="<%=botConfigInfo.getId()%>" type="checkbox" name="active"></td>
            <td><input id="<%="6274_" + botConfigInfo.getId()%>" type="checkbox" name="<%="6274_" + botConfigInfo.getId()%>" <%=botConfigInfo.getAllowedBankIds().contains(6274L) ? "checked" : ""%>></td>
            <td><input id="<%="6275_" + botConfigInfo.getId()%>" type="checkbox" name="<%="6275_" + botConfigInfo.getId()%>" <%=botConfigInfo.getAllowedBankIds().contains(6275L) ? "checked" : ""%>></td>
            <td><%=botConfigInfo.getId()%></td>
            <td><%=botConfigInfo.getMqNickname()%></td>
            <td <%=botConfigInfo.isActive() ? "class=active" : "class=inactive"%>>
                <input type="checkbox" id="ACTIVE_<%=botConfigInfo.getId()%>"
                       name="active_<%=botConfigInfo.getId()%>" <%=botConfigInfo.isActive() ? "checked" : ""%>>
               <!-- <label for="ACTIVE_<%=botConfigInfo.getId()%>"><%=botConfigInfo.isActive() ? "ON" : "OFF"%></label>-->
            </td>
            <td>
                <input type="checkbox" id="BG_MAXCRASHGAME_<%=botConfigInfo.getId()%>"
                       name="dragonstone_<%=botConfigInfo.getId()%>" <%=botConfigInfo.getAllowedGames().contains(GameType.BG_MAXCRASHGAME) ? "checked" : ""%>>
                <label for="BG_MAXCRASHGAME_<%=botConfigInfo.getId()%>"><%=GameType.BG_MAXCRASHGAME.name()%></label>
            </td>
            <td>
                <input type="checkbox" id="SHOOTING_RATE_<%=botConfigInfo.getId()%>" name="shootingRate<%=botConfigInfo.getId()%>" >
                <label for="SHOOTING_RATE_<%=botConfigInfo.getId()%>"><%=botConfigInfo.getShootsRates()%></label>
            </td>
            <td>
                <input type="checkbox" id="BULLET_RATE_<%=botConfigInfo.getId()%>" name="bulletRate<%=botConfigInfo.getId()%>" >
                <label for="BULLET_RATE_<%=botConfigInfo.getId()%>"><%=botConfigInfo.getBulletsRates()%></label>
            </td>
            <td>
                <input type="checkbox" id="ROOM_VALUE_6274_<%=botConfigInfo.getId()%>" name="roomValue6274_<%=botConfigInfo.getId()%>" >
                <label for="ROOM_VALUE_6274_<%=botConfigInfo.getId()%>"><%=botConfigInfo.getAllowedRoomValuesSet(MMC_BankId)%></label>
            </td>
            <td>
                <input type="checkbox" id="ROOM_VALUE_6275_<%=botConfigInfo.getId()%>" name="roomValue6275_<%=botConfigInfo.getId()%>" >
                <label for="ROOM_VALUE_6275_<%=botConfigInfo.getId()%>"><%=botConfigInfo.getAllowedRoomValuesSet(MQC_BankId)%></label>
            </td>
            <td><a href="<%="addAts.jsp?action=edit&id=" + botConfigInfo.getId()%>">Edit</a></td>
            <td><a href="<%="details.jsp?id=" + botConfigInfo.getId()%>">Details</a></td>
            <td><%=roomId != null ? "roomId: " + roomId : ""%><%=gameType != null ? "game: " + gameType.getGameId() + ": " + gameType.name() : ""%></td>
            <td><%=String.format("%,.2f", BigDecimal.valueOf(botConfigInfo.getMqcBalance()).divide(BigDecimal.valueOf(100)))%></td>
            <td><%=String.format("%,.2f", BigDecimal.valueOf(botConfigInfo.getMmcBalance()).divide(BigDecimal.valueOf(100)))%></td>
            <td><%=botConfigInfo.isFake() ? "YES" : ""%></td>
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
            <td>
                <form><input type="button" value="Remove" onclick="removeFunc(<%=botConfigInfo.getId()%>)"></form>
            </td>
        </tr>
        <% } %>
</table>

<script>

    function setBotServiceEnabled() {

        const checkbox = document.getElementById("botServiceEnabled");
        const enabled = checkbox && checkbox.checked;

        const link = 'enable_AtsService.jsp?enabled=' + enabled;

        const xhr = new XMLHttpRequest();
        xhr.open('POST', link);
        xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
        xhr.onload = function () {
            if (xhr.status === 200) {
                alert(xhr.response);
                window.location.reload();
            } else if (xhr.status !== 200) {
                alert('Cannot sync accounts ACTIVE to Ats system. ' + xhr.response);
            }
        };
        xhr.send();
    }

    function removeFunc(botId) {
        const link = 'remove.jsp?botId=' + botId;

        const xhr = new XMLHttpRequest();
        xhr.open('POST', link);
        xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
        xhr.onload = function () {
            if (xhr.status === 200) {
                alert(xhr.response);
                window.location.reload();
            } else if (xhr.status !== 200) {
                alert('Cannot remove account from Ats system. ' + xhr.response);
            }
        };
        xhr.send();
    }

    function syncFunc() {

        const idsSync = [];

        const atsTable = document.getElementById("atsTable");
        const rows = atsTable.getElementsByTagName("tr");

        // Loop through rows, skipping the header row
        for (let i = 1; i < rows.length; i++) {
            const cells = rows[i].getElementsByTagName("td"); // Get all cells in the row
            const checkbox = cells[0].querySelector("input[type='checkbox']"); // Find checkbox
            if (checkbox && checkbox.checked) {
                idsSync.push(checkbox.id); // Add checkbox ID if it is checked
            }
        }

        const link = 'sync_Balance.jsp?botIds=' + idsSync;

        const xhr = new XMLHttpRequest();
        xhr.open('POST', link);
        xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
        xhr.onload = function () {
            if (xhr.status === 200) {
                alert(xhr.response);
                window.location.reload();
            } else if (xhr.status !== 200) {
                alert('Cannot sync accounts Balances to Ats system. ' + xhr.response);
            }
        };
        xhr.send();
    }

    function syncFunc6274() {

        const idsSync = [];

        const atsTable = document.getElementById("atsTable");
        const rows = atsTable.getElementsByTagName("tr");

        // Loop through rows, skipping the header row
        for (let i = 1; i < rows.length; i++) {
            const cells = rows[i].getElementsByTagName("td"); // Get all cells in the row
            const checkbox = cells[1].querySelector("input[type='checkbox']"); // Find checkbox
            if (checkbox && checkbox.checked) {
                let prefix = "6274_";
                let result_id =  checkbox.id.startsWith(prefix) ?
                    checkbox.id.slice(prefix.length) : checkbox.id;
                idsSync.push(result_id); // Add checkbox ID if it is checked
            }
        }

        const link = 'sync_6274.jsp?botIds=' + idsSync;

        const xhr = new XMLHttpRequest();
        xhr.open('POST', link);
        xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
        xhr.onload = function () {
            if (xhr.status === 200) {
                alert(xhr.response);
                window.location.reload();
            } else if (xhr.status !== 200) {
                alert('Cannot sync accounts for bankId 6274 to Ats system. ' + xhr.response);
            }
        };
        xhr.send();
    }

    function syncFunc6275() {

        const idsSync = [];

        const atsTable = document.getElementById("atsTable");
        const rows = atsTable.getElementsByTagName("tr");

        // Loop through rows, skipping the header row
        for (let i = 1; i < rows.length; i++) {
            const cells = rows[i].getElementsByTagName("td"); // Get all cells in the row
            const checkbox = cells[2].querySelector("input[type='checkbox']"); // Find checkbox
            if (checkbox && checkbox.checked) {
                let prefix = "6275_";
                let result_id =  checkbox.id.startsWith(prefix) ?
                    checkbox.id.slice(prefix.length) : checkbox.id;
                idsSync.push(result_id); // Add checkbox ID if it is checked
            }
        }

        const link = 'sync_6275.jsp?botIds=' + idsSync;

        const xhr = new XMLHttpRequest();
        xhr.open('POST', link);
        xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
        xhr.onload = function () {
            if (xhr.status === 200) {
                alert(xhr.response);
                window.location.reload();
            } else if (xhr.status !== 200) {
                alert('Cannot sync accounts for bankId 6274 to Ats system. ' + xhr.response);
            }
        };
        xhr.send();
    }

    function syncFuncActive() {

        const idsSync = [];

        const atsTable = document.getElementById("atsTable");
        const rows = atsTable.getElementsByTagName("tr");

        // Loop through rows, skipping the header row
        for (let i = 1; i < rows.length; i++) {
            const cells = rows[i].getElementsByTagName("td"); // Get all cells in the row
            const checkbox = cells[5].querySelector("input[type='checkbox']"); // Find checkbox
            if (checkbox && checkbox.checked) {
                let prefix = "ACTIVE_";
                let result_id =  checkbox.id.startsWith(prefix) ?
                    checkbox.id.slice(prefix.length) : checkbox.id;
                idsSync.push(result_id); // Add checkbox ID if it is checked
            }
        }

        const link = 'sync_Active.jsp?botIds=' + idsSync;

        const xhr = new XMLHttpRequest();
        xhr.open('POST', link);
        xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
        xhr.onload = function () {
            if (xhr.status === 200) {
                alert(xhr.response);
                window.location.reload();
            } else if (xhr.status !== 200) {
                alert('Cannot sync accounts ACTIVE to Ats system. ' + xhr.response);
            }
        };
        xhr.send();
    }

    function syncFuncMBC() {

        const idsSync = [];

        const atsTable = document.getElementById("atsTable");
        const rows = atsTable.getElementsByTagName("tr");

        // Loop through rows, skipping the header row
        for (let i = 1; i < rows.length; i++) {
            const cells = rows[i].getElementsByTagName("td"); // Get all cells in the row
            const checkbox = cells[6].querySelector("input[type='checkbox']"); // Find checkbox
            if (checkbox && checkbox.checked) {
                let prefix = "BG_MAXCRASHGAME_";
                let result_id =  checkbox.id.startsWith(prefix) ?
                    checkbox.id.slice(prefix.length) : checkbox.id;
                idsSync.push(result_id); // Add checkbox ID if it is checked
            }
        }

        const link = 'sync_BG_MAXCRASHGAME.jsp?botIds=' + idsSync;

        const xhr = new XMLHttpRequest();
        xhr.open('POST', link);
        xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
        xhr.onload = function () {
            if (xhr.status === 200) {
                alert(xhr.response);
                window.location.reload();
            } else if (xhr.status !== 200) {
                alert('Cannot sync accounts BG_MAXCRASHGAME to Ats system. ' + xhr.response);
            }
        };
        xhr.send();
    }

    function syncShootsRates() {

        const idsSync = [];

        const atsTable = document.getElementById("atsTable");
        const rows = atsTable.getElementsByTagName("tr");

        // Loop through rows, skipping the header row
        for (let i = 1; i < rows.length; i++) {
            const cells = rows[i].getElementsByTagName("td"); // Get all cells in the row
            const checkbox = cells[7].querySelector("input[type='checkbox']"); // Find checkbox
            if (checkbox && checkbox.checked) {
                let prefix = "SHOOTING_RATE_";
                let result_id =  checkbox.id.startsWith(prefix) ?
                    checkbox.id.slice(prefix.length) : checkbox.id;
                idsSync.push(result_id); // Add checkbox ID if it is checked
            }
        }

        const shootingRates = document.getElementById("SHOOT_RATE_VALUE").value;
        const link = 'sync_SHOOTING_RATE.jsp?'
            + 'shootingRates=' + encodeURIComponent(shootingRates)
            + '&botIds=' + idsSync;

        const xhr = new XMLHttpRequest();
        xhr.open('POST', link);
        xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
        xhr.onload = function () {
            if (xhr.status === 200) {
                alert(xhr.response);
                window.location.reload();
            } else if (xhr.status !== 200) {
                alert('Cannot sync accounts SHOOTING_RATE to Ats system. ' + xhr.response);
            }
        };
        xhr.send();
    }

    function syncBulletsRates() {

        const idsSync = [];

        const atsTable = document.getElementById("atsTable");
        const rows = atsTable.getElementsByTagName("tr");

        // Loop through rows, skipping the header row
        for (let i = 1; i < rows.length; i++) {
            const cells = rows[i].getElementsByTagName("td"); // Get all cells in the row
            const checkbox = cells[8].querySelector("input[type='checkbox']"); // Find checkbox
            if (checkbox && checkbox.checked) {
                let prefix = "BULLET_RATE_";
                let result_id =  checkbox.id.startsWith(prefix) ?
                    checkbox.id.slice(prefix.length) : checkbox.id;
                idsSync.push(result_id); // Add checkbox ID if it is checked
            }
        }

        const bulletsRates = document.getElementById("BULLET_RATE_VALUE").value;
        const link = 'sync_BULLETS_RATE.jsp?'
            + 'bulletsRates=' + encodeURIComponent(bulletsRates)
            + '&botIds=' + idsSync;

        const xhr = new XMLHttpRequest();
        xhr.open('POST', link);
        xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
        xhr.onload = function () {
            if (xhr.status === 200) {
                alert(xhr.response);
                window.location.reload();
            } else if (xhr.status !== 200) {
                alert('Cannot sync accounts SHOOTING_RATE to Ats system. ' + xhr.response);
            }
        };
        xhr.send();
    }

    function syncRoomValues6274() {

        const idsSync = [];

        const atsTable = document.getElementById("atsTable");
        const rows = atsTable.getElementsByTagName("tr");

        // Loop through rows, skipping the header row
        for (let i = 1; i < rows.length; i++) {
            const cells = rows[i].getElementsByTagName("td"); // Get all cells in the row
            const checkbox = cells[9].querySelector("input[type='checkbox']"); // Find checkbox
            if (checkbox && checkbox.checked) {
                let prefix = "ROOM_VALUE_6274_";
                let result_id =  checkbox.id.startsWith(prefix) ?
                    checkbox.id.slice(prefix.length) : checkbox.id;
                idsSync.push(result_id); // Add checkbox ID if it is checked
            }
        }

        const allowedValues = document.getElementById("ROOM_VALUE_6274").value;
        const link = 'sync_6274_values.jsp?'
            + 'allowedValues=' + encodeURIComponent(allowedValues)
            + '&botIds=' + idsSync;

        const xhr = new XMLHttpRequest();
        xhr.open('POST', link);
        xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
        xhr.onload = function () {
            if (xhr.status === 200) {
                alert(xhr.response);
                window.location.reload();
            } else if (xhr.status !== 200) {
                alert('Cannot sync accounts ROOM VALUES 6274 to Ats system. ' + xhr.response);
            }
        };
        xhr.send();
    }

    function syncRoomValues6275() {

        const idsSync = [];

        const atsTable = document.getElementById("atsTable");
        const rows = atsTable.getElementsByTagName("tr");

        // Loop through rows, skipping the header row
        for (let i = 1; i < rows.length; i++) {
            const cells = rows[i].getElementsByTagName("td"); // Get all cells in the row
            const checkbox = cells[10].querySelector("input[type='checkbox']"); // Find checkbox
            if (checkbox && checkbox.checked) {
                let prefix = "ROOM_VALUE_6275_";
                let result_id =  checkbox.id.startsWith(prefix) ?
                    checkbox.id.slice(prefix.length) : checkbox.id;
                idsSync.push(result_id); // Add checkbox ID if it is checked
            }
        }

        const allowedValues = document.getElementById("ROOM_VALUE_6275").value;
        const link = 'sync_6275_values.jsp?'
            + 'allowedValues=' + encodeURIComponent(allowedValues)
            + '&botIds=' + idsSync;

        const xhr = new XMLHttpRequest();
        xhr.open('POST', link);
        xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
        xhr.onload = function () {
            if (xhr.status === 200) {
                alert(xhr.response);
                window.location.reload();
            } else if (xhr.status !== 200) {
                alert('Cannot sync accounts ROOM VALUES 6275 to Ats system. ' + xhr.response);
            }
        };
        xhr.send();
    }

    // Function to toggle all checkboxes in a column
    function toggleCheckboxes(mainCheckbox, tableId, columnIndex) {
        const table = document.getElementById(tableId); // Get the table by ID
        const rows = table.getElementsByTagName("tr"); // Get all rows in the table

        // Loop through rows, skipping the header row
        for (let i = 1; i < rows.length; i++) {
            const cells = rows[i].getElementsByTagName("td"); // Get all cells in the row
            if (cells.length > columnIndex) {
                const checkbox = cells[columnIndex].querySelector("input[type='checkbox']"); // Find checkbox
                if (checkbox) {
                    checkbox.checked = mainCheckbox.checked; // Toggle checkbox based on main checkbox
                }
            }
        }
    }
</script>
</body>
</html>
