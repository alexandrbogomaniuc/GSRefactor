<%@ page import="com.betsoft.casino.mp.config.WebSocketRouter" %>
<%@ page import="com.betsoft.casino.mp.model.room.IRoomInfo" %>
<%@ page import="com.betsoft.casino.mp.model.RoomTemplate" %>
<%@ page import="com.betsoft.casino.mp.service.SingleNodeRoomInfoService" %>
<%@ page import="com.betsoft.casino.mp.service.RoomTemplateService" %>
<%@ page import="com.dgphoenix.casino.common.util.string.StringUtils" %>
<%@ page import="org.springframework.context.ApplicationContext" %>
<%@ page import="com.betsoft.casino.mp.model.Money" %>
<%--
  Created by IntelliJ IDEA.
  User: anaz
  Date: 09.07.18
  Time: 14:14
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Add room to mpserver</title>
</head>
<body>
<%
    ApplicationContext appContext = WebSocketRouter.getApplicationContext();
    SingleNodeRoomInfoService roomInfoService = appContext.getBean("singleNodeRoomInfoService", SingleNodeRoomInfoService.class);

    String sRoomId = request.getParameter("roomId");
    String sStake = request.getParameter("stake");
    String currency = request.getParameter("currency");
    if (StringUtils.isTrimmedEmpty(sRoomId)) {
        response.getWriter().println("RoomId not found");
    } else if (StringUtils.isTrimmedEmpty(sStake)) {
        response.getWriter().println("stake not found");
    } else if (StringUtils.isTrimmedEmpty(currency)) {
        response.getWriter().println("currency not found");
    } else {
        Long roomId = Long.valueOf(sRoomId);
        Long stake = Long.valueOf(sStake);
        IRoomInfo room = roomInfoService.getRoom(roomId);
        if (room == null) {
            response.getWriter().println("Room not found for roomId=" + roomId);
        } else {
            response.getWriter().println("Room found: " + room);
            long templateId = room.getTemplateId();
            RoomTemplateService roomTemplateService = appContext.getBean("roomTemplateService", RoomTemplateService.class);
            RoomTemplate roomTemplate = roomTemplateService.get(templateId);
            IRoomInfo newRoom = roomInfoService.createForTemplate(roomTemplate, room.getBankId(),
                    Money.fromCents(stake), currency);
            response.getWriter().println("\n\n<br><br>New room added: " + newRoom);
        }
    }
%>
</body>
</html>
