<%@ page import="com.dgphoenix.casino.common.web.BaseAction" %>
<!DOCTYPE html>
<head>
    <title>Lobby Test Client</title>
    <link rel="stylesheet" href="style.css"/>
</head>
<body>
<div>
    <div class="button" id="connect">Connect</div>
    <div class="button" id="openRoom">OpenRoom</div>
    <div class="button" id="closeRoom">CloseRoom</div>
    <div class="button" id="sitIn">SitIn</div>
    <div class="button" id="sitOut">SitOut</div>
    <div class="button" id="buyIn">BuyIn</div>
    <div class="button" id="changeStake">ChangeStake</div>
    <div class="button" id="shot">Shot</div>
    <div class="button" id="fullInfo">FullInfo</div>
    <div class="button" id="disconnect">Disconnect</div>
    <div class="button reset" id="reset">Reset</div>
</div>
<div>
    <textarea id="log" disabled></textarea>
</div>
<div class="block">
    <label>Enable request log: <input type="checkbox" id="enableLog"></label>
    <label>RoomId: <input type="text" id="roomId" value="1"></label>
    <label>AmmoCount: <input type="text" id="ammoCount" value="1000"></label>
    <label>Stake: <input type="text" id="stake" value="10"></label>
    <h4>Shot:</h4>
    <label>EnemyId: <input type="text" id="enemyId" value="1"></label>
    <label>X: <input type="text" id="x" value="11"></label>
    <label>Y: <input type="text" id="y" value="21"></label>
</div>
<div class="block map">
    <canvas id="canvas" width="1305" height="738"></canvas>
</div>
<img id="bg" src="grid.png">
<img id="head" src="mummy-head-2.jpg">
    <%
    String webSocketUrl = request.getParameter(BaseAction.WEB_SOCKET_URL);
    String clientWebSocketUrl = webSocketUrl.replace("mplobby", "mpgame");
%>
<script>
    const ENEMY_SIZE = 2;
    var _log = document.getElementById('log');
    var rid = 0;
    var roomOpened = false;
    var enemies = {/*1:{x: 10, y: 10},2:{x: 11, y: 11},3:{x: 12, y: 12}*/};
    var logEnabled = false;

    function log(message) {
        if (logEnabled && message.indexOf("EnemiesMoved") === -1) {
            _log.innerHTML += '[' + new Date().toTimeString().substr(0, 8) + '] ' + message + '\n';
        }
    }

    document.getElementById('enableLog').addEventListener('change', function () {
        logEnabled = this.checked;
    });

    function request(message) {
        if (socket && socket.readyState === WebSocket.OPEN) {
            message.date = Date.now();
            message.rid = ++rid;
            log("Request: " + JSON.stringify(message));
            socket.send(JSON.stringify(message));
        } else {
            log('Not connected');
            roomOpened = false;
        }
    }
    function handleProtocolError(message) {
        switch (message.code) {
            case 1000:
                log('Failed to login');
                roomOpened = false;
                break;
        }
    }
    function response(message) {
        log("Response: " + message.data);
        var msg;
        try {
            msg = JSON.parse(message.data);
        } catch (e) {
            log('Unable to parse: "' + message.data + '"');
        }
        switch (msg.class) {
            case 'Error':
                handleProtocolError(msg);
                break;
            case 'NewEnemy':
                var enemy = msg.newEnemy;
                enemies[enemy.id] = {x: enemy.x, y: enemy.y};
                break;
            case 'EnemiesMoved':
                msg.enemyMoves.forEach(move => {
                    enemies[move.id].x = move.x;
                    enemies[move.id].y = move.y;
                });
                break;
            case 'FullGameInfo':
                msg.roomEnemies.forEach(enemy => {
                    enemies[enemy.id] = {x: enemy.x, y: enemy.y};
                });
                break;
            case 'GameStateChanged':
                switch (msg.state) {
                    case 'QUALIFY':
                        enemies = {};
                        break;
                }
        }
    }

    function closed() {
        log("Connection closed");
        roomOpened = false;
        socket = null;
    }

    var socket;
    document.getElementById('connect').addEventListener('click', function () {
        if (!socket) {
            socket = new WebSocket("<%=clientWebSocketUrl%>");
            socket.onerror = function (data) {
                log('Error: ' + data);
            };
            socket.onmessage = response;
            socket.onclose = closed;
        }
    });
    document.getElementById('openRoom').addEventListener('click', function () {
        if (!roomOpened) {
            roomOpened = true;
            var message = {
                "class": "OpenRoom",
                "sid": "<%=request.getParameter(BaseAction.SESSION_ID_ATTRIBUTE)%>",
                "roomId": document.getElementById('roomId').value,
                "serverId": <%=request.getParameter("serverId")%>,
                "lang": "<%=request.getParameter("lang")%>"
            };
            request(message);
        } else {
            log('Room already opened');
        }
    });
    document.getElementById('closeRoom').addEventListener('click', function () {
        request({
            "class": "CloseRoom",
            "roomId": document.getElementById('roomId').value
        });
    });
    document.getElementById('sitIn').addEventListener('click', function () {
        request({
            "class": "SitIn",
            "ammoAmount": document.getElementById('ammoCount').value,
            "stake": document.getElementById('stake').value,
            "lang": "<%=request.getParameter("lang")%>"
        });
    });
    document.getElementById('sitOut').addEventListener('click', function () {
        request({
            "class": "SitOut"
        });
    });
    document.getElementById('buyIn').addEventListener('click', function () {
        request({
            "class": "BuyIn",
            "ammoAmount": document.getElementById('ammoCount').value
        });
    });
    document.getElementById('changeStake').addEventListener('click', function () {
        request({
            "class": "ChangeStake",
            "stake": document.getElementById('stake').value
        });
    });
    document.getElementById('shot').addEventListener('click', function () {
        request({
            "class": "Shot",
            "enemyId": document.getElementById('enemyId').value,
            "x": document.getElementById('x').value,
            "y": document.getElementById('y').value
        });
    });
    document.getElementById('fullInfo').addEventListener('click', function () {
        request({
            "class": "GetFullGameInfo"
        });
    });

    var canvas = document.getElementById('canvas');
    var ctx = canvas.getContext('2d');
    var image = document.getElementById('bg');
    var head = document.getElementById('head');

    function draw() {
        ctx.drawImage(image, 0, 0, 1305, 738);
        Object.values(enemies).forEach(enemy => {
            ctx.drawImage(head, 10 + 27 * enemy.x, 5 + 27 * enemy.y, 27 * ENEMY_SIZE, 27 * ENEMY_SIZE);
        });
    }

    setInterval(draw, 100);
</script>