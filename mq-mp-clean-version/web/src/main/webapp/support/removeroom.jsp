<%@ page import="org.springframework.context.annotation.AnnotationConfigApplicationContext" %>
<%@ page import="com.betsoft.casino.mp.config.WebSocketRouter" %>
<%@ page import="com.betsoft.casino.mp.service.SingleNodeRoomInfoService" %>
<%@ page import="org.springframework.context.ApplicationContext" %>
<%@ page import="com.dgphoenix.casino.common.util.string.StringUtils" %>
<%@ page import="com.betsoft.casino.mp.model.room.IRoomInfo" %>
<%@ page import="com.betsoft.casino.mp.service.RoomTemplateService" %>
<%@ page import="com.betsoft.casino.mp.model.RoomTemplate" %><%--
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
    if (StringUtils.isTrimmedEmpty(sRoomId)) {
        response.getWriter().println("RoomId not found");
    } else {
        Long roomId = Long.valueOf(sRoomId);
        IRoomInfo room = roomInfoService.getRoom(roomId);
        if (room == null) {
            response.getWriter().println("Room not found for roomId=" + roomId);
        } else {
            response.getWriter().println("Room found: " + room);
            long templateId = room.getTemplateId();
            roomInfoService.remove(room.getId());
            response.getWriter().println("\n\n<br><br>Room removed");
        }
    }
%>
</body>
</html>
