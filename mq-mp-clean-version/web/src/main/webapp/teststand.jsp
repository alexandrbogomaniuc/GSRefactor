<%@ page import="com.betsoft.casino.teststand.TestStandLocal" %>
<%@ page import="com.betsoft.casino.teststand.TestStandFeature" %>
<%@ page import="java.util.Collection" %>
<%@ page import="com.dgphoenix.casino.common.util.string.StringUtils" %>
<%@ page import="com.betsoft.casino.mp.service.RoomPlayerInfoService" %>
<%@ page import="com.betsoft.casino.mp.config.WebSocketRouter" %>
<%@ page import="org.springframework.context.ApplicationContext" %>
<%@ page import="com.betsoft.casino.mp.service.SingleNodeRoomInfoService" %>
<%@ page import="com.betsoft.casino.mp.model.*" %>
<%@ page import="com.betsoft.casino.mp.model.room.IRoomInfo" %>
<%@ page import="com.betsoft.casino.mp.sectorx.model.math.EnemyRange" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="com.betsoft.casino.mp.service.BGPrivateRoomInfoService" %>

<!DOCTYPE HTML>
<html>
<head>
    <meta charset="utf-8">
    <title>Simple TestStand</title>
</head>
<body>

<%
    String sid = request.getParameter("sid");
    ApplicationContext appContext = WebSocketRouter.getApplicationContext();
    RoomPlayerInfoService playerInfoService = appContext.getBean("playerInfoService", RoomPlayerInfoService.class);
    SingleNodeRoomInfoService roomInfoService = appContext.getBean("singleNodeRoomInfoService", SingleNodeRoomInfoService.class);
    BGPrivateRoomInfoService bgPrivateRoomInfoService = appContext.getBean("bgPrivateRoomInfoService", BGPrivateRoomInfoService.class);

    long roomId = -1;
    Integer bossForRoom = -1;
    boolean bossExists = false;
    String[] bosses = null;
    GameType gameType = null;
    boolean enemiesExists = false;
    List<String> enemies = new ArrayList<>();
    Integer enemyIdForRoom = -1;

    if (!StringUtils.isTrimmedEmpty(sid)) {
        Collection<IRoomPlayerInfo> bySessionId = playerInfoService.getBySessionId(sid);
        if (bySessionId != null && !bySessionId.isEmpty()) {
            IRoomPlayerInfo roomPlayerInfo = bySessionId.iterator().next();
            roomId = roomPlayerInfo.getRoomId();
            IRoomInfo roomInfo = roomInfoService.getRoom(roomId);
            if (roomInfo == null) {
                roomInfo = bgPrivateRoomInfoService.getRoom(roomId);
            }
            gameType = roomInfo.getGameType();
            bossForRoom = TestStandLocal.getInstance().getBossForRoom(roomId);
            bossExists = bossForRoom != -1;
            if (gameType.equals(GameType.PIRATES) || gameType.equals(GameType.PIRATES_POV) || gameType.equals(GameType.DMC_PIRATES)) {
                bosses = new String[]{"Captain Brutus", "Alure The Siren", "The Leviathan"};
            } else if (gameType.equals(GameType.REVENGE_OF_RA)) {
                bosses = new String[]{"Anubis", "Osiris", "Thoth"};
            } else if (gameType.equals(GameType.AMAZON) || gameType.equals(GameType.MISSION_AMAZON) || gameType.equals(GameType.BG_MISSION_AMAZON)) {
                bosses = new String[]{"Spider Queen", "Rock Golem", "Ape King"};
            } else if (gameType.equals(GameType.DRAGONSTONE) || gameType.equals(GameType.BG_DRAGONSTONE)) {
                bosses = new String[]{"Dragon"};
            } else if (gameType.equals(GameType.CLASH_OF_THE_GODS)) {
                bosses = new String[]{"LionSnake", "TaoWu", "Bull_Demon"};
            } else if (gameType.equals(GameType.SECTOR_X) || gameType.equals(GameType.BG_SECTOR_X)) {
                bosses = new String[]{"Boss 1", "Boss 2", "Boss 3", "Boss 4"};
            }

            enemyIdForRoom = TestStandLocal.getInstance().getEnemyIdForRoom(roomId);
            enemiesExists =  enemyIdForRoom != -1;

            if(gameType.equals(GameType.SECTOR_X) || gameType.equals(GameType.BG_SECTOR_X)){
                EnemyRange.SPECIAL_ITEMS.getEnemies().forEach(enemyType -> enemies.add(enemyType.getName()));
                EnemyRange.HUGE_PAY_ENEMIES.getEnemies().forEach(enemyType -> enemies.add(enemyType.getName()));
            }
        }
    }
%>

<script type="text/javascript">
    function sendFeature() {
        var selectFeature = document.getElementById("selectFeature");
        var selectedIndex = selectFeature.selectedIndex;
        var link;

        if (selectedIndex === 0) {
            link = '/support/teststandfeatures.jsp?action=remove&sid=' + '<%=sid%>';
        } else {
            link = '/support/teststandfeatures.jsp?action=add&sid=' + '<%=sid%>'
                + '&featureId=' + selectFeature.options[selectedIndex].id;
        }

        var xhr = new XMLHttpRequest();
        xhr.open('POST', link);
        xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
        xhr.onload = function () {
            if (xhr.status === 200) {
                window.location.reload();
            } else if (xhr.status !== 200) {
                alert('Request failed.  Returned status of ' + xhr.status);
            }
        };
        xhr.send();
    }

    function sendBossFeature() {
        var selectFeature = document.getElementById("selectFeatureBoss");
        var selectedIndex = selectFeature.selectedIndex;
        var link;

        if (selectedIndex === 0) {
            link = '/support/teststandfeatures.jsp?action=removeBoss&sid=' + '<%=sid%>'
                + '&roomId=' + '<%=roomId%>';
            ;
        } else {
            var id = selectFeature.options[selectedIndex].id;
            link = '/support/teststandfeatures.jsp?action=addBoss&sid=' + '<%=sid%>'
                + '&bossId=' + id
                + '&roomId=' + '<%=roomId%>';
        }

        var xhr = new XMLHttpRequest();
        xhr.open('POST', link);
        xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
        xhr.onload = function () {
            if (xhr.status === 200) {
                window.location.reload();
            } else if (xhr.status !== 200) {
                alert('Request failed.  Returned status of ' + xhr.status);
            }
        };
        xhr.send();
    }

    function sendEnemyIdFeature() {
        var selectFeature = document.getElementById("selectFeatureEnemyId");
        var selectedIndex = selectFeature.selectedIndex;
        var link;

        if (selectedIndex === 0) {
            link = '/support/teststandfeatures.jsp?action=removeEnemy&sid=' + '<%=sid%>'
                    + '&roomId=' + '<%=roomId%>';
            ;
        } else {
            var id = selectFeature.options[selectedIndex].id;
            link = '/support/teststandfeatures.jsp?action=addEnemyId&sid=' + '<%=sid%>'
                    + '&enemyId=' + id
                    + '&roomId=' + '<%=roomId%>';
        }

        var xhr = new XMLHttpRequest();
        xhr.open('POST', link);
        xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
        xhr.onload = function () {
            if (xhr.status === 200) {
                window.location.reload();
            } else if (xhr.status !== 200) {
                alert('Request failed.  Returned status of ' + xhr.status);
            }
        };
        xhr.send();
    }


    function sendMinEnemiesModeFeature() {
        var link = '/support/teststandfeatures.jsp?action=addMinEnemiesModeForRoom&sid=' + '<%=sid%>'
            + '&roomId=' + '<%=roomId%>';

        var xhr = new XMLHttpRequest();
        xhr.open('POST', link);
        xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
        xhr.onload = function () {
            if (xhr.status === 200) {
                window.location.reload();
            } else if (xhr.status !== 200) {
                alert('Request failed.  Returned status of ' + xhr.status);
            }
        };
        xhr.send();
    }

    function spawnEnemy() {
        let gameId = document.getElementById('gameId').value;
        let roomId = document.getElementById('roomId').value;
        let typeId = document.getElementById('typeId').value;
        let skinId = document.getElementById('skinId').value;
        let trajectory = document.getElementById('trajectory').value;
        let fixTime = document.getElementById('fixTime').checked;
        let data = {
            gameId: gameId,
            roomId: roomId,
            typeId: typeId,
            skinId: skinId,
            trajectory: trajectory,
            fixTime: fixTime
        };
        fetch('/support/spawnEnemy.jsp', {
            method: 'POST',
            body: JSON.stringify(data)
        }).then(response => console.log(response));
    }

    function setSpawnProbability() {
        let roomId = document.getElementById('roomId').value;
        let spawnProb = document.getElementById('spawnProbability').value;
        fetch('/support/setRoomProperties.jsp?roomId=' + roomId + '&spawnProbability=' + spawnProb)
            .then(response => console.log(response));
    }
</script>

<%
    if (sid != null) {
        TestStandLocal testStandLocal = TestStandLocal.getInstance();
        TestStandFeature featureBySid = testStandLocal.getFeatureBySid(sid);
        boolean featureExists = featureBySid != null;
%>

<div>
    <form name="feature" style="padding: 0;margin: 0;">
        <select id="selectFeature" name="selectFeature">
            <option <%if (!featureExists) {%> selected="selected"<%}%>>No feature</option>
            <%
                Collection<TestStandFeature> possibleFeatures = gameType == null ? testStandLocal.getAllPossibleFeatures() :
                        testStandLocal.getAllPossibleFeatures(gameType.getGameId());
                for (TestStandFeature possibleFeature : possibleFeatures) {
                    if (gameType != null && (possibleFeature.getId() < 200 || gameType.equals(GameType.AMAZON) || gameType.equals(GameType.MISSION_AMAZON))
                    ) {

            %>
            <option id="<%=possibleFeature.getId()%>" <%if (featureExists && featureBySid.getId() == possibleFeature.getId()) {%>
                    selected="selected"<%}%>>
                <%=possibleFeature.getName()%>
            </option>
            <%
                    }
                }
            %>
        </select>
        <input id="feature" type="button" value="Update teststand" onclick="sendFeature()"/>
    </form>

    <%if (roomId != -1) {%>
    <form name="featureBoss" style="padding: 0;margin: 0;">
        <select id="selectFeatureBoss" name="selectFeature">
            <option <%if (!bossExists) {%> selected="selected"<%}%>>No Boss</option>
            <%
                int idx = 1;
                for (String boss : bosses) {
            %>
            <option id="<%=idx%>" <%if (bossExists && idx == bossForRoom) {%> selected="selected"<%}%>>
                <%=boss%>
            </option>
            <%
                    idx++;
                }
            %>
        </select>
        <input id="featureBoss" type="button" value="Update Boss" onclick="sendBossFeature()"/>
    </form>

    <form name="featureEnemy" style="padding: 0;margin: 0;">
        <select id="selectFeatureEnemyId" name="selectFeatureEnemy">
            <option <%if (!enemiesExists) {%> selected="selected"<%}%>>No Enemy</option>
            <%
                idx = 1;
                for (String enemy : enemies) {
            %>
            <option id="<%=idx%>" <%if (enemiesExists && idx == enemyIdForRoom) {%> selected="selected"<%}%>>
                <%=enemy%>
            </option>
            <%
                    idx++;
                }
            %>
        </select>
        <input id="featureEnemy" type="button" value="Update requested EnemyId" onclick="sendEnemyIdFeature()"/>
    </form>

    <form name="addMinimalEnemiesModeForRoom" style="padding: 0;margin: 0;">
        <input id="EnemiesModeForRoom" type="button" value="Add minimal number of enemies mode for room (one round)"
               onclick="sendMinEnemiesModeFeature()"/>
    </form>

    <%}%>

    <div>
        <h3>Spawn enemy</h3>
        <label>GameId: <input type="number" id="gameId"></label>
        <label>RoomId: <input type="number" id="roomId"></label>
        <label>TypeId: <input type="number" id="typeId"></label>
        <label>SkinId: <input type="number" id="skinId"></label><br>
        <label>Trajectory: <textarea id="trajectory"></textarea></label><br>
        <label>FixTime: <input type="checkbox" id="fixTime" checked="checked"></label>
        <button onclick="spawnEnemy()">Spawn</button>
    </div>
    <div>
        <label>Spawn probability: <input type="number" id="spawnProbability"></label>
        <button onclick="setSpawnProbability()">Set</button>
    </div>
    <%}%>

</div>

<br>
</body>
</html>
