<%@ page contentType="text/html;charset=UTF-8" language="java"
%><%@ page import="com.betsoft.casino.mp.config.WebSocketRouter"
%><%@ page import="com.betsoft.casino.mp.server.ServersCoordinatorService"
%><%@ page import="org.springframework.context.ApplicationContext"
%><%
ApplicationContext appContext = WebSocketRouter.getApplicationContext();
ServersCoordinatorService serversCoordinatorService = appContext.getBean("serverIdLockerService", ServersCoordinatorService.class);
%><%=serversCoordinatorService.getServerId()%>