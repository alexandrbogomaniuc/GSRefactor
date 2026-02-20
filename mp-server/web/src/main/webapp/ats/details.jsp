<%@ page import="com.betsoft.casino.mp.config.WebSocketRouter" %>
<%@ page import="com.betsoft.casino.mp.service.BotConfigInfoService" %>
<%@ page import="com.betsoft.casino.mp.model.bots.BotConfigInfo" %>
<%@ page import="java.util.stream.Collectors" %>
<%@ page import="com.betsoft.casino.mp.service.BotManagerService" %>
<%@ page import="com.betsoft.casino.mp.model.bots.TimeFrame" %>
<%@ page import="java.time.LocalTime" %>
<%@ page import="java.time.DayOfWeek" %>
<%@ page import="com.dgphoenix.casino.common.util.string.StringUtils" %>

<!DOCTYPE HTML>
<html>
<head>
    <meta charset="utf-8">
    <title>Ats Admin Panel</title>
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
<h1>Ats Admin Panel</h1>
<h2>Ats profile</h2>
<hr>
<form action="listAts.jsp">
    <input type="submit" value="Back">
</form>
<hr>
<%
    String id = request.getParameter("id");
    String mqNickname = request.getParameter("mqNickname");
    String username = request.getParameter("username");
    BotConfigInfoService botConfigInfoService = WebSocketRouter.getApplicationContext().getBean(BotConfigInfoService.class);
    BotConfigInfo botInfo = null;

    if(!StringUtils.isTrimmedEmpty(id)) {

        botInfo = botConfigInfoService.get(Long.parseLong(id));

    } else if(!StringUtils.isTrimmedEmpty(mqNickname)) {

        botInfo = botConfigInfoService.getByMqNickName(mqNickname);

    }else if(!StringUtils.isTrimmedEmpty(username)) {

        botInfo = botConfigInfoService.getByUserName(username);

    }

    if (botInfo == null) {
        throw new IllegalArgumentException("No Ats with id=" + id);
    }

    BotManagerService botManagerService = WebSocketRouter.getApplicationContext().getBean(BotManagerService.class);
    String activeBotInfo = botManagerService.getActiveBotInfo(botInfo.getId());

    String startTime="";
    String endTime="";

    TimeFrame timeFrame = null;
    if(botInfo.getTimeFrames() != null && !botInfo.getTimeFrames().isEmpty()) {
        timeFrame = botInfo.getTimeFrames().stream().findFirst().orElse(null);
    }

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
%>
<div>
    <h3>Common Info</h3>
    <p>MQ Nickname: <%=botInfo.getMqNickname()%></p>
    <p>MQ BTG World: <%=botInfo.getAllowedGames().stream().map(Enum::name).collect(Collectors.joining(", "))%></p>
    <p>Allowed Banks: <%=botInfo.getAllowedBankIds()%></p>
    <p>Allowed Room Values: <%=botInfo.getAllowedRoomValues()%></p>
    <p>Status: <%=botInfo.isActive() ? "Active" : "Inactive"%></p>
    <p>Start Time: <%=startTime%> From: 00:00, Till: 23:59:59.999999999</p>
    <p>End Time: <%=endTime%> From: 00:00, Till: 23:59:59.999999999</p>
    <p><%=DayOfWeek.MONDAY%>: <%=timeFrame == null || timeFrame.daysOfWeekContains(DayOfWeek.MONDAY) ? "True" : "False"%></p>
    <p><%=DayOfWeek.TUESDAY%>: <%=timeFrame == null || timeFrame.daysOfWeekContains(DayOfWeek.TUESDAY) ? "True" : "False"%></p>
    <p><%=DayOfWeek.WEDNESDAY%>: <%=timeFrame == null || timeFrame.daysOfWeekContains(DayOfWeek.WEDNESDAY) ? "True" : "False"%></p>
    <p><%=DayOfWeek.THURSDAY%>: <%=timeFrame == null || timeFrame.daysOfWeekContains(DayOfWeek.THURSDAY) ? "True" : "False"%></p>
    <p><%=DayOfWeek.FRIDAY%>: <%=timeFrame == null || timeFrame.daysOfWeekContains(DayOfWeek.FRIDAY) ? "True" : "False"%></p>
    <p><%=DayOfWeek.SATURDAY%>: <%=timeFrame == null || timeFrame.daysOfWeekContains(DayOfWeek.SATURDAY) ? "True" : "False"%></p>
    <p><%=DayOfWeek.SUNDAY%>: <%=timeFrame == null || timeFrame.daysOfWeekContains(DayOfWeek.SUNDAY) ? "True" : "False"%></p>
    <p>Bot server info: <%=activeBotInfo.isEmpty() ? "Info not available" : activeBotInfo%></p>
    <hr>
    <h3>Auth data for MQB</h3>
    <p>Username: <%=botInfo.getUsername()%></p>
    <p>Password: <%=botInfo.getPassword()%></p>
    <h3>Balance</h3>
    <p>MQC: <%=botInfo.getMqcBalance()%></p>
    <p>MMC: <%=botInfo.getMmcBalance()%></p>
    <p>Shoots Rates: <%=botInfo.getShootsRates()%></p>
    <p>Bullets Rates: <%=botInfo.getBulletsRates()%></p>
</div>
</body>
</html>
