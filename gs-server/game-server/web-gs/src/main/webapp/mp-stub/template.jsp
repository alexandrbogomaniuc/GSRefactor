<%@ page import="com.abs.casino.common.web.BaseAction" %>
<%@ page import="com.dgphoenix.casino.gs.GameServer" %>
<!DOCTYPE html>
<head>
    <title>Lobby Test Client</title>
    <link rel="stylesheet" href="style.css"/>
</head>
<body>
<div>
    <div class="button" id="connect">Connect</div>
    <div class="button" id="login">Login</div>
    <div class="button" id="rooms">Rooms</div>
    <div class="button" id="roomInfo">GetRoom</div>
    <div class="button" id="subRoom">SubRoom</div>
    <div class="button" id="unSubRoom">UnSubRoom</div>
    <div class="button" id="getStartUrl">StartUrl</div>
    <div class="button" id="leaderBoard">LeaderBoard</div>
    <div class="button" id="openRoom">OpenRoom</div>
    <div class="button" id="disconnect">Disconnect</div>
    <div class="button reset" id="reset">Reset</div>
</div>
<div>
    <textarea id="log" disabled></textarea>
</div>
<label>Name: <input type="text" id="name"></label>
<label>RoomId: <input type="text" id="roomId"></label>
<script>
    function getWebSocketUrl() {
        return "<%=request.getParameter(BaseAction.WEB_SOCKET_URL)%>";
    }

    function getSessionId() {
        return "<%=request.getParameter(BaseAction.SESSION_ID_ATTRIBUTE)%>";
    }

    function getServerId() {
        return "<%=GameServer.getInstance().getServerId()%>";
    }

    function getLang() {
        return "<%=request.getParameter(BaseAction.LANG_ID_ATTRIBUTE)%>";
    }
</script>
<script type="text/javascript" src="lobby.js"></script>
</body>
