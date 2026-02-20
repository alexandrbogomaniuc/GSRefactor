<%@ page contentType="application/json; charset=UTF-8" pageEncoding="UTF-8" language="java"
%><%@ page import="com.betsoft.casino.mp.config.WebSocketRouter"
%><%@ page import="com.betsoft.casino.mp.service.SocketClientStatisticsService"
%><%@ page import="org.springframework.context.ApplicationContext"
%><%
ApplicationContext appContext = WebSocketRouter.getApplicationContext();
SocketClientStatisticsService socketClientStatisticsService = appContext.getBean("socketClientStatisticsService", SocketClientStatisticsService.class);
%><%=socketClientStatisticsService.getStatsJson()%>