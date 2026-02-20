<%@ page import="com.betsoft.casino.mp.config.WebSocketRouter" %>
<%@ page import="com.betsoft.casino.mp.common.GameMapStore" %>
<%
    GameMapStore mapStore = WebSocketRouter.getApplicationContext().getBean(GameMapStore.class);
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
        <div class="column">Map ID:</div>
        <div class="column">
            <select name="rooms" id="room-select">
                <option value="">-- Choose map --</option>
                <% for (int mapId : mapStore.getMapIds()) { %>
                <option value="<%=mapId%>"><%=mapId%></option>
                <% } %>
            </select>
        </div>
    </div>
    <div class="hidden" id="info">
        <div class="row">
            <div class="column">Actual config:</div>
            <div class="column" id="config-date"></div>
            <div class="column-2"><input type="button" value="Download" onclick="downloadConfig()" id="download"></div>
            <div class="column-2"><input type="button" value="Remove" onclick="removeConfig()" id="remove" class="hidden"></div>
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
    let mapId;

    function hideInfo() {
        info.classList.add('hidden');
    }

    function showInfo() {
        info.classList.remove('hidden');
    }

    function loadInfo() {
        hideInfo();
        fetch('/support/loadMapInfo.jsp?mapId=' + mapId)
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
        mapId = event.target.value;
        loadInfo();
    }

    function upload(file) {
        let xhr = new XMLHttpRequest();
        xhr.upload.onprogress = function (event) {
            log(event.loaded + ' / ' + event.total);
        }
        xhr.onload = xhr.onerror = function () {
            if (this.status === 200) {
                console.log("success");
            } else {
                alert("Error " + this.status + ": " + xhr.responseText);
            }
            window.location.reload();
        };

        xhr.open("POST", "/support/addMapConfig.jsp?mapId=" + mapId, true);
        let formData = new FormData();
        formData.append("file", file);
        xhr.send(formData);
    }

    function downloadConfig() {
        window.open("/support/downloadMapConfig.jsp?mapId=" + mapId, '_blank');
    }

    function removeConfig() {
        fetch('/support/removeMapConfig.jsp?mapId=' + mapId, {
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
