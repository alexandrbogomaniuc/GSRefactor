<%@ page import="com.dgphoenix.casino.common.util.ApplicationContextHelper" %>
<%@ page import="com.dgphoenix.casino.system.configuration.GameServerConfiguration" %><%--
  Created by IntelliJ IDEA.
  User: hawk
  Date: 26.04.2021
  Time: 18:55
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Title</title>
</head>
<body>
pong
<%
    GameServerConfiguration bean = ApplicationContextHelper.getBean(GameServerConfiguration.class);
    bean.getServerLabel();
%>
</body>
</html>
