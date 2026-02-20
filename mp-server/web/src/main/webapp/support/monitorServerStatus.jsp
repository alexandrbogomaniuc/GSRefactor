<%@ page import="com.betsoft.casino.mp.config.WebSocketRouter" %>
<%@ page import="org.springframework.context.ApplicationContext" %>
<%@ page import="com.betsoft.casino.mp.service.RoomPlayerInfoService" %>
<%@ page contentType="plain/text;charset=UTF-8" language="java" trimDirectiveWhitespaces="true" %>

<%
    try {
        ApplicationContext appContext = WebSocketRouter.getApplicationContext();
        RoomPlayerInfoService playerInfoService = appContext.getBean("playerInfoService", RoomPlayerInfoService.class);
        response.getWriter().println("OK");
    } catch (Exception e) {
        response.getWriter().println("ERROR");
    }%>