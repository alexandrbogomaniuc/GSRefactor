<%@ page import="com.betsoft.casino.mp.config.WebSocketRouter" %>
<%@ page import="com.betsoft.casino.mp.service.SingleNodeRoomInfoService" %>
<%@ page import="com.betsoft.casino.mp.model.GameType" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.Arrays" %>
<%! Set<Integer> allowedBanks = new HashSet<>(Arrays.asList(271, 583, 1728, 3618, 6274, 6275, 9138)); %>
<%
    String bank = request.getParameter("bankId");
    String gameId = request.getParameter("gameId");

    int bankId;
    try {
        bankId = Integer.parseInt(bank);
    } catch (Exception e) {
        response.getWriter().write("missed bankId param");
        return;
    }
    if (!allowedBanks.contains(bankId)) {
        response.getWriter().write("wrong bank id");
        return;
    }

    GameType gameType;
    try {
        gameType = GameType.getByGameId(Integer.parseInt(gameId));
    } catch (Exception e) {
        response.getWriter().write("missed gameId param");
        return;
    }

    SingleNodeRoomInfoService roomInfoService = WebSocketRouter.getApplicationContext().getBean(SingleNodeRoomInfoService.class);
    List<Long> roomIds = roomInfoService.getRoomIds(bankId, gameType);
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>Config TestStand</title>
    <style>
        body {
            font-family: sans-serif;
        }

        .hidden {
            visibility: hidden;
        }

        .table {
            display: flex;
            width: 800px;
            flex-direction: column;
        }

        .row {
            display: flex;
            height: 30px;
            align-items: center;
            padding: 10px;
        }

        .dark {
            background-color: #f9e335;
        }

        .column {
            width: 250px;
        }

        .column-2 {
            width: 120px;
        }

        input[type=button], ::-webkit-file-upload-button {
            width: 100px;
            height: 30px;
            font-weight: bold;
        }

        select {
            width: 185px;
            height: 30px;
            font-weight: bold;
            border-radius: 3px;
        }
    </style>
</head>
<body>
<div class="table">
    <div class="row dark">
        <div class="column">Room ID:</div>
        <div class="column">
            <select name="rooms" id="room-select">
                <option value="">-- Choose a room --</option>
                <% for (long roomId : roomIds) { %>
                <option value="<%=roomId%>"><%=roomId%>
                </option>
                <% } %>
            </select>
        </div>
    </div>
    <div class="hidden" id="info">
        <div class="row">
            <div class="column">Actual config:</div>
            <div class="column" id="config-date"></div>
            <div class="column-2"><input type="button" value="Download" onclick="downloadConfig()" id="download"></div>
            <div class="column-2"><input type="button" value="Remove" onclick="removeConfig()" id="remove"
                                         class="hidden"></div>
        </div>
        <div class="row dark">
            <div class="column">Select config (.json):</div>
            <div class="column"><input type="file" id="config" name="config"></div>
            <div class="column"><input type="button" value="Upload" onclick="uploadConfig()"></div>
        </div>
    </div>
</div>
<script type="text/javascript">
    let info = document.getElementById('info');
    let removeButton = document.getElementById('remove');
    let roomId;

    function hideInfo() {
        info.classList.add('hidden');
    }

    function showInfo() {
        info.classList.remove('hidden');
    }

    function loadInfo() {
        hideInfo();
        fetch('/support/loadRoomSpawnInfo.jsp?roomId=' + roomId)
            .then(response => response.json())
            .then(result => {
                console.log(result);
                document.getElementById('config-date').innerText = result.default ? 'Default' : result.date;
                if (result.default) {
                    removeButton.classList.add('hidden');
                } else {
                    removeButton.classList.remove('hidden');
                }
                showInfo();
            });
    }

    document.getElementById('room-select').onchange = function (event) {
        roomId = event.target.value;
        loadInfo();
    }

    function upload(file) {
        let xhr = new XMLHttpRequest();
        xhr.upload.onprogress = function (event) {
            console.log(event.loaded + ' / ' + event.total);
        }
        xhr.onload = xhr.onerror = function () {
            if (this.status === 200) {
                console.log("success");
            } else {
                alert("Error " + this.status + ": " + xhr.responseText);
            }
            window.location.reload();
        };

        xhr.open("POST", "/support/addSpawnConfig.jsp?gameId=<%=gameId%>&roomId=" + roomId, true);
        let formData = new FormData();
        formData.append("file", file);
        xhr.send(formData);
    }

    function downloadConfig() {
        window.open("/support/downloadSpawnConfig.jsp?gameId=<%=gameId%>&roomId=" + roomId, '_blank');
    }

    function removeConfig() {
        fetch('/support/removeSpawnConfig.jsp?gameId=<%=gameId%>&roomId=' + roomId, {
            method: 'POST',
            body: ""
        }).then(() => {
            loadInfo();
        });
    }

    function uploadConfig() {
        let input = document.getElementById('config');
        let file = input.files[0];
        if (file) {
            upload(file);
        }
        return false;
    }
</script>
</body>
</html>
