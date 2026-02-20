<%@ page import="com.betsoft.casino.mp.data.service.ServerConfigService" %>
<%@ page import="com.betsoft.casino.mp.config.WebSocketRouter" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Title</title>
</head>
<body>
pong
<%
    ServerConfigService bean = WebSocketRouter.getApplicationContext().getBean(ServerConfigService.class);
    bean.getServerId();
%>
</body>
</html>
