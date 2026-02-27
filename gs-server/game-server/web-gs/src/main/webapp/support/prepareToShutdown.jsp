<%@ page import="com.dgphoenix.casino.gs.GameServer" %>
<%@ page import="com.abs.casino.init.ShutdownFilter" %>
<%@ taglib prefix="html" uri="http://struts.apache.org/tags-html" %>
<%@ taglib prefix="logic" uri="http://struts.apache.org/tags-logic" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head><title>ShutdownFilter</title></head>
<body>
<%
    ShutdownFilter filter = GameServer.getInstance().getShutdownFilter();
    if (filter == null) {
        response.getWriter().println("filter not registered" + "\n<br>");
    } else {
        filter.markDown();
        response.getWriter().println("Ok");
    }
    response.getWriter().flush();
%>
</body>
</html>