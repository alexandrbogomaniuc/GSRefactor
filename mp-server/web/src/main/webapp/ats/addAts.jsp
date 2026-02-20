<%@ page import="com.dgphoenix.casino.common.util.string.StringUtils" %>
<%@ page import="com.betsoft.casino.mp.config.WebSocketRouter" %>
<%@ page import="com.betsoft.casino.mp.model.*" %>
<%@ page import="com.betsoft.casino.mp.service.BotConfigInfoService" %>
<%@ page import="com.betsoft.casino.mp.model.bots.BotConfigInfo" %>
<%@ page import="java.util.*" %>
<%@ page import="com.betsoft.casino.mp.model.bots.TimeFrame" %>
<%@ page import="java.time.LocalTime" %>
<%@ page import="java.time.DayOfWeek" %>
<%@ page import="static com.betsoft.casino.mp.model.bots.BotConfigInfo.MMC_BankId" %>
<%@ page import="static com.betsoft.casino.mp.model.bots.BotConfigInfo.MQC_BankId" %>

<!DOCTYPE HTML>
<html>
<head>
    <meta charset="utf-8">
    <title>ATS Admin Panel</title>
    <style>
        body {
            margin: 0;
            background-color: #dcdcdc;
        }

        div {
            font-size: 20px;
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
<h1>ATS Admin Panel</h1>
<h2>Add new/Edit Ats</h2>
<hr>
<form action="listAts.jsp">
    <input type="submit" value="Back">
</form>
<hr>
<form>
    <%
        BotConfigInfoService botConfigInfoService = WebSocketRouter.getApplicationContext().getBean(BotConfigInfoService.class);

        String action = request.getParameter("action");
        boolean createAction = "add".equals(action);

        String id = request.getParameter("id");

        BotConfigInfo botInfo = new BotConfigInfo(0, 0, new HashSet<>(), false, "", "", "", null, null, null, null, null, null);

        if (!StringUtils.isTrimmedEmpty(id)) {
            botInfo = botConfigInfoService.get(Long.parseLong(id));
        }

        if (botInfo == null) {
            throw new IllegalArgumentException("Bot with id=" + id + " not found");
        }


        TimeFrame timeFrame = null;
        if(botInfo.getTimeFrames() != null && !botInfo.getTimeFrames().isEmpty()) {
            timeFrame = botInfo.getTimeFrames().stream().findFirst().orElse(null);
        }

        String startTime="";
        String endTime="";

        if(timeFrame != null) {
            LocalTime sTime = timeFrame.getStartTime();
            if(sTime == null) {
                sTime = LocalTime.of(0, 0, 0, 0);
            }

            startTime = sTime.toString();

            LocalTime eTime = timeFrame.getEndTime();
            if(eTime == null) {
                eTime = LocalTime.of(23, 59, 59, 999999999);
            }

            endTime = eTime.toString();
        }

        double dsShootingRate = 1;
        double maShootingRate = 1;
        double sxShootingRate = 0;
        if(botInfo.getShootsRates() != null ) {
            Double dsShootingRateNullable = botInfo.getShootsRates().get(GameType.BG_DRAGONSTONE.getGameId());
            if(dsShootingRateNullable != null) {
                dsShootingRate = dsShootingRateNullable;
            }

            Double maShootingRateNullable = botInfo.getShootsRates().get(GameType.BG_MISSION_AMAZON.getGameId());
            if(maShootingRateNullable != null) {
                maShootingRate = maShootingRateNullable;
            }

            Double sxShootingRateNullable = botInfo.getShootsRates().get(GameType.BG_SECTOR_X.getGameId());
            if(sxShootingRateNullable != null) {
                sxShootingRate = sxShootingRateNullable;
            }
        }

        double dsBulletRate = 1;
        double maBulletRate = 1;
        double sxBulletRate = 0.9;
        if(botInfo.getBulletsRates() != null ) {
            Double dsBulletRateNullable = botInfo.getBulletsRates().get(GameType.BG_DRAGONSTONE.getGameId());
            if(dsBulletRateNullable != null) {
                dsBulletRate = dsBulletRateNullable;
            }

            Double maBulletRateNullable = botInfo.getBulletsRates().get(GameType.BG_MISSION_AMAZON.getGameId());
            if(maBulletRateNullable != null) {
                maBulletRate = maBulletRateNullable;
            }

            Double sxBulletRateNullable = botInfo.getBulletsRates().get(GameType.BG_SECTOR_X.getGameId());
            if(sxBulletRateNullable != null) {
                sxBulletRate = sxBulletRateNullable;
            }
        }

%>
    <div>ATS type:
            <select id="bankId" name="bankId" <%=createAction ? "" : "disabled"%>>
                <option value="6274">MQB ATS(6274 and 6275 bank)</option>
                <option value="271" <%= (botInfo.isFake())?"selected":"" %>>STUB ATS(271 bank only)</option>
            </select>
    </div>
    <br>

    <div>Username <input id="username" type="text" name="username" value="<%=botInfo.getUsername()%>" <%=createAction ? "" : "disabled"%>></div>
    <br>

    <div>Password <input id="password" type="text" name="password" value="<%=botInfo.getPassword()%>"></div>
    <br>

    <div>MQ Nickname <input id="mqNickName" type="text" name="mqNickname" value="<%=botInfo.getMqNickname()%>"></div>
    <br>

    <div>MQC<input id="mqMqc" type="text" name="mqMqc" value="<%=botInfo.getMqcBalance()%>" disabled></div>
    <br>

    <div>MMC<input id="mqMmc" type="text" name="mqMmc" value="<%=botInfo.getMmcBalance()%>" disabled></div>
    <br>

    <div>Active <input id="active" type="checkbox" name="active" <%=botInfo.isActive() ? "checked" : ""%>></div>
    <br>

    <div>Start Time <input id="startTime" type="text" name="startTime" value="<%=startTime%>"> From: 00:00, Till: 23:59:59.999999999</div>
    <br>

    <div>End Time <input id="endTime" type="text" name="endTime" value="<%=endTime%>"> From: 00:00, Till: 23:59:59.999999999</div>
    <br>
    <fieldset>
    <div>
        <input id="<%=DayOfWeek.MONDAY%>" type="checkbox"
               name="<%=DayOfWeek.MONDAY%>"
            <%=timeFrame == null || timeFrame.daysOfWeekContains(DayOfWeek.MONDAY) ? "checked" : ""%>>
        <%=DayOfWeek.MONDAY%>
    </div>
    <div>
        <input id="<%=DayOfWeek.TUESDAY%>" type="checkbox"
               name="<%=DayOfWeek.TUESDAY%>"
            <%=timeFrame == null || timeFrame.daysOfWeekContains(DayOfWeek.TUESDAY) ? "checked" : ""%>>
        <%=DayOfWeek.TUESDAY%>
    </div>
    <div>
        <input id="<%=DayOfWeek.WEDNESDAY%>" type="checkbox"
               name="<%=DayOfWeek.WEDNESDAY%>"
            <%=timeFrame == null || timeFrame.daysOfWeekContains(DayOfWeek.WEDNESDAY) ? "checked" : ""%>>
        <%=DayOfWeek.WEDNESDAY%>
    </div>
    <div>
        <input id="<%=DayOfWeek.THURSDAY%>" type="checkbox"
               name="<%=DayOfWeek.THURSDAY%>"
            <%=timeFrame == null || timeFrame.daysOfWeekContains(DayOfWeek.THURSDAY) ? "checked" : ""%>>
        <%=DayOfWeek.THURSDAY%>
    </div>
    <div>
        <input id="<%=DayOfWeek.FRIDAY%>" type="checkbox"
               name="<%=DayOfWeek.FRIDAY%>"
            <%=timeFrame == null || timeFrame.daysOfWeekContains(DayOfWeek.FRIDAY) ? "checked" : ""%>>
        <%=DayOfWeek.FRIDAY%>
    </div>
    <div>
        <input id="<%=DayOfWeek.SATURDAY%>" type="checkbox"
               name="<%=DayOfWeek.SATURDAY%>"
            <%=timeFrame == null || timeFrame.daysOfWeekContains(DayOfWeek.SATURDAY) ? "checked" : ""%>>
        <%=DayOfWeek.SATURDAY%>
    </div>
    <div>
        <input id="<%=DayOfWeek.SUNDAY%>" type="checkbox"
               name="<%=DayOfWeek.SUNDAY%>"
            <%=timeFrame == null || timeFrame.daysOfWeekContains(DayOfWeek.SUNDAY) ? "checked" : ""%>>
        <%=DayOfWeek.SUNDAY%>
    </div>
    </fieldset>
    <br>
    <fieldset>
        <legend>MQ BTG WORLD</legend>
        <div>
            <input type="checkbox" id="BG_DRAGONSTONE"
                   name="dragonstone" <%=botInfo.getAllowedGames().contains(GameType.BG_DRAGONSTONE) ? "checked" : ""%>>
            <label for="BG_DRAGONSTONE">Dragonstone</label>
        </div>

        <div>
            <input type="checkbox" id="BG_MISSION_AMAZON"
                   name="amazon" <%=botInfo.getAllowedGames().contains(GameType.BG_MISSION_AMAZON) ? "checked" : ""%>>
            <label for="BG_MISSION_AMAZON">Mission Amazon</label>
        </div>

        <div>
            <input type="checkbox" id="BG_SECTOR_X"
                   name="maxcrash" <%=botInfo.getAllowedGames().contains(GameType.BG_SECTOR_X) ? "checked" : ""%>>
            <label for="BG_SECTOR_X">Sector X</label>
        </div>

        <div>
            <input type="checkbox" id="BG_MAXCRASHGAME"
                   name="maxcrash" <%=botInfo.getAllowedGames().contains(GameType.BG_MAXCRASHGAME) ? "checked" : ""%>>
            <label for="BG_MAXCRASHGAME">Maxcrash</label>
        </div>

    </fieldset>
    <br>
    <fieldset>
        <legend>Allowed Banks</legend>
        <div>
            <input type="checkbox" id="MMC_6274"
                   name="MMC_6274" <%=botInfo.getAllowedBankIds().contains(MMC_BankId) ? "checked" : ""%>>
            <label for="MMC_6274">MMC(6274)</label>
            <div><input id="MMC_6274_AVAILABLE_VALUES" type="text" name="MMC_6274_AVAILABLE_VALUES" value="<%=botInfo.getAllowedRoomValuesSet(MMC_BankId)%>"> Example: [100, 200]</div>
        </div>

        <div>
            <input type="checkbox" id="MQC_6275"
                   name="MQC_6275" <%=botInfo.getAllowedBankIds().contains(MQC_BankId) ? "checked" : ""%>>
            <label for="MQC_6275">MQC(6275)</label>
            <div><input id="MQC_6275_AVAILABLE_VALUES" type="text" name="MQC_6275_AVAILABLE_VALUES" value="<%=botInfo.getAllowedRoomValuesSet(MQC_BankId)%>"> Example: [100, 200]</div>
        </div>
    </fieldset>
    <fieldset>
        <legend>Bullet Rates</legend>
        <div>Dragonstone<input id="BG_DRAGONSTONE_BULLET_RATE" type="text" name="BG_DRAGONSTONE_BULLET_RATE" value="<%=dsBulletRate%>"> From: 0, Till: 1</div>
        <br>
        <div>Mission Amazon<input id="BG_MISSION_AMAZON_BULLET_RATE" type="text" name="BG_MISSION_AMAZON_BULLET_RATE" value="<%=maBulletRate%>"> From: 0, Till: 1</div>
        <br>
        <div>Sector X<input id="BG_SECTOR_X_BULLET_RATE" type="text" name="BG_SECTOR_X_BULLET_RATE" value="<%=sxBulletRate%>"> From: 0, Till: 1</div>
        <br>
    </fieldset>
    <fieldset>
        <legend>Shoot Rates</legend>
        <div>Dragonstone<input id="BG_DRAGONSTONE_SHOOTING_RATE" type="text" name="BG_DRAGONSTONE_SHOOTING_RATE" value="<%=dsShootingRate%>"> From: 0, Till: 1</div>
        <br>
        <div>Mission Amazon<input id="BG_MISSION_AMAZON_SHOOTING_RATE" type="text" name="BG_MISSION_AMAZON_SHOOTING_RATE" value="<%=maShootingRate%>"> From: 0, Till: 1</div>
        <br>
        <div>Sector X<input id="BG_SECTOR_X_SHOOTING_RATE" type="text" name="BG_SECTOR_X_SHOOTING_RATE" value="<%=sxShootingRate%>"> From: 0, Till: 1</div>
        <br>
    </fieldset>
    <hr>
    <input type="button" value="<%=createAction ? "Create" : "Update"%>" onclick="create()">
</form>
<script>
    function create() {
        const bankId = document.getElementById("bankId").value;
        const username = document.getElementById("username").value;
        const password = document.getElementById("password").value;
        const mqNickName = document.getElementById("mqNickName").value;
        const active = document.getElementById("active").checked;
        const startTime = document.getElementById("startTime").value;
        const endTime = document.getElementById("endTime").value;
        const mon = document.getElementById("<%=DayOfWeek.MONDAY%>").checked;
        const tue = document.getElementById("<%=DayOfWeek.TUESDAY%>").checked;
        const wed = document.getElementById("<%=DayOfWeek.WEDNESDAY%>").checked;
        const thu = document.getElementById("<%=DayOfWeek.THURSDAY%>").checked;
        const fri = document.getElementById("<%=DayOfWeek.FRIDAY%>").checked;
        const sat = document.getElementById("<%=DayOfWeek.SATURDAY%>").checked;
        const sun = document.getElementById("<%=DayOfWeek.SUNDAY%>").checked;
        const days = [];
        if (mon === true) {
            days.push('<%=DayOfWeek.MONDAY.ordinal() + 1%>');
        }
        if (tue === true) {
            days.push('<%=DayOfWeek.TUESDAY.ordinal() + 1%>');
        }
        if (wed === true) {
            days.push('<%=DayOfWeek.WEDNESDAY.ordinal() + 1%>');
        }
        if (thu === true) {
            days.push('<%=DayOfWeek.THURSDAY.ordinal() + 1%>');
        }
        if (fri === true) {
            days.push('<%=DayOfWeek.FRIDAY.ordinal() + 1%>');
        }
        if (sat === true) {
            days.push('<%=DayOfWeek.SATURDAY.ordinal() + 1%>');
        }
        if (sun === true) {
            days.push('<%=DayOfWeek.SUNDAY.ordinal() + 1%>');
        }

        const dragonstone = document.getElementById("BG_DRAGONSTONE").checked;
        const amazon = document.getElementById("BG_MISSION_AMAZON").checked;
        const sectorx = document.getElementById("BG_SECTOR_X").checked;
        const maxcrash = document.getElementById("BG_MAXCRASHGAME").checked;
        const games = [];

        if (dragonstone === true) {
            games.push('<%=GameType.BG_DRAGONSTONE.getGameId()%>');
        }
        if (amazon === true) {
            games.push('<%=GameType.BG_MISSION_AMAZON.getGameId()%>');
        }
        if (sectorx === true) {
            games.push('<%=GameType.BG_SECTOR_X.getGameId()%>');
        }
        if (maxcrash === true) {
            games.push('<%=GameType.BG_MAXCRASHGAME.getGameId()%>');
        }

        const mmc_6274 = document.getElementById("MMC_6274").checked;
        const mqc_6275 = document.getElementById("MQC_6275").checked;
        const allowedBankIds = [];

        if (mmc_6274 === true) {
            allowedBankIds.push('6274');
        }
        if (mqc_6275 === true) {
            allowedBankIds.push('6275');
        }

        const mmc_6274_room_values = document.getElementById("MMC_6274_AVAILABLE_VALUES").value;
        const mqc_6275_room_values = document.getElementById("MQC_6275_AVAILABLE_VALUES").value;

        const dsBulletRate = document.getElementById("BG_DRAGONSTONE_BULLET_RATE").value;
        const maBulletRate = document.getElementById("BG_MISSION_AMAZON_BULLET_RATE").value;
        const sxBulletRate = document.getElementById("BG_SECTOR_X_BULLET_RATE").value;

        const dsShootingRate = document.getElementById("BG_DRAGONSTONE_SHOOTING_RATE").value;
        const maShootingRate = document.getElementById("BG_MISSION_AMAZON_SHOOTING_RATE").value;
        const sxShootingRate = document.getElementById("BG_SECTOR_X_SHOOTING_RATE").value;

        const link = 'add.jsp?' +
                'bankId=' + encodeURIComponent(bankId) +
                '&username=' + encodeURIComponent(username) +
                '&password=' + encodeURIComponent(password) +
                '&mqNickName=' + encodeURIComponent(mqNickName) +
                '&active=' + encodeURIComponent(active) +
                '&startTime=' + encodeURIComponent(startTime) +
                '&endTime=' + encodeURIComponent(endTime) +
                '&days=' + days +
                '&games=' + games +
                '&bankIds=' + allowedBankIds +
                '&values6274=' + encodeURIComponent(mmc_6274_room_values) +
                '&values6275=' + encodeURIComponent(mqc_6275_room_values) +
                '&dsBR=' + encodeURIComponent(dsBulletRate) +
                '&maBR=' + encodeURIComponent(maBulletRate) +
                '&sxBR=' + encodeURIComponent(sxBulletRate) +
                '&dsSR=' + encodeURIComponent(dsShootingRate) +
                '&maSR=' + encodeURIComponent(maShootingRate) +
                '&sxSR=' + encodeURIComponent(sxShootingRate) +
                '&botId=' + <%=botInfo.getId()%>;

        const xhr = new XMLHttpRequest();
        xhr.open('POST', link);
        xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
        xhr.onload = function () {
            if (xhr.status === 200) {
                alert(xhr.response);
                window.location.reload();
            } else if (xhr.status !== 200) {
                alert('Cannot add account to Ats system. ' + xhr.response);
            }
        };
        xhr.send();
    }
</script>
</body>
</html>
