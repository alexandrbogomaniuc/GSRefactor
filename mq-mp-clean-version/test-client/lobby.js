var _log = document.getElementById('log');
var rid = 0;
var loggedIn = false;
var startGameUrl = null;
function log(message) {
    _log.innerHTML += '[' + new Date().toTimeString().substr(0, 8) + '] ' + message + '\n';
}
function request(message) {
    if (socket && socket.readyState === WebSocket.OPEN) {
        message.date = Date.now();
        message.rid = ++rid;
        log("Request: " + JSON.stringify(message));
        socket.send(JSON.stringify(message));
    } else {
        log('Not connected');
        loggedIn = false;
    }
}
function handleProtocolError(message) {
    switch (message.code) {
        case 1000:
            log('Failed to login');
            loggedIn = false;
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
        case 'GetStartGameUrlResponse':
            startGameUrl = msg.startGameUrl;
            break;
    }
}
function closed() {
    log("Connection closed");
    loggedIn = false;
    socket = null;
}
var socket;
document.getElementById('connect').addEventListener('click', function () {
    if (!socket) {
        socket = new WebSocket(getWebSocketUrl());
        socket.onerror = function (data) {
            log('Error: ' + data);
        };
        socket.onmessage = response;
        socket.onclose = closed;
    }
});
document.getElementById('login').addEventListener('click', function () {
    if (!loggedIn) {
        loggedIn = true;
        var message = {
            "sid": getSessionId(),
            "class": "EnterLobby",
            "nickname": document.getElementById('name').value,
            "serverId": getServerId()
        };
        request(message);
    } else {
        log('You already logged in');
    }
});
document.getElementById('disconnect').addEventListener('click', function () {
    if (socket) {
        socket.close();
    }
});
document.getElementById('rooms').addEventListener('click', function () {
    request({
        "type": "UNDISCOVERED_EGYPT",
        "class": "GetRooms"
    });
});
document.getElementById('roomInfo').addEventListener('click', function () {
    request({
        "roomId": document.getElementById('roomId').value,
        "class": "GetRoomInfo"
    });
});
document.getElementById('subRoom').addEventListener('click', function () {
    request({
        "roomId": document.getElementById('roomId').value,
        "class": "SubscribeRoomInfo"
    });
});
document.getElementById('unSubRoom').addEventListener('click', function () {
    request({
        "roomId": document.getElementById('roomId').value,
        "class": "UnsubscribeRoomInfo"
    });
});
document.getElementById('getStartUrl').addEventListener('click', function () {
    request({
        "roomId": document.getElementById('roomId').value,
        "class": "GetStartGameUrl"
    });
});
document.getElementById('reset').addEventListener('click', function () {
    if (socket) {
        socket.close();
    }
    loggedIn = false;
    _log.innerHTML = '';
});
document.getElementById('openRoom').addEventListener('click', function () {
    if (loggedIn && startGameUrl !== null) {
        window.open(startGameUrl, '_blank');
    } else {
        log('You should be logged in and get start game url first');
    }
});
document.getElementById('getLeaderboards').addEventListener('click', function () {
    request({
        "class": "GetLeaderboards"
    });
});
document.getElementById('getLeaderboard').addEventListener('click', function () {
    request({
        "class": "GetLeaderboardScores",
        "leaderboardId": 2,
        "from": 2,
        "to": 5
    });
});
document.getElementById('getLeaderboardPosition').addEventListener('click', function () {
    request({
        "class": "GetLeaderboardPosition",
        "leaderboardId": 2
    });
});