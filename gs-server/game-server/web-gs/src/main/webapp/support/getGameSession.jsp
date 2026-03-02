<%@ page import="com.abs.casino.cassandra.persist.CassandraGameSessionPersister" %>
<%@ page import="com.abs.casino.common.cache.data.session.GameSession" %>
<%@ page import="com.abs.casino.common.util.ApplicationContextHelper" %>
<%@ page import="com.abs.casino.cassandra.CassandraPersistenceManager" %>
<%--
  Created by IntelliJ IDEA.
  User: zhevlakoval
  Date: 03.11.15
  Time: 13:57
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title></title>
</head>
<body>
<%
    try {
        long gameSessionId = Long.parseLong(request.getParameter("gameSessionId"));
        CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
                .getBean("persistenceManager", CassandraPersistenceManager.class);
        CassandraGameSessionPersister gameSessionPersister = persistenceManager.getPersister(CassandraGameSessionPersister.class);
        GameSession gameSession = gameSessionPersister.get(gameSessionId);
        response.getWriter().println(gameSession);
    } catch (Exception ex) {
        ex.printStackTrace(response.getWriter());
    }
%>
</body>
</html>
